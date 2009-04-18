package no.resheim.aggregator.core.data;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Subscription.Archiving;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;

/**
 * @author torkild
 */
public abstract class AggregatorItemParent extends AggregatorItem {
	/**
	 * Note that because of the cache we're guaranteed that the same instance is
	 * retrieved when an item is requested. Thus we won't get into trouble
	 * because a viewer does not have the same instance as the action that
	 * manipulated the item. TODO: Can we make totally sure using a comparator?
	 * 
	 * @uml.property name="children"
	 */
	private ArrayList<AggregatorItem> children;

	/**
	 * Lock to prevent that the cache is being read/modified by different
	 * threads at the same time.
	 */
	private final ReentrantLock cacheLock = new ReentrantLock();

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
		cacheLock.lock();
		try {
			children.remove(item);
		} finally {
			cacheLock.unlock();
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
		cacheLock.lock();
		try {
			children.add(item);
		} finally {
			cacheLock.unlock();
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
		getCollection().addNew(new AggregatorItem[] { item });
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
		cacheLock.lock();
		try {
			for (AggregatorItem child : children) {
				if (child.getOrdering() == index)
					return child;
			}
		} finally {
			cacheLock.unlock();
		}
		// If nothing is found we must check the storage
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			AggregatorItem child = storage.getChildAt(this, index);
			if (child != null) {
				cacheLock.lock();
				try {
					children.add(child);
				} finally {
					cacheLock.unlock();
				}
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
	public void cleanUp(Subscription site) throws CoreException {
		Archiving archiving = site.getArchiving();
		ArrayList<Article> trashed = new ArrayList<Article>();
		int days = site.getArchivingDays();
		int articles = site.getArchivingItems();
		boolean doTrash = false;
		switch (archiving) {
		case KEEP_ALL:
			// Do nothing as we want to keep all items
			break;
		// Keep only items that are newer than the specified date. The read
		// state of the item is not considered.
		// TODO: Optimise
		case KEEP_NEWEST:
			long lim = System.currentTimeMillis() - ((long) days * DAY);
			for (AggregatorItem item : getChildren(EnumSet.of(ItemType.ARTICLE))) {
				doTrash = false;
				Article article = (Article) item;
				if (site.keepUnread()) {
					if (((Article) item).isRead()) {
						doTrash = considerDate(lim, article);
					}
				} else {
					doTrash = considerDate(lim, article);
				}
				if (doTrash) {
					trashed.add((Article) item);
					item.setFlag(Flag.TRASHED);
				}
			}
			break;
		// Remove the oldest items so that only a specified number of items is
		// kept. Does not consider the read state.
		case KEEP_SOME:
			// FIXME: This method is broken, it does not consider folders
			// properly
			int children = getChildCount(EnumSet.allOf(ItemType.class));
			// Make sure we're not disturbed
			synchronized (this) {
				int position = 0;
				while (children > articles) {
					doTrash = false;
					AggregatorItem item = getChildAt(position);
					// We're at the end
					if (item == null)
						break;
					if (site.keepUnread()) {
						if (((Article) item).isRead()) {
							doTrash = true;
						}
					} else {
						doTrash = true;
					}
					if (doTrash) {
						trashed.add((Article) item);
						item.setFlag(Flag.TRASHED);
						children--;
					}
					position++;
				}
			}
			break;
		default:
			break;
		}
		if (trashed.size() > 0) {
			FeedCollection collection = getCollection();
			Folder trashFolder = collection.getTrashFolder();
			ArrayList<Article> tempTrashed = new ArrayList<Article>();
			// Move each set of consecutive items into the trash folder. We try
			// to do this set-wise as it will save us a lot of processing and
			// updating of the GUI.
			for (Article article : trashed) {
				if (tempTrashed.size() == 0) {
					tempTrashed.add(article);
				} else {
					Article prev = tempTrashed.get(tempTrashed.size() - 1);
					if (prev.getParent().equals(article.getParent())
							&& prev.getOrdering() == article.getOrdering() - 1) {
						tempTrashed.add(article);
					} else {
						Article[] items = tempTrashed
								.toArray(new Article[tempTrashed.size()]);
						collection.move(items, trashFolder);
						collection.notifyListerners(items, EventType.MOVED);
						tempTrashed.clear();
					}
				}
			} // for
			if (tempTrashed.size() > 0) {
				Article[] items = tempTrashed.toArray(new Article[tempTrashed
						.size()]);
				collection.move(items, trashFolder);
				collection.notifyListerners(items, EventType.MOVED);

			}
		}
	}

	private boolean considerDate(long lim, Article article) {
		if (article.getPublicationDate() > 0
				&& article.getPublicationDate() <= lim) {
			return true;
		} else if (article.getPublicationDate() == 0
				&& article.addedDate <= lim) {
			return true;
		}
		return false;
	}

	public List<Folder> getDescendingFolders() throws CoreException {
		return getDescendingFolders(this);
	}

	private List<Folder> getDescendingFolders(AggregatorItem item)
			throws CoreException {
		ArrayList<Folder> descendants = new ArrayList<Folder>();
		if (item instanceof AggregatorItemParent) {
			AggregatorItem[] folders = ((AggregatorItemParent) item)
					.getChildren(EnumSet.of(ItemType.FOLDER));
			for (AggregatorItem aggregatorItem : folders) {
				descendants.add((Folder) aggregatorItem);
				descendants.addAll(getDescendingFolders(aggregatorItem));
			}
		}
		return descendants;
	}

	/**
	 * Moves the item to the trash folder of the associated feed and removes it
	 * from this parent.
	 * 
	 * @param item
	 *            the item to move to the trash folder.
	 * @throws CoreException
	 */
	public void trash(AggregatorItem item) throws CoreException {
		FeedCollection c = getCollection();
		Folder trashFolder = c.getTrashFolder();
		int newPosition = trashFolder.getChildCount(EnumSet
				.allOf(ItemType.class));
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
		cacheLock.lock();
		try {
			children.remove(item);
		} finally {
			cacheLock.unlock();
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
	 * @param types
	 *            the item types to count
	 * @return the number of child items
	 * @throws CoreException
	 */
	public int getChildCount(EnumSet<ItemType> types) throws CoreException {
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			return storage.getChildCount(this, types);
		} finally {
			storage.readLock().unlock();
		}
	}

	/**
	 * Returns all child items or the specified item types.
	 * 
	 * @param types
	 *            the item types to retrieve
	 * @return the child items
	 * @throws CoreException
	 */
	public AggregatorItem[] getChildren(EnumSet<ItemType> types)
			throws CoreException {
		int count = getChildCount(types);
		AggregatorItem[] items = new AggregatorItem[count];
		for (int p = 0; p < count; p++) {
			items[p] = getChildAt(p);
		}
		return items;
	}
}
