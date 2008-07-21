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
package no.resheim.aggregator.data.internal;

import java.util.EnumSet;
import java.util.UUID;

import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;

/**
 * This implementation is internal and is not intended to be used by clients.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AggregatorItem implements IAggregatorItem {

	public enum Mark {
		DONE, FIRST_PRIORITY, IMPORTANT, SECOND_PRIORITY, THIRD_PRIORITY, TODO
	}

	private EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);

	protected int ordering;

	protected IAggregatorItem parent;

	protected FeedCollection registry;

	protected boolean serialized;

	protected UUID uuid;

	/**
	 * @param parent
	 */
	public AggregatorItem(IAggregatorItem parent) {
		this.parent = parent;
	}

	public EnumSet<Mark> getMarks() {
		return marks;
	}

	public int getOrdering() {
		return ordering;
	};

	public IAggregatorItem getParent() {
		return parent;
	}

	/**
	 * Returns the identifier of this feed item.
	 * 
	 * @return
	 */
	public UUID getUUID() {
		return uuid;
	}

	public boolean isSerialized() {
		return serialized;
	}

	public void setMarks(EnumSet<Mark> mark) {
		this.marks = mark;
	}

	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}

	public void setParent(IAggregatorItem parent) {
		this.parent = parent;
	}

	/**
	 * Marks the item as serialised. This is done whenever serialised or
	 * deserialised such as when read from a database.
	 * 
	 * @param serialized
	 */
	public void setSerialized(boolean serialized) {
		this.serialized = serialized;
	}

	/**
	 * Sets the identifier of this feed item.
	 * 
	 * @param uuid
	 */
	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

}
