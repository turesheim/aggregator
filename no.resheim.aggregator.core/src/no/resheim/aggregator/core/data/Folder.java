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
package no.resheim.aggregator.core.data;

import java.util.UUID;

import org.eclipse.core.runtime.CoreException;

/**
 * Aggregator item representing a folder. Folders are used to contain other type
 * of aggregator items such as articles and other folders. A folder may also
 * <i>point</i> to a {@link Subscription} , meaning that the folder is the
 * default location for the feed articles. In this case the folder can be used
 * to obtain the feed instance.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Folder extends AggregatorItemParent {

	/**
	 * @uml.property name="feed"
	 */
	protected UUID feed;

	/**
	 * @param registryId
	 * @param title
	 */
	public Folder(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
	}

	/**
	 * Returns the associated feed identifier or <b>null</b>. If a value is
	 * returned this folder is the default folder for the articles of the feed.
	 * 
	 * @return the feed identifier
	 */
	public UUID getFeedUUID() {
		return feed;
	}

	/**
	 * @return
	 * @uml.property name="feed"
	 */
	public Subscription getFeed() {
		if (feed == null) {
			return null;
		} else {
			try {
				return getCollection().getFeeds().get(feed);
			} catch (CoreException e) {
				e.printStackTrace();
				return null;
			}
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		return sb.toString();
	}

	public void setFeed(UUID feed) {
		this.feed = feed;
	}
}
