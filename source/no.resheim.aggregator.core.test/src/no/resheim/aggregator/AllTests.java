package no.resheim.aggregator;

import junit.framework.Test;
import junit.framework.TestSuite;
import no.resheim.aggregator.data.FolderTest;
import no.resheim.aggregator.data.PersistentCollectionTest;
import no.resheim.aggregator.data.VolatileCollectionTest;
import no.resheim.aggregator.data.rss.internal.FeedParserTest;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for no.resheim.aggregator.data"); //$NON-NLS-1$
		// $JUnit-BEGIN$
		suite.addTestSuite(FolderTest.class);
		suite.addTestSuite(PersistentCollectionTest.class);
		suite.addTestSuite(VolatileCollectionTest.class);
		suite.addTestSuite(FeedParserTest.class);
		// $JUnit-END$
		return suite;
	}

}
