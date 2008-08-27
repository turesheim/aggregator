package no.resheim.aggregator.data;

import java.util.UUID;

import junit.framework.TestCase;
import no.resheim.aggregator.TestUtils;
import no.resheim.aggregator.data.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.data.internal.InternalArticle;
import no.resheim.aggregator.data.internal.InternalFolder;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public abstract class AbstractCollectionTest extends TestCase {

	private Feed feed;
	private Folder feedFolder;

	/**
	 * Used to obtain a correctly configured feed collection.
	 * 
	 * @return the feed collection
	 */
	protected abstract FeedCollection getCollection();

	protected void compareAggregatorItems(AggregatorItem item_a,
			AggregatorItem item_b) {
		if (!item_a.getUUID().equals(item_a.getUUID())) {
			failNotEquals("Unique identifier differs", item_b.getUUID(), item_a //$NON-NLS-1$
					.getUUID());
		}
		if (!item_a.getMarks().equals(item_a.getMarks())) {
			failNotEquals("Marks identifier differs", item_b.getMarks(), item_a //$NON-NLS-1$
					.getMarks());
		}
		if (item_a.getOrdering() != item_a.getOrdering()) {
			failNotEquals(
					"Ordering identifier differs", item_b.getOrdering(), item_a //$NON-NLS-1$
							.getOrdering());
		}
		if (!item_a.getParent().equals(item_a.getParent())) {
			failNotEquals("Parent differs", item_b.getParent(), item_a //$NON-NLS-1$
					.getParent());
		}
		if (!item_a.getTitle().equals(item_a.getTitle())) {
			failNotEquals("Title differs", item_b.getTitle(), item_a //$NON-NLS-1$
					.getTitle());
		}
		if (item_a.isHidden() != item_b.isHidden()) {
			failNotEquals("Hidden differs", item_b.isHidden(), item_a //$NON-NLS-1$
					.isHidden());
		}
	}

	/**
	 * Tests that the declared collection can be retrieved from the plug-in.
	 */
	@Test
	public final void testGetCollection() {
		FeedCollection collection = getCollection();
		if (collection == null) {
			fail("Persistent collection not declared or could not be retrieved"); //$NON-NLS-1$
		}
	}

	@Test
	public final void testAddFolder() throws CoreException {
		// Create the folder instance
		InternalFolder folder_a = new InternalFolder(getCollection(), UUID
				.randomUUID());
		// Add it to the collection
		getCollection().addNew(folder_a);
		// See that it's available in from the storage
		AggregatorItem item = getCollection().getChildAt(0);
		if (item == null) {
			fail("Folder item could not be retrieved"); //$NON-NLS-1$
		}
		// And that the storage item is a folder
		if (!(item instanceof Folder)) {
			fail("Returned item is not folder"); //$NON-NLS-1$
		}
		// Compare the two
		Folder folder_b = (Folder) item;
		compareAggregatorItems(folder_a, folder_b);
	}

	/**
	 * Assumes that the folder item created in the previous method still exists
	 * and attempts to delete it. Fails if the item did not exist and if it
	 * still exists after deleting it.
	 * 
	 * @throws CoreException
	 */
	public final void testDeleteFolder() throws CoreException {
		FeedCollection collection = getCollection();
		// Assume the folder was added in the method above
		AggregatorItem item = collection.getChildAt(0);
		if (item == null) {
			fail("Folder item could not be retrieved"); //$NON-NLS-1$
		}
		item.parent.delete(item);
		item = collection.getChildAt(0);
		if (item != null) {
			fail("Folder item was not deleted"); //$NON-NLS-1$
		}

	}

	public final void testAddFeed() throws CoreException {
		// Create the feed
		feed = TestUtils.createNewFeed("New feed"); //$NON-NLS-1$
		// This should also add a new folder automatically as we did not specify
		// the location for the feed.
		getCollection().addNew(feed);
		AggregatorItem item = getCollection().getChildAt(0);
		if (item == null) {
			fail("Feed item could not be retrieved"); //$NON-NLS-1$
		}
		// And that the storage item is a folder
		if (!(item instanceof Folder)) {
			fail("Returned item is not feed"); //$NON-NLS-1$
		}
	}

	/**
	 * Tests the creation of a folder associated with a feed.
	 * 
	 * @throws CoreException
	 */
	public final void testAddFeedFolder() throws CoreException {
		// Create the folder instance
		InternalFolder folder_a = new InternalFolder(getCollection(), UUID
				.randomUUID());
		// Add it to the collection
		getCollection().addNew(folder_a);
		// See that it's available in from the storage
		AggregatorItem item = getCollection().getChildAt(0);
		if (item == null) {
			fail("Folder item could not be retrieved"); //$NON-NLS-1$
		}
		// And that the storage item is a folder
		if (!(item instanceof Folder)) {
			fail("Returned item is not folder"); //$NON-NLS-1$
		}
	}

	/**
	 * Tests the deletion of the folder item created in the previous method.
	 */
	public final void testDeleteFeedFolder() {
		fail("Not implemented yet");
	}

	public final void testAddArticle() throws CoreException {
		FeedCollection collection = getCollection();
		// Create the feed
		Feed feed = TestUtils.createNewFeed("Feed title"); //$NON-NLS-1$
		// This should also add a new folder automatically as we did not specify
		// the location for the feed.
		Folder folder = collection.addNew(feed);
		// Create the article
		InternalArticle article_a = new InternalArticle(
				(AggregatorItemParent) folder, UUID.randomUUID(), feed
						.getUUID());
		article_a.setGuid("myGUID"); //$NON-NLS-1$
		// Add it to the collection
		collection.addNew(article_a);
		// See that it's there (at position 0 in the folder)
		AggregatorItem item = folder.getChildAt(0);
		if (item == null) {
			fail("Article item could not be retrieved"); //$NON-NLS-1$
		}
		// And that the item is a folder
		if (!(item instanceof Article)) {
			fail("Returned item is not article"); //$NON-NLS-1$
		}
		Article folder_b = (Article) item;
		// Compare the basics
		compareAggregatorItems(article_a, folder_b);
	}

	/**
	 * Note that this test takes significantly longer time when we have change
	 * listeners as these most likely will update the UI.
	 */
	public final void testAdd1000Articles() {
		FeedCollection collection = getCollection();
		Feed feed = TestUtils.createNewFeed("** Test feed **"); //$NON-NLS-1$
		Folder folder = collection.addNew(feed);
		collection.notifyListerners(new AggregatorItemChangedEvent(feed,
				FeedChangeEventType.UPDATING));
		for (int a = 0; a < 1000; a++) {
			InternalArticle article = new InternalArticle(folder, UUID
					.randomUUID(), feed.getUUID());
			article.setTitle("Article #" + a); //$NON-NLS-1$
			article.setGuid(article.getUUID().toString());
			article.setDescription(""); //$NON-NLS-1$
			article.setLink(""); //$NON-NLS-1$
			collection.addNew(article);
		}
		collection.notifyListerners(new AggregatorItemChangedEvent(feed,
				FeedChangeEventType.UPDATED));
	}

	public final void testGet1000Articles() {

	}
}
