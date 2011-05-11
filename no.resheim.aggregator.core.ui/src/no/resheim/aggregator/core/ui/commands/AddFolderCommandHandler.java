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
package no.resheim.aggregator.core.ui.commands;

import java.util.UUID;

import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler for adding folder items to a feed collection. The folder will
 * be added as a child of the currently selected item.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AddFolderCommandHandler extends RenameFolderCommand implements
		IHandler {

	public AddFolderCommandHandler() {
		super(true);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			AggregatorCollection collection = ((IFeedView) part).getFeedCollection();
			if (collection == null) {
				return null;
			}
			Viewer viewer = ((IFeedView) part).getFeedViewer();
			// We must have a tree viewer for this to work
			if (!(viewer instanceof TreeViewer)) {
				return null;
			}
			TreeViewer treeViewer = (TreeViewer) viewer;
			TreeItem newItem = null;
			AggregatorItemParent parent = null;
			// We're expecting that the selected item is a parent here.
			// handleSelection should ensure that this is the case.
			TreeItem[] items = treeViewer.getTree().getSelection();
			if (items.length == 0) {
				newItem = new TreeItem(treeViewer.getTree(), SWT.NONE);
				parent = collection;
			} else {
				newItem = new TreeItem(items[0], SWT.NONE, items[0]
						.getItemCount());
				parent = (AggregatorItemParent) items[0].getData();
				// Make sure we'll see the new item
				items[0].setExpanded(true);
			}
			// Create the folder
			Folder folder = new Folder(parent, UUID.randomUUID());
			folder.setTitle(Messages.AddFolderCommandHandler_NewFolderName);
			newItem.setData(folder);
			try {
				// Add the folder to it's parent (and the database)
				parent.add(folder);
				// The new item must be selected
				treeViewer.getTree().deselectAll();
				treeViewer.getTree().select(newItem);
				// Do the renaming
				renameItem(newItem, treeViewer, folder, collection);
			} catch (CoreException e) {
				e.printStackTrace();
			}

		}
		return null;
	}

	@Override
	protected boolean handleSelection(ISelection selection) {
		// Allow no selection
		if (selection.isEmpty())
			return true;
		if (!isFolderSelected(selection))
			return false;
		return super.handleSelection(selection);
	}
}
