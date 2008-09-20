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

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.data.AggregatorItem;
import no.resheim.aggregator.data.AggregatorItemParent;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.internal.InternalFolder;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler for adding folder items to a feed collection. The folder will
 * be added as a child of the currently selected item.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class AddFolderCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public AddFolderCommandHandler() {
		super(true);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			if (collection == null) {
				return null;
			}
			AggregatorItem parent = getSelection(event);
			if (parent == null)
				parent = collection;
			if (parent instanceof AggregatorItemParent) {
				InternalFolder folder = new InternalFolder(
						(AggregatorItemParent) parent, UUID.randomUUID());
				folder.setTitle(Messages.AddFolderCommandHandler_NewFolderName);
				collection.addNew(folder);
			}
		}
		return null;
	}

	@Override
	protected boolean handleSelection(ISelection selection) {
		boolean enabled = super.handleSelection(selection);
		if (enabled) {
			enabled = isFolderSelected(selection);
		}
		return enabled;
	}
}
