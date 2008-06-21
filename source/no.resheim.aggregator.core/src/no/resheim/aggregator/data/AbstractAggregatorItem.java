package no.resheim.aggregator.data;

import java.util.EnumSet;
import java.util.UUID;

public abstract class AbstractAggregatorItem implements IAggregatorItem {

	protected UUID uuid;
	protected UUID parent_uuid;
	protected IAggregatorItem parent;
	protected FeedCollection registry;
	protected int fOrdering;

	private EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);

	public enum Mark {
		FIRST_PRIORITY, SECOND_PRIORITY, THIRD_PRIORITY, TODO, IMPORTANT
	};

	public EnumSet<Mark> getMarks() {
		return marks;
	}

	public void setMarks(EnumSet<Mark> mark) {
		this.marks = mark;
	}

	public void setOrdering(int ordering) {
		fOrdering = ordering;
	}

	public int getOrdering() {
		return fOrdering;
	}

	/**
	 * Returns the identifier of this feed item.
	 * 
	 * @return
	 */
	public UUID getUUID() {
		return uuid;
	}

	/**
	 * Sets the identifier of this feed item.
	 * 
	 * @param uuid
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * Returns the identifier of the parent feed item. For stories this would
	 * normally be a category or a feed.
	 * 
	 * @return
	 */
	public UUID getParentUUID() {
		return parent_uuid;
	}

	public void setParentUUID(UUID parent_uuid) {
		this.parent_uuid = parent_uuid;
	}

	// XXX: Should be not need to expose this
	public FeedCollection getRegistry() {
		return registry;
	}

	public void setRegistry(FeedCollection registry) {
		this.registry = registry;
	}

	public IAggregatorItem getParent() {
		return parent;
	}

}
