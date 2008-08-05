/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.data.internal;

import java.util.UUID;

import no.resheim.aggregator.data.AggregatorUIItem;
import no.resheim.aggregator.data.Article;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class InternalArticle extends Article {
	/**
	 * Access to the constructor is limited as we
	 */
	public InternalArticle(AggregatorUIItem parent, UUID uuid) {
		super(parent, uuid);
	}

	/**
	 * Sets the date (in long representation) of when this item was downloaded
	 * and added to the feed.
	 * 
	 * @param added
	 *            The added dates.
	 */
	public void setAddedDate(long added) {
		this.addedDate = added;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * @param description
	 *            the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 
	 * @param feed_uuid
	 */
	public void setFeedUUID(UUID feed_uuid) {
		this.feed_uuid = feed_uuid;
	}

	/**
	 * Sets the unique identifier for the feed item.
	 * 
	 * @param guid
	 *            The identifier to set
	 */
	public void setGuid(String guid) {
		this.guid = guid;
	}

	/**
	 * @param link
	 *            the link to set
	 */
	public void setLink(String link) {
		this.link = link;
	}

	public void setLocation(UUID location) {
		this.location = location;
	}

	/**
	 * Publication date.
	 * 
	 * @param date
	 */
	public void setPublicationDate(long date) {
		this.publicationDate = date;
	}

	/**
	 * Scope is set to <i>package</i> as clients should use
	 * FeedRegistry.setRead(Item) instead.
	 * 
	 * @param read
	 *            <b>true</b> if the feed item has been read
	 */
	public void setRead(boolean read) {
		this.read = read;
	}

	public void setReadDate(long readDate) {
		this.readDate = readDate;
	}

}
