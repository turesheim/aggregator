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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.internal.CollectionUpdateJob;
import no.resheim.aggregator.core.filter.Filter;
import no.resheim.aggregator.core.synch.AbstractSynchronizer;
import no.resheim.aggregator.core.synch.DirectSynchronizer;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * This type represents a root data element. It holds all folders and using
 * these one can access all other items. It also holds an instance of
 * {@link IAggregatorStorage} which is the "back-end" where data us actually
 * stored. Use this type to access items, instead of the storage.
 * 
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AggregatorCollection extends AggregatorItemParent {

	private static final UUID COLLECTION_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	/** The list of feed change listeners */
	private static ArrayList<IAggregatorEventListener> feedListeners = new ArrayList<IAggregatorEventListener>();

	/** Name of the trash folder */
	private static final String TRASH_FOLDER_NAME = "Trash"; //$NON-NLS-1$

	/** Trash folder identifier */
	private static final UUID TRASH_ID = UUID
			.fromString("448119fa-609c-4463-89cf-31d41d94ad05"); //$NON-NLS-1$
	private static final UUID LABEL_FOLDER_ID = UUID
			.fromString("449119fa-609c-4463-89cf-31d41d94ad05"); //$NON-NLS-1$

	/**
	 * The persistent storage for our data.
	 */
	private IAggregatorStorage fDatabase;

	private boolean fDefault;

	/**
	 * List of <i>live</i> feeds that we must keep track of even if none of the
	 * viewers has opened the feed for viewing so that it has had a chance of
	 * being created. This list is populated at startup and maintained
	 * thereafter.
	 */
	private HashMap<UUID, Subscription> fFeeds;

	private ArrayList<Filter> fFilters;

	private boolean fPublic;

	/**
	 * Job that is awaken now and then for updating the collection.
	 */
	private final CollectionUpdateJob fCollectionUpdateJob = new CollectionUpdateJob(
			this);

	/**
	 * The single instance of a trash folder
	 */
	private Folder fTrashFolder;
	private Folder fLabelFolder;

	/**
	 * The identifier of the registry as specified when the registry was
	 * declared.
	 */
	private String id;

	/**
	 * Initialises the collection.
	 * 
	 * @param id
	 *            the identifier of the collection
	 * @param pub
	 *            whether or not the collection is public
	 * @param def
	 *            whether or not the collection is the default collection
	 */
	public AggregatorCollection(String id, boolean pub, boolean def) {
		super(null, COLLECTION_ID);
		this.id = id;
		fPublic = pub;
		fDefault = def;
		fFilters = new ArrayList<Filter>();
	}

	/**
	 * Add a listener to be notified about feed changes. The added listener will
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
	 * Adds a new items to the database and immediately stores it's data in the
	 * persistent storage.
	 * 
	 * @param subscription
	 *            the aggregator item to add
	 */
	public IStatus addNew(AggregatorItem[] items) {
		MultiStatus ms = new MultiStatus(AggregatorPlugin.PLUGIN_ID,
				IStatus.OK, "Adding new items", null); //$NON-NLS-1$
		ArrayList<AggregatorItem> newItems = new ArrayList<AggregatorItem>();
		try {
			fDatabase.writeLock().lock();
			for (AggregatorItem item : items) {
				if (item instanceof Folder) {
					AggregatorItem folder = (AggregatorItem) item;
					IStatus status = fDatabase.add(folder);
					ms.add(status);
					if (status.isOK()) {
						newItems.add(folder);
					}
				} else if (item instanceof Article) {
					Article feedItem = (Article) item;
					validate(feedItem);
					IStatus status = fDatabase.add(feedItem);
					ms.add(status);
					if (status.isOK()) {
						newItems.add(feedItem);
					}
				} else if (item instanceof ArticleLabel) {
					ArticleLabel feedItem = (ArticleLabel) item;
					IStatus status = fDatabase.add(feedItem);
					ms.add(status);
					if (status.isOK()) {
						newItems.add(feedItem);
					}
				}
			}
		} finally {
			fDatabase.writeLock().unlock();
		}
		notifyListerners(newItems.toArray(new AggregatorItem[newItems.size()]),
				EventType.CREATED);
		return ms;
	}

	/**
	 * Adds a new feed to the collection. If the feed location is not specified
	 * a new folder will automatically be created and associated with the feed.
	 * 
	 * @param subscription
	 *            the new feed to add
	 * @return the {@link Folder} where feed items will be put
	 */
	public Folder addNew(Subscription subscription) {
		subscription.validate();
		Folder folder = null;
		try {
			fDatabase.writeLock().lock();
			// No location has been specified for the feed so we must create a
			// new folder at the collection root and use this.
			if (subscription.getLocation() == null) {
				folder = new Folder(this, UUID.randomUUID());
				folder.setFeed(subscription.getUUID());
				folder.setTitle(subscription.getTitle());
				fDatabase.add(folder);
				move(folder, this, this);
				subscription.setLocation(folder.getUUID());
				// XXX: Bad hack. Find a better way to create the RS
				// fDatabase.getChildAt(folder, 0);
			} else {
				// FIXME: Determine the folder from the location
			}
			fFeeds.put(subscription.getUUID(), subscription);
			fDatabase.add(subscription);
			AbstractSynchronizer synchronizer = AggregatorPlugin
					.getSynchronizer(subscription.getSynchronizer());
			synchronizer.setCollection(this);
			synchronizer.setSubscription(subscription);
			synchronizer.schedule();
		} catch (CoreException e) {
			e.printStackTrace();
		} finally {
			fDatabase.writeLock().unlock();
		}
		if (folder != null) {
			notifyListerners(new Object[] { folder }, EventType.CREATED);
		}
		return folder;
	}

	/**
	 * Creates a new trash folder if it does not already exist.
	 */
	private void createSystemItems() {
		try {
			for (AggregatorItem item : getChildren(EnumSet.of(ItemType.FOLDER))) {
				if (item.getUUID().equals(TRASH_ID)) {
					fTrashFolder = (Folder) item;
					break;
				}
				if (item.getUUID().equals(LABEL_FOLDER_ID)) {
					fLabelFolder = (Folder) item;
					break;
				}
			}
			if (fTrashFolder == null) {
				Folder trash = new Folder(this, TRASH_ID);
				trash.setSystem(true);
				trash.setFlags(EnumSet.of(Flag.TRASH));
				trash.setTitle(TRASH_FOLDER_NAME);
				addNew(new AggregatorItem[] { trash });
				fTrashFolder = trash;
			}
			if (fLabelFolder == null) {
				Folder trash = new Folder(this, LABEL_FOLDER_ID);
				trash.setSystem(true);
				trash.setFlags(EnumSet.of(Flag.LABEL_ROOT));
				trash.setTitle("Labels");
				addNew(new AggregatorItem[] { trash });
				fTrashFolder = trash;
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Updates the feed data in the persistent storage. Should only be called by
	 * {@link DirectSynchronizer} after the feed has be updated with new
	 * information.
	 * 
	 * @param feed
	 *            the feed to update
	 */
	public void feedUpdated(Subscription feed) {
		try {
			fDatabase.writeLock().lock();
			fDatabase.updateSubscription(feed);
		} finally {
			fDatabase.writeLock().unlock();
		}
	}

	public String getDescription(Article item) {
		try {
			fDatabase.readLock().lock();
			return fDatabase.getDescription(item);
		} finally {
			fDatabase.readLock().unlock();
		}
	}

	/**
	 * Returns the complete list of feeds that this registry is maintaining.
	 * Note
	 * 
	 * @return The list of feeds
	 */
	public HashMap<UUID, Subscription> getFeeds() {
		return fFeeds;
	}

	public ArrayList<Filter> getFilters() {
		return fFilters;
	}

	/**
	 * Returns the identifier of the feed collection as specified in the
	 * collection declaration.
	 * 
	 * @return the feed identifier string
	 * @uml.property name="id"
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
	public int getUnreadItemCount(AggregatorItem element) {
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

	IAggregatorStorage getStorage() {
		return fDatabase;
	}

	public String getTitle() {
		return title;
	}

	/**
	 * Returns the trash folder instance of the collection.
	 * 
	 * @return the trash folder instance
	 */
	public Folder getTrashFolder() {
		return fTrashFolder;
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
	 * case <code>true</code> is returned. This method relies on the globally
	 * unique identifier of the feed item.
	 * 
	 * @param guid
	 *            the unique identifier of the item.
	 * @return <code>true</code> if the item exists
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
			return fDatabase.hasSubscription(url);
		} finally {
			fDatabase.readLock().unlock();
		}
	}

	/**
	 * Loads all feeds from the given backend storage and initialises.
	 * 
	 * @param storage
	 */
	public void initialize(IAggregatorStorage storage) {
		this.fDatabase = storage;
		fFeeds = storage.getSubscriptions();
		createSystemItems();
		// Start a new update job that will periodically wake up and create
		// FeedUpdateJobs when a feed is scheduled for an update.
		fCollectionUpdateJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				fCollectionUpdateJob.schedule(30000);
			}
		});
		fCollectionUpdateJob.schedule();
	}

	public boolean isDefault() {
		return fDefault;
	}

	public boolean isPublic() {
		return fPublic;
	}

	/**
	 * Moves an item from one location to another. The given instance will be
	 * updated with new information and which can be used after the storage
	 * update.
	 * 
	 * TODO: Move to AggregatorItemParent
	 * 
	 * @param item
	 *            the item that is being moved
	 * @param parentUuid
	 *            the new parent of the moved item
	 * @param newOrder
	 *            the new order of the item
	 * @throws CoreException
	 * 
	 */
	public void move(AggregatorItem item, AggregatorItem oldParent,
			AggregatorItemParent newParent) throws CoreException {
		try {
			fDatabase.writeLock().lock();
			int details = 0;
			if (!oldParent.equals(newParent)) {
				// The item is moved into a new parent
				details |= AggregatorItemChangedEvent.NEW_PARENT;
				item.parent = newParent;
				// Update the database
				fDatabase.moved(item);
			}
		} finally {
			fDatabase.writeLock().unlock();
		}
	}

	/**
	 * Moves a number of consecutive items to a new parent. This method assumes
	 * that all the supplied items have the same parent and that they are in
	 * consecutive order. If this is not the case unpredictable results will
	 * occur. No verification is performed.
	 * 
	 * @param items
	 *            the items to move
	 * @param newParent
	 *            the new parent of the items
	 * @throws CoreException
	 */
	public void move(AggregatorItem[] items, AggregatorItemParent newParent)
			throws CoreException {
		// We don't care about empty arrays
		if (items.length == 0)
			return;
		try {
			fDatabase.writeLock().lock();
			int details = 0;
			// The (shared) parent item
			AggregatorItemParent oldParent = items[0].getParent();
			if (!oldParent.equals(newParent)) {
				// The item is moved into a new parent
				details |= AggregatorItemChangedEvent.NEW_PARENT;
				// Switch parent on the moved items
				for (AggregatorItem item : items) {
					// XXX:
					// oldParent.internalRemove(item);
					item.parent = newParent;
					// item.getParent().internalAdd(item);
					// Update the database
					fDatabase.moved(item);
				}
			}
		} finally {
			fDatabase.writeLock().unlock();
		}
	}

	/**
	 * Notify feed listeners about the item change.
	 * 
	 * @param event
	 *            the change event with details
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
	 * Synchronises feeds associated with the given folder item or it's
	 * descendants.
	 * 
	 * @param item
	 *            the folder item to synchronise
	 * @return the synchronisation status
	 * @throws CoreException
	 */
	public IStatus synchronize(AggregatorItem item) throws CoreException {
		Assert.isTrue(item instanceof Folder);
		AggregatorItem[] descendants = ((Folder) item).getChildren(EnumSet
				.of(ItemType.FOLDER));
		AggregatorItem[] items = new AggregatorItem[descendants.length + 1];
		items[0] = item;
		System.arraycopy(descendants, 0, items, 1, descendants.length);
		for (AggregatorItem aggregatorItem : items) {
			UUID feedId = ((Folder) aggregatorItem).getFeedUUID();
			if (feedId != null) {
				Subscription feed = getFeeds().get(feedId);
				if (!feed.isUpdating()) {
					AbstractSynchronizer synchronizer = AggregatorPlugin
							.getSynchronizer(feed.getSynchronizer());
					synchronizer.setCollection(this);
					synchronizer.setSubscription(feed);
					synchronizer.schedule();
				} else {
					return new Status(IStatus.ERROR,
							AggregatorPlugin.PLUGIN_ID, MessageFormat.format(
									Messages.FeedCollection_UpdateInProgress,
									feed.getTitle()));
				}
			}
		}
		return Status.OK_STATUS;
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
		if (item instanceof Article) {
			((Article) item).setRead(true);
			if (item instanceof Article) {
				writeBack(item);
			}
		} else if (item instanceof AggregatorItemParent) {
			AggregatorItem[] children = ((AggregatorItemParent) item)
					.getChildren(EnumSet.allOf(ItemType.class));
			for (AggregatorItem child : children) {
				setRead(child);
				if (child instanceof Article) {
					writeBack(child);
				}
			}
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Writes back the aggregator item data so that the
	 * {@link IAggregatorStorage} database (if any) is in sync with the live
	 * object.
	 * 
	 * @param item
	 *            the aggregator item
	 */
	public void writeBack(AggregatorItem item) {
		try {
			fDatabase.writeLock().lock();
			if (item instanceof Article) {
				((Article) item).setLastChanged(System.currentTimeMillis());
			}
			fDatabase.writeBack((AggregatorItem) item);
		} finally {
			fDatabase.writeLock().unlock();
		}
		notifyListerners(new Object[] { item }, EventType.CHANGED);
	}

	public void updateFeedData(Subscription sub) {
		try {
			fDatabase.writeLock().lock();
			// Ensure that the local list has a copy of the same instance.
			fFeeds.put(sub.getUUID(), sub);
			fDatabase.updateSubscription((Subscription) sub);
		} finally {
			fDatabase.writeLock().unlock();
		}
	}

	/**
	 * Returns a list of all the articles that was changed (locally) since the
	 * subscription was last updated.
	 * 
	 * @return a list of changed articles
	 */
	public List<Article> getChangedArticles(Subscription subscription, long time) {
		return fDatabase.getChangedArticles(subscription, time);
	}

	private void validate(Article article) {
		Assert
				.isNotNull(article.getGuid(),
						"Cannot add article without a guid"); //$NON-NLS-1$
	}
}
