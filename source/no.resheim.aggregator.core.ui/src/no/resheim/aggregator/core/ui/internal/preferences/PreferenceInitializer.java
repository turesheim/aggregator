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
package no.resheim.aggregator.core.ui.internal.preferences;

import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.PreferenceConstants;
import no.resheim.aggregator.core.ui.PreferenceConstants.LinkOpen;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;

/**
 * Class used to initialize default preference values.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.setDefault(PreferenceConstants.P_OPEN_LINK,
				PreferenceConstants.LinkOpen.VIEW.toString());
		store.setDefault(PreferenceConstants.P_PREVIEW_IS_READ, true);
		store.setDefault(PreferenceConstants.P_SHOW_UNREAD_COUNT, true);
		store.setDefault(PreferenceConstants.P_ARCHIVING_METHOD,
				Archiving.KEEP_NEWEST.toString());
		store.setDefault(PreferenceConstants.P_UPDATING_PERIOD,
				UpdatePeriod.HOURS.toString());
		store.setDefault(PreferenceConstants.P_UPDATING_INTERVAL, 1);
		store.setDefault(PreferenceConstants.P_ARCHIVING_ITEMS, 50);
		store.setDefault(PreferenceConstants.P_ARCHIVING_DAYS, 30);
		store.setDefault(PreferenceConstants.P_BROWSER_LOCATION_BAR, false);
		store.setDefault(PreferenceConstants.P_BROWSER_NAVIGATION_BAR, false);
		store
				.setDefault(PreferenceConstants.P_BROWSER_CREATE_NEW_WINDOW,
						false);
		store.setDefault(PreferenceConstants.P_OPEN_LINK, LinkOpen.EDITOR
				.toString());
		PreferenceConverter.setDefault(store,
				PreferenceConstants.P_UNREAD_ITEM_COLOR, new RGB(0, 0, 255));
	}

}
