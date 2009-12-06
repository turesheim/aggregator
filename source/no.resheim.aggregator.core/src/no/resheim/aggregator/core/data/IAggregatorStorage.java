/*******************************************************************************
 * Copyright (c) 2008-2009 Torkild Ulvøy Resheim.
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

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;

import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * This interface is internal and is not intended to be implemented by clients.
 * Standard implementations of the various storage methods are already supplied
 * so there should be no real need to add another.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IAggregatorStorage extends ISaveParticipant {

	/**
	 * Adds the given item to the storage.
	 * 
	 * @param item
	 *            the item to add
	 */
	public abstract IStatus add(AggregatorItem item);

	/**
	 * Deletes the specified item from the storage. The implementor is
	 * responsible for also deleting all child items.
	 * 
	 * @param item
	 *            the item to delete
	 */
	public abstract void delete(AggregatorItem item);

	/**
	 * Deletes the feed from the storage.
	 * 
	 * @param subscription
	 *            the subscription to delete
	 */
	public abstract void delete(Subscription subscription);

	/**
	 * Adds a new subscription to the storage.
	 * 
	 * @param subscription
	 *            the subscription to add.
	 */
	public abstract void add(Subscription subscription);

	/**
	 * Calculates and returns the number of children the <i>parent</i> item has.
	 * 
	 * @param parent
	 *            the parent item
	 * @param types
	 *            the types of children to count
	 * @return the number of children
	 */
	public abstract int getChildCount(AggregatorItem parent,
			EnumSet<ItemType> types);

	/**
	 * Returns the description string of the article if such a description does
	 * exist.
	 * 
	 * @param item
	 *            the item to get the description for
	 * @return the description
	 */
	public abstract String getDescription(Article item);

	/**
	 * Returns a map of all feeds regardless of the placement in the tree
	 * structure. The map key is the subscription's unique identifier.
	 * 
	 * @return a map of all subscriptions
	 */
	public abstract HashMap<UUID, Subscription> getSubscriptions();

	/**
	 * Returns the article with the given <i>guid</i>.
	 * 
	 * @param guid
	 *            the globally unique identifier
	 * @return the article or <code>null</code>
	 */
	public abstract boolean hasArticle(String guid);

	/**
	 * Returns the item at the given index in the specified parent item. If no
	 * item could be found <code>null</code> is returned. A
	 * {@link CoreException} is thrown if somthing went amiss.
	 * 
	 * @param parent
	 *            the parent item
	 * @param index
	 *            the index of the child item
	 * @return the child item or <code>null</code>
	 * @throws CoreException
	 */
	public abstract AggregatorItem getChildAt(AggregatorItemParent parent,
			EnumSet<ItemType> types, int index) throws CoreException;

	/**
	 * Returns the number of unread articles the given parent item has.
	 * 
	 * @param parent
	 *            the parent item
	 * @return the number of unread articles
	 */
	public abstract int getUnreadCount(AggregatorItem parent);

	/**
	 * Returns a list of articles belonging to the given subscription that has
	 * been changed locally since the given time stamp.
	 * 
	 * @param subscription
	 *            the subscription
	 * @param time
	 *            the time stamp
	 * @return a list of changed items
	 */
	public abstract List<Article> getChangedArticles(Subscription subscription,
			long time);

	/**
	 * Tests to see if the feed with the given URL already exists in the
	 * database.
	 * 
	 * @param url
	 *            the URL of the subscription
	 * @return <b>true</b> if a subscription with the given URL exists
	 */
	public abstract boolean hasSubscription(String url);

	/**
	 * Updates the database to indicate that the item has a new parent. This
	 * method must be called whenever an item has been relocated in order to
	 * synchronise the back-end.
	 * 
	 * @param item
	 *            the moved item
	 */
	// TODO: Replace with a more generic method
	public abstract void moved(AggregatorItem item);

	/**
	 * Shuts down the storage. Implementors should use this opportunity to clean
	 * up the database and remove items that are scheduled for deletion.
	 * 
	 * @return the status of the shut-down
	 */
	public IStatus shutdown();

	/**
	 * Starts up the storage. This is the first method called on any storage
	 * instance.
	 * 
	 * @param monitor
	 *            a monitor for reporting progress
	 * @return the status of the start-up
	 */
	public IStatus startup(IProgressMonitor monitor);

	/**
	 * Updates the database with subscription data.
	 * 
	 * @param feed
	 */
	// TODO: Replace with a more generic method
	public abstract void updateSubscription(Subscription subscription);

	/**
	 * Writes back the aggregator item data so that the database is in sync with
	 * the live object.
	 * 
	 * @param item
	 *            the aggregator item
	 */
	public abstract void writeBack(AggregatorItem item);

	/**
	 * Updates the storage with the given filters.
	 * 
	 * @param filters
	 *            the new filter set
	 */
	public abstract void setFilters(Filter[] filters);

	/**
	 * Retrieves the filter set from the storage.
	 * 
	 * @return the filter set
	 */
	public abstract Filter[] getFilters();

	/**
	 * Used to obtain a lock for writing to the storage.
	 * 
	 * @returnt the lock.
	 */
	public abstract Lock writeLock();

	/**
	 * Used to obtain a lock for reading from the storage.
	 * 
	 * @return the lock.
	 */
	public abstract Lock readLock();

}