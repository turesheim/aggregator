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

import java.text.MessageFormat;
import java.util.EnumSet;
import java.util.UUID;

import no.resheim.aggregator.core.AggregatorPlugin;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * This type implements the UI presentable information for aggregator items such
 * as articles and folders
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * @noextend This class is not intended to be subclassed by clients.
 */
public abstract class AggregatorItem {

	public enum Mark {
		/** No marking */
		NONE,
		/** Important marking */
		IMPORTANT,
		/** Todo marking */
		TODO,
		/** First priority marking */
		FIRST_PRIORITY,
		/** Second priority marking */
		SECOND_PRIORITY,
		/** Trash folder marking */
		THIRD_PRIORITY
	}

	/** Flags are for internal use */
	public enum Flag {
		/** The item is a trash folder item */
		TRASH,
		/** The item has been prepared for deletion */
		TRASHED,
		/** The item is protected from deletion */
		PROTECTED
	}

	private Mark fMark = Mark.NONE;

	private EnumSet<Flag> fFlags = EnumSet.noneOf(Flag.class);

	protected int ordering;

	protected AggregatorItemParent parent;

	protected boolean serialized;

	protected UUID uuid = null;

	private boolean fSystem = false;

	/** The folder title */
	protected String title = ""; //$NON-NLS-1$

	public boolean isSystem() {
		return fSystem;
	}

	public void setSystem(boolean hidden) {
		this.fSystem = hidden;
	}

	/**
	 * @param parent
	 */
	protected AggregatorItem(AggregatorItemParent parent, UUID uuid) {
		this.parent = parent;
		this.uuid = uuid;
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
	 * Returns the system flags of this aggregator item.
	 * 
	 * @return the flags
	 */
	public EnumSet<Flag> getFlags() {
		return fFlags;
	}

	public int getOrdering() {
		return ordering;
	};

	public AggregatorItemParent getParent() {
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

	public void setMark(Mark mark) {
		this.fMark = mark;
	}

	public void setFlags(EnumSet<Flag> flags) {
		this.fFlags = flags;
	}

	public void setFlag(Flag flag) {
		fFlags.add(flag);
	}

	/**
	 * <b>Must only be called when inside a collection lock</b>
	 * 
	 * @param ordering
	 */
	public void setOrdering(int ordering) {
		this.ordering = ordering;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	protected FeedCollection getCollection() throws CoreException {
		AggregatorItem p = this;
		AggregatorItem o = p;
		while (!(p instanceof FeedCollection)) {
			p = p.getParent();
			if (p == null) {
				throw new CoreException(
						new Status(
								IStatus.ERROR,
								AggregatorPlugin.PLUGIN_ID,
								MessageFormat
										.format(
												"Aggregator item {0} does not have a parent", new Object[] { o}))); //$NON-NLS-1$
			}
			o = p;
		}
		return (FeedCollection) p;
	}

}
