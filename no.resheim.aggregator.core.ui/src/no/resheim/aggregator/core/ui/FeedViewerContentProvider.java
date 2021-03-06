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

import java.util.EnumSet;

import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.IAggregatorEventListener;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IBasicPropertyConstants;
import org.eclipse.jface.viewers.ILazyTreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

/**
 * This type provides structured viewers with feeds and feed contents. The given
 * input should be an instance of a {@link AggregatorCollection} or
 * {@link AggregatorItemParent}. Using the constructor one can determine which
 * types of items this class will provide.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedViewerContentProvider implements ILazyTreeContentProvider,
		IAggregatorEventListener {
	protected static final String[] STATE_PROPERTIES = new String[] {
			IBasicPropertyConstants.P_TEXT, IBasicPropertyConstants.P_IMAGE };

	/** The tree viewer we're providing content for */
	private TreeViewer fViewer;

	/** The input */
	private AggregatorCollection fCollection;

	private EnumSet<ItemType> fItemTypes;

	/**
	 * 
	 * 
	 * @param types
	 *            the item types this feed viewer will show
	 */
	public FeedViewerContentProvider(EnumSet<ItemType> types) {
		super();
		fItemTypes = types;
	}

	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (v instanceof TreeViewer) {
			fViewer = (TreeViewer) v;
		}
		// Clear the selection so it does not attempt to select the same item
		// number in the new selection.
		fViewer.setSelection(new StructuredSelection());
		if (newInput instanceof Folder) {
			try {
				AggregatorCollection fc = ((Folder) newInput).getCollection();
				if (!fc.equals(this.fCollection)) {
					if (fCollection != null) {
						fCollection.removeFeedListener(this);
					}
					fCollection = fc;
					fCollection.addFeedListener(this);
					// v.refresh();
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		if (newInput instanceof AggregatorCollection
				&& !newInput.equals(this.fCollection)) {
			if (fCollection != null) {
				fCollection.removeFeedListener(this);
			}
			fCollection = (AggregatorCollection) newInput;
			fCollection.addFeedListener(this);
		} else if (newInput == null) {
			if (fCollection != null) {
				fCollection.removeFeedListener(this);
			}
		}
	}

	public void dispose() {
	}

	public Object getParent(Object child) {
		if (child instanceof AggregatorItem) {
			return ((AggregatorItem) child).getParent();
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
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (fViewer != null) {
					synchronized (fViewer) {
						// Refresh everything if we're displaying folders
						if (fItemTypes.contains(ItemType.FOLDER)) {
							fViewer.refresh();
						} else {
							for (Object item : event.getItems()) {
								System.out.println("Updating item " + item);
								fViewer.refresh(item);
							}
						}
					}
				}
			}
		});
	}

	/**
	 * @param item
	 * @param indent
	 */
	void printItems(TreeItem item, int indent) {
		for (TreeItem i : item.getItems()) {
			for (int in = 0; in < indent; in++) {
				System.out.print("  "); //$NON-NLS-1$
			}
			System.out.println(i + "  " + i.getData()); //$NON-NLS-1$
			printItems(i, indent + 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ILazyTreeContentProvider#updateChildCount(java
	 * .lang.Object, int)
	 */
	public void updateChildCount(final Object element,
			final int currentChildCount) {
		if (element instanceof AggregatorItemParent) {
			int length = 0;
			AggregatorItemParent node = (AggregatorItemParent) element;
			try {
				length = node.getChildCount(fItemTypes);
				if (length != currentChildCount) {
					fViewer.setChildCount(element, length);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ILazyTreeContentProvider#updateElement(java
	 * .lang.Object, int)
	 */
	public void updateElement(final Object parent, final int index) {
		if (parent instanceof AggregatorItemParent) {
			Object element;
			try {
				element = ((AggregatorItemParent) parent).getChildAt(
						fItemTypes, index);
				fViewer.replace(parent, index, element);
				updateChildCount(element, -1);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
