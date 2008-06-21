/*******************************************************************************
 * Copyright (c) 2007-2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui;

import no.resheim.aggregator.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.IAggregatorEventListener;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * This type provides structured viewers with feeds and feed contents. The given
 * input should be an instance of a {@link FeedCollection}.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedViewerContentProvider implements ILazyTreeContentProvider,
		IAggregatorEventListener {
	protected static final String[] STATE_PROPERTIES = new String[] {
			IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE
	};
	private TreeViewer fViewer;
	private FeedCollection fCollection;

	/**
	 * 
	 */
	public FeedViewerContentProvider() {
		super();
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (v instanceof TreeViewer) {
			fViewer = (TreeViewer) v;
		}
		if (newInput instanceof FeedCollection
				&& !newInput.equals(this.fCollection)) {
			if (fCollection != null) {
				fCollection.removeFeedListener(this);
			}
			fCollection = (FeedCollection) newInput;
			fCollection.addFeedListener(this);
		}
	}

	public void dispose() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent != null) {
			if (parent instanceof IAggregatorItem) {
				return fCollection.getChildren((IAggregatorItem) parent);
			}
		}
		return new Object[0];
	}

	public Object getParent(Object child) {
		if (child instanceof IAggregatorItem) {
			fCollection.getItem(((IAggregatorItem) child).getParentUUID());
		}
		return null;
	}

	// FIXME: Stale order of the tree items after removing an item.
	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.FeedListener#feedChanged(no.resheim.aggregator.model.FeedChangedEvent)
	 */
	public void aggregatorItemChanged(final AggregatorItemChangedEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (fViewer != null) {
					// FIXME: Stale ordering after removing an item (using
					// shuffle)
					IAggregatorItem parent = fCollection.getItem(event
							.getItem().getParentUUID());
					switch (event.getType()) {
					case READ:
						fViewer.update(event.getItem(), STATE_PROPERTIES);
						fViewer.update(parent, STATE_PROPERTIES);
						break;
					case UPDATED:
						// We _have_ to refresh deeply after adding new articles
						// or the viewer will become confused.
						fViewer.refresh(parent, true);
						break;
					case MOVED:
						// Make sure the item reference is updated as the one
						// in the viewer has wrong "ordering" member variable.
						// We're assuming that the view already knows about the
						// change but only needs to get it's data updated.
						fViewer.replace(parent, event.getItem().getOrdering(),
								event.getItem());
						// Update label and image too
						fViewer.update(event.getItem(), STATE_PROPERTIES);
						break;
					case REMOVED:
						// Maybe the number of "read" has changed
						fViewer.update(parent, STATE_PROPERTIES);
						// Remove the item itself
						fViewer.remove(event.getItem());
						break;
					case CREATED:
						fViewer.add(parent, event.getItem());
						// The number of "read" items has most likely changed
						fViewer.update(parent, STATE_PROPERTIES);
						break;
					case UPDATING:
						fViewer.update(event.getItem(), STATE_PROPERTIES);
						break;
					default:
						if (event.getItem() instanceof Feed
								|| event.getItem() instanceof Folder)
							fViewer.refresh();
						break;
					}
				}
			}
		});
	}

	public void updateChildCount(final Object element,
			final int currentChildCount) {
		if (element instanceof IAggregatorItem) {
			int length = 0;
			IAggregatorItem node = (IAggregatorItem) element;
			length = fCollection.getChildCount(node);
			if (length != currentChildCount) {
				fViewer.setChildCount(element, length);
			}
		}
	}

	public void updateElement(final Object parent, final int index) {
		if (parent instanceof IAggregatorItem) {
			Object element = fCollection.getItemAt(((IAggregatorItem) parent)
					.getUUID(), index);
			fViewer.replace(parent, index, element);
			updateChildCount(element, -1);
		}
	}
}
