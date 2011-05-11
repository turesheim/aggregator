package no.resheim.aggregator.data.rss.internal;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import junit.framework.TestCase;
import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.IAggregatorEventListener;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.rss.internal.FeedParser;
import no.resheim.aggregator.core.test.TestUtils;

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

	protected AggregatorCollection getCollection() {
		AggregatorCollection collection = AggregatorPlugin.getDefault()
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
		Subscription feed = TestUtils.createNewFeed(FEED_TITLE);
		// This will also create a new folder to keep the feed items
		getCollection().addNew(feed);
		getCollection().addFeedListener(new IAggregatorEventListener() {

			public void aggregatorItemChanged(AggregatorItemChangedEvent event) {
				System.out.println(event.getType());
				if (event.getType().equals(EventType.CHANGED)) {
					// The folder should now contain 20 items
					try {
						AggregatorItemParent folder = (AggregatorItemParent) getCollection()
								.getChildAt(EnumSet.allOf(ItemType.class), 0);
						int count = folder.getChildCount(EnumSet
								.allOf(ItemType.class));
						if (count != 20) {
							fail("The associated folder should contain 20 items, not " //$NON-NLS-1$
									+ count);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
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
