package no.resheim.aggregator.data;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;

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

	public AggregatorItemParent(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
		children = new ArrayList<AggregatorItem>();
	}

	private FeedCollection getCollection() throws CoreException {
		AggregatorItem p = this;
		AggregatorItem o = p;
		while (!(p instanceof FeedCollection)) {
			p = p.getParent();
			if (p == null) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								AggregatorPlugin.PLUGIN_ID,
								MessageFormat
										.format(
												"Aggregator item {0} does not have a parent", new Object[] { o}))); //$NON-NLS-1$
			}
			o = p;
		}
		return (FeedCollection) p;
	}

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
				UUID feedId = ((Folder) item).getFeed();
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
	 * Returns the child items of the given parent item.
	 * 
	 * @param item
	 *            the parent item
	 * @return the child items
	 * @throws CoreException
	 */
	public AggregatorItem[] getChildren() throws CoreException {
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			return storage.getChildren(this);
		} finally {
			storage.readLock().unlock();
		}
	}
}
