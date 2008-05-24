/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.ui.views;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Action for renaming an item directly inside the tree view. Only categories
 * (folders) and feeds are supported. Feed articles cannot be renamed.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RenameAction extends Action {

	private TreeViewer treeView;
	private TreeEditor treeEditor;

	public RenameAction(TreeViewer treeView) {
		this.treeView = treeView;
		treeEditor = new TreeEditor(treeView.getTree());
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
	}

	public void run() {
		ISelection selection = treeView.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		renameItem(treeView.getTree().getSelection()[0], (IAggregatorItem) obj);
	} // run

	private void renameItem(final TreeItem item,
			final IAggregatorItem aggregatorItem) {
		// Create a text field to do the editing
		final Text text = new Text(treeView.getTree(), SWT.NONE);
		text.setText(aggregatorItem.getTitle());
		text.selectAll();
		text.setFocus();

		// If the text field loses focus, set its text into the tree
		// and end the editing session
		text.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				item.setText(text.getText());
				aggregatorItem.setTitle(text.getText());
				aggregatorItem.getRegistry().rename(aggregatorItem);
				text.dispose();
			}
		});

		// If they hit Enter, set the text into the tree and end the editing
		// session. If they hit Escape, ignore the text and end the editing
		// session
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.CR:
					// Enter hit--set the text into the tree and drop through
					item.setText(text.getText());
					aggregatorItem.setTitle(text.getText());
					aggregatorItem.getRegistry().rename(aggregatorItem);
				case SWT.ESC:
					// End editing session
					text.dispose();
					break;
				}
			}
		});
		// Set the text field into the editor
		treeEditor.setEditor(text, item);
	}

	@Override
	public boolean isEnabled() {
		ISelection selection = treeView.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		return (obj instanceof Folder || obj instanceof Feed);
	}

}
