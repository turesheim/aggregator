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
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorEventListener;
import no.resheim.aggregator.data.IAggregatorItem;
import no.resheim.aggregator.data.internal.AggregatorUIItem;

import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

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

	public Object getParent(Object child) {
		if (child instanceof AggregatorUIItem) {
			return ((AggregatorUIItem) child).getParent();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.FeedListener#feedChanged(no.resheim.aggregator
	 * .model.FeedChangedEvent)
	 */
	public void aggregatorItemChanged(final AggregatorItemChangedEvent event) {
		synchronized (fViewer) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					if (fViewer != null) {
						switch (event.getType()) {
						case READ:
							fViewer.refresh();
							break;
						case UPDATED:
							// We _have_ to refresh deeply after adding new
							// articles or the viewer will become confused.
							fViewer.refresh();
							break;
						case MOVED:
							fViewer.refresh();
							break;
						case REMOVED:
							fViewer.refresh();
							break;
						case CREATED:
							// fViewer.add(event.getItem().getParent(), event
							// .getItem());
							// fViewer.update(event.getItem().getParent(),
							// STATE_PROPERTIES);
							fViewer.refresh();
							break;
						case UPDATING:
							fViewer.update(event.getItem(), STATE_PROPERTIES);
							break;
						default:
							fViewer.refresh();
							break;
						}
					}
				}
			});
		}
	}

	void printItems(TreeItem item, int indent) {
		for (TreeItem i : item.getItems()) {
			for (int in = 0; in < indent; in++) {
				System.out.print("  "); //$NON-NLS-1$
			}
			System.out.println(i + "  " + i.getData()); //$NON-NLS-1$
			printItems(i, indent + 1);
		}
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
			Object element = fCollection.getItemAt(((IAggregatorItem) parent),
					index);
			fViewer.replace(parent, index, element);
			updateChildCount(element, -1);
		}
	}
}
