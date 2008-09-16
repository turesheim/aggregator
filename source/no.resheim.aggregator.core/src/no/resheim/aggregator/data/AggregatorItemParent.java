package no.resheim.aggregator.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.data.Feed.Archiving;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

public abstract class AggregatorItemParent extends AggregatorItem {
	/**
	 * Note that because of the cache we're guaranteed that the same instance is
	 * retrieved when an item is requested. Thus we won't get into trouble
	 * because a viewer does not have the same instance as the action that
	 * manipulated the item. TODO: Can we make totally sure using a comparator?
	 */
	private ArrayList<AggregatorItem> children;

	/** The number of milliseconds in a day */
	private static final long DAY = 86400000;

	public AggregatorItemParent(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
		children = new ArrayList<AggregatorItem>();
	}

	/**
	 * Removes the child item from the internal list, but does not update the
	 * database. Should only be used when moving from one parent to another.
	 * 
	 * @param item
	 *            the item to remove
	 */
	void internalRemove(AggregatorItem item) {
		synchronized (children) {
			children.remove(item);
		}
	}

	/**
	 * Adds the item to the internal list, but does not update the database.
	 * Should only be used when moving from one parent to another.
	 * 
	 * @param item
	 *            the item to add
	 */
	void internalAdd(AggregatorItem item) {
		synchronized (children) {
			children.add(item);
		}
	}

	/**
	 * Returns the child at the given position starting at 0.
	 * 
	 * @param index
	 * @return
	 * @throws CoreException
	 */
	public AggregatorItem getChildAt(int index) throws CoreException {
		// Check the cache first
		synchronized (children) {
			for (AggregatorItem child : children) {
				if (child.getOrdering() == index)
					return child;
			}
		}
		// If nothing is found we must check the storage
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			AggregatorItem child = storage.getItem(this, index);
			if (child != null) {
				children.add(child);
			}
			return child;
		} finally {
			storage.readLock().unlock();
		}
	}

	/**
	 * Cleans up the parent item using the rules of the specified site. Only
	 * direct children are affected.
	 * 
	 * @param site
	 * @throws CoreException
	 */
	void cleanUp(Feed site) throws CoreException {
		Archiving archiving = site.getArchiving();
		int days = site.getArchivingDays();
		int articles = site.getArchivingItems();
		switch (archiving) {
		case KEEP_ALL:
			// Do nothing as we want to keep all items
			break;
		case KEEP_NEWEST:
			long lim = System.currentTimeMillis() - ((long) days * DAY);
			for (AggregatorItem item : getChildren()) {
				if (item instanceof Article) {
					Article article = (Article) item;
					if (article.isRead()) {
						if (article.getPublicationDate() > 0
								&& article.getPublicationDate() <= lim) {
							delete(article);
						} else if (article.getPublicationDate() == 0
								&& article.addedDate <= lim) {
							delete(article);
						}
					}
				}
			}
			break;
		case KEEP_SOME:
			while (getChildCount() > articles) {
				delete(getChildAt(0));
			}
			break;
		default:
			break;
		}
	}

	protected List<Folder> getDescendingFolders() throws CoreException {
		return getDescendingFolders(this);
	}

	private List<Folder> getDescendingFolders(AggregatorItem item)
			throws CoreException {
		ArrayList<Folder> descendants = new ArrayList<Folder>();
		if (item instanceof AggregatorItemParent) {
			AggregatorItem[] children = ((AggregatorItemParent) item)
					.getChildren();
			for (AggregatorItem aggregatorItem : children) {
				if (aggregatorItem instanceof Folder) {
					descendants.add((Folder) aggregatorItem);
					descendants.addAll(getDescendingFolders(aggregatorItem));
				}
			}
		}
		return descendants;
	}

	/**
	 * Removes the specified item from the collection and underlying database.
	 * 
	 * @param item
	 *            the element to remove
	 * @throws CoreException
	 */
	public IStatus delete(AggregatorItem item) throws CoreException {
		// Remove the item from the cache (if it's there).
		synchronized (children) {
			children.remove(item);
		}
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.writeLock().lock();
			long start = System.currentTimeMillis();
			getCollection().shiftUp((AggregatorItem) item);
			storage.delete((AggregatorItem) item);
			if (item instanceof Folder) {
				UUID feedId = ((Folder) item).getFeedUUID();
				// Make sure we also delete the associated feed instance
				if (feedId != null) {
					storage.delete(getCollection().getFeeds().remove(feedId));
					ISecurePreferences root = SecurePreferencesFactory
							.getDefault().node(
									AggregatorPlugin.SECURE_STORAGE_ROOT);
					ISecurePreferences feedNode = root.node(feedId.toString());
					feedNode.removeNode();
				}
			}
			getCollection().notifyListerners(
					new AggregatorItemChangedEvent(item,
							FeedChangeEventType.REMOVED, System
									.currentTimeMillis()
									- start));
			return Status.OK_STATUS;
		} finally {
			storage.writeLock().unlock();
		}
	}

	/**
	 * Returns the number of items contained within the given parent item.
	 * 
	 * @param parent
	 *            the parent item
	 * @return the number of child items
	 * @throws CoreException
	 */
	public int getChildCount() throws CoreException {
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			return storage.getChildCount(this);
		} finally {
			storage.readLock().unlock();
		}
	}

	/**
	 * Returns all child items. This
	 * 
	 * @param item
	 *            the parent item
	 * @return the child items
	 * @throws CoreException
	 */
	public AggregatorItem[] getChildren() throws CoreException {
		int count = getChildCount();
		AggregatorItem[] items = new AggregatorItem[count];
		for (int p = 0; p < count; p++) {
			items[p] = getChildAt(p);
		}
		return items;
	}
}
