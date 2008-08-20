package no.resheim.aggregator.data.rss.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.TestUtils;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;
import no.resheim.aggregator.rss.internal.FeedParser;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.xml.sax.SAXException;

public class FeedParserTest extends TestCase {

	private static final String FEED_TITLE = "test"; //$NON-NLS-1$

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected FeedCollection getCollection() {
		FeedCollection collection = AggregatorPlugin.getDefault()
				.getFeedCollection(TestUtils.PERSISTENT_COLLECTION_ID);
		if (collection == null) {
			fail("Collection not declared or could not be retrieved"); //$NON-NLS-1$
		}
		return collection;
	}

	public void testIssue_547() throws SAXException,
			ParserConfigurationException, IOException {
		Feed feed = TestUtils.createNewFeed(FEED_TITLE);
		// This will also create a new folder to keep the feed items
		getCollection().addNew(feed);
		FeedParser handler = new FeedParser(getCollection(), feed);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		InputStream is = FileLocator.openStream(TestUtils.getDefault()
				.getBundle(), new Path("/data/issue_547.xml"), true); //$NON-NLS-1$
		parser.parse(is, handler);
		is.close();
		// The folder should now contain 20 items
		IAggregatorItem folder = getCollection().getItemAt(getCollection(), 0);
		int count = getCollection().getChildCount(folder);
		if (count != 20) {
			fail("The associated folder should contain 20 items, not " + count);
		}
	}
}
