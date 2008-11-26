package no.resheim.aggregator.core.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Feed.Archiving;

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

	/**
	 * Use a feed collection to create an instance of this type or use the
	 * "internal" subclass constructor.
	 * 
	 * @param parent
	 *            the parent item
	 * @param uuid
	 *            the unique identifier
	 */
	protected AggregatorItemParent(AggregatorItemParent parent, UUID uuid) {
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
	 * Adds a new feed to the database and immediately stores it's data in the
	 * persistent storage.
	 * 
	 * @param feed
	 *            the aggregator item to add
	 * @throws CoreException
	 */
	public void add(AggregatorItem item) throws CoreException {
		internalAdd(item);
		getCollection().addNew(new AggregatorItem[] {
			item
		});
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
	 * 
	 * @param site
	 * @throws CoreException
	 */
	void cleanUp(Feed site) throws CoreException {
		Archiving archiving = site.getArchiving();
		ArrayList<Article> trashed = new ArrayList<Article>();
		int days = site.getArchivingDays();
		int articles = site.getArchivingItems();
		switch (archiving) {
		case KEEP_ALL:
			// Do nothing as we want to keep all items
			break;
		// Keep only items that are newer than the specified date. The read
		// state of the item is not considered.
		case KEEP_NEWEST:
			long lim = System.currentTimeMillis() - ((long) days * DAY);
			for (AggregatorItem item : getChildren()) {
				if (item instanceof Article) {
					Article article = (Article) item;
					if (article.getPublicationDate() > 0
							&& article.getPublicationDate() <= lim) {
						trashed.add((Article) item);
						item.setFlag(Flag.TRASHED);
					} else if (article.getPublicationDate() == 0
							&& article.addedDate <= lim) {
						trashed.add((Article) item);
						item.setFlag(Flag.TRASHED);
					}
				}
			}
			break;
		// Remove the oldest items so that only a specified number of items is
		// kept. Does not consider the read state.
		case KEEP_SOME:
			int children = getChildCount();
			// Make sure we're not disturbed
			synchronized (this) {
				int position = 0;
				while (children > articles) {
					AggregatorItem item = getChildAt(position);
					trashed.add((Article) item);
					item.setFlag(Flag.TRASHED);
					position++;
					children--;
				}
			}
			break;
		default:
			break;
		}
		if (trashed.size() > 0) {
			FeedCollection collection = getCollection();
			Folder trashFolder = collection.getTrashFolder();
			Article[] items = trashed.toArray(new Article[trashed.size()]);
			collection.move(items, trashFolder);
			collection.notifyListerners(items, EventType.MOVED);
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
	 * Moves the item to the trash.
	 * 
	 * @param item
	 * @throws CoreException
	 */
	public void trash(AggregatorItem item) throws CoreException {
		FeedCollection c = getCollection();
		Folder trashFolder = c.getTrashFolder();
		int newPosition = trashFolder.getChildCount();
		trash(item, c, trashFolder, newPosition);
	}

	private void trash(AggregatorItem item, FeedCollection c,
			Folder trashFolder, int newPosition) throws CoreException {
		item.setFlag(Flag.TRASHED);
		c.move(item, item.getParent(), item.getOrdering(), trashFolder,
				newPosition);
	}

	/**
	 * Removes the specified item from the collection and underlying database.
	 * Normally <i>shift</i> should be set to <b>true</b> but in some cases,
	 * such as when removing all child items of a folder this will create a lot
	 * of overhead and is not required.
	 * 
	 * @param item
	 *            the element to remove
	 * @param shift
	 *            whether or not to shift siblings upwards
	 * @throws CoreException
	 */
	public IStatus deleteChild(AggregatorItem item, boolean shift)
			throws CoreException {
		// Remove the item from the cache (if it's there).
		synchronized (children) {
			children.remove(item);
		}
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.writeLock().lock();
			if (shift)
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
