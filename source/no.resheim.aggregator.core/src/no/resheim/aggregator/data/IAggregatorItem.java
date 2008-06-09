package no.resheim.aggregator.data;

import java.util.UUID;

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
	public abstract FeedCollection getRegistry();

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

	public abstract void setOrdering(long ordering);

	public abstract void setRegistry(FeedCollection registry);

	public abstract long getOrdering();

}
