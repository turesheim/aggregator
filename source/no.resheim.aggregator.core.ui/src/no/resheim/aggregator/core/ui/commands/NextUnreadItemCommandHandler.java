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
package no.resheim.aggregator.core.ui.commands;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * 
 */
public class NextUnreadItemCommandHandler extends
		AbstractAggregatorCommandHandler implements IHandler {

	public NextUnreadItemCommandHandler() {
		super(false);
	}

	@Override
	public boolean isEnabled() {
		// Do not enable action if the collection has not initialised yet.
		if (!AggregatorPlugin.isInitialized())
			return false;
		return true;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		AggregatorItem item = getSelection(event);
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		// If the user want to go to the next unread article while an unread
		// article is selected we shall mark the selection as read.
		if (item instanceof Article && !((Article) item).isRead()) {
			try {
				getCollection(event).setRead(item);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		while (item != null) {
			if (item instanceof Article) {
				if (!((Article) item).isRead()) {
					if (part instanceof IFeedView) {
						Viewer v = ((IFeedView) part).getFeedViewer();
						v.setSelection(new StructuredSelection(item), true);
						return null;
					}
				}
			}
			try {
				item = item.getParent().getChildAt(item.getOrdering() + 1);
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}
}
