/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.data;

import java.util.UUID;

import no.resheim.aggregator.data.internal.AggregatorUIItem;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Article extends AggregatorUIItem {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The date and time the feed was added to the database */
	private long addedDate;

	/** The creator of the feed */
	private String creator;

	/** The description or content */
	private String description = null;

	/** The UUID of the feed this article belongs to */
	protected UUID feed_uuid;

	/** The globally unique identifier */
	private String guid = EMPTY_STRING;

	/** Link of the item */
	private String link = EMPTY_STRING;

	/** The publication date */
	private long publicationDate;

	/** Read flag */
	private boolean read = false;

	/** The read date */
	private long readDate;

	private UUID location;

	public UUID getLocation() {
		return location;
	}

	public void setLocation(UUID location) {
		this.location = location;
	}

	/**
	 * Access to the constructor is limited as we
	 */
	Article(AggregatorUIItem parent) {
		super(parent);
	}

	public Article() {
		super(null);
		this.uuid = UUID.randomUUID();
	}

	/**
	 * Returns the date (in long representation) of when this item was
	 * downloaded and added to the feed.
	 * 
	 * @return The added date.
	 */
	public long getAdded() {
		return addedDate;
	}

	public String getCreator() {
		return creator;
	}

	/**
	 * Clients must obtain the description from the feed registry. The
	 * description kept in this member variable is only temporarily held while
	 * the feed is being updated.
	 * 
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * 
	 * @return
	 */
	public UUID getFeedUUID() {
		return feed_uuid;
	}

	/**
	 * Returns the unique identifier for the feed item.
	 * 
	 * @return The identifier
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @return the link
	 */
	public String getLink() {
		return link;
	}

	/**
	 * Returns the publication date of the feed item. For some feeds, such as
	 * RSS 1.0 this value is most likely zero.
	 * 
	 * @return The publication date
	 */
	public long getPublicationDate() {
		return publicationDate;
	}

	public long getReadDate() {
		return readDate;
	}

	public boolean isRead() {
		return read;
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

	public void setParentItem(AggregatorUIItem parent) {
		this.parent = parent;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(" ["); //$NON-NLS-1$
		sb.append(getOrdering());
		sb.append(']');
		return sb.toString();
	}
}
