/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.data;

import java.util.UUID;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Article extends AbstractAggregatorItem implements IAggregatorItem {
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	/** The date and time the feed was added to the database */
	private long addedDate;
	private String creator;
	/** The description or content */
	private String description = EMPTY_STRING;
	protected UUID feed_uuid;
	private String guid = EMPTY_STRING;
	/** Link of the item */
	private String link = EMPTY_STRING;
	/** Only used in the UI as a link to the parent element */
	private IAggregatorItem parent;
	private long publicationDate;
	/** Read flag */
	private boolean read = false;

	private long readDate;
	/** Title of the item */
	private String title = EMPTY_STRING;

	public Article() {
	}

	/**
	 * This constructor should only be called from one of the feed handlers as
	 * it will
	 * 
	 * @param feedId
	 *            The feed identifier
	 */
	public Article(Feed feed) {
		this.parent = feed;
		this.feed_uuid = feed.getUUID();
		this.parent_uuid = feed.getUUID();
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
	String getDescription() {
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

	@Override
	public IAggregatorItem getParent() {
		return parent;
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

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	public boolean isRead() {
		return read;
	}

	boolean isValid() {
		if (title == null || link == null || description == null
				|| guid == null) {
			return false;
		}
		if (uuid == null || parent_uuid == null || feed_uuid == null) {
			return false;
		}
		return true;
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

	public void setParent(IAggregatorItem parent) {
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
	void setRead(boolean read) {
		this.read = read;
	}

	public void setReadDate(long readDate) {
		this.readDate = readDate;
	}

	/**
	 * @param title
	 *            the title to set
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	@Override
	public String toString() {
		return getTitle();
	}
}
