package no.resheim.aggregator.data;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Tests for no.resheim.aggregator.data"); //$NON-NLS-1$
		// $JUnit-BEGIN$
		suite.addTestSuite(FolderTest.class);
		suite.addTestSuite(PersistentCollectionTest.class);
		suite.addTestSuite(VolatileCollectionTest.class);
		// $JUnit-END$
		return suite;
	}

}
