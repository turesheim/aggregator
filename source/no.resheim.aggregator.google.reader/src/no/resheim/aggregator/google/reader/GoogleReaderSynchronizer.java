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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.synch.AbstractSynchronizer;
import no.resheim.aggregator.core.synch.Messages;
import no.resheim.aggregator.google.reader.ui.PreferenceConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;

public class GoogleReaderSynchronizer extends AbstractSynchronizer {
	private static final String ENCODING = "UTF-8";
	private static final String STATE_STARRED = "user/-/state/com.google/starred";
	private static final String STATE_READ = "user/-/state/com.google/read";
	private static final String LABEL_PREFIX = "user/-/label/";
	private static final String FEED_URL_PREFIX = "http://www.google.com/reader/atom/feed/";

	/**
	 * Overrides the default implementation by fetching a given number of items
	 * (from preference settings) if the feed has never been updated and only
	 * items since last update if the feed has previously been updated.
	 */
	protected URL getURL() throws MalformedURLException {
		IPreferenceStore store = GoogleReaderPlugin.getDefault()
				.getPreferenceStore();
		int count = store.getInt(PreferenceConstants.P_AMOUNT);
		String base = FEED_URL_PREFIX + subscription.getURL();
		if (subscription.getLastUpdate() > 0) {
			// ot = The time (Unix time, number of seconds from January 1st,
			// 1970 00:00 UTC) from which to start to get items. Only works for
			// order r=o mode. If the time is older than one month ago, one
			// month ago will be used instead.
			// ck = current time stamp, probably used as a quick hack to be sure
			// that
			// cache won't be triggered.
			base += "?ot=" + subscription.getLastUpdate() / 1000 + "&r=o&ck="
					+ System.currentTimeMillis() / 1000;
		} else {
			base += "?n=" + count;
		}
		return new URL(base);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		subscription.setUpdating(true);
		subscription.getTempItems().clear();
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
			// First log in or cancel if credentials could not be obtained.
			if (!GoogleReaderPlugin.login()) {
				ms.add(new Status(IStatus.WARNING, AggregatorPlugin.PLUGIN_ID,
						MessageFormat.format(
								"Logging into Google account cancelled",
								new Object[] {})));
				subscription.setUpdating(false);
				collection.notifyListerners(
						new Subscription[] { subscription }, EventType.CHANGED);
				return ms;
			}
			// TODO: Handle the count
			ms.add(download());
			if (ms.isOK()) {
				// Upload changes
				List<Article> changed = collection.getChangedArticles(
						subscription, subscription.getLastUpdate());
				setName("Synchronizing changes");
				SubProgressMonitor update = new SubProgressMonitor(monitor, 1);
				update.beginTask("Synchronizing changes", changed.size());
				for (Article article : changed) {
					update.setTaskName(article.getTitle());
					synchronize(article);
					update.worked(1);
				}
			}
			Collections.sort(subscription.getTempItems());
			if (subscription.getTempItems().size() > 0) {
				ms.addAll(collection.addNew(subscription.getTempItems()
						.toArray(
								new AggregatorItem[subscription.getTempItems()
										.size()])));
			}
			// Wrap up
			subscription.setLastUpdate(System.currentTimeMillis());
			subscription.setLastStatus(ms);
			// Store changes to the feed
			collection.feedUpdated(subscription);
		} catch (CoreException e) {
			// Log the status from the exception
			ms.add(e.getStatus());
		}
		subscription.setUpdating(false);
		collection.notifyListerners(new Subscription[] { subscription },
				EventType.CHANGED);
		return ms;
	}

	private String encode(boolean state, String id)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(URLEncoder.encode(state ? "a" : "r", ENCODING));
		sb.append("=");
		sb.append(URLEncoder.encode(id, ENCODING));
		return sb.toString();
	}

	private String getStateString(Article article, String encoding)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		sb.append(encode(article.isStarred(), STATE_STARRED));
		sb.append('&');
		sb.append(encode(article.isRead(), STATE_READ));
		return sb.toString();
	}

	private String getLabelString(Article article, String encoding)
			throws UnsupportedEncodingException {
		boolean more = false;
		StringBuilder sb = new StringBuilder();
		for (String label : article.getLabels()) {
			if (more) {
				sb.append('&');
			}
			sb.append(URLEncoder.encode("a", encoding));
			sb.append('=');
			sb.append(URLEncoder.encode(LABEL_PREFIX + label, encoding));
			more = true;
		}
		return sb.toString();
	}

	/**
	 * TODO: Change to become a generic method.
	 * 
	 * @param article
	 */
	private void synchronize(Article article) throws CoreException {
		try {
			// We need this or we'll get a 401
			GoogleReaderPlugin.login();
			String token = GoogleReaderPlugin.getToken();
			URL url = new URL("http://www.google.com/reader/api/0/edit-tag");
			URLConnection yc = AggregatorPlugin.getDefault().getConnection(url,
					true, null);
			yc.setDoOutput(true);
			String data = URLEncoder.encode("i", ENCODING) + "="
					+ URLEncoder.encode(article.getGuid(), ENCODING);
			data += "&" + getStateString(article, ENCODING);
			data += "&" + getLabelString(article, ENCODING);
			data += "&" + URLEncoder.encode("ac", ENCODING) + "="
					+ URLEncoder.encode("edit", ENCODING);
			data += "&" + URLEncoder.encode("T", ENCODING) + "="
					+ URLEncoder.encode(token, ENCODING);
			OutputStreamWriter wr = new OutputStreamWriter(yc.getOutputStream());
			wr.write(data);
			wr.flush();
			System.out.println(data);
			BufferedReader rd = new BufferedReader(new InputStreamReader(yc
					.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				if (!line.equals("OK")) {
					throw new IOException("Item update did not succeed.");
				}
			}
			rd.close();
		} catch (MalformedURLException e) {
			throwException("Could not synchronize Google Reader item", e);
		} catch (UnknownHostException e) {
			throwException("Could not synchronize Google Reader item", e);
		} catch (IOException e) {
			throwException("Could not synchronize Google Reader item", e);
		} catch (StorageException e) {
			throwException("Could not synchronize Google Reader item", e);
		}
	}

	private void throwException(String message, Exception e)
			throws CoreException {
		throw new CoreException(new Status(IStatus.ERROR,
				GoogleReaderPlugin.PLUGIN_ID, message, e));
	}

}
