package no.resheim.aggregator.data.rss.internal;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.TestUtils;
import no.resheim.aggregator.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorEventListener;
import no.resheim.aggregator.data.AggregatorItemParent;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.EventType;
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

	/**
	 * This test is likely to fail because the database will not have all items
	 * available when the feed has been parsed.
	 * 
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public void testIssue_547() throws SAXException,
			ParserConfigurationException, IOException {
		Feed feed = TestUtils.createNewFeed(FEED_TITLE);
		// This will also create a new folder to keep the feed items
		getCollection().addNew(feed);
		getCollection().addFeedListener(new IAggregatorEventListener() {

			public void aggregatorItemChanged(AggregatorItemChangedEvent event) {
				System.out.println(event.getType());
				if (event.getType().equals(EventType.CHANGED)) {
					// The folder should now contain 20 items
					try {
						AggregatorItemParent folder = (AggregatorItemParent) getCollection()
								.getChildAt(0);
						int count = folder.getChildCount();
						if (count != 20) {
							fail("The associated folder should contain 20 items, not "
									+ count);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
					System.out.println("OK");
				}
			}

		});
		FeedParser handler = new FeedParser(getCollection(), feed);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		InputStream is = FileLocator.openStream(TestUtils.getDefault()
				.getBundle(), new Path("/data/issue_547.xml"), true); //$NON-NLS-1$
		parser.parse(is, handler);
		is.close();
	}
}
