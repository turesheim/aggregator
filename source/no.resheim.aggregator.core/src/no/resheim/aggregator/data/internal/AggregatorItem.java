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

	protected UUID uuid;
	protected IAggregatorItem parent;
	protected FeedCollection registry;
	protected int fOrdering;

	private EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);

	/**
	 * @param parent
	 */
	public AggregatorItem(IAggregatorItem parent) {
		this.parent = parent;
	}

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

	public FeedCollection getCollection() {
		return registry;
	}

	public void setCollection(FeedCollection registry) {
		this.registry = registry;
	}

	public IAggregatorItem getParent() {
		return parent;
	}

	public void setParent(IAggregatorItem parent) {
		this.parent = parent;
	}

}
