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
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class UpdateFeedCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public UpdateFeedCommandHandler() {
		super(false);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			AggregatorCollection registry = ((IFeedView) part).getFeedCollection();
			if (registry == null) {
				return null;
			}
			AggregatorItem item = getSelection(event);
			if (item != null && item instanceof Folder) {
				try {
					IStatus s = registry.synchronize(item);
					if (!s.isOK()) {
						StatusManager.getManager().handle(s,
								StatusManager.BLOCK);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			} else {
				// registry.updateAllFeeds();
			}
		}
		return null;
	}

	@Override
	protected boolean handleSelection(ISelection selection) {
		return isFeedSelected(selection);
	}
}
