package no.resheim.aggregator.data;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.test.TestUtils;

public class VolatileCollectionTest extends AbstractCollectionTest {
	@Override
	protected AggregatorCollection getCollection() {
		return AggregatorPlugin.getDefault().getFeedCollection(TestUtils.VOLATILE_COLLECTION_ID);
	}

}
