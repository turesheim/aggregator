package no.resheim.aggregator;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;

import org.eclipse.core.runtime.Plugin;

public class TestUtils extends Plugin {

	private static TestUtils instance;

	public static TestUtils getDefault() {
		return instance;
	}

	public static final String VOLATILE_COLLECTION_ID = "no.resheim.aggregator.core.test.collection.volatile"; //$NON-NLS-1$

	public TestUtils() {
		instance = this;
	}

	public static Feed createNewFeed(String title) {
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

	public static final String PERSISTENT_COLLECTION_ID = "no.resheim.aggregator.core.test.collection.persistent"; //$NON-NLS-1$
}
