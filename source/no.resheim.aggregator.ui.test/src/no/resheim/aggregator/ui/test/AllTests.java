package no.resheim.aggregator.ui.test;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(
				"Test for no.resheim.aggregator.ui.test");
		// $JUnit-BEGIN$
		suite.addTestSuite(TestDragAndDrop.class);
		suite.addTestSuite(TestRSSParser.class);
		// $JUnit-END$
		return suite;
	}

}
