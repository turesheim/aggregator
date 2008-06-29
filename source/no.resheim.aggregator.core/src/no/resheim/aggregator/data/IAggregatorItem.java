package no.resheim.aggregator.data;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * 
 */
public interface IAggregatorItem {

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

	public abstract IAggregatorItem getParent();

}
