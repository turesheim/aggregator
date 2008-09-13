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

import java.util.ArrayList;

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.data.AggregatorItem;
import no.resheim.aggregator.data.FeedCollection;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractAggregatorCommandHandler extends AbstractHandler {
	/**
	 * Returns the feed collection that the command's view is connected to.
	 * <b>Null</b> is returned if the view could not be determined.
	 * 
	 * @param event
	 *            the event
	 * @return the collection or <b>null</b>
	 */
	protected FeedCollection getCollection(ExecutionEvent event) {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			return collection;
		}
		return null;
	}

	protected AggregatorItem getSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o != null && o instanceof AggregatorItem) {
				return ((AggregatorItem) o);
			}
		}
		return null;
	}

	protected AggregatorItem[] getSelectedItems(ExecutionEvent event) {
		ArrayList<AggregatorItem> items = new ArrayList<AggregatorItem>();
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			for (Object o : ((IStructuredSelection) selection).toArray()) {
				if (o != null && o instanceof AggregatorItem) {
					items.add((AggregatorItem) o);
				}
			}
		}
		return items.toArray(new AggregatorItem[items.size()]);
	}
}
