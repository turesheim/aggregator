package no.resheim.aggregator.data;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * 
 */
public interface IAggregatorItem {

	/**
	 * Sets a new title for the item.
	 * 
	 * @param title
	 *            the new title
	 */
	public abstract void setTitle(String title);

	/**
	 * Returns the title of the item.
	 * 
	 * @return the title string
	 */
	public abstract String getTitle();

	/**
	 * Returns the parent of the item.
	 * 
	 * @return the parent item
	 */
	public abstract IAggregatorItem getParent();

}
