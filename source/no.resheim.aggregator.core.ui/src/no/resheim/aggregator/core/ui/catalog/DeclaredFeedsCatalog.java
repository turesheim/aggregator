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
package no.resheim.aggregator.core.ui.catalog;

import java.util.ArrayList;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.catalog.AbstractFeedCatalog;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DeclaredFeedsCatalog extends AbstractFeedCatalog {

	public Subscription[] getFeeds() {
		ArrayList<Subscription> feeds = new ArrayList<Subscription>();
		IExtensionRegistry ereg = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = ereg
				.getConfigurationElementsFor(AggregatorPlugin.FEEDS_EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			// We're not going to allow an exception here to disrupt.
			try {
				if (element.getName().equals("feed")) { //$NON-NLS-1$
					String url = element.getAttribute("url"); //$NON-NLS-1$
					String collectionId = element.getAttribute("collection"); //$NON-NLS-1$
					boolean add = Boolean.parseBoolean(element
							.getAttribute("create")); //$NON-NLS-1$
					// Will use the default collection if the collectionId is
					// null.
					FeedCollection collection = AggregatorPlugin.getDefault()
							.getFeedCollection(collectionId);
					if (collection != null) {
						Subscription feed = createNewFeed(collection, element);
						if (add && !collection.hasFeed(url)) {
							collection.addNew(feed);
						}
						feeds.add(feed);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return feeds.toArray(new Subscription[feeds.size()]);
	}

	private Subscription createNewFeed(FeedCollection parent,
			IConfigurationElement element) {
		Subscription feed = new Subscription(this);
		// Initialise with default values from the preference store.
		// This is done here as the preference system is a UI component.
		feed.setTitle(element.getAttribute("title")); //$NON-NLS-1$
		feed.setURL(element.getAttribute("url")); //$NON-NLS-1$
		feed.setArchiving(Archiving.valueOf(element
				.getAttribute("archivingMethod"))); //$NON-NLS-1$
		feed.setArchivingDays(Integer.parseInt(element
				.getAttribute("archivingDays"))); //$NON-NLS-1$
		feed.setArchivingItems(Integer.parseInt(element
				.getAttribute("archivingItems"))); //$NON-NLS-1$
		feed.setUpdateInterval(Integer.parseInt(element
				.getAttribute("updateInterval"))); //$NON-NLS-1$
		feed.setUpdatePeriod(UpdatePeriod.valueOf(element
				.getAttribute("updatePeriod"))); //$NON-NLS-1$
		return feed;
	}

	public boolean isEnabled() {
		return true;
	}
}
