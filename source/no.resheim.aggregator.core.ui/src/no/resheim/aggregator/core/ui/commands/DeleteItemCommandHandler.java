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
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Deletes the selected IAggregatorItem.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DeleteItemCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			if (collection == null) {
				return null;
			}
			IAggregatorItem item = getSelection(event);
			if (item != null) {
				IStatus status = collection.delete(item);
				// Show a message if something went wrong.
				if (!status.isOK()) {
					StatusManager.getManager().handle(status,
							StatusManager.SHOW);
				}
			}
		}
		return null;
	}
}
