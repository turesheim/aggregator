/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.core.data;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Article extends AggregatorItem implements Comparable<Article> {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMMM yyyy kk:mm:ss"); //$NON-NLS-1$

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The date and time the feed was added to the database */
	protected long addedDate;

	/** The creator of the feed */
	protected String creator;

	protected int enclosureDuration = 0;

	protected String enclosureType = EMPTY_STRING;

	protected String enclosureURL = EMPTY_STRING;

	/** The UUID of the feed this article belongs to */
	protected UUID feed_uuid;

	/** The globally unique identifier (should never be null) */
	protected String guid = null;

	/** Link of the item */
	protected String link = EMPTY_STRING;

	protected UUID location;

	protected ArrayList<MediaContent> mediaContent;

	protected String mediaPlayerURL = EMPTY_STRING;

	/** The publication date */
	protected long publicationDate;

	/** Read flag */
	protected boolean read = false;

	/** The read date */
	protected long readDate;

	/** The description or content */
	protected String text = null;

	protected Article(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
		mediaContent = new ArrayList<MediaContent>();
	}

	public int compareTo(Article arg0) {
		return (int) (arg0.addedDate - addedDate);
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

	public String getDetails() {
		StringBuilder sb = new StringBuilder();
		if (creator != null && publicationDate > 0) {
			sb.append(MessageFormat.format(
					Messages.Article_PublishedByAndDownloaded, new Object[] {
							creator,
							dateFormat.format(new Date(publicationDate)),
							dateFormat.format(new Date(addedDate))
					}));
		} else if (creator != null) {
			sb.append(MessageFormat.format(
					Messages.Article_PublishedAndDownloaded, new Object[] {
							dateFormat.format(new Date(publicationDate)),
							dateFormat.format(new Date(addedDate))
					}));
		} else if (publicationDate > 0) {
			sb.append(MessageFormat.format(
					Messages.Article_PublishedAndDownloaded, new Object[] {
							dateFormat.format(new Date(publicationDate)),
							dateFormat.format(new Date(addedDate))
					}));

		} else {
			sb.append(MessageFormat.format(Messages.Article_Downloaded,
					new Object[] {
						dateFormat.format(new Date(addedDate))
					}));

		}
		return sb.toString();
	}

	/**
	 * Returns the enclosure <i>duration</i>. There may be only one enclosure
	 * element per feed item. This value represents the <b>duration</b>
	 * attribute. See http://en.wikipedia.org/wiki/RSS_Enclosures for details.
	 * 
	 * @return
	 */
	public int getEnclosureDuration() {
		return enclosureDuration;
	}

	/**
	 * Returns the enclosure <i>type</i>. There may be only one enclosure
	 * element per feed item. This value represents the <b>type</b> attribute.
	 * See http://en.wikipedia.org/wiki/RSS_Enclosures for details.
	 * 
	 * @return
	 */
	public String getEnclosureType() {
		return enclosureType;
	}

	/**
	 * Returns the enclosure <i>URL</i>. There may be only one enclosure element
	 * per feed item. This value represents the <b>url</b> attribute. See
	 * http://en.wikipedia.org/wiki/RSS_Enclosures for details.
	 * 
	 * @return
	 */
	public String getEnclosureURL() {
		return enclosureURL;
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

	public MediaContent[] getMediaContent() {
		return mediaContent.toArray(new MediaContent[mediaContent.size()]);
	}

	/**
	 * Returns the URL to the media player. There may be only one media player
	 * per feed.
	 * 
	 * @return
	 */
	public String getMediaPlayerURL() {
		return mediaPlayerURL;
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

	public String getText() {
		try {
			return getCollection().getDescription(this);
		} catch (CoreException e) {
			return null;
		}
	}

	public boolean isRead() {
		return read;
	}

	public boolean hasMedia() {
		return getEnclosureType().length() > 0 || getMediaContent().length > 0;
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
