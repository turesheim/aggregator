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
import java.util.UUID;

import no.resheim.aggregator.data.AbstractAggregatorItem;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Virtual tree viewer supporting drag and drop while presenting
 * IAggregatorItems.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedTreeViewer extends TreeViewer {
	private static final int DND_OFFSET = 3;
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

	public FeedTreeViewer(Composite parent) {
		this(parent, SWT.NONE);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FeedTreeViewer(Composite parent, int style) {
		super(parent, style | SWT.VIRTUAL);
		setComparer(comparer);
		// Must be true
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
				if (event.detail == DND.DROP_MOVE) {
					dragSourceItem[0].dispose();
					dragSourceItem[0] = null;
				}
			}
		});
		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener(new DropTargetAdapter() {

			private FeedCollection collection;

			public void dragOver(DropTargetEvent event) {
				if (event.item != null) {
					Rectangle rect = ((TreeItem) event.item).getBounds();
					Point pt = tree.toControl(event.x, event.y);
					if (pt.y < rect.y + DND_OFFSET)
						event.feedback = DND.FEEDBACK_INSERT_BEFORE;
					if (pt.y > rect.y + rect.height - DND_OFFSET)
						event.feedback = DND.FEEDBACK_INSERT_AFTER;
				}
				event.feedback |= DND.FEEDBACK_SCROLL | DND.FEEDBACK_EXPAND;
			}

			public void drop(DropTargetEvent event) {
				IAggregatorItem source = (IAggregatorItem) ((IStructuredSelection) getSelection())
						.getFirstElement();
				collection = source.getCollection();

				// We must drop in something
				if (event.item == null || event.item.equals(dragSourceItem[0])) {
					event.detail = DND.DROP_NONE;
				} else {
					System.out
							.println("========================================================");
					TreeItem item = (TreeItem) event.item;
					Image image = dragSourceItem[0].getImage();
					String text = dragSourceItem[0].getText();
					int children = dragSourceItem[0].getItemCount();
					AbstractAggregatorItem destination = (AbstractAggregatorItem) item
							.getData();
					int newOrder = 0;
					int oldOrder = source.getOrdering();
					TreeItem newItem = null;
					Rectangle rect = item.getBounds();
					Point pt = tree.toControl(event.x, event.y);
					UUID newParent;
					try {
						if (pt.y < rect.y + DND_OFFSET) {
							// Before
							newOrder = getItemIndex(item) - 1;
							newParent = source.getParentUUID();
							newItem = getNewItem(item, newOrder + 1);
						} else if (pt.y > rect.y + rect.height - DND_OFFSET) {
							// After
							newOrder = getItemIndex(item);
							newParent = source.getParentUUID();
							newItem = getNewItem(item, newOrder + 1);
						} else {
							// Drop into
							newParent = destination.getUUID();
							newOrder = collection.getChildCount(collection
									.getItem(newParent));
							newItem = getNewItem(item, newOrder);
						}

						// Create a new tree item to hold the position
						newItem.setImage(image);
						newItem.setText(text);
						newItem.setItemCount(children);
						newItem.setData(source);

						if (newParent.equals(source.getParentUUID())) {
							if (newOrder > oldOrder) {
								moveUp(item, oldOrder, newOrder);
								collection.move(source, newParent, newOrder);
							} else {
								moveDown(item, oldOrder, newOrder);
								collection
										.move(source, newParent, newOrder + 1);
							}
						} else {
							System.out.println(MessageFormat.format(
									"Dropping {0} into {1} at {2}",
									new Object[] {
											source, newParent, newOrder
									}));
							collection.move(source, newParent, newOrder);
							moveUp(item, oldOrder + 1,
									getParentChildCount(item) - 1);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			/**
			 * Updates the tree items and the associated aggregator item
			 * following the given tree item by incrementing the ordering
			 * property by one.
			 * 
			 * @param treeItem
			 *            The tree item that was moved
			 */
			private void moveDown(TreeItem treeItem, int from, int to) {
				TreeItem child;

				for (int i = to + 2; i <= from; i++) {
					child = getSiblingAt(treeItem, i);
					IAggregatorItem data = (IAggregatorItem) child.getData();
					collection.move(data, data.getParentUUID(), data
							.getOrdering() + 1);
				}
			}

			// OK
			private void moveUp(TreeItem treeItem, int from, int to) {
				TreeItem child;
				for (int i = from + 1; i <= to; i++) {
					child = getSiblingAt(treeItem, i);
					IAggregatorItem data = (IAggregatorItem) child.getData();
					// If the tree item is virtual it will have no data so we
					// need to obtain the aggregator item directly from the
					// collection.
					if (data == null) {
						IAggregatorItem itemData = (IAggregatorItem) treeItem
								.getData();
						data = collection.getItemAt(itemData.getParentUUID(),
								i - 1);
					}
					System.out.println(MessageFormat.format(
							"Moving {0} in {1} from {2} to {3}", new Object[] {
									data, child, data.getOrdering(),
									data.getOrdering() - 1
							}));
					collection.move(data, data.getParentUUID(), data
							.getOrdering() - 1);
				}
			}

			private TreeItem getSiblingAt(TreeItem treeItem, int i) {
				TreeItem child;
				if (treeItem.getParentItem() == null) {
					child = treeItem.getParent().getItem(i);
				} else {
					child = treeItem.getParentItem().getItem(i);
				}
				return child;
			}

			private TreeItem getNewItem(TreeItem item, int index) {
				TreeItem newItem;
				if (item.getParentItem() == null) {
					Tree parent = item.getParent();
					// Is this not zero-relative?
					newItem = new TreeItem(parent, SWT.NONE, index);
				} else {
					TreeItem parent = item.getParentItem();
					newItem = new TreeItem(parent, SWT.NONE, index);
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

			/**
			 * Calculates and returns the index of the tree item in its parent.
			 * 
			 * @param item
			 *            the tree item
			 * @return the index of the item
			 */
			private int getParentChildCount(TreeItem item) {
				if (item.getParentItem() == null) {
					return item.getParent().getItemCount();
				} else {
					return item.getParentItem().getItemCount();
				}
			}
		});
	}
}
