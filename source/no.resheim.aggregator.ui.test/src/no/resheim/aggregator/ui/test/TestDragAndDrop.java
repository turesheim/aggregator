package no.resheim.aggregator.ui.test;

import java.util.EnumSet;

import junit.framework.TestCase;
import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.ui.FeedTreeViewer;
import no.resheim.aggregator.core.ui.FeedViewerContentProvider;

import org.eclipse.swt.widgets.Shell;

public class TestDragAndDrop extends TestCase {

	public void setUp() throws Exception {
		FeedCollection collection = AggregatorPlugin.getDefault()
				.getFeedCollection(TestPlugin.ID_TEST_COLLECTION);
		System.out.println(collection);
		Shell shell = new Shell();
		FeedTreeViewer viewer = new FeedTreeViewer(shell);
		viewer.setContentProvider(new FeedViewerContentProvider(EnumSet
				.allOf(ItemType.class)));
		viewer.setInput(collection);
		System.out.println("done");
	}

	public void tearDown() throws Exception {
	}

	public void testMoveUpBefore() throws Exception {

	}

	public void testMoveUpAfter() throws Exception {

	}

	public void testMoveDownBefore() throws Exception {

	}

	public void testMoveDownAfter() throws Exception {

	}

	public void testMoveInto() throws Exception {

	}

}
