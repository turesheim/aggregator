package no.resheim.aggregator.data;

import no.resheim.aggregator.TestUtils;
import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.FeedCollection;

public class VolatileCollectionTest extends AbstractCollectionTest {
	@Override
	protected FeedCollection getCollection() {
		return AggregatorPlugin.getDefault().getFeedCollection(TestUtils.VOLATILE_COLLECTION_ID);
	}

}
