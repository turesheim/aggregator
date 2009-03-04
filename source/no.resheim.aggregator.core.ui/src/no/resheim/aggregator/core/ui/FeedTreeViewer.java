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

import java.text.MessageFormat;
import java.util.EnumSet;

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;

/**
 * Virtual tree viewer presenting aggregator items and supporting drag and drop.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedTreeViewer extends TreeViewer {
	/**
	 * The purpose of using this type is to handle situations where for instance
	 * the selection is set using an {@link Article} that has been created by a
	 * feed update. That will not be the same instance as presented in the
	 * viewer as these are normally created from the database.
	 */
	class Comparer implements IElementComparer {

		public boolean equals(Object a, Object b) {
			if (a instanceof AggregatorItem && b instanceof AggregatorItem) {
				return (((AggregatorItem) a).getUUID()
						.equals(((AggregatorItem) b).getUUID()));
			}
			return false;
		}

		public int hashCode(Object element) {
			if (element instanceof AggregatorItem) {
				return ((AggregatorItem) element).getUUID().hashCode();
			}
			return element.hashCode();
		}
	}

	/**
	 * Expands all ancestors of the given element or tree path so that the given
	 * element becomes visible in this viewer's tree control, and then expands
	 * the subtree rooted at the given element to the given level.
	 * 
	 * @param elementOrTreePath
	 *            the element
	 * @param level
	 *            non-negative level, or <code>ALL_LEVELS</code> to expand all
	 *            levels of the tree
	 */
	public void expandToLevel(Object elementOrTreePath, int level) {
		// if (checkBusy())
		// return;
		Widget w = internalExpand(elementOrTreePath, true);
		if (w != null) {
			internalExpandToLevel(w, level);
		}
	}

	public FeedTreeViewer(Composite parent) {
		this(parent, SWT.VIRTUAL);
		initDND();
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FeedTreeViewer(Composite parent, int style) {
		super(parent, style | SWT.VIRTUAL);
		setComparer(new Comparer());
		setUseHashlookup(true);
		initDND();
	}

	/**
	 * Does nothing. If the manual arranging and sorting is going to work we
	 * must use the built in {@link OrderedSorter}.
	 */
	@Override
	public void setComparator(ViewerComparator comparator) {
	}

	private void initDND() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		int operations = DND.DROP_MOVE;
		final Tree tree = getTree();
		final DragSource source = new DragSource(tree, operations);
		source.setTransfer(types);
		final TreeItem[] dragSourceItem = new TreeItem[1];
		source.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = tree.getSelection();
				event.doit = false;
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItem[0] = selection[0];
				}
			};

			public void dragSetData(DragSourceEvent event) {
				AggregatorItem item = (AggregatorItem) dragSourceItem[0]
						.getData();
				event.data = item.getUUID().toString();
			}

			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					dragSourceItem[0].dispose();
					dragSourceItem[0] = null;
				}
			}
		});
		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener(new ViewerDropAdapter(this) {

			@Override
			protected Object determineTarget(DropTargetEvent event) {
				fItem = (TreeItem) event.item;
				return super.determineTarget(event);
			}

			private FeedCollection collection;

			private TreeItem fItem;

			/*
			 * Calculates and returns the index of the tree item in its parent.
			 * 
			 * @param item the tree item
			 * 
			 * @return the index of the item
			 */
			private int getItemIndex(TreeItem item) {
				if (item.getParentItem() == null) {
					return item.getParent().indexOf(item);
				} else {
					return item.getParentItem().indexOf(item);
				}
			}

			private AggregatorItemParent getParent(TreeItem item) {
				if (item.getParentItem() == null) {
					return collection;
				} else {
					return (AggregatorItemParent) item.getParentItem()
							.getData();
				}
			}

			@Override
			public boolean performDrop(Object data) {
				AggregatorItem source = (AggregatorItem) ((IStructuredSelection) getSelection())
						.getFirstElement();

				Object input = getInput();
				if (!(input instanceof FeedCollection)) {
					return false;
				}
				collection = ((FeedCollection) input);
				AggregatorItemParent newParent = (AggregatorItemParent) getCurrentTarget();
				AggregatorItemParent oldParent = getParent(dragSourceItem[0]);
				int newOrder = 0;
				int oldOrder = getItemIndex(dragSourceItem[0]);

				int location = getCurrentLocation();

				try {
					if (location == LOCATION_BEFORE) {
						// Before
						newOrder = getItemIndex(fItem) - 1;
						newParent = oldParent;
					} else if (location == LOCATION_AFTER) {
						// After
						newOrder = getItemIndex(fItem);
						newParent = oldParent;
					} else {
						newOrder = newParent.getChildCount(EnumSet
								.allOf(ItemType.class));
					}

					if (newParent.equals(oldParent)) {
						if (newOrder > oldOrder) {
							System.out.println(MessageFormat.format(
									"Moving {0} downwards to {1}", //$NON-NLS-1$
									new Object[] { source, newOrder }));
							collection.move(source, oldParent, oldOrder,
									newParent, newOrder);
						} else {
							System.out.println(MessageFormat.format(
									"Moving {0} upwards to {1}", new Object[] { //$NON-NLS-1$
									source, newOrder + 1 }));
							collection.move(source, oldParent, oldOrder,
									newParent, newOrder + 1);
						}
					} else {
						System.out.println(MessageFormat.format(
								"Dropping {0} into {1} at {2}", new Object[] { //$NON-NLS-1$
								source, fItem, newOrder }));
						collection.move(source, oldParent, oldOrder, newParent,
								newOrder);
					}
					// Tell our listeners that the deed is done
					collection.notifyListerners(new Object[] { source },
							EventType.MOVED);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}

			@Override
			public boolean validateDrop(Object target, int operation,
					TransferData transferType) {

				return target instanceof Folder;
			}
		});
	}
}
