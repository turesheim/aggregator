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
package no.resheim.aggregator.core.data.internal;

import java.util.UUID;

import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.MediaContent;

import org.eclipse.core.runtime.Assert;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class InternalArticle extends Article {
	/**
	 * @param the
	 *            parent item (folder)
	 * @param the
	 *            unique identifier of the item
	 * @param the
	 *            identifier of the feed
	 */
	public InternalArticle(AggregatorItemParent parent, UUID uuid, UUID feedId) {
		super(parent, uuid);
		Assert.isNotNull(parent);
		location = parent.getUUID();
		feed_uuid = feedId;
	}

	public InternalArticle(Feed feed, UUID uuid) {
		super(null, uuid);
		Assert.isNotNull(feed);
		location = feed.getLocation();
		feed_uuid = feed.getUUID();
	}

	public void addMediaContent(MediaContent content) {
		mediaContent.add(content);
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

	/**
	 * Sets the creator name of the article.
	 * 
	 * @param creator
	 *            the creator name
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * Sets the text of the article. This method should only be used by feed
	 * parsers et cetera that are required to set the text.
	 * 
	 * @param text
	 *            the article text
	 */
	public void internalSetText(String text) {
		this.text = text;
	}

	/**
	 * Returns the text of the article. This method should only be used by feed
	 * parsers et cetera that are required to obtain the text of the article
	 * without looking it up in the storage.
	 * 
	 * @return the internal text representation
	 */
	public String internalGetText() {
		return text;
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

	/**
	 * The location field is only used when the article is being constructed
	 * where the article parent item is not available. This would usually be
	 * when read from a HTTP stream.
	 * 
	 * @return the UUID of the parent item
	 */
	// TODO: Internalise this member variable
	public UUID getLocation() {
		return location;
	}

	public void setMediaPlayerURL(String mediaPlayerURL) {
		this.mediaPlayerURL = mediaPlayerURL;
	}

	public void setMediaEnclosureURL(String mediaEnclosureURL) {
		this.enclosureURL = mediaEnclosureURL;
	}

	public void setMediaEnclosureDuration(int mediaEnclosureDuration) {
		this.enclosureDuration = mediaEnclosureDuration;
	}

	public void setMediaEnclosureType(String mediaEnclosureType) {
		this.enclosureType = mediaEnclosureType;
	}

}
