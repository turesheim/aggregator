/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.google.reader;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Collections;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.synch.AbstractSynchronizer;
import no.resheim.aggregator.core.synch.Messages;
import no.resheim.aggregator.google.reader.ui.PreferenceConstants;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.jface.preference.IPreferenceStore;

public class GoogleReaderSynchronizer extends AbstractSynchronizer {
	private final String URL_PREFIX = "http://www.google.com/reader/atom/feed/";

	/**
	 * Overrides the default implementation by fetching a given number of items
	 * (from preference settings) if the feed has never been updated and only
	 * items since last update if the feed has previously been updated.
	 */
	protected URL getURL() throws MalformedURLException {
		IPreferenceStore store = GoogleReaderPlugin.getDefault()
				.getPreferenceStore();
		int count = store.getInt(PreferenceConstants.P_AMOUNT);
		String base = URL_PREFIX + subscription.getURL();
		if (subscription.getLastUpdate() > 0) {
			base += "?ot=" + subscription.getLastUpdate() + "&r=o&ck="
					+ System.currentTimeMillis();
		} else {
			base += "?n=" + count;
		}
		System.out.println(base);
		return new URL(base);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		synchronized (subscription) {
			subscription.setUpdating(true);
			subscription.getTempItems().clear();
		}
		// Why this?
		collection.notifyListerners(new Subscription[] { subscription },
				EventType.CHANGED);
		// If the feed does not use archiving it's better to remove all items
		// before downloading new ones.
		if (subscription.getArchiving() == Archiving.KEEP_NONE) {
			// TODO: Implement cleanup.
		}
		MultiStatus ms = new MultiStatus(AggregatorPlugin.PLUGIN_ID,
				IStatus.OK, MessageFormat.format(
						Messages.FeedUpdateJob_StatusTitle,
						new Object[] { subscription.getTitle() }), null);
		try {
			// First log in.
			GoogleReaderPlugin.login();
			// TODO: Handle the count
			ms.add(download());
			if (ms.isOK()) {
				setName(MessageFormat.format(Messages.FeedUpdateJob_CleaningUp,
						new Object[] { subscription.getTitle() }));
				cleanUp();
			}
			synchronized (subscription) {
				subscription.setUpdating(false);
			}
			Collections.sort(subscription.getTempItems());
			if (subscription.getTempItems().size() > 0) {
				ms.addAll(collection.addNew(subscription.getTempItems()
						.toArray(
								new AggregatorItem[subscription.getTempItems()
										.size()])));
			}
			subscription.setLastUpdate(System.currentTimeMillis());
			subscription.setLastStatus(ms);
			// Store changes to the feed
			collection.feedUpdated(subscription);
			collection.notifyListerners(new Subscription[] { subscription },
					EventType.CHANGED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ms;
	}
}
