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
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class AddFolderCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			if (collection == null) {
				return null;
			}
			IAggregatorItem parent = getSelection(event);
			if (parent == null)
				parent = collection;
			Folder folder = collection.newFolderInstance(parent);
			folder.setTitle(Messages.AddFolderCommandHandler_NewFolderName);
			collection.addNew(folder);
		}
		return null;
	}
}
