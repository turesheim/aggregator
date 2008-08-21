/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.data;

/**
 * This event type is used to add information to feed changed events.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AggregatorItemChangedEvent {

	public enum FeedChangeEventType {
		/** The item has been created */
		CREATED,
		/** Something bad happened */
		FAILED,
		/** The item has been moved */
		MOVED,
		/** The article has been read */
		READ,
		/** The item has been removed */
		REMOVED,
		/** The feed has been restored from the database */
		RESTORED,
		/** The item has been moved as a result of another item being moved */
		SHIFTED,
		/** The item has been updated */
		UPDATED,
		/** The item is being updated */
		UPDATING
	}

	/**
	 * Used in conjunction with the MOVED event type indicating that not only
	 * was the item moved, but it also got a new parent.
	 */
	public static final int NEW_PARENT = 0x1;

	private int details;

	private Object item;

	private int oldOrder;

	private Object oldParent;

	private long time;

	private FeedChangeEventType type;

	public AggregatorItemChangedEvent(Feed feed, FeedChangeEventType type) {
		this.item = feed;
		this.type = type;
	}

	public AggregatorItemChangedEvent(AggregatorItem feed,
			FeedChangeEventType type, int details,
			AggregatorItemParent oldParent, int oldOrder, long time) {
		this(feed, type, details);
		this.oldParent = oldParent;
		this.oldOrder = oldOrder;
		this.time = time;
	}

	public AggregatorItemChangedEvent(AggregatorItem feed,
			FeedChangeEventType type, long time) {
		this.item = feed;
		this.type = type;
		this.time = time;
	}

	public AggregatorItemChangedEvent(AggregatorItem feed,
			FeedChangeEventType type, long time, int details) {
		this(feed, type, time);
		this.details = details;
	}

	public int getDetails() {
		return details;
	}

	public Object getItem() {
		return item;
	}

	public int getOldOrder() {
		return oldOrder;
	}

	public Object getOldParent() {
		return oldParent;
	}

	public FeedChangeEventType getType() {
		return type;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.toString());
		sb.append(": "); //$NON-NLS-1$
		sb.append(item.toString());
		if (time > 0) {
			sb.append(" in "); //$NON-NLS-1$
			sb.append(time);
			sb.append("ms"); //$NON-NLS-1$
		}
		switch (type) {
		case MOVED:
			//			sb.append(MessageFormat.format(" from \"{0},{1}\" to \"{2},{3}\"", //$NON-NLS-1$
			// new Object[] {
			// oldParent, oldOrder, item.getParent(),
			// item.getOrdering()
			// }));
			break;
		default:
			break;
		}
		return sb.toString();
	}
}
