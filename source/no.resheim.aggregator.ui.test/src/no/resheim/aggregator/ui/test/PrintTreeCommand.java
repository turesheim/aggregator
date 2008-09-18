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
package no.resheim.aggregator.ui.test;

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.commands.AbstractAggregatorCommandHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class PrintTreeCommand extends AbstractAggregatorCommandHandler {

	public PrintTreeCommand() {
		super(false);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			try {
				TreeViewer viewer = (TreeViewer) ((IFeedView) part)
						.getFeedViewer();
				Tree tree = viewer.getTree();
				for (TreeItem item : tree.getItems()) {
					System.out.println(item);
					printItems(item, 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
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
}
