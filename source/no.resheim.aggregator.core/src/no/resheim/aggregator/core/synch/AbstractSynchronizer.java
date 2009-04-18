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
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.Subscription;
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

	/** The subscription that is being synchronized */
	protected Subscription subscription;

	/** The collection of the subscription */
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

	/**
	 * Sets the collection that is being synchronised.
	 * 
	 * @param collection
	 *            the collection
	 */
	public void setCollection(FeedCollection collection) {
		this.collection = collection;
	}

	/**
	 * Sets the subscription that we're going to use for synchronise with the
	 * collection.
	 * 
	 * @param feed
	 *            the subscription
	 */
	public void setSubscription(Subscription feed) {
		this.subscription = feed;
		setName(MessageFormat.format(Messages.FeedUpdateJob_Title,
				new Object[] { feed.getTitle() }));
	}

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 *            the site to clean up
	 */
	protected void cleanUp() {
		try {
			for (Folder folder : collection.getDescendingFolders()) {
				if (folder.getUUID().equals(subscription.getLocation())) {
					folder.cleanUp(subscription);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns the URL to use when updating the feed. In cases where special
	 * handling of the feed URL is required, this method should be
	 * re-implemented in the subclass.
	 * 
	 * @param feed
	 *            the feed to calculate the URL for
	 * @return the calculated URL
	 * @throws MalformedURLException
	 */
	protected URL getURL() throws MalformedURLException {
		return new URL(subscription.getURL());
	}

	/**
	 * 
	 * @param debug
	 * @return
	 */
	protected IStatus download() {
		try {

			URL feedURL = getURL();
			if (feedURL == null) {
				return Status.CANCEL_STATUS;
			}
			// Download the feed
			URLConnection yc = AggregatorPlugin.getDefault().getConnection(
					feedURL, subscription.isAnonymousAccess(),
					subscription.getUUID().toString());
			FeedParser handler = new FeedParser(collection, subscription);
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser parser = factory.newSAXParser();
			InputStream is = yc.getInputStream();
			parser.parse(is, handler);
			is.close();
			// Download the favicon
			dowloadFavicon(subscription, feedURL);
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

	protected void dowloadFavicon(Subscription feed, URL feedURL)
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
