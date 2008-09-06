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
package no.resheim.aggregator.data;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.rss.internal.FeedParser;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.xml.sax.SAXException;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedUpdateJob extends Job {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private Feed feed;
	private FeedCollection registry;

	public FeedUpdateJob(FeedCollection registry, Feed feed) {
		super(MessageFormat.format(Messages.FeedUpdateJob_Title, new Object[] {
			feed.getTitle()
		}));
		this.feed = feed;
		this.registry = registry;
		setPriority(Job.DECORATE);
		setUser(false);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		// We won't update these
		if (feed.getURL().startsWith("test://")) { //$NON-NLS-1$
			return Status.OK_STATUS;
		}
		synchronized (feed) {
			feed.setUpdating(true);
		}
		boolean debug = AggregatorPlugin.getDefault().isDebugging();
		registry.notifyListerners(new AggregatorItemChangedEvent(feed,
				FeedChangeEventType.UPDATING));
		// If the feed does not use archiving it's better to remove all items
		// before downloading new ones.
		if (feed.getArchiving() == Archiving.KEEP_NONE) {
			// TODO: Implement cleanup.
		}
		IStatus ds = download(feed, debug);
		feed.setLastStatus(ds);
		if (ds.isOK()) {
			registry.cleanUp(feed);
			feed.setLastUpdate(System.currentTimeMillis());
			// Store changes to the feed
			registry.feedUpdated(feed);
		}
		registry.notifyListerners(new AggregatorItemChangedEvent(feed,
				FeedChangeEventType.UPDATED));
		synchronized (feed) {
			feed.setUpdating(false);
		}
		return ds;

	}

	/**
	 * 
	 * @param debug
	 * @return
	 */
	private IStatus download(Feed site, boolean debug) {
		try {
			URL feed = new URL(site.getURL());
			if (feed == null) {
				return Status.CANCEL_STATUS;
			}

			// IProxyService proxy = Aggregator.getDefault().getProxyService();
			URLConnection yc = feed.openConnection();
			yc.setAllowUserInteraction(true);
			if (!site.isAnonymousAccess()) {
				ISecurePreferences root = SecurePreferencesFactory.getDefault()
						.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
				ISecurePreferences feedNode = root.node(site.getUUID()
						.toString());
				try {
					String credentials = feedNode.get(
							AggregatorPlugin.SECURE_STORAGE_USERNAME,
							EMPTY_STRING)
							+ ":" //$NON-NLS-1$
							+ feedNode.get(
									AggregatorPlugin.SECURE_STORAGE_PASSWORD,
									EMPTY_STRING);
					String encoding = EncodingUtils.encodeBase64(credentials
							.getBytes());
					yc.setRequestProperty("Authorization", "Basic" + encoding); //$NON-NLS-1$ //$NON-NLS-2$
				} catch (StorageException e) {

				}

			}
			FeedParser handler = new FeedParser(registry, site);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputStream is = yc.getInputStream();
			parser.parse(is, handler);
			is.close();
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		} catch (ParserConfigurationException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		} catch (SAXException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		}
	}
}
