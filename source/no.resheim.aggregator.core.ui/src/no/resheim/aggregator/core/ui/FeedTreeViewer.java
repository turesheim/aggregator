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

import no.resheim.aggregator.data.AbstractAggregatorItem;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedTreeViewer extends TreeViewer {
	/**
	 * Element comparer that uses the UUID of the aggregator item instead of the
	 * object's hash code to compare two items. This ensures that the item is
	 * recognized correctly even if two different instances are compared.
	 */
	private static final IElementComparer comparer = new IElementComparer() {

		public boolean equals(Object a, Object b) {
			if (a instanceof IAggregatorItem && b instanceof IAggregatorItem) {
				return (((IAggregatorItem) a).getUUID()
						.equals(((IAggregatorItem) b).getUUID()));
			}
			return a.equals(b);
		}

		public int hashCode(Object element) {
			return element.hashCode();
		}

	};

	class OrderedSorter extends ViewerComparator {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof AbstractAggregatorItem
					&& e2 instanceof AbstractAggregatorItem) {
				AbstractAggregatorItem i1 = (AbstractAggregatorItem) e1;
				AbstractAggregatorItem i2 = (AbstractAggregatorItem) e2;

				long t1 = i1.getOrdering();
				long t2 = i2.getOrdering();
				// Note that we're putting the oldest items last!
				if (t1 == t2)
					return 0;
				else if (t1 < t2)
					return 1;
				else
					return -1;
			}
			return super.compare(viewer, e1, e2);
		}
	}

	public FeedTreeViewer(Composite parent) {
		super(parent);
		setComparer(comparer);
		super.setComparator(new OrderedSorter());
		initDND();
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FeedTreeViewer(Composite parent, int style) {
		super(parent, style);
		setComparer(comparer);
		super.setComparator(new OrderedSorter());
		initDND();
	}

	/**
	 * @param tree
	 */
	public FeedTreeViewer(Tree tree) {
		super(tree);
		setComparer(comparer);
		super.setComparator(new OrderedSorter());
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
		Transfer[] types = new Transfer[] {
			TextTransfer.getInstance()
		};
		int operations = DND.DROP_MOVE;
		final Tree tree = getTree();
		final DragSource source = new DragSource(tree, operations);
		source.setTransfer(types);
		final TreeItem[] dragSourceItem = new TreeItem[1];
		source.addDragListener(new DragSourceListener() {
			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = tree.getSelection();
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItem[0] = selection[0];
				} else {
					event.doit = false;
				}
			};

			public void dragSetData(DragSourceEvent event) {
				IAggregatorItem item = (IAggregatorItem) dragSourceItem[0]
						.getData();
				event.data = item.getUUID().toString();
			}

			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE)
					dragSourceItem[0].dispose();
				dragSourceItem[0] = null;
			}
		});
		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {

			public void dragOver(DropTargetEvent event) {
				if (event.item != null) {
					Rectangle rect = ((TreeItem) event.item).getBounds();
					Point pt = tree.toControl(event.x, event.y);
					if (pt.y < rect.y + 3)
						event.feedback = DND.FEEDBACK_INSERT_BEFORE;
					if (pt.y > rect.y + rect.height - 3)
						event.feedback = DND.FEEDBACK_INSERT_AFTER;
				}
				event.feedback |= DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
			}

			public void drop(DropTargetEvent event) {
				IAggregatorItem source = (IAggregatorItem) ((IStructuredSelection) getSelection())
						.getFirstElement();
				// We must drop in something
				if (event.item == null) {
					event.detail = DND.DROP_NONE;
				} else {
					TreeItem item = (TreeItem) event.item;
					AbstractAggregatorItem destination = (AbstractAggregatorItem) item
							.getData();
					long newOrder = 0;
					TreeItem newItem = null;
					Rectangle rect = item.getBounds();
					Point pt = tree.toControl(event.x, event.y);
					try {
						// TODO: Fix setting of new order, it's fragile
						if (pt.y < rect.y + 3) {
							newOrder = destination.getOrdering()
									+ ((getOrderBefore(item) - destination
											.getOrdering()) / 2);
							newItem = getNewItem(item, 0);
							source.getRegistry().move(source,
									source.getParentUUID(), newOrder);
						} else if (pt.y > rect.y + rect.height - 3) {
							newOrder = destination.getOrdering()
									- ((destination.getOrdering() - getOrderAfter(item)) / 2);
							newItem = getNewItem(item, 1);
							source.getRegistry().move(source,
									source.getParentUUID(), newOrder);
						} else {
							source.getRegistry().move(source,
									destination.getUUID(), newOrder);
							newItem = new TreeItem(item, SWT.NONE, 0);
						}
						newItem.setImage(dragSourceItem[0].getImage());
						newItem.setText(dragSourceItem[0].getText());
						newItem.setItemCount(dragSourceItem[0].getItemCount());
						newItem.setData(source);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			private TreeItem getNewItem(TreeItem item, int offset) {
				int newIndex;
				TreeItem newItem;
				if (item.getParentItem() == null) {
					Tree parent = item.getParent();
					newIndex = parent.indexOf(item);
					newItem = new TreeItem(parent, SWT.NONE, newIndex + offset);
				} else {
					TreeItem parent = item.getParentItem();
					newIndex = parent.indexOf(item);
					newItem = new TreeItem(parent, SWT.NONE, newIndex + offset);
				}
				return newItem;
			}

			/**
			 * Calculates and returns the index of the tree item in its parent.
			 * 
			 * @param item
			 *            the tree item
			 * @return the index of the item
			 */
			private int getItemIndex(TreeItem item) {
				if (item.getParentItem() == null) {
					return item.getParent().indexOf(item);
				} else {
					return item.getParentItem().indexOf(item);
				}
			}

			private long getOrderBefore(TreeItem item) {
				int index = getItemIndex(item);
				if (index == 0) {
					return Long.MAX_VALUE;
				}
				if (item.getParentItem() == null) {
					AbstractAggregatorItem aItem = (AbstractAggregatorItem) item
							.getParent().getItem(index - 1).getData();
					return aItem.getOrdering();
				} else {
					AbstractAggregatorItem aItem = (AbstractAggregatorItem) item
							.getParentItem().getItem(index - 1).getData();
					return aItem.getOrdering();
				}
			}

			private long getOrderAfter(TreeItem item) {
				int index = getItemIndex(item);
				if (item.getParentItem() == null) {
					int count = item.getParent().getItemCount();
					if (index == count - 1) {
						return 0;
					}
					AbstractAggregatorItem aItem = (AbstractAggregatorItem) item
							.getParent().getItem(index + 1).getData();
					return aItem.getOrdering();
				} else {
					int count = item.getParentItem().getItemCount();
					if (index == count - 1) {
						return 0;
					}
					AbstractAggregatorItem aItem = (AbstractAggregatorItem) item
							.getParentItem().getItem(index + 1).getData();
					return aItem.getOrdering();
				}
			}

		});

	}
}
