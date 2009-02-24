package no.resheim.aggregator.data;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.test.TestUtils;

public class VolatileCollectionTest extends AbstractCollectionTest {
	@Override
	protected FeedCollection getCollection() {
		return AggregatorPlugin.getDefault().getFeedCollection(TestUtils.VOLATILE_COLLECTION_ID);
	}

}
