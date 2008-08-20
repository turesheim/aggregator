package no.resheim.aggregator.data;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.TestUtils;

public class VolatileCollectionTest extends AbstractCollectionTest {
	@Override
	protected FeedCollection getCollection() {
		return AggregatorPlugin.getDefault().getFeedCollection(TestUtils.VOLATILE_COLLECTION_ID);
	}

}
