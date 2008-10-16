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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.rss.internal.FeedParser;

import org.eclipse.core.runtime.CoreException;
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
	private FeedCollection collection;

	public FeedUpdateJob(FeedCollection collection, Feed feed) {
		super(MessageFormat.format(Messages.FeedUpdateJob_Title, new Object[] {
			feed.getTitle()
		}));
		this.feed = feed;
		this.collection = collection;
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
			feed.getTempItems().clear();
		}
		boolean debug = AggregatorPlugin.getDefault().isDebugging();
		// registry.notifyListerners(new AggregatorItemChangedEvent(feed,
		// FeedChangeEventType.UPDATING));
		// If the feed does not use archiving it's better to remove all items
		// before downloading new ones.
		if (feed.getArchiving() == Archiving.KEEP_NONE) {
			// TODO: Implement cleanup.
		}
		IStatus ds = download(feed, debug);
		feed.setLastStatus(ds);
		if (ds.isOK()) {
			setName(Messages.FeedUpdateJob_CleaningUp);
			cleanUp(feed);
			feed.setLastUpdate(System.currentTimeMillis());
			// Store changes to the feed
			collection.feedUpdated(feed);
		}
		synchronized (feed) {
			feed.setUpdating(false);
			Collections.sort(feed.getTempItems());
			if (feed.getTempItems().size() > 0) {
				collection.addNew(feed.getTempItems().toArray(
						new AggregatorItem[feed.getTempItems().size()]));
			}
		}
		return ds;

	}

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	void cleanUp(Feed site) {
		// First find the folder
		try {
			for (Folder folder : collection.getDescendingFolders()) {
				if (folder.getUUID().equals(site.getLocation())) {
					folder.cleanUp(site);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @param debug
	 * @return
	 */
	private IStatus download(Feed feed, boolean debug) {
		try {
			URL feedURL = new URL(feed.getURL());
			if (feedURL == null) {
				return Status.CANCEL_STATUS;
			}

			// Download the feed
			URLConnection yc = getConnection(feed, feedURL);
			FeedParser handler = new FeedParser(collection, feed);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputStream is = yc.getInputStream();
			parser.parse(is, handler);
			is.close();
			// Download the favicon
			dowloadFavicon(feed, feedURL);
			return Status.OK_STATUS;
		} catch (StorageException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not obtain credentials", e); //$NON-NLS-1$
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

	private void dowloadFavicon(Feed feed, URL feedURL)
			throws MalformedURLException, StorageException {
		URL favicon = new URL(MessageFormat.format("{0}://{1}/favicon.ico", //$NON-NLS-1$
				new Object[] {
						feedURL.getProtocol(), feedURL.getHost()
				}));
		try {
			URLConnection ic = getConnection(feed, favicon);
			InputStream is = ic.getInputStream();
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			byte buffer[] = new byte[0xffff];
			int nbytes;
			while ((nbytes = is.read(buffer)) != -1)
				fos.write(buffer, 0, nbytes);
			feed.setImageData(fos.toByteArray());
			is.close();
			System.out.println(feed.getImageData().length);
		} catch (IOException e) {
			// Silently ignore that the image file could not be found
		}
	}

	private URLConnection getConnection(Feed site, URL feed)
			throws IOException, StorageException {
		URLConnection yc = feed.openConnection();
		yc.setAllowUserInteraction(true);
		if (!site.isAnonymousAccess()) {
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(site.getUUID().toString());
			String credentials = feedNode.get(
					AggregatorPlugin.SECURE_STORAGE_USERNAME, EMPTY_STRING)
					+ ":" //$NON-NLS-1$
					+ feedNode.get(AggregatorPlugin.SECURE_STORAGE_PASSWORD,
							EMPTY_STRING);
			String encoding = EncodingUtils
					.encodeBase64(credentials.getBytes());
			yc.setRequestProperty("Authorization", "Basic" + encoding); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return yc;
	}
}
