package no.resheim.aggregator.data;

/**
 * This event type is used to add information to feed changed events.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AggregatorItemChangedEvent {
	public enum FeedChangeEventType {
		/** The feed has been restored from the database */
		RESTORED,
		/** The item has been created */
		CREATED,
		/** The item has been removed */
		REMOVED,
		/** The item has been updated */
		UPDATED,
		/** The article has been read */
		READ,
		/** The item is being updated */
		UPDATING,
		/** The item was moved */
		MOVED,
		/** Something bad happened */
		FAILED
	}

	private IAggregatorItem oldItem;
	private IAggregatorItem feed;
	private FeedChangeEventType type;
	private int oldOrder;

	public AggregatorItemChangedEvent(IAggregatorItem feed,
			FeedChangeEventType type) {
		this.feed = feed;
		this.type = type;
	}

	public AggregatorItemChangedEvent(IAggregatorItem feed,
			FeedChangeEventType type, int oldOrder) {
		this(feed, type);
		this.oldOrder = oldOrder;
	}

	public IAggregatorItem getItem() {
		return feed;
	}

	public FeedChangeEventType getType() {
		return type;
	}

	public void setType(FeedChangeEventType type) {
		this.type = type;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.toString());
		sb.append(": "); //$NON-NLS-1$
		sb.append(feed.toString());
		return sb.toString();
	}

	public int getOldOrder() {
		return oldOrder;
	}
}
