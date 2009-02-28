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
package no.resheim.aggregator.core.synch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collections;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Feed.Archiving;
import no.resheim.aggregator.core.rss.internal.FeedParser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.StorageException;
import org.xml.sax.SAXException;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DirectSynchronizer extends AbstractSynchronizer {

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		synchronized (feed) {
			feed.setUpdating(true);
			feed.getTempItems().clear();
		}
		boolean debug = AggregatorPlugin.getDefault().isDebugging();
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
			if (!feed.getURL().startsWith("test://")) { //$NON-NLS-1$
				ms.add(download(feed, debug));
			}
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

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	private void cleanUp(Feed site) {
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
			URLConnection yc = AggregatorPlugin.getDefault().getConnection(
					feedURL, feed.isAnonymousAccess(),
					feed.getUUID().toString());
			FeedParser handler = new FeedParser(collection, feed);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputStream is = yc.getInputStream();
			parser.parse(is, handler);
			is.close();
			// Download the favicon
			dowloadFavicon(feed, feedURL);
			return Status.OK_STATUS;
		} catch (UnknownHostException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.FeedUpdateJob_HostError,
							new Object[] { e.getMessage() }));
		} catch (StorageException e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					Messages.FeedUpdateJob_CredentialsError, e);
		} catch (IOException e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		} catch (SAXException e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		} catch (Exception e) {
			e.printStackTrace();
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID, 0,
					Messages.FeedUpdateJob_Error_Title, e);
		}
	}

	private void dowloadFavicon(Feed feed, URL feedURL)
			throws MalformedURLException, StorageException {
		URL favicon = new URL(MessageFormat.format("{0}://{1}/favicon.ico", //$NON-NLS-1$
				new Object[] { feedURL.getProtocol(), feedURL.getHost() }));
		try {
			URLConnection yc = AggregatorPlugin.getDefault().getConnection(
					favicon, feed.isAnonymousAccess(),
					feed.getUUID().toString());
			InputStream is = yc.getInputStream();
			ByteArrayOutputStream fos = new ByteArrayOutputStream();
			byte buffer[] = new byte[0xffff];
			int nbytes;
			while ((nbytes = is.read(buffer)) != -1)
				fos.write(buffer, 0, nbytes);
			feed.setImageData(fos.toByteArray());
			is.close();
		} catch (IOException e) {
			// Silently ignore that the image file could not be found
		}
	}
}
