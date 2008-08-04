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

import java.util.EnumSet;
import java.util.UUID;


/**
 * This type implements the UI presentable information for aggregator items such
 * as articles and folders
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AggregatorUIItem implements IAggregatorItem {

	public enum Mark {
		DONE, FIRST_PRIORITY, IMPORTANT, SECOND_PRIORITY, THIRD_PRIORITY, TODO
	}

	private EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);

	protected int ordering;

	protected AggregatorUIItem parent;

	protected FeedCollection registry;

	protected boolean serialized;

	protected UUID uuid;

	private boolean hidden;

	/** The folder title */
	protected String title;

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param parent
	 */
	public AggregatorUIItem(AggregatorUIItem parent) {
		this.parent = parent;
	}

	public EnumSet<Mark> getMarks() {
		return marks;
	}

	public int getOrdering() {
		return ordering;
	};

	public AggregatorUIItem getParent() {
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

	public void setParent(AggregatorUIItem parent) {
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
