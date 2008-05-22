package no.resheim.aggregator.model;

import java.util.UUID;

public interface IAggregatorItem {

	public abstract IAggregatorItem getParent();

	/**
	 * Returns the unique identifier for this aggregator item.
	 * 
	 * @return The item identifier
	 */
	public abstract UUID getUUID();

	/**
	 * Returns the registry instance of which this aggregator item belongs.
	 * 
	 * @return The feed registry
	 */
	public abstract FeedRegistry getRegistry();

	/**
	 * Sets a new title for the aggregator item
	 * 
	 * @param title
	 *            The new title
	 */
	public abstract void setTitle(String title);

	public abstract String getTitle();

}
