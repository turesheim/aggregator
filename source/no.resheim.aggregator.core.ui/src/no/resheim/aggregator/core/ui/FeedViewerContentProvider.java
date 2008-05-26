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
public class FeedViewerContentProvider implements IStructuredContentProvider,
		ITreeContentProvider, FeedListener {
	protected static final String[] STATE_PROPERTIES = new String[] {
			IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE
	};
	TreeViewer treeView;
	FeedCollection registry;

	/**
	 * 
	 */
	public FeedViewerContentProvider() {
		super();
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (v instanceof TreeViewer) {
			treeView = (TreeViewer) v;
		}
		if (newInput instanceof FeedCollection
				&& !newInput.equals(this.registry)) {
			if (registry != null) {
				registry.removeFeedListener(this);
			}
			registry = (FeedCollection) newInput;
			registry.addFeedListener(this);
		}
	}

	public void dispose() {
	}

	public Object[] getElements(Object parent) {
		return getChildren(parent);
	}

	public Object getParent(Object child) {
		if (child instanceof IAggregatorItem) {
			return ((IAggregatorItem) child).getParent();
		}
		return null;
	}

	public Object[] getChildren(Object parent) {
		if (parent != null) {
			if (parent instanceof IAggregatorItem) {
				return registry.getChildren((IAggregatorItem) parent);
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
				if (treeView != null) {
					IAggregatorItem parent = event.getItem().getParent();
					switch (event.getType()) {
					case READ:
						treeView.update(event.getItem(), STATE_PROPERTIES);
						if (parent != null)
							treeView.update(parent, STATE_PROPERTIES);
						break;
					case UPDATED:
						treeView.update(event.getItem(), STATE_PROPERTIES);
						break;
					case REMOVED:
						treeView.refresh(parent, true);
						if (parent != null)
							treeView.update(parent, STATE_PROPERTIES);
						break;
					case CREATED:
						treeView.refresh(parent, true);
						if (parent != null)
							treeView.update(parent, STATE_PROPERTIES);
						break;
					case UPDATING:
						treeView.update(event.getItem(), STATE_PROPERTIES);
						break;
					default:
						if (event.getItem() instanceof Feed
								|| event.getItem() instanceof Folder)
							treeView.refresh();
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
				if (treeView != null) {
					treeView.update(item, null);
				}
			};
		};
		Display.getDefault().asyncExec(update);
	}

	public void refreshItem(final Object item) {
		Runnable update = new Runnable() {
			public void run() {
				if (treeView != null) {
					treeView.refresh();
				}
			};
		};
		Display.getDefault().asyncExec(update);
	}
}
