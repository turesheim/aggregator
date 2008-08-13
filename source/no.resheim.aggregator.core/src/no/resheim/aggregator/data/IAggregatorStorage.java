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

package no.resheim.aggregator.data;

import java.util.HashMap;
import java.util.UUID;

import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * This interface is internal and is not intended to be implemented by clients.
 * Standard implementations of the various storage methods are already supplied
 * so there should be no real need to add another.
 * <p>
 * A few methods are not typical for a simple storage type. This is because it's
 * desirable to optimise on the database level instead of having to instantiate
 * aggregator items.
 * </p>
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IAggregatorStorage extends ISaveParticipant {

	/**
	 * Adds the given aggregator item to the storage.
	 * 
	 * @param item
	 *            The item to add
	 */
	public abstract IStatus add(IAggregatorItem item);

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
	 * Calculates and returns the number of children the <i>parent</i> item has.
	 * 
	 * @param parent
	 *            the parent item
	 * @return the number of children
	 */
	public abstract int getChildCount(AggregatorUIItem parent);

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
	public abstract IAggregatorItem[] getChildren(AggregatorUIItem item);

	/**
	 * Returns the description string of the aggregator item if such a
	 * description does exist.
	 * 
	 * @param item
	 * @return
	 */
	public abstract String getDescription(Article item);

	/**
	 * Returns a map of all feeds regardless of the placement in the tree
	 * structure. The map key is the feeds' unique identifier.
	 * 
	 * @return a map of all feeds
	 */
	public abstract HashMap<UUID, Feed> getFeeds();

	/**
	 * Returns the article with the given <i>guid</i>.
	 * 
	 * @param guid
	 *            the globally unique identifier
	 * @return the Article or <i>null</i>
	 */
	public abstract boolean hasArticle(String guid);

	/**
	 * Returns the item at the given index in the specified parent item. If no
	 * item could be found <i>null</i> is returned.
	 * 
	 * @param parent
	 * @param index
	 * @return
	 */
	public abstract IAggregatorItem getItem(AggregatorUIItem parent, int index);

	/**
	 * Returns the number of unread articles the given parent item has.
	 * 
	 * @param parent
	 *            the parent item
	 * @return the number of unread articles
	 */
	public abstract int getUnreadCount(AggregatorUIItem parent);

	/**
	 * Tests to see if the feed with the given URL already exists in the
	 * database.
	 * 
	 * @param url
	 *            the URL of the feed
	 * @return <b>true</b> if a feed with the given URL exists in the collection
	 */
	public abstract boolean hasFeed(String url);

	/**
	 * Keeps the <i>keep</i> newest articles in the feed. The rest are deleted.
	 * Articles are deleted even if they are not placed as a direct child of the
	 * feed in the tree structure.
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
	 *            the moved item
	 * @param parent
	 *            the new parent
	 * @param order
	 *            the new order of the item
	 */
	public abstract void move(AggregatorUIItem item, AggregatorUIItem parent,
			int order);

	/**
	 * Renames the given item.
	 * 
	 * @param item
	 *            the item to rename
	 */
	public abstract void rename(AggregatorUIItem item);

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
	public abstract void updateReadFlag(AggregatorUIItem item);

}