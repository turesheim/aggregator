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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.internal.CollectionUpdateJob;
import no.resheim.aggregator.data.internal.InternalArticle;
import no.resheim.aggregator.data.internal.InternalFolder;

import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedCollection extends AggregatorUIItem {

	private static final UUID DEFAULT_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	/** The list of feed change listeners */
	private static ArrayList<IAggregatorEventListener> feedListeners = new ArrayList<IAggregatorEventListener>();

	/** The storage for our data */
	private IAggregatorStorage fDatabase;

	/** The number of milliseconds in a day */
	private final long DAY = 86400000;

	private boolean fPublic;

	private boolean fDefault;

	final CollectionUpdateJob fRegistryUpdateJob = new CollectionUpdateJob(this);

	/**
	 * The identifier of the registry as specified when the registry was
	 * declared.
	 */
	private String id;

	/**
	 * Handles concurrency for the database, making sure that no readers get
	 * access while writing and only one writer gets access at the time.
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * List of <i>live</i> feeds that we must keep track of even if none of the
	 * viewers has opened the feed for viewing so that it has had a chance of
	 * being created. This list is populated at startup and maintained
	 * thereafter.
	 */
	private HashMap<UUID, Feed> fFeeds;

	public FeedCollection(String id, boolean pub, boolean def) {
		super(null, DEFAULT_ID);
		this.id = id;
		this.fPublic = pub;
		this.fDefault = def;
	}

	public boolean isDefault() {
		return fDefault;
	}

	/**
	 * Add listener to be notified about feed changes. The added listener will
	 * be notified when feeds are added, removed and when their contents has
	 * changed.
	 * 
	 * @param listener
	 *            the listener to be notified
	 */
	public void addFeedListener(IAggregatorEventListener listener) {
		feedListeners.add(listener);
	}

	/**
	 * Adds a new feed to the database and immediately stores it's data in the
	 * persistent storage.
	 * 
	 * @param feed
	 *            the aggregator item to add
	 */
	public void addNew(AggregatorUIItem item) {
		long start = System.currentTimeMillis();
		try {
			lock.writeLock().lock();
			if (item instanceof Folder) {
				AggregatorUIItem folder = (AggregatorUIItem) item;
				fDatabase.add(folder);
			} else if (item instanceof Article) {
				Article feedItem = (Article) item;
				fDatabase.add(feedItem);
			}
		} finally {
			lock.writeLock().unlock();
		}
		notifyListerners(new AggregatorItemChangedEvent(item,
				FeedChangeEventType.CREATED, System.currentTimeMillis() - start));
	}

	public void addNew(Feed feed) {
		long start = System.currentTimeMillis();
		try {
			lock.writeLock().lock();
			// No location has been specified for the feed so we must create a
			// new folder at the collection root and use this.
			if (feed.getLocation() == null) {
				InternalFolder folder = new InternalFolder(this, UUID
						.randomUUID());
				folder.setFeed(feed.getUUID());
				folder.setTitle(feed.getTitle());
				addNew(folder);
				feed.setLocation(folder.getUUID());
			}
			fFeeds.put(feed.getUUID(), feed);
			fDatabase.add(feed);
			FeedUpdateJob job = new FeedUpdateJob(this, feed);
			job.schedule();
		} finally {
			lock.writeLock().unlock();
		}
		notifyListerners(new AggregatorItemChangedEvent(feed,
				FeedChangeEventType.CREATED, System.currentTimeMillis() - start));
	}

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	void cleanUp(Feed site) {
		try {
			lock.writeLock().lock();
			Archiving archiving = site.getArchiving();
			int days = site.getArchivingDays();
			int articles = site.getArchivingItems();
			switch (archiving) {
			case KEEP_ALL:
				break;
			case KEEP_NEWEST:
				long lim = System.currentTimeMillis() - ((long) days * DAY);
				fDatabase.deleteOutdated(site, lim);
				break;
			case KEEP_SOME:
				fDatabase.keepMaximum(site, articles);
				break;
			default:
				break;
			}
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Updates the feed data in the persistent storage. Should only be called by
	 * {@link FeedUpdateJob} after the feed has be updated with new information.
	 * 
	 * @param feed
	 *            the feed to update
	 */
	void feedUpdated(Feed feed) {
		try {
			lock.writeLock().lock();
			fDatabase.updateFeed(feed);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Returns the number of items contained within the given parent item.
	 * 
	 * @param parent
	 *            the parent item
	 * @return the number of child items
	 */
	public int getChildCount(IAggregatorItem parent) {
		try {
			lock.readLock().lock();
			return fDatabase.getChildCount((AggregatorUIItem) parent);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the child items of the given parent item.
	 * 
	 * @param item
	 *            the parent item
	 * @return the child items
	 */
	public IAggregatorItem[] getChildren(IAggregatorItem item) {
		try {
			lock.readLock().lock();
			return fDatabase.getChildren((AggregatorUIItem) item);
		} finally {
			lock.readLock().unlock();
		}
	}

	private List<IAggregatorItem> getDescendants(IAggregatorItem item) {
		ArrayList<IAggregatorItem> descendants = new ArrayList<IAggregatorItem>();
		for (IAggregatorItem aggregatorItem : getChildren(item)) {
			descendants.add(aggregatorItem);
			descendants.addAll(getDescendants(aggregatorItem));
		}
		return descendants;
	}

	public FeedCollection getCollection() {
		return this;
	}

	public String getDescription(Article item) {
		try {
			lock.readLock().lock();
			return fDatabase.getDescription(item);
		} finally {
			lock.readLock().unlock();
		}
	}

	public String getFeedId() {
		return null;
	}

	/**
	 * Returns the complete list of feeds that this registry is maintaining.
	 * Note
	 * 
	 * @return The list of feeds
	 */
	public HashMap<UUID, Feed> getFeeds() {
		return fFeeds;
	}

	/**
	 * Returns the identifier of the feed collection as specified in the
	 * collection declaration.
	 * 
	 * @return the feed identifier string
	 */
	public String getId() {
		return id;
	}

	public IAggregatorItem getItemAt(IAggregatorItem parent, int index) {
		try {
			lock.readLock().lock();
			IAggregatorItem item = fDatabase.getItem((AggregatorUIItem) parent,
					index);
			return item;
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Returns the number of <b>unread</b> articles.
	 * 
	 * @param element
	 * @return
	 */
	public int getItemCount(IAggregatorItem element) {
		try {
			lock.readLock().lock();
			return fDatabase.getUnreadCount((AggregatorUIItem) element);
		} finally {
			lock.readLock().unlock();
		}
	}

	public int getOrdering() {
		return 0;
	}

	public AggregatorUIItem getParent() {
		return null;
	}

	public UUID getParentUUID() {
		return null;
	}

	public String getTitle() {
		return title;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.data.IAggregatorItem#getUUID()
	 */
	public UUID getUUID() {
		return DEFAULT_ID;
	}

	/**
	 * Tests to see if the item already exists in the database. If this is the
	 * case <i>true</i> is returned. This method relies on the globally unique
	 * identifier of the feed item.
	 * 
	 * @param item
	 * @return
	 */
	public boolean hasArticle(String guid) {
		try {
			lock.readLock().lock();
			return fDatabase.hasArticle(guid);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Tests if the feed exists in the repository.
	 * 
	 * @return <b>True</b> if the feed exists.
	 */
	public boolean hasFeed(String url) {
		try {
			lock.readLock().lock();
			return fDatabase.hasFeed(url);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * Loads all feeds from the given backend storage and initializes.
	 * 
	 * @param storage
	 */
	public void initialize(IAggregatorStorage storage) {
		this.fDatabase = storage;
		fFeeds = storage.getFeeds();
		// Start a new update job that will periodically wake up and create
		// FeedUpdateJobs when a feed is scheduled for an update.
		fRegistryUpdateJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				fRegistryUpdateJob.schedule(30000);
			}
		});
		fRegistryUpdateJob.schedule();
	}

	public boolean isPublic() {
		return fPublic;
	}

	/**
	 * Moves an aggregator item from one location to another. The given instance
	 * will be updated with new information and which can be used after the
	 * storage update.
	 * 
	 * @param item
	 *            the item that is being moved
	 * @param parentUuid
	 *            the new parent of the moved item
	 * @param newOrder
	 *            the new order of the item
	 */
	public void move(AggregatorUIItem item, AggregatorUIItem oldParent,
			int oldOrder, AggregatorUIItem newParent, int newOrder) {
		try {
			lock.writeLock().lock();
			long start = System.currentTimeMillis();
			int details = 0;
			if (!oldParent.equals(newParent)) {
				// The item is moved into a new parent
				details |= AggregatorItemChangedEvent.NEW_PARENT;
				shiftUp((AggregatorUIItem) item);
				fDatabase.move((AggregatorUIItem) item,
						(AggregatorUIItem) newParent, newOrder);
			} else if (newOrder > oldOrder) {
				// The item is moved down (new order is higher)
				shiftUp(item, oldOrder, newOrder);
				fDatabase.move((AggregatorUIItem) item,
						(AggregatorUIItem) newParent, newOrder);
			} else {
				// The item is moved up
				shiftDown(item, oldOrder, newOrder);
				fDatabase.move((AggregatorUIItem) item,
						(AggregatorUIItem) newParent, newOrder);
			}
			// Tell our listeners that the deed is done
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.MOVED,
					AggregatorItemChangedEvent.NEW_PARENT, oldParent, oldOrder,
					System.currentTimeMillis() - start));
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Notify feed listeners about the aggregator item change.
	 * 
	 * @param event
	 *            The change event with details
	 */
	public void notifyListerners(final AggregatorItemChangedEvent event) {
		if (AggregatorPlugin.getDefault().isDebugging()) {
			System.out.println("[DEBUG] " + event); //$NON-NLS-1$
		}
		for (final IAggregatorEventListener listener : feedListeners) {
			SafeRunner.run(new ISafeRunnable() {
				public void handleException(Throwable exception) {
					exception.printStackTrace();
				}

				public void run() throws Exception {
					listener.aggregatorItemChanged(event);
				}

			});
		}
	}

	/**
	 * Removes the specified item from the collection and underlying database.
	 * 
	 * @param item
	 *            the element to remove
	 */
	public IStatus delete(IAggregatorItem item) {
		try {
			lock.writeLock().lock();
			long start = System.currentTimeMillis();
			shiftUp((AggregatorUIItem) item);
			fDatabase.delete((AggregatorUIItem) item);
			if (item instanceof Folder) {
				UUID feedId = ((Folder) item).getFeed();
				// Make sure we also delete the associated feed instance
				if (feedId != null) {
					fDatabase.delete(fFeeds.remove(feedId));
				}
			}
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.REMOVED, System.currentTimeMillis()
							- start));
			return Status.OK_STATUS;
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * Removes the event listener from the list. The specified listener will no
	 * longer be notified of aggregator events.
	 * 
	 * @param listener
	 *            the event listener to remove
	 */
	public void removeFeedListener(IAggregatorEventListener listener) {
		feedListeners.remove(listener);
	}

	/**
	 * Renames the given aggregator item, but does not fire an event.
	 * 
	 * @param item
	 *            the item to rename
	 */
	public void rename(IAggregatorItem item) {
		try {
			lock.writeLock().lock();
			fDatabase.rename((AggregatorUIItem) item);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void setOrdering(int ordering) {
	}

	public void setOrdering(long ordering) {
	}

	public void setParentUUID(UUID parent_uuid) {
	}

	/**
	 * Indicates that the given item has been read. If the item is an article
	 * only the article is marked as read. If the item is a feed or folder the
	 * contained articles are marked as read.
	 * 
	 * @param item
	 *            the item to mark as read
	 */
	public void setRead(IAggregatorItem item) {
		long start = System.currentTimeMillis();
		try {
			lock.writeLock().lock();
			fDatabase.updateReadFlag((AggregatorUIItem) item);
		} finally {
			lock.writeLock().unlock();
		}
		if (item instanceof Article) {
			((InternalArticle) item).setRead(true);
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.READ, System.currentTimeMillis()
							- start));
		} else {
			IAggregatorItem[] children = getChildren(item);
			for (IAggregatorItem child : children) {
				if (child instanceof Article)
					notifyListerners(new AggregatorItemChangedEvent(child,
							FeedChangeEventType.READ, System
									.currentTimeMillis()
									- start));
			}
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Updates the sibling item positions by shifting them downwards.
	 * 
	 * @param item
	 *            the item that was moved
	 * @param from
	 *            the initial position of the moved item
	 * @param to
	 *            the new position of the moved item
	 */
	private void shiftDown(AggregatorUIItem item, int from, int to) {
		IAggregatorItem parent = item.getParent();
		for (int i = from - 1; i >= to; i--) {
			long start = System.currentTimeMillis();
			AggregatorUIItem sibling = (AggregatorUIItem) getItemAt(parent, i);
			int oldOrder = sibling.getOrdering();
			fDatabase.move((AggregatorUIItem) sibling,
					(AggregatorUIItem) parent, sibling.getOrdering() + 1);
			notifyListerners(new AggregatorItemChangedEvent(sibling,
					FeedChangeEventType.SHIFTED,
					AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder,
					System.currentTimeMillis() - start));
		}
	}

	private List<AggregatorItemChangedEvent> shiftUp(AggregatorUIItem item) {
		IAggregatorItem parent = item.getParent();
		int count = getChildCount(parent);
		ArrayList<AggregatorItemChangedEvent> events = new ArrayList<AggregatorItemChangedEvent>();
		for (int i = item.getOrdering() + 1; i < count; i++) {
			long start = System.currentTimeMillis();
			AggregatorUIItem sibling = (AggregatorUIItem) getItemAt(parent, i);
			int oldOrder = sibling.getOrdering();
			fDatabase.move((AggregatorUIItem) sibling,
					(AggregatorUIItem) parent, sibling.getOrdering() - 1);
			events.add(new AggregatorItemChangedEvent(sibling,
					FeedChangeEventType.SHIFTED,
					AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder,
					System.currentTimeMillis() - start));
		}
		return events;
	}

	/**
	 * Updates the sibling item positions by shifting them upwards.
	 * 
	 * @param item
	 *            the item that was moved
	 * @param from
	 *            the initial position of the moved item
	 * @param to
	 *            the new position of the moved item
	 */
	private List<AggregatorItemChangedEvent> shiftUp(AggregatorUIItem item,
			final int from, final int to) {
		IAggregatorItem parent = item.getParent();
		ArrayList<AggregatorItemChangedEvent> events = new ArrayList<AggregatorItemChangedEvent>();
		for (int i = from + 1; i <= to; i++) {
			long start = System.currentTimeMillis();
			AggregatorUIItem sibling = (AggregatorUIItem) getItemAt(parent, i);
			int oldOrder = sibling.getOrdering();
			fDatabase.move((AggregatorUIItem) sibling,
					(AggregatorUIItem) parent, sibling.getOrdering() - 1);
			events.add(new AggregatorItemChangedEvent(sibling,
					FeedChangeEventType.SHIFTED,
					AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder,
					System.currentTimeMillis() - start));
		}
		return events;
	}

	public void update(IAggregatorItem item) {
		List<IAggregatorItem> items = getDescendants(item);
		items.add(item);
		for (IAggregatorItem aggregatorItem : items) {
			if (aggregatorItem instanceof Folder) {
				UUID feedId = ((Folder) aggregatorItem).getFeed();
				if (feedId != null) {
					Feed feed = getFeeds().get(feedId);
					if (!feed.isUpdating()) {
						FeedUpdateJob job = new FeedUpdateJob(this, feed);
						job.schedule();
					}
				}
			}
		}
	}

	public void updateFeedData(IAggregatorItem item) {
		try {
			lock.writeLock().lock();
			if (item instanceof Feed) {
				// Ensure that the local list has a copy of the same instance.
				fFeeds.put(((Feed) item).getUUID(), (Feed) item);
				fDatabase.updateFeed((Feed) item);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
}
