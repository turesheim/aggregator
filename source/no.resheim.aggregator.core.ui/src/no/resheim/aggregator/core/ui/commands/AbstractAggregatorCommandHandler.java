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
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Folder;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractAggregatorCommandHandler extends AbstractHandler
		implements ISelectionListener {

	private boolean fIgnoreSystemItems = false;

	ISelectionService service = null;

	/**
	 * @param ignoreSystemItems
	 */
	public AbstractAggregatorCommandHandler(boolean ignoreSystemItems) {
		super();
		fIgnoreSystemItems = ignoreSystemItems;
		service = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getSelectionService();
		service.addSelectionListener(this);
	}

	@Override
	public void dispose() {
		if (service != null) {
			service.removeSelectionListener(this);
		}
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

	protected boolean isArticleSelected(ISelection selection) {
		AggregatorItem[] items = getSelectedItems(selection);
		if (items.length == 1) {
			if (items[0] instanceof Article) {
				return true;
			}
		}
		return false;
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (fIgnoreSystemItems && part instanceof IFeedView) {
			setBaseEnabled(true);
			for (AggregatorItem item : getSelectedItems(selection)) {
				if (item.isSystem()) {
					setBaseEnabled(false);
					break;
				}

			}
		}
	}
}
