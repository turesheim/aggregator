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

import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
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

	public FeedTreeViewer(Composite parent) {
		super(parent);
		setComparer(comparer);
		initDND();
	}

	/**
	 * @param parent
	 * @param style
	 */
	public FeedTreeViewer(Composite parent, int style) {
		super(parent, style);
		setComparer(comparer);
		initDND();
	}

	/**
	 * @param tree
	 */
	public FeedTreeViewer(Tree tree) {
		super(tree);
		setComparer(comparer);
		initDND();
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
				event.feedback = DND.FEEDBACK_EXPAND | DND.FEEDBACK_SCROLL;
				if (event.item != null) {
					event.feedback |= DND.FEEDBACK_SELECT;
				}
			}

			public void drop(DropTargetEvent event) {
				IAggregatorItem source = (IAggregatorItem) ((IStructuredSelection) getSelection())
						.getFirstElement();
				// We must drop in something
				if (event.item == null) {
					event.detail = DND.DROP_NONE;
				} else {
					TreeItem item = (TreeItem) event.item;
					IAggregatorItem destination = (IAggregatorItem) item
							.getData();
					IAggregatorItem parent = destination.getParent();
					while (!(parent instanceof FeedCollection)) {
						parent = parent.getParent();
					}
					((FeedCollection) parent).move(source, destination);
					TreeItem newItem = new TreeItem(item, SWT.NONE);
					newItem.setImage(dragSourceItem[0].getImage());
					newItem.setText(dragSourceItem[0].getText());
					newItem.setItemCount(dragSourceItem[0].getItemCount());
					newItem.setData(source);
				}
			}
		});
	}
}
