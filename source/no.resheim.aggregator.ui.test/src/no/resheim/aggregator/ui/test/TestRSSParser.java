package no.resheim.aggregator.ui.test;

import junit.framework.TestCase;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Feed.Archiving;
import no.resheim.aggregator.core.data.Feed.UpdatePeriod;

public class TestRSSParser extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private Feed createNewFeed(FeedCollection parent, String title) {
		Feed feed = new Feed();
		// Initialise with default values from the preference store.
		// This is done here as the preference system is a UI component.
		feed.setTitle(title);
		feed.setURL("test://"); //$NON-NLS-1$
		feed.setArchiving(Archiving.KEEP_ALL);
		feed.setArchivingDays(30);
		feed.setArchivingItems(1000);
		feed.setUpdateInterval(0);
		feed.setUpdatePeriod(UpdatePeriod.DAYS);
		return feed;
	}
}
