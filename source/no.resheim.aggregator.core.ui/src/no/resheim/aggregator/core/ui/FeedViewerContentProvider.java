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
import no.resheim.aggregator.data.FeedListener;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;

/**
 * This type provides structured viewers with feeds and feed contents. The given
 * input should be an instance of a {@link FeedCollection}.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedViewerContentProvider implements IStructuredContentProvider,
		ITreeContentProvider, FeedListener {
	protected static final String[] STATE_PROPERTIES = new String[] {
			IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE
	};
	private StructuredViewer fViewer;
	private FeedCollection fCollection;

	/**
	 * 
	 */
	public FeedViewerContentProvider() {
		super();
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (v instanceof StructuredViewer) {
			fViewer = (StructuredViewer) v;
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
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof IAggregatorItem) {
			return ((IAggregatorItem) child).getParentItem();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent != null) {
			if (parent instanceof IAggregatorItem) {
				return fCollection.getChildren((IAggregatorItem) parent);
			}
		}
		return new Object[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object parent) {
		if (parent instanceof IAggregatorItem) {
			return (((IAggregatorItem) parent).getRegistry().getChildCount(
					(IAggregatorItem) parent) > 0);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.FeedListener#feedChanged(no.resheim.aggregator.model.FeedChangedEvent)
	 */
	public void feedChanged(final AggregatorItemChangedEvent event) {
		Runnable update = new Runnable() {
			public void run() {
				if (fViewer != null) {
					IAggregatorItem parent = event.getItem().getParentItem();
					switch (event.getType()) {
					case READ:
						fViewer.update(event.getItem(), STATE_PROPERTIES);
						if (parent != null)
							fViewer.update(parent, STATE_PROPERTIES);
						break;
					case UPDATED:
						fViewer.update(event.getItem(), STATE_PROPERTIES);
						break;
					case REMOVED:
						fViewer.refresh(parent, true);
						if (parent != null)
							fViewer.update(parent, STATE_PROPERTIES);
						break;
					case CREATED:
						fViewer.refresh(parent, true);
						if (parent != null)
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
			};
		};
		Display.getDefault().asyncExec(update);
	}

	/**
	 * Asynchronously refreshes the submitted item.
	 * 
	 * @param item
	 */
	public void itemUpdated(final Object item) {
		Runnable update = new Runnable() {
			public void run() {
				if (fViewer != null) {
					fViewer.update(item, null);
				}
			};
		};
		Display.getDefault().asyncExec(update);
	}

	public void refreshItem(final Object item) {
		Runnable update = new Runnable() {
			public void run() {
				if (fViewer != null) {
					fViewer.refresh();
				}
			};
		};
		Display.getDefault().asyncExec(update);
	}
}
