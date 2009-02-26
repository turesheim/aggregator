/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.core.data;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Article extends AggregatorItem implements Comparable<Article> {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat(
			"EEE, dd MMMM yyyy kk:mm:ss"); //$NON-NLS-1$

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/** The date and time the feed was added to the database */
	protected long addedDate;

	/**
	 * The creator of the feed
	 * 
	 * @uml.property name="creator"
	 */
	protected String creator;

	/** The UUID of the feed this article belongs to */
	protected UUID feed_uuid;

	/**
	 * The globally unique identifier (should never be null)
	 * 
	 * @uml.property name="guid"
	 */
	protected String guid = null;

	/**
	 * Link of the item
	 * 
	 * @uml.property name="link"
	 */
	protected String link = EMPTY_STRING;

	protected UUID location;

	/**
	 * @uml.property name="mediaContent"
	 */
	protected ArrayList<MediaContent> mediaContent;

	/**
	 * @uml.property name="mediaPlayerURL"
	 */
	protected String mediaPlayerURL = EMPTY_STRING;

	/**
	 * The publication date
	 * 
	 * @uml.property name="publicationDate"
	 */
	protected long publicationDate;

	/**
	 * Read flag
	 * 
	 * @uml.property name="read"
	 */
	protected boolean read = false;

	/**
	 * The read date
	 * 
	 * @uml.property name="readDate"
	 */
	protected long readDate;

	/**
	 * The description or content
	 * 
	 * @uml.property name="text"
	 */
	protected String text = null;

	/**
	 * The feed that this article comes from. Note that this value
	 * 
	 * @uml.property name="fFeed"
	 * @uml.associationEnd
	 */
	protected Feed fFeed;

	public Article(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
		mediaContent = new ArrayList<MediaContent>();
	}

	public Article(Feed feed, UUID uuid) {
		super(null, uuid);
		Assert.isNotNull(feed);
		fFeed = feed;
		location = feed.getLocation();
		feed_uuid = feed.getUUID();
	}

	/**
	 * @param the
	 *            parent item (folder)
	 * @param the
	 *            unique identifier of the item
	 * @param the
	 *            identifier of the feed
	 */
	public Article(AggregatorItemParent parent, UUID uuid, UUID feedId) {
		this(parent, uuid);
		Assert.isNotNull(parent);
		location = parent.getUUID();
		feed_uuid = feedId;
	}

	public int compareTo(Article arg0) {
		return (int) (arg0.addedDate - addedDate);
	}

	/**
	 * Returns the feed that the article belongs to.
	 * 
	 * @return the feed instance
	 */
	public Feed getFeed() {
		if (fFeed == null) {
			try {
				fFeed = getCollection().getFeeds().get(feed_uuid);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return fFeed;
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

	/**
	 * @return
	 * @uml.property name="creator"
	 */
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
	 * @uml.property name="guid"
	 */
	public String getGuid() {
		return guid;
	}

	/**
	 * @return the link
	 * @uml.property name="link"
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @return
	 * @uml.property name="mediaContent"
	 */
	public MediaContent[] getMediaContent() {
		return mediaContent.toArray(new MediaContent[mediaContent.size()]);
	}

	/**
	 * Returns the URL to the media player. There may be only one media player
	 * per feed.
	 * 
	 * @return
	 * @uml.property name="mediaPlayerURL"
	 */
	public String getMediaPlayerURL() {
		return mediaPlayerURL;
	}

	/**
	 * Returns the publication date of the feed item. For some feeds, such as
	 * RSS 1.0 this value is most likely zero.
	 * 
	 * @return The publication date
	 * @uml.property name="publicationDate"
	 */
	public long getPublicationDate() {
		return publicationDate;
	}

	/**
	 * @return
	 * @uml.property name="readDate"
	 */
	public long getReadDate() {
		return readDate;
	}

	/**
	 * @return
	 * @uml.property name="text"
	 */
	public String getText() {
		try {
			return getCollection().getDescription(this);
		} catch (CoreException e) {
			return null;
		}
	}

	/**
	 * @return
	 * @uml.property name="read"
	 */
	public boolean isRead() {
		return read;
	}

	public boolean hasMedia() {
		return getMediaContent().length > 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(" ["); //$NON-NLS-1$
		sb.append(getOrdering());
		sb.append(']');
		return sb.toString();
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
	public UUID getLocation() {
		return location;
	}

	public void setMediaPlayerURL(String mediaPlayerURL) {
		this.mediaPlayerURL = mediaPlayerURL;
	}
}
