package no.resheim.aggregator;

import no.resheim.aggregator.data.FeedCollection;

public interface IFeedCollectionEventListener {
	/**
	 * Fired only once just after a feed collection has been created, it's
	 * storage is ready and any declared feeds has been added to it.
	 * 
	 * @param collection
	 *            the initialized collection
	 */
	public void collectionInitialized(FeedCollection collection);

}
