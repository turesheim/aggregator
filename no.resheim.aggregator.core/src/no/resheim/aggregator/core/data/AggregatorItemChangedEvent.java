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
package no.resheim.aggregator.core.data;

/**
 * This event type is used to add information to feed changed events.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AggregatorItemChangedEvent {

	/**
	 * @author torkild
	 */
	public enum EventType {
		CREATED, MOVED, REMOVED, CHANGED
	}

	/**
	 * Used in conjunction with the MOVED event type indicating that not only
	 * was the item moved, but it also got a new parent.
	 */
	public static final int NEW_PARENT = 0x1;

	private Object[] items;

	private EventType type;

	/**
	 * 
	 * @param items
	 *            the affected items
	 * @param type
	 *            the type of event
	 */
	public AggregatorItemChangedEvent(Object[] items, EventType type) {
		this.items = items;
		this.type = type;
	}

	public Object[] getItems() {
		return items;
	}

	public EventType getType() {
		return type;
	}
}
