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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.internal.AggregatorItem;
import no.resheim.aggregator.data.internal.IAggregatorStorage;
import no.resheim.aggregator.data.internal.RegistryUpdateJob;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedCollection extends AggregatorItem {

	private static final UUID DEFAULT_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	/** The list of feed change listeners */
	private static ArrayList<IAggregatorEventListener> feedListeners = new ArrayList<IAggregatorEventListener>();
	private IAggregatorStorage database;

	/** The number of milliseconds in a day */
	private final long DAY = 86400000;

	private boolean fPublic;

	final RegistryUpdateJob fRegistryUpdateJob = new RegistryUpdateJob(this);

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
	 * List of <i>live</i> feeds that we must keep track of even if the any
	 * viewers has not opened the feed for viewing so that it has had a chance
	 * of being created. This list is populated at startup and maintained
	 * thereafter.
	 */
	private HashMap<UUID, Feed> sites;

	private String title;

	public FeedCollection(String id, boolean pub) {
		super(null);
		this.id = id;
		this.fPublic = pub;
	}

	/**
	 * Add listener to be notified about feed changes. The added listener will
	 * be notified when feeds are added, removed and when their contents has
	 * changed.
	 * 
	 * @param listener
	 *            The listener to be notified
	 */
	public void addFeedListener(IAggregatorEventListener listener) {
		feedListeners.add(listener);
	}

	/**
	 * Adds a new feed to the database and immediately stores it's data in the
	 * persistent storage.
	 * 
	 * @param feed
	 *            The aggregator item to add
	 */
	public void addNew(AggregatorItem item) {
		try {
			lock.writeLock().lock();
			try {
				if (item.getParent().equals(this)) {
					item.setOrdering(getChildCount(this));
				} else {
					item.setOrdering(getChildCount(item.getParent()));
				}
				if (item instanceof Feed) {
					Feed feed = (Feed) item;
					sites.put(feed.getUUID(), feed);
					database.add(feed);
					FeedUpdateJob job = new FeedUpdateJob(this, feed);
					job.schedule();
				} else if (item instanceof Folder) {
					Folder folder = (Folder) item;
					database.add(folder);
				} else if (item instanceof Article) {
					Article feedItem = (Article) item;
					feedItem.setAddedDate(System.currentTimeMillis());
					database.add(feedItem);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			lock.writeLock().unlock();
		}
		notifyListerners(new AggregatorItemChangedEvent(item,
				FeedChangeEventType.CREATED));
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
				database.deleteOutdated(site, lim);
				break;
			case KEEP_SOME:
				database.keepMaximum(site, articles);
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
	 *            The feed to update
	 */
	void feedUpdated(Feed feed) {
		try {
			lock.writeLock().lock();
			database.updateFeed(feed);
		} finally {
			lock.writeLock().unlock();
		}
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public int getChildCount(IAggregatorItem parent) {
		try {
			lock.readLock().lock();
			return database.getChildCount((AggregatorItem) parent);
		} finally {
			lock.readLock().unlock();
		}
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	public IAggregatorItem[] getChildren(IAggregatorItem item) {
		try {
			lock.readLock().lock();
			return database.getChildren((AggregatorItem) item);
		} finally {
			lock.readLock().unlock();
		}
	}

	public FeedCollection getCollection() {
		return this;
	}

	public String getDescription(Article item) {
		try {
			lock.readLock().lock();
			return database.getDescription(item);
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
	public Collection<Feed> getFeeds() {
		return sites.values();
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
			IAggregatorItem item = database.getItem(parent, index);
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
			if (element instanceof Feed) {
				return database.getUnreadCount(((Feed) element));
			}
			return -1;
		} finally {
			lock.readLock().unlock();
		}
	}

	public int getOrdering() {
		return 0;
	}

	public IAggregatorItem getParent() {
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
	public boolean hasArticle(Article item) {
		try {
			lock.readLock().lock();
			return database.hasArticle(item.getGuid());
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
			return database.hasFeed(url);
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
		this.database = storage;
		sites = storage.getFeeds();
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

	public boolean isLocked(Feed feed) {
		final IExtensionRegistry ereg = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = ereg
				.getConfigurationElementsFor(AggregatorPlugin.REGISTRY_EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			if (element.getName().equals("feed") && element.getAttribute("url").equals(feed.getURL())) { //$NON-NLS-1$ //$NON-NLS-2$
				if (element.getAttribute("locked") != null) { //$NON-NLS-1$
					return Boolean.parseBoolean(element.getAttribute("locked")); //$NON-NLS-1$
				}
			}
		}
		return false;
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
	public void move(IAggregatorItem item, IAggregatorItem oldParent,
			int oldOrder, IAggregatorItem newParent, int newOrder) {
		try {
			lock.writeLock().lock();
			int details = 0;
			if (!oldParent.equals(newParent)) {
				// The item is moved into a new parent
				details |= AggregatorItemChangedEvent.NEW_PARENT;
				shiftUp((AggregatorItem) item);
				database.move(item, newParent, newOrder);
			} else if (newOrder > oldOrder) {
				// The item is moved down (new order is higher)
				shiftUp(item, oldOrder, newOrder);
				database.move(item, newParent, newOrder);
			} else {
				// The item is moved up
				shiftDown(item, oldOrder, newOrder);
				database.move(item, newParent, newOrder);
			}
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.MOVED,
					AggregatorItemChangedEvent.NEW_PARENT, oldParent, oldOrder));
		} finally {
			lock.writeLock().unlock();
		}
	}

	public Article newArticleInstance(IAggregatorItem parent) {
		Article article = new Article(parent);
		article.setUUID(UUID.randomUUID());
		return article;
	}

	/**
	 * Factory method to create a new feed instance associated with this feed
	 * collection and with an unique identifier.
	 * 
	 * @param parent
	 *            the parent item
	 * @return a new feed instance
	 */
	public Feed newFeedInstance(IAggregatorItem parent) {
		Feed feed = new Feed(parent);
		feed.setUUID(UUID.randomUUID());
		return feed;
	}

	public Folder newFolderInstance(IAggregatorItem parent) {
		Folder folder = new Folder(parent);
		folder.setUUID(UUID.randomUUID());
		return folder;
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
	 * @param element
	 *            the element to remove
	 */
	public IStatus remove(IAggregatorItem element) {
		try {
			lock.writeLock().lock();
			if (element instanceof Feed) {
				if (isLocked((Feed) element))
					return new Status(IStatus.CANCEL,
							AggregatorPlugin.PLUGIN_ID,
							Messages.FeedCollection_NoDelete_Locked);
				sites.remove(((Feed) element).getUUID());
			}
			try {
				database.delete(element);
				shiftUp((AggregatorItem) element);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			lock.writeLock().unlock();
		}
		notifyListerners(new AggregatorItemChangedEvent(element,
				FeedChangeEventType.REMOVED));
		return Status.OK_STATUS;
	}

	public void removeFeedListener(IAggregatorEventListener listener) {
		feedListeners.remove(listener);
	}

	/**
	 * Renames the given aggregator item, but does not fire an event.
	 * 
	 * @param item
	 */
	public void rename(IAggregatorItem item) {
		try {
			lock.writeLock().lock();
			database.rename(item);
		} finally {
			lock.writeLock().unlock();
		}
	}

	public void setCollection(FeedCollection registry) {
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
	 */
	public void setRead(IAggregatorItem item) {
		try {
			lock.writeLock().lock();
			database.updateReadFlag(item);
		} finally {
			lock.writeLock().unlock();
		}
		if (item instanceof Article) {
			((Article) item).setRead(true);
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.READ));
		} else {
			IAggregatorItem[] children = getChildren(item);
			for (IAggregatorItem child : children) {
				if (child instanceof Article)
					notifyListerners(new AggregatorItemChangedEvent(child,
							FeedChangeEventType.READ));
			}
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Updates the tree items and the associated aggregator item following the
	 * given tree item by incrementing the ordering property by one.
	 * 
	 * @param treeItem
	 *            The tree item that was moved
	 */
	private void shiftDown(IAggregatorItem item, int from, int to) {
		IAggregatorItem parent = item.getParent();
		for (int i = from - 1; i >= to; i--) {
			AggregatorItem sibling = (AggregatorItem) getItemAt(parent, i);
			int oldOrder = sibling.getOrdering();
			database.move(sibling, parent, sibling.getOrdering() + 1);
			notifyListerners(new AggregatorItemChangedEvent(sibling,
					FeedChangeEventType.SHIFTED,
					AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder));
		}
	}

	private List<AggregatorItemChangedEvent> shiftUp(AggregatorItem item) {
		IAggregatorItem parent = item.getParent();
		int count = getChildCount(parent);
		ArrayList<AggregatorItemChangedEvent> events = new ArrayList<AggregatorItemChangedEvent>();
		for (int i = item.getOrdering() + 1; i < count; i++) {
			AggregatorItem sibling = (AggregatorItem) getItemAt(parent, i);
			int oldOrder = sibling.getOrdering();
			database.move(sibling, parent, sibling.getOrdering() - 1);
			events.add(new AggregatorItemChangedEvent(sibling,
					FeedChangeEventType.SHIFTED,
					AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder));
		}
		return events;
	}

	private List<AggregatorItemChangedEvent> shiftUp(IAggregatorItem item,
			final int from, final int to) {
		IAggregatorItem parent = item.getParent();
		ArrayList<AggregatorItemChangedEvent> events = new ArrayList<AggregatorItemChangedEvent>();
		for (int i = from + 1; i <= to; i++) {
			AggregatorItem sibling = (AggregatorItem) getItemAt(parent, i);
			int oldOrder = sibling.getOrdering();
			database.move(sibling, parent, sibling.getOrdering() - 1);
			events.add(new AggregatorItemChangedEvent(sibling,
					FeedChangeEventType.SHIFTED,
					AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder));
		}
		return events;
	}

	public void updateAllFeeds() {
		for (Feed feed : getFeeds()) {
			updateFeed(feed);
		}
	}

	public void updateFeed(Feed feed) {
		if (!feed.isUpdating()) {
			FeedUpdateJob job = new FeedUpdateJob(this, feed);
			job.schedule();
		}
	}

	public void updateFeedData(IAggregatorItem item) {
		try {
			lock.writeLock().lock();
			if (item instanceof Feed) {
				// Ensure that the local list has a copy of the same instance.
				sites.put(((Feed) item).getUUID(), (Feed) item);
				database.updateFeed((Feed) item);
			}
		} finally {
			lock.writeLock().unlock();
		}
	}
}
