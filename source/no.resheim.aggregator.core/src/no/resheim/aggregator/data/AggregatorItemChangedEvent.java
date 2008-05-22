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
		/** The feed has been created */
		CREATED,
		/** The feed has been removed */
		REMOVED,
		/** The feed has been updated */
		UPDATED,
		/** The feed item has been read */
		READ,
		/** The feed is being updated */
		UPDATING, MOVED, FAILED
	}

	private IAggregatorItem feed;
	private FeedChangeEventType type;

	public AggregatorItemChangedEvent(IAggregatorItem feed,
			FeedChangeEventType type) {
		this.feed = feed;
		this.type = type;
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
}
