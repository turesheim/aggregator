package no.resheim.aggregator.data;

import java.util.EnumSet;
import java.util.UUID;

import junit.framework.TestCase;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.test.TestUtils;

import org.eclipse.core.runtime.CoreException;
import org.junit.Test;

public abstract class AbstractCollectionTest extends TestCase {

	private Subscription feed;

	/**
	 * Used to obtain a correctly configured feed collection.
	 * 
	 * @return the feed collection
	 */
	protected abstract AggregatorCollection getCollection();

	protected void compareAggregatorItems(AggregatorItem item_a,
			AggregatorItem item_b) {
		if (!item_a.getUUID().equals(item_a.getUUID())) {
			failNotEquals("Unique identifier differs", item_b.getUUID(), item_a //$NON-NLS-1$
					.getUUID());
		}
		if (!item_a.getFlags().equals(item_a.getFlags())) {
			failNotEquals("Marks identifier differs", item_b.getFlags(), item_a //$NON-NLS-1$
					.getFlags());
		}

		if (!item_a.getParent().equals(item_a.getParent())) {
			failNotEquals("Parent differs", item_b.getParent(), item_a //$NON-NLS-1$
					.getParent());
		}
		if (!item_a.getTitle().equals(item_a.getTitle())) {
			failNotEquals("Title differs", item_b.getTitle(), item_a //$NON-NLS-1$
					.getTitle());
		}
		if (item_a.isSystem() != item_b.isSystem()) {
			failNotEquals("System differs", item_b.isSystem(), item_a //$NON-NLS-1$
					.isSystem());
		}
	}

	/**
	 * Tests that the declared collection can be retrieved from the plug-in.
	 */
	@Test
	public final void testGetCollection() {
		AggregatorCollection collection = getCollection();
		if (collection == null) {
			fail("Persistent collection not declared or could not be retrieved"); //$NON-NLS-1$
		}
	}

	@Test
	public final void testAddFolder() throws CoreException {
		// Create the folder instance
		Folder folder_a = new Folder(getCollection(), UUID.randomUUID());
		// Add it to the collection
		getCollection().addNew(new Folder[] { folder_a });
		// See that it's available in from the storage
		AggregatorItem item = getCollection().getChildAt(
				EnumSet.allOf(ItemType.class), 0);
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
		AggregatorCollection collection = getCollection();
		// Assume the folder was added in the method above
		AggregatorItem item = collection.getChildAt(EnumSet
				.allOf(ItemType.class), 0);
		if (item == null) {
			fail("Folder item could not be retrieved"); //$NON-NLS-1$
		}
		item.getParent().trash(item);
		item = collection.getChildAt(EnumSet.allOf(ItemType.class), 0);
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
		AggregatorItem item = getCollection().getChildAt(
				EnumSet.allOf(ItemType.class), 0);
		// Remove the folder.
		getCollection().trash(item);

		// Compare
		if (item == null) {
			fail("Feed folder could not be retrieved"); //$NON-NLS-1$
		}
		// And that the storage item is a folder
		if (!(item instanceof Folder)) {
			fail("Returned item is not feed folder"); //$NON-NLS-1$
		}
	}

	public final void testAddArticle() throws CoreException {
		AggregatorCollection collection = getCollection();
		// Create the feed
		Subscription feed = TestUtils.createNewFeed("Feed title"); //$NON-NLS-1$
		// This should also add a new folder automatically as we did not specify
		// the location for the feed.
		Folder folder = collection.addNew(feed);
		// Create the article
		Article article_a = new Article((AggregatorItemParent) folder, UUID
				.randomUUID(), feed.getUUID());
		article_a.setGuid("myGUID"); //$NON-NLS-1$
		// Add it to the collection
		collection.addNew(new Article[] { article_a });
		// See that it's there (at position 0 in the folder)
		AggregatorItem item = folder.getChildAt(EnumSet.allOf(ItemType.class),
				0);
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
	 * 
	 * @throws CoreException
	 */
	public final void testAdd1000Articles() throws CoreException {
		AggregatorCollection collection = getCollection();
		Subscription feed = TestUtils.createNewFeed("1000 articles"); //$NON-NLS-1$
		Folder folder = collection.addNew(feed);
		for (int a = 0; a < 1000; a++) {
			Article article = new Article(folder, UUID.randomUUID(), feed
					.getUUID());
			article.setTitle("Article #" + a); //$NON-NLS-1$
			article.setGuid(article.getUUID().toString());
			article.internalSetText(""); //$NON-NLS-1$
			article.setLink(""); //$NON-NLS-1$
			collection.addNew(new Article[] { article });
		}
	}

	public final void testGetThe1000Articles() throws CoreException {
		AggregatorItem folder = getCollection().getChildAt(
				EnumSet.allOf(ItemType.class), 0);
		System.out.println(folder);
		for (int a = 0; a < 1000; a++) {
			AggregatorItem item = ((Folder) folder).getChildAt(EnumSet
					.allOf(ItemType.class), 0);
			System.out.println(item);
			if (item == null)
				fail("Retrieved item is null"); //$NON-NLS-1$
		}
	}

	public final void testGetThe1000ArticlesAgain() throws CoreException {
		testGetThe1000Articles();
	}
}
