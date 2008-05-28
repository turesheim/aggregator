/*******************************************************************************
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
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.IAggregatorStorage;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.internal.RegistryUpdateJob;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedCollection implements IAggregatorItem {

	public class AggregatorDatabase {
	}

	private IAggregatorStorage database;

	/** The list of feed change listeners */
	private static ArrayList<FeedListener> feedListeners = new ArrayList<FeedListener>();
	private static final UUID DEFAULT_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	/**
	 * List of <i>live</i> feeds that we must keep track of even if the any
	 * viewers has not opened the feed for viewing so that it has had a chance
	 * of being created. This list is populated at startup and maintained
	 * thereafter.
	 */
	private HashMap<UUID, Feed> sites;

	/**
	 * The identifier of the registry as specified when the registry was
	 * declared.
	 */
	private String id;

	private String title;

	private boolean fPublic;

	public FeedCollection(String id, boolean pub) {
		this.id = id;
		this.fPublic = pub;
	}

	public boolean isPublic() {
		return fPublic;
	}

	/**
	 * Loads all feeds from the given backend storage and initializes.
	 * 
	 * @param storage
	 */
	public void initialize(IAggregatorStorage storage) {
		this.database = storage;
		sites = storage.initializeFeeds();
		// Start a new update job that will periodically wake up and create
		// FeedUpdateJobs when a feed is scheduled for an update.
		final RegistryUpdateJob job = new RegistryUpdateJob(this);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				job.schedule(30000);
			}
		});
		job.schedule();
	}

	/**
	 * Adds a new feed to the database and immediately stores it's data in the
	 * persistent storage.
	 * 
	 * @param feed
	 *            The feed to add
	 */
	public void add(IAggregatorItem item) {
		try {
			if (item instanceof Feed) {
				Feed feed = (Feed) item;
				sites.put(feed.getUUID(), feed);
				database.add(feed);
				feed.setRegistry(this);
			} else if (item instanceof Folder) {
				Folder folder = (Folder) item;
				folder.setRegistry(this);
				database.add(folder);
			} else if (item instanceof Article) {
				Article feedItem = (Article) item;
				feedItem.setAddedDate(System.currentTimeMillis());
				feedItem.setRegistry(this);
				database.add(feedItem);
			}
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.CREATED));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void move(IAggregatorItem item, IAggregatorItem newParent) {
		database.move(item, newParent);
	}

	/**
	 * Renames the given aggregator item, but does not fire an event.
	 * 
	 * @param item
	 */
	public void rename(IAggregatorItem item) {
		database.rename(item);
	}

	/**
	 * Updates the feed data in the persistent storage. Should only be called by
	 * {@link FeedUpdateJob} after the feed has be updated with new information.
	 * 
	 * @param feed
	 *            The feed to update
	 */
	void feedUpdated(Feed feed) {
		database.updateFeed(feed);
	}

	public void updateFeedData(IAggregatorItem item) {
		if (item instanceof Feed) {
			// Ensure that the local list has a copy of the same instance.
			sites.put(item.getUUID(), (Feed) item);
			database.updateFeed((Feed) item);
		}
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	public IAggregatorItem[] getChildren(IAggregatorItem item) {
		return database.getChildren(item);
	}

	/**
	 * 
	 * @param parent
	 * @return
	 */
	public int getChildCount(IAggregatorItem parent) {
		return database.getChildCount(parent);
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

	/**
	 * Tests if the feed exists in the repository.
	 * 
	 * @return <b>True</b> if the feed exists.
	 */
	public boolean hasFeed(String url) {
		return database.hasFeed(url);
	}

	/**
	 * Returns the number of <b>unread</b> articles.
	 * 
	 * @param element
	 * @return
	 */
	public int getItemCount(IAggregatorItem element) {
		if (element instanceof Feed) {
			return database.getUnreadCount(((Feed) element));
		}
		return -1;
	}

	public UUID getUUID() {
		return DEFAULT_ID;
	}

	/**
	 * 
	 * @return
	 */
	public String getId() {
		return id;
	}

	public String getDescription(Article item) {
		return database.getDescription(item);
	}

	/**
	 * Indicates that the given item has been read. If the item is an article
	 * only the article is marked as read. If the item is a feed or folder the
	 * contained articles are marked as read.
	 * 
	 * @param item
	 */
	public void setRead(IAggregatorItem item) {
		if (item instanceof Article) {
			((Article) item).setRead(true);
			database.updateReadFlag(item);
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.READ));
		} else {
			database.updateReadFlag(item);
			IAggregatorItem[] children = getChildren(item);
			for (IAggregatorItem child : children) {
				if (child instanceof Article)
					notifyListerners(new AggregatorItemChangedEvent(child,
							FeedChangeEventType.READ));
			}
		}
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
		Assert.isNotNull(item.getGuid());
		if (database.getItem(item.getGuid()) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Removes the specified item from the database.
	 * 
	 * @param element
	 *            The element to be removed.
	 */
	public void remove(IAggregatorItem element) {
		if (element instanceof Feed) {
			sites.remove(((Feed) element).getUUID());
		}
		database.delete(element);
		notifyListerners(new AggregatorItemChangedEvent(element,
				FeedChangeEventType.REMOVED));
	}

	/** The number of milliseconds in a day */
	private final long DAY = 86400000;

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	void cleanUp(Feed site) {
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
	}

	/**
	 * The feed registry does not have a parent so this method will return
	 * <b>null</b>.
	 * 
	 * @return The parent (null).
	 */
	public IAggregatorItem getParentItem() {
		return null;
	}

	/**
	 * Add listener to be notified about feed changes. The added listener will
	 * be notified when feeds are added, removed and when their contents has
	 * changed.
	 * 
	 * @param listener
	 *            The listener to be notified
	 */
	public void addFeedListener(FeedListener listener) {
		feedListeners.add(listener);
	}

	public void removeFeedListener(FeedListener listener) {
		feedListeners.remove(listener);
	}

	/**
	 * Notify feed listeners about the feed change. If the feed change was
	 * caused by a update, a new update is scheduled.
	 * 
	 * @param event
	 *            The feed change event with details
	 */
	public void notifyListerners(AggregatorItemChangedEvent event) {
		if (AggregatorPlugin.getDefault().isDebugging()) {
			System.out.println("[DEBUG] " + event); //$NON-NLS-1$
		}
		for (FeedListener listener : feedListeners) {
			listener.feedChanged(event);
		}
	}

	public FeedCollection getRegistry() {
		return this;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
}
