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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Contribution item that creates one or more command instances is used to
 * select a <i>public</i> feed collection.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedCollectionMenu extends
		org.eclipse.ui.actions.CompoundContributionItem {

	public FeedCollectionMenu() {
	}

	public FeedCollectionMenu(String id) {
		super(id);
	}

	@SuppressWarnings( {
			"unchecked", "deprecation"
	})
	@Override
	protected IContributionItem[] getContributionItems() {
		ArrayList<IContributionItem> actions = new ArrayList<IContributionItem>();
		Collection<AggregatorCollection> collections = AggregatorPlugin.getDefault()
				.getCollections();
		// No need to show the menu if we just have one item. It's always
		// selected per default.
		if (collections.size() <= 1) {
			return new IContributionItem[0];
		}
		IServiceLocator locator = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		// TODO: Fix this deprecation issue when no longer supporting 3.3
		for (AggregatorCollection feedRegistry : collections) {
			if (feedRegistry.isPublic()) {
				Map parms = new HashMap();
				parms.put(FeedCollectionSelectionHandler.PARM_COLLECTION,
						feedRegistry.getId());
				CommandContributionItem item = new CommandContributionItem(
						locator, feedRegistry.getId(),
						AggregatorUIPlugin.CMD_SELECT_COLLECTION, parms, null,
						null, null, feedRegistry.getTitle(), null, null,
						CommandContributionItem.STYLE_CHECK);
				actions.add(item);
			}

		}
		return actions.toArray(new IContributionItem[actions.size()]);
	}
}
