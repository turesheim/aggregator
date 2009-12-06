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

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem.Flag;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Deletes the selected AggregatorItems.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DeleteItemCommandHandler extends AbstractAggregatorCommandHandler {

	@Override
	protected boolean handleSelection(ISelection selection) {
		for (AggregatorItem item : getSelectedItems(selection)) {
			if (item.getFlags().contains(Flag.TRASHED)) {
				return false;
			}
		}
		return super.handleSelection(selection);
	}

	/**
	 * 
	 */
	public DeleteItemCommandHandler() {
		super(true, false);
	}

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			final AggregatorCollection collection = ((IFeedView) part)
					.getFeedCollection();
			if (collection == null) {
				return null;
			}
			Runnable runnable = new Runnable() {
				public void run() {
					AggregatorItem[] items = getSelectedItems(event);
					((IFeedView) part).getFeedViewer().setSelection(null);
					for (AggregatorItem item : items) {
						try {
							item.getParent().trash(item);
						} catch (CoreException e) {
							e.printStackTrace();
						}
					}
					// Tell our listeners that the deed is done
					collection.notifyListerners(items, EventType.MOVED);
				}
			};
			Display display = part.getSite().getPart().getSite()
					.getWorkbenchWindow().getShell().getDisplay();
			BusyIndicator.showWhile(display, runnable);
			display.syncExec(runnable);
		}
		return null;
	}
}
