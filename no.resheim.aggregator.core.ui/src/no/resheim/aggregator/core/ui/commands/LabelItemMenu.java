/*******************************************************************************
 * Copyright (c) 2007-2009 Torkild Ulvøy Resheim.
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

import java.util.HashMap;
import java.util.Map;

import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.PreferenceConstants;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;

/**
 * Connects to the preferences and creates menu items for the labels allowed to
 * be used for articles.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class LabelItemMenu extends CompoundContributionItem {

	private static final String COMMAND_ID = "no.resheim.aggregator.core.ui.markItem"; //$NON-NLS-1$

	@Override
	protected IContributionItem[] getContributionItems() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		String[] labels = store.getString(PreferenceConstants.P_ITEM_LABELS)
				.split(",");
		IContributionItem[] actions = new IContributionItem[labels.length];
		for (int a = 0; a < labels.length; a++) {
			actions[a] = createAction(labels[a]);
		}
		return actions;
	}

	@SuppressWarnings("unchecked")
	private IContributionItem createAction(String title) {
		Map parms = new HashMap();
		parms.put(LabelItemSelectionHandler.PARM_MARK, title);
		CommandContributionItemParameter item = new CommandContributionItemParameter(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
				COMMAND_ID + "_" + title, COMMAND_ID, SWT.CHECK);
		item.label = title;
		item.parameters = parms;
		return new CommandContributionItem(item);
	}
}
