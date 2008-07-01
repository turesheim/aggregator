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
	/**
	 * Used in conjunction with the MOVED event type indicating that not only
	 * was the item moved, but it also got a new parent.
	 */
	public static final int NEW_PARENT = 0x1;

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
		/** The item has been moved */
		MOVED,
		/** The item has been moved */
		SHUFFLED,
		/** Something bad happened */
		FAILED
	}

	private IAggregatorItem item;

	private IAggregatorItem oldParent;

	private int oldOrder;

	public int getOldOrder() {
		return oldOrder;
	}

	public IAggregatorItem getOldParent() {
		return oldParent;
	}

	private FeedChangeEventType type;

	private int details;

	public AggregatorItemChangedEvent(IAggregatorItem feed,
			FeedChangeEventType type) {
		this.item = feed;
		this.type = type;
	}

	public AggregatorItemChangedEvent(IAggregatorItem feed,
			FeedChangeEventType type, int details) {
		this(feed, type);
		this.details = details;
	}

	public AggregatorItemChangedEvent(IAggregatorItem feed,
			FeedChangeEventType type, int details, IAggregatorItem oldParent,
			int oldOrder) {
		this(feed, type, details);
		this.oldParent = oldParent;
		this.oldOrder = oldOrder;
	}

	public IAggregatorItem getItem() {
		return item;
	}

	public FeedChangeEventType getType() {
		return type;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(type.toString());
		sb.append(": "); //$NON-NLS-1$
		sb.append(item.toString());
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

	public int getDetails() {
		return details;
	}
}
