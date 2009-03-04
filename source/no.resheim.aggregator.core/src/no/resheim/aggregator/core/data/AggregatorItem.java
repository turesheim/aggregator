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

import java.util.EnumSet;
import java.util.UUID;

import org.eclipse.core.runtime.CoreException;

/**
 * This type implements the UI presentable information for aggregator items such
 * as articles and folders
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class AggregatorItem {
	/**
	 * The type of aggregator item. This enumeration is normally used when
	 * wanting to define a set of item types to handle. In some situations only
	 * folders are wanted, in others all types are wanted.
	 */
	public enum ItemType {
		FOLDER, ARTICLE
	};

	/**
	 * Flags are for internal use
	 */
	public enum Flag {
		/** The item is protected from deletion */
		PROTECTED,
		/** The item is a trash folder */
		TRASH,
		/** The item has been trashed */
		TRASHED,
	}

	/**
	 * "Marks" can be used by the user to mark the aggregator item in question.
	 * <code>Flags</code> are used to for system marking.
	 */
	public enum Mark {
		FIRST_PRIORITY, IMPORTANT, NONE, SECOND_PRIORITY, THIRD_PRIORITY, TODO
	}

	private EnumSet<Flag> fFlags = EnumSet.noneOf(Flag.class);

	private Mark fMark = Mark.NONE;

	private boolean fSystem = false;

	/**
	 * @uml.property name="ordering"
	 */
	protected int ordering;

	/**
	 * @uml.property name="parent"
	 * @uml.associationEnd
	 */
	protected AggregatorItemParent parent;

	/**
	 * The folder title
	 * 
	 * @uml.property name="title"
	 */
	protected String title = ""; //$NON-NLS-1$

	/**
	 * The unique identifier of the item.
	 */
	protected UUID uuid = null;

	/**
	 * @param parent
	 */
	protected AggregatorItem(AggregatorItemParent parent, UUID uuid) {
		this.parent = parent;
		this.uuid = uuid;
	}

	/**
	 * Iterates upwards in order to find the collection instance for the item
	 * and returns this.
	 * 
	 * @return the collection of the item.
	 * @throws CoreException
	 */
	public FeedCollection getCollection() throws CoreException {
		AggregatorItem p = this;
		while (!(p instanceof FeedCollection)) {
			p = p.getParent();
		}
		return (FeedCollection) p;
	}

	/**
	 * Returns the system flags of this aggregator item.
	 * 
	 * @return the flags
	 */
	public EnumSet<Flag> getFlags() {
		return fFlags;
	}

	/**
	 * Returns the user marks of this aggregator item.
	 * 
	 * @return the marks
	 */
	public Mark getMark() {
		return fMark;
	}

	/**
	 * @return
	 * @uml.property name="ordering"
	 */
	public int getOrdering() {
		return ordering;
	}

	/**
	 * The parent item may be <code>null</code> if the item has just been
	 * created and is not assigned a parent instance yet, or if the item is a
	 * collection. Collections do not have parent items.
	 * 
	 * @return the parent item or <code>null</code>
	 * @uml.property name="parent"
	 */
	public AggregatorItemParent getParent() {
		return parent;
	}

	/**
	 * @return
	 * @uml.property name="title"
	 */
	public String getTitle() {
		return title;
	};

	/**
	 * Returns the identifier of this feed item.
	 * 
	 * @return
	 */
	public UUID getUUID() {
		return uuid;
	}

	public boolean isSystem() {
		return fSystem;
	}

	public void setFlag(Flag flag) {
		fFlags.add(flag);
	}

	public void setFlags(EnumSet<Flag> flags) {
		this.fFlags = flags;
	}

	public void setMark(Mark mark) {
		this.fMark = mark;
	}

	/**
	 * <b>Must only be called when inside a collection lock</b>
	 * 
	 * @param ordering
	 * @uml.property name="ordering"
	 */
	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}

	public void setSystem(boolean hidden) {
		this.fSystem = hidden;
	}

	/**
	 * @param title
	 * @uml.property name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}

}
