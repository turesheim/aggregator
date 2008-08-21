package no.resheim.aggregator.data;

import java.text.MessageFormat;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public abstract class AggregatorItemParent extends AggregatorItem {

	public AggregatorItemParent(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
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

	public IAggregatorItem getChildAt(int index) throws CoreException {
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			return storage.getItem(this, index);
		} finally {
			storage.readLock().unlock();
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
	public IAggregatorItem[] getChildren() throws CoreException {
		IAggregatorStorage storage = getCollection().getStorage();
		try {
			storage.readLock().lock();
			return storage.getChildren(this);
		} finally {
			storage.readLock().unlock();
		}
	}
}
