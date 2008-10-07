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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.data.internal.CollectionUpdateJob;
import no.resheim.aggregator.data.internal.InternalArticle;
import no.resheim.aggregator.data.internal.InternalFolder;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedCollection extends AggregatorItemParent {

	private static final String TRASH_FOLDER_NAME = "Trash"; //$NON-NLS-1$
	private static final UUID COLLECTION_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	/** Trash folder identifier */
	private static final UUID TRASH_ID = UUID
			.fromString("448119fa-609c-4463-89cf-31d41d94ad05"); //$NON-NLS-1$

	/** The list of feed change listeners */
	private static ArrayList<IAggregatorEventListener> feedListeners = new ArrayList<IAggregatorEventListener>();

	/** The storage for our data */
	private IAggregatorStorage fDatabase;

	private boolean fPublic;

	private boolean fDefault;

	final CollectionUpdateJob fRegistryUpdateJob = new CollectionUpdateJob(this);

	/**
	 * The identifier of the registry as specified when the registry was
	 * declared.
	 */
	private String id;

	/**
	 * List of <i>live</i> feeds that we must keep track of even if none of the
	 * viewers has opened the feed for viewing so that it has had a chance of
	 * being created. This list is populated at startup and maintained
	 * thereafter.
	 */
	private HashMap<UUID, Feed> fFeeds;

	public FeedCollection(String id, boolean pub, boolean def) {
		super(null, COLLECTION_ID);
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
	public void addNew(AggregatorItem item) {
		long start = System.currentTimeMillis();
		try {
			fDatabase.writeLock().lock();
			if (item instanceof Folder) {
				AggregatorItem folder = (AggregatorItem) item;
				fDatabase.add(folder);
			} else if (item instanceof Article) {
				Article feedItem = (Article) item;
				validate(feedItem);
				fDatabase.add(feedItem);
			}
		} finally {
			fDatabase.writeLock().unlock();
		}
		notifyListerners(new Object[] {
			item
		}, EventType.CREATED);
	}

	private void validate(Article article) {
		Assert
				.isNotNull(article.getGuid(),
						"Cannot add article without a guid"); //$NON-NLS-1$
	}

	/**
	 * @param feed
	 * @return the {@link Folder} where feed items will be put
	 */
	public Folder addNew(Feed feed) {
		Assert.isNotNull(feed.getUUID(),
				"Cannot add feed with unspecified UUID"); //$NON-NLS-1$
		long start = System.currentTimeMillis();
		InternalFolder folder = null;
		try {
			fDatabase.writeLock().lock();
			// No location has been specified for the feed so we must create a
			// new folder at the collection root and use this.
			if (feed.getLocation() == null) {
				folder = new InternalFolder(this, UUID.randomUUID());
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
			fDatabase.writeLock().unlock();
		}
		notifyListerners(new Object[] {
			folder
		}, EventType.CREATED);
		return folder;
	}

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	void cleanUp(Feed site) {
		// First find the folder
		try {
			for (Folder folder : this.getDescendingFolders()) {
				if (folder.getUUID().equals(site.getLocation())) {
					folder.cleanUp(site);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
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
			fDatabase.writeLock().lock();
			fDatabase.updateFeed(feed);
		} finally {
			fDatabase.writeLock().unlock();
		}
	}

	private List<AggregatorItem> getDescendants(AggregatorItem item)
			throws CoreException {
		ArrayList<AggregatorItem> descendants = new ArrayList<AggregatorItem>();
		if (item instanceof AggregatorItemParent) {
			AggregatorItem[] children = ((AggregatorItemParent) item)
					.getChildren();
			for (AggregatorItem aggregatorItem : children) {
				descendants.add(aggregatorItem);
				descendants.addAll(getDescendants(aggregatorItem));
			}
		}
		return descendants;
	}

	public String getDescription(Article item) {
		try {
			fDatabase.readLock().lock();
			return fDatabase.getDescription(item);
		} finally {
			fDatabase.readLock().unlock();
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

	/**
	 * Returns the number of <b>unread</b> articles.
	 * 
	 * @param element
	 * @return
	 */
	public int getItemCount(AggregatorItem element) {
		try {
			fDatabase.readLock().lock();
			return fDatabase.getUnreadCount((AggregatorItem) element);
		} finally {
			fDatabase.readLock().unlock();
		}
	}

	public int getOrdering() {
		return 0;
	}

	public AggregatorItemParent getParent() {
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
	 * @see no.resheim.aggregator.data.AggregatorItem#getUUID()
	 */
	public UUID getUUID() {
		return COLLECTION_ID;
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
			fDatabase.readLock().lock();
			return fDatabase.hasArticle(guid);
		} finally {
			fDatabase.readLock().unlock();
		}
	}

	/**
	 * Tests if the feed exists in the repository.
	 * 
	 * @return <b>True</b> if the feed exists.
	 */
	public boolean hasFeed(String url) {
		try {
			fDatabase.readLock().lock();
			return fDatabase.hasFeed(url);
		} finally {
			fDatabase.readLock().unlock();
		}
	}

	private Folder fTrashFolder;

	/**
	 * Loads all feeds from the given backend storage and initializes.
	 * 
	 * @param storage
	 */
	public void initialize(IAggregatorStorage storage) {
		this.fDatabase = storage;
		fFeeds = storage.getFeeds();
		createTrashFolder();
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

	private void createTrashFolder() {
		try {
			for (AggregatorItem item : getChildren()) {
				if (item.getUUID().equals(TRASH_ID)) {
					fTrashFolder = (Folder) item;
					break;
				}
			}
			if (fTrashFolder == null) {
				InternalFolder trash = new InternalFolder(this, TRASH_ID);
				trash.setSystem(true);
				trash.setFlags(EnumSet.of(Flag.TRASH));
				trash.setTitle(TRASH_FOLDER_NAME);
				addNew(trash);
				fTrashFolder = trash;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	public Folder getTrashFolder() {
		return fTrashFolder;
	}

	IAggregatorStorage getStorage() {
		return fDatabase;
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
	 * @throws CoreException
	 */
	public void move(AggregatorItem item, AggregatorItem oldParent,
			int oldOrder, AggregatorItemParent newParent, int newOrder)
			throws CoreException {
		try {
			fDatabase.writeLock().lock();
			int details = 0;
			if (!oldParent.equals(newParent)) {
				// The item is moved into a new parent
				details |= AggregatorItemChangedEvent.NEW_PARENT;
				// Shift affected siblings
				shiftUp((AggregatorItem) item);
				// Switch parent item
				item.getParent().internalRemove(item);
				item.parent = newParent;
				item.getParent().internalAdd(item);
				// Change the order
				item.setOrdering(newOrder);
				// Update the database
				fDatabase.move((AggregatorItem) item);
			} else if (newOrder > oldOrder) {
				// The item is moved down (new order is higher)
				shiftUp(item, oldOrder, newOrder);
				item.setOrdering(newOrder);
				fDatabase.move((AggregatorItem) item);
			} else {
				// The item is moved up
				shiftDown(item, oldOrder, newOrder);
				item.setOrdering(newOrder);
				fDatabase.move((AggregatorItem) item);
			}
		} finally {
			fDatabase.writeLock().unlock();
		}
	}

	int count = 0;

	/**
	 * Notify feed listeners about the aggregator item change.
	 * 
	 * @param event
	 *            The change event with details
	 */
	public void notifyListerners(Object[] items, EventType type) {
		final AggregatorItemChangedEvent event = new AggregatorItemChangedEvent(
				items, type);
		for (final IAggregatorEventListener listener : feedListeners) {
			// SafeRunner.run(new ISafeRunnable() {
			// public void handleException(Throwable exception) {
			// exception.printStackTrace();
			// }
			//
			// public void run() throws Exception {
			listener.aggregatorItemChanged(event);
			// }
			//
			// });
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
	public void rename(AggregatorItem item) {
		try {
			fDatabase.writeLock().lock();
			fDatabase.rename((AggregatorItem) item);
		} finally {
			fDatabase.writeLock().unlock();
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
	 * @throws CoreException
	 */
	public void setRead(AggregatorItem item) throws CoreException {
		try {
			fDatabase.writeLock().lock();
			fDatabase.updateReadFlag((AggregatorItem) item);
		} finally {
			fDatabase.writeLock().unlock();
		}
		if (item instanceof Article) {
			((InternalArticle) item).setRead(true);
			notifyListerners(new Object[] {
				item
			}, EventType.READ);
		} else if (item instanceof AggregatorItemParent) {
			AggregatorItem[] children = ((AggregatorItemParent) item)
					.getChildren();
			for (AggregatorItem child : children) {
				if (child instanceof Article)
					notifyListerners(new Object[] {
						child
					}, EventType.READ);
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
	 * @throws CoreException
	 */
	private void shiftDown(AggregatorItem item, int from, int to)
			throws CoreException {
		AggregatorItemParent parent = item.getParent();
		for (int i = from - 1; i >= to; i--) {
			AggregatorItem sibling = parent.getChildAt(i);
			sibling.setOrdering(sibling.getOrdering() + 1);
			fDatabase.move((AggregatorItem) sibling);
			// notifyListerners(new AggregatorItemChangedEvent(sibling,
			// FeedChangeEventType.SHIFTED,
			// AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder,
			// System.currentTimeMillis() - start));
		}
	}

	List<AggregatorItemChangedEvent> shiftUp(AggregatorItem item)
			throws CoreException {
		AggregatorItemParent parent = item.getParent();
		int count = parent.getChildCount();
		ArrayList<AggregatorItemChangedEvent> events = new ArrayList<AggregatorItemChangedEvent>();
		for (int i = item.getOrdering() + 1; i < count; i++) {
			AggregatorItem sibling = parent.getChildAt(i);
			Assert.isNotNull(sibling);
			sibling.setOrdering(sibling.getOrdering() - 1);
			fDatabase.move((AggregatorItem) sibling);
			// events.add(new AggregatorItemChangedEvent(sibling,
			// FeedChangeEventType.SHIFTED,
			// AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder,
			// System.currentTimeMillis() - start));
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
	 * @throws CoreException
	 */
	private List<AggregatorItemChangedEvent> shiftUp(AggregatorItem item,
			final int from, final int to) throws CoreException {
		AggregatorItemParent parent = item.getParent();
		ArrayList<AggregatorItemChangedEvent> events = new ArrayList<AggregatorItemChangedEvent>();
		for (int i = from + 1; i <= to; i++) {
			AggregatorItem sibling = parent.getChildAt(i);
			sibling.setOrdering(sibling.getOrdering() - 1);
			fDatabase.move((AggregatorItem) sibling);
			// events.add(new AggregatorItemChangedEvent(sibling,
			// FeedChangeEventType.SHIFTED,
			// AggregatorItemChangedEvent.NEW_PARENT, parent, oldOrder,
			// System.currentTimeMillis() - start));
		}
		return events;
	}

	public IStatus update(AggregatorItem item) throws CoreException {
		List<AggregatorItem> items = getDescendants(item);
		items.add(item);
		for (AggregatorItem aggregatorItem : items) {
			if (aggregatorItem instanceof Folder) {
				UUID feedId = ((Folder) aggregatorItem).getFeedUUID();
				if (feedId != null) {
					Feed feed = getFeeds().get(feedId);
					if (!feed.isUpdating()) {
						FeedUpdateJob job = new FeedUpdateJob(this, feed);
						job.schedule();
					} else {
						return new Status(
								IStatus.ERROR,
								AggregatorPlugin.PLUGIN_ID,
								MessageFormat
										.format(
												Messages.FeedCollection_UpdateInProgress,
												feed.getTitle()));
					}
				}
			}
		}
		return Status.OK_STATUS;
	}

	public void updateFeedData(Feed item) {
		try {
			fDatabase.writeLock().lock();
			// Ensure that the local list has a copy of the same instance.
			fFeeds.put(item.getUUID(), item);
			fDatabase.updateFeed((Feed) item);
		} finally {
			fDatabase.writeLock().unlock();
		}
	}
}
