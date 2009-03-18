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
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Feed.Archiving;
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
	protected URL getURL(Feed feed) throws MalformedURLException {
		IPreferenceStore store = GoogleReaderPlugin.getDefault()
				.getPreferenceStore();
		int count = store.getInt(PreferenceConstants.P_AMOUNT);
		String base = URL_PREFIX + feed.getURL();
		if (feed.getLastUpdate() > 0) {
			base += "?ot=" + feed.getLastUpdate() + "&r=o&ck="
					+ System.currentTimeMillis();
		} else {
			base += "?n=" + count;
		}
		return new URL(base);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		synchronized (feed) {
			feed.setUpdating(true);
			feed.getTempItems().clear();
		}
		// Why this?
		collection.notifyListerners(new Feed[] { feed }, EventType.CHANGED);
		// If the feed does not use archiving it's better to remove all items
		// before downloading new ones.
		if (feed.getArchiving() == Archiving.KEEP_NONE) {
			// TODO: Implement cleanup.
		}
		MultiStatus ms = new MultiStatus(AggregatorPlugin.PLUGIN_ID,
				IStatus.OK, MessageFormat.format(
						Messages.FeedUpdateJob_StatusTitle, new Object[] { feed
								.getTitle() }), null);
		try {
			// First log in.
			GoogleReaderPlugin.login();
			// TODO: Handle the count
			ms.add(download(feed, true));
			if (ms.isOK()) {
				setName(MessageFormat.format(Messages.FeedUpdateJob_CleaningUp,
						new Object[] { feed.getTitle() }));
				cleanUp(feed);
			}
			synchronized (feed) {
				feed.setUpdating(false);
			}
			Collections.sort(feed.getTempItems());
			if (feed.getTempItems().size() > 0) {
				ms.addAll(collection.addNew(feed.getTempItems().toArray(
						new AggregatorItem[feed.getTempItems().size()])));
			}
			feed.setLastUpdate(System.currentTimeMillis());
			feed.setLastStatus(ms);
			// Store changes to the feed
			collection.feedUpdated(feed);
			collection.notifyListerners(new Feed[] { feed }, EventType.CHANGED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ms;
	}
}
