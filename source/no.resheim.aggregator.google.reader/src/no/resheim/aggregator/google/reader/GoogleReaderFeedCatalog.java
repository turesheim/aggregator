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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.catalog.AbstractFeedCatalog;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.google.reader.rss.SubscriptionsParser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.ui.statushandlers.StatusManager;
import org.xml.sax.SAXException;

/**
 * This extension of {@link AbstractFeedCatalog} logs in to Google reader and
 * obtains a list of all feeds currently subscribed to.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.1
 */
public class GoogleReaderFeedCatalog extends AbstractFeedCatalog {

	private static final String SUBSCRIPTIONS_URL = "http://www.google.com/reader/api/0/subscription/list?output=xml";

	public GoogleReaderFeedCatalog() {
	}

	public Subscription[] getFeeds() {
		ArrayList<Subscription> feeds = new ArrayList<Subscription>();
		IStatus login = GoogleReaderPlugin.login();
		if (login.isOK()) {
			try {
				URL url = new URL(SUBSCRIPTIONS_URL);
				URLConnection yc = AggregatorPlugin.getDefault().getConnection(
						url, true, null);
				SubscriptionsParser handler = new SubscriptionsParser(this,
						feeds);
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser parser = factory.newSAXParser();
				InputStream is = yc.getInputStream();
				parser.parse(yc.getInputStream(), handler);
				is.close();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (StorageException e) {
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			}
		} else {
			StatusManager.getManager().handle(login, StatusManager.BLOCK);
		}
		return feeds.toArray(new Subscription[feeds.size()]);
	}

	public boolean isEnabled() {
		return true;
	}

}
