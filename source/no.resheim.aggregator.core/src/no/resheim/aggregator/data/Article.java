/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.data;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Article extends AggregatorUIItem {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The date and time the feed was added to the database */
	protected long addedDate;

	/** The creator of the feed */
	protected String creator;

	/** The description or content */
	protected String description = null;

	/** The UUID of the feed this article belongs to */
	protected UUID feed_uuid;

	/** The globally unique identifier */
	protected String guid = EMPTY_STRING;

	/** Link of the item */
	protected String link = EMPTY_STRING;

	protected UUID location;

	/** The publication date */
	protected long publicationDate;

	/** Read flag */
	protected boolean read = false;

	/** The read date */
	protected long readDate;

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMMM yyyy kk:mm:ss"); //$NON-NLS-1$

	public Article(AggregatorUIItem parent, UUID uuid) {
		super(parent, uuid);
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

	public void setParentItem(AggregatorUIItem parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(" ["); //$NON-NLS-1$
		sb.append(getOrdering());
		sb.append(']');
		return sb.toString();
	}

	public String getDetails() {
		StringBuilder sb = new StringBuilder();
		if (creator != null && publicationDate > 0) {
			sb.append(MessageFormat.format(
					"Published by {0} on {1}. Downloaded on {2}.",
					new Object[] {
							creator,
							dateFormat.format(new Date(publicationDate)),
							dateFormat.format(new Date(addedDate))
					}));
		} else if (creator != null) {
			sb.append(MessageFormat.format(
					"Published on {0}. Downloaded on {1}.", new Object[] {
							dateFormat.format(new Date(publicationDate)),
							dateFormat.format(new Date(addedDate))
					}));
		} else if (publicationDate > 0) {
			sb.append(MessageFormat.format(
					"Published on {0}. Downloaded on {1}.", new Object[] {
							dateFormat.format(new Date(publicationDate)),
							dateFormat.format(new Date(addedDate))
					}));

		} else {
			sb.append(MessageFormat.format("Downloaded on {0}.", new Object[] {
				dateFormat.format(new Date(addedDate))
			}));

		}
		return sb.toString();
	}
}
