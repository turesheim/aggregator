package no.resheim.aggregator.core.synch;

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;

public interface IFeedSynchronizer {

	/** Sets the feed to synchronise */
	public void setFeed(Feed feed);

	/** Sets the collection to synchronise */
	public void setCollection(FeedCollection collection);

}
