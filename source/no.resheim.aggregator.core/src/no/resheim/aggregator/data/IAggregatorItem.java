package no.resheim.aggregator.data;

import java.util.UUID;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * 
 */
public interface IAggregatorItem {

	/**
	 * Returns the unique identifier for this aggregator item.
	 * 
	 * @return The item identifier
	 */
	public abstract UUID getUUID();

	public abstract UUID getParentUUID();

	/**
	 * Returns the feed registry instance of which this item belongs.
	 * 
	 * @return The feed registry
	 */
	public abstract FeedCollection getCollection();

	/**
	 * Sets a new title for the item.
	 * 
	 * @param title
	 *            The new title
	 */
	public abstract void setTitle(String title);

	/**
	 * Returns the title of the item.
	 * 
	 * @return The title string
	 */
	public abstract String getTitle();

	/**
	 * Sets the order of the item.
	 * 
	 * @param ordering
	 *            the new order of the item
	 */
	public abstract void setOrdering(int ordering);

	public abstract void setCollection(FeedCollection registry);

	/**
	 * Returns the order of the item.
	 * 
	 * @return the item order
	 */
	public abstract int getOrdering();

	/**
	 * 
	 * @param parent_uuid
	 */
	public void setParentUUID(UUID parent_uuid);

}
