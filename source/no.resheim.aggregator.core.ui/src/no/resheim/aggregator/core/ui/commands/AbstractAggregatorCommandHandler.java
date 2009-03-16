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

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler that uses the current
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractAggregatorCommandHandler extends AbstractHandler {

	private boolean fDisallowSystemItems = true;
	private boolean fDisregardSelection = false;

	/**
	 * @param disallowSystemItems
	 *            if <code>true</code> system items may not be selected
	 * @param disregardSelection
	 *            if <code>true</code> the selection does not matter
	 */
	public AbstractAggregatorCommandHandler(boolean disallowSystemItems,
			boolean disregardSelection) {
		super();
		fDisallowSystemItems = disallowSystemItems;
		fDisregardSelection = disregardSelection;
	}

	/**
	 * @param disallowSystemItems
	 */
	public AbstractAggregatorCommandHandler(boolean disallowSystemItems) {
		this(disallowSystemItems, false);
	}

	@Override
	public boolean isEnabled() {
		// Do not enable action if the collection has not initialised yet.
		if (!AggregatorPlugin.isInitialized())
			return false;
		ISelection selection = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		return handleSelection(selection);
	}

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

	protected AggregatorItem[] getSelectedItems(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		return getSelectedItems(selection);
	}

	protected AggregatorItem[] getSelectedItems(ISelection selection) {
		ArrayList<AggregatorItem> items = new ArrayList<AggregatorItem>();
		if (selection instanceof IStructuredSelection) {
			for (Object o : ((IStructuredSelection) selection).toArray()) {
				if (o != null && o instanceof AggregatorItem) {
					items.add((AggregatorItem) o);
				}
			}
		}
		return items.toArray(new AggregatorItem[items.size()]);
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

	/**
	 * Tests of the item in the selection is a Folder representing a feed and if
	 * that is the case, <b>true</b> is returned. If more than one item is
	 * selected <b>false</b> is always returned.
	 * 
	 * @param selection
	 *            the selection
	 * @return <b>true</b>if a feed is selected
	 */
	protected boolean isFeedSelected(ISelection selection) {
		AggregatorItem[] items = getSelectedItems(selection);
		if (items.length == 1) {
			if (items[0] instanceof Folder) {
				Feed feed = ((Folder) items[0]).getFeed();
				if (feed != null)
					return true;
			}
		}
		return false;
	}

	/**
	 * Tests of the item in the selection is a Folder <u>not</u> representing a
	 * feed and if that is the case, <b>true</b> is returned. If more than one
	 * item is selected <b>false</b> is always returned.
	 * 
	 * @param selection
	 *            the selection
	 * @return <b>true</b>if a feed is selected
	 */
	protected boolean isFolderSelected(ISelection selection) {
		AggregatorItem[] items = getSelectedItems(selection);
		if (items.length == 1) {
			if (items[0] instanceof Folder) {
				Feed feed = ((Folder) items[0]).getFeed();
				if (feed == null)
					return true;
			}
		}
		return false;
	}

	/**
	 * Tests if the selection consists of only one article and returns
	 * <b>true</b> if this is the case.
	 * 
	 * @param selection
	 *            the selection to consider
	 * @return <b>true</b> if one article is selected
	 */
	protected boolean isArticleSelected(ISelection selection) {
		AggregatorItem[] items = getSelectedItems(selection);
		if (items.length == 1) {
			if (items[0] instanceof Article) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 
	 * @param selection
	 *            the selection to consider
	 * @return <code>true</code> if the selection should be handled
	 */
	protected boolean handleSelection(ISelection selection) {
		if (fDisregardSelection)
			return super.isEnabled();

		if (selection.isEmpty()) {
			return false;
		}
		if (fDisallowSystemItems) {
			for (AggregatorItem item : getSelectedItems(selection)) {
				if (item.isSystem()) {
					return false;
				}
			}
		}
		return true;
	}
}
