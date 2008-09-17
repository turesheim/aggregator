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

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.data.AggregatorItem;
import no.resheim.aggregator.data.FeedCollection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Deletes the selected AggregatorItems.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DeleteItemCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler, ISelectionListener {

	@Override
	public void dispose() {
		super.dispose();
		// ISelectionService s = PlatformUI.getWorkbench()
		// .getActiveWorkbenchWindow().getSelectionService();
		// s.removeSelectionListener(this);
	}

	/**
	 * 
	 */
	public DeleteItemCommandHandler() {
		super();
		ISelectionService s = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService();
		s.addSelectionListener(this);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			if (collection == null) {
				return null;
			}
			for (AggregatorItem item : getSelectedItems(event)) {
				try {
					item.getParent().trash(item);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (part instanceof IFeedView) {
			setBaseEnabled(true);
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			for (AggregatorItem item : getSelectedItems(selection)) {
				if (item.getUUID()
						.equals(collection.getTrashFolder().getUUID())) {
					setBaseEnabled(false);
					break;
				}

			}
		}
	}
}
