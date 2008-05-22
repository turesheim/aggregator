package no.resheim.aggregator;

import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface IAggregatorStorage {

	/**
	 * Deletes the specified item from the storage.
	 * 
	 * @param item
	 *            The item to delete
	 */
	public abstract void delete(IAggregatorItem item);

	/**
	 * Deletes all articles from the specified feed which publication date (or
	 * added date if the publication date is not available) is older than the
	 * given date.
	 * 
	 * @param feed
	 *            The feed to delete articles from
	 * @param date
	 *            The limit date
	 */
	public abstract void deleteOutdated(Feed feed, long date);

	/**
	 * Retrieves all the child articles of the given parent node. If the node is
	 * <i>null</i>; all registries are returned. if it's a registry; categories
	 * and feeds are returned, if it's a category; categories and feeds are
	 * returned. And if it's a feed; feed articles are returned.
	 * 
	 * @param parent
	 *            The parent item
	 * @return An array of aggregator articles
	 */
	public abstract IAggregatorItem[] getChildren(IAggregatorItem item);

	/**
	 * 
	 * 
	 * @param guid
	 *            The globally unique identifier
	 * @return The FeedItem or <i>null</i>
	 */
	public abstract Article getItem(String guid);

	/**
	 * 
	 * @return
	 */
	public abstract HashMap<UUID, Feed> initializeFeeds();

	/**
	 * Shuts down the storage.
	 * 
	 * @return
	 */
	public IStatus shutdown();

	/**
	 * Starts up the storage
	 * 
	 * @param monitor
	 *            A monitor for reporting progress
	 * @return
	 */
	public IStatus startup(IProgressMonitor monitor);

	/**
	 * Adds the given aggregator item to the storage.
	 * 
	 * @param item
	 *            The item to add
	 */
	public abstract void add(IAggregatorItem item);

	/**
	 * Keeps the <i>keep</i> newest articles in the feed. The rest are deleted.
	 * 
	 * @param feed
	 *            The feed to remove articles from
	 * @param keep
	 *            The number of articles to keep
	 */
	public abstract void keepMaximum(Feed feed, int keep);

	/**
	 * Updates the database to indicate that the aggregator item has a new
	 * parent.
	 * 
	 * @param item
	 *            The moved item
	 * @param newParent
	 *            The new parent
	 */
	public abstract void move(IAggregatorItem item, IAggregatorItem newParent);

	/**
	 * Renames the given item.
	 * 
	 * @param item
	 */
	public abstract void rename(IAggregatorItem item);

	/**
	 * Returns the description string of the aggregator item if such a
	 * description does exist.
	 * 
	 * @param item
	 * @return
	 */
	public abstract String getDescription(Article item);

	/**
	 * Returns the number of unread articles the given feed has.
	 * 
	 * @param parent
	 *            The parent item
	 * @return The number of child items
	 */
	public abstract int getUnreadCount(Feed parent);

	/**
	 * Updates the database with feed data.
	 * 
	 * @param feed
	 */
	public abstract void updateFeed(Feed feed);

	/**
	 * Indicates that the feed item has been read.
	 * 
	 * @param item
	 *            The item to update
	 */
	public abstract void updateReadFlag(Article item);

}