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
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class AggregatorUIItem implements IAggregatorItem {

	public enum Mark {
		DONE, FIRST_PRIORITY, IMPORTANT, SECOND_PRIORITY, THIRD_PRIORITY, TODO
	}

	private EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);

	protected int ordering;

	protected ParentingAggregatorItem parent;

	protected boolean serialized;

	protected UUID uuid = null;

	private boolean hidden = false;

	/** The folder title */
	protected String title = ""; //$NON-NLS-1$

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param parent
	 */
	public AggregatorUIItem(ParentingAggregatorItem parent, UUID uuid) {
		this.parent = parent;
		this.uuid = uuid;
	}

	public EnumSet<Mark> getMarks() {
		return marks;
	}

	public int getOrdering() {
		return ordering;
	};

	public ParentingAggregatorItem getParent() {
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

	public void setMarks(EnumSet<Mark> mark) {
		this.marks = mark;
	}

	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}

	/**
	 * Used to set the new parent item of the aggregator item.
	 * 
	 * @param parent
	 *            the new parent item
	 */
	public void setParent(ParentingAggregatorItem parent) {
		this.parent = parent;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
