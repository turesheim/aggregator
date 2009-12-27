package no.resheim.aggregator.rcp.macosx;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.core.data.IAggregatorEventListener;

import org.eclipse.ui.IStartup;

/**
 * Connects to the default collection and listens to feed changed events.
 * Whenever an event has taken place the application icon badge will be updated
 * to reflect the number of unread items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CollectionEventListener implements IStartup {

	@Override
	public void earlyStartup() {
		final NSDockTile tile = NSDockTile.getApplicationDockTile();
		final AggregatorCollection collection = AggregatorPlugin.getDefault()
				.getFeedCollection(null);
		collection.addFeedListener(new IAggregatorEventListener() {

			public void aggregatorItemChanged(AggregatorItemChangedEvent event) {
				updateUnreadCount(tile, collection);
			}
		});
		updateUnreadCount(tile, collection);
	}

	private void updateUnreadCount(final NSDockTile tile,
			final AggregatorCollection collection) {
		int count = collection.getUnreadItemCount(collection);
		if (count > 0) {
			tile.setBadgeLabel(Integer.toString(count));
		} else {
			tile.setBadgeLabel("");
		}
	}

}
