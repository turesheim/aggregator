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

import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;

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

	public FeedTreeViewer(Composite parent) {
		this(parent, SWT.VIRTUAL);
		setUseHashlookup(true);
		initDND();
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FeedTreeViewer(Composite parent, int style) {
		super(parent, style | SWT.VIRTUAL);
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
				event.data = item.getTitle();
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

			// Note that this method does not actually move anything. It only
			// gathers the required data for the feed collection to do the
			// moving. It is up to the content provider to react on the move
			// and update the data.
			public void drop(DropTargetEvent event) {
				IAggregatorItem source = (IAggregatorItem) ((IStructuredSelection) getSelection())
						.getFirstElement();
				Object input = getInput();
				if (!(input instanceof FeedCollection)) {
					return;
				}
				collection = ((FeedCollection) input);

				// We must drop in something
				if (event.item == null || event.item.equals(dragSourceItem[0])) {
					event.detail = DND.DROP_NONE;
				} else {
					TreeItem item = (TreeItem) event.item;

					IAggregatorItem newParent = (IAggregatorItem) item
							.getData();
					IAggregatorItem oldParent = getParent(dragSourceItem[0]);
					int newOrder = 0;
					int oldOrder = getItemIndex(dragSourceItem[0]);

					Rectangle rect = item.getBounds();
					Point pt = tree.toControl(event.x, event.y);
					try {
						if (pt.y < rect.y + DND_OFFSET) {
							// Before
							newOrder = getItemIndex(item) - 1;
							newParent = oldParent;
						} else if (pt.y > rect.y + rect.height - DND_OFFSET) {
							// After
							newOrder = getItemIndex(item);
							newParent = oldParent;
						} else {
							newOrder = collection.getChildCount(newParent);
						}

						if (newParent.equals(oldParent)) {
							if (newOrder > oldOrder) {
								System.out.println(MessageFormat.format(
										"Moving {0} downwards to {1}",
										new Object[] {
												source, newOrder
										}));
								collection.move(source, oldParent, oldOrder,
										newParent, newOrder);
							} else {
								System.out.println(MessageFormat.format(
										"Moving {0} upwards to {1}",
										new Object[] {
												source, newOrder + 1
										}));
								collection.move(source, oldParent, oldOrder,
										newParent, newOrder + 1);
							}
						} else {
							System.out.println(MessageFormat.format(
									"Dropping {0} into {1} at {2}",
									new Object[] {
											source, item, newOrder
									}));
							collection.move(source, oldParent, oldOrder,
									newParent, newOrder);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
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

			private IAggregatorItem getParent(TreeItem item) {
				if (item.getParentItem() == null) {
					return collection;
				} else {
					return (IAggregatorItem) item.getParentItem().getData();
				}
			}
		});
	}
}
