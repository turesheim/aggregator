/**
 * 
 */
package no.resheim.aggregator.data;


import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.test.TestUtils;

import org.junit.After;
import org.junit.Before;

/**
 * @author torkild
 * 
 */
public class PersistentCollectionTest extends AbstractCollectionTest {
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Override
	protected AggregatorCollection getCollection() {
		return AggregatorPlugin.getDefault().getFeedCollection(TestUtils.PERSISTENT_COLLECTION_ID);
	}
}
