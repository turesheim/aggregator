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
package no.resheim.aggregator.core.synch;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.rss.internal.FeedParser;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.equinox.security.storage.StorageException;
import org.xml.sax.SAXException;

/**
 * Abstract implementation of a subscription synchroniser. Subclasses of this
 * type are responsible for using information from the supplied subscription for
 * obtaining data and populate the collection.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractSynchronizer extends Job {
	protected Feed feed;
	protected FeedCollection collection;

	protected static final String EMPTY_STRING = "";

	protected static final String CORE_NET_BUNDLE = "org.eclipse.core.net";

	public AbstractSynchronizer() {
		super(EMPTY_STRING);
		setPriority(Job.DECORATE);
		setUser(false);
	}

	@Override
	protected abstract IStatus run(IProgressMonitor monitor);

	public void setCollection(FeedCollection collection) {
		this.collection = collection;
	}

	public void setSubscription(Feed feed) {
		this.feed = feed;
		setName(MessageFormat.format(Messages.FeedUpdateJob_Title,
				new Object[] { feed.getTitle() }));
	}

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	protected void cleanUp(Feed site) {
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
	protected IStatus download(Feed feed, boolean debug) {
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

	protected void dowloadFavicon(Feed feed, URL feedURL)
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
