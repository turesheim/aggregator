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

	/**
	 * Flags are for internal use
	 */
	public enum Flag {
		/**
		 * @uml.property name="protected"
		 * @uml.associationEnd
		 */
		PROTECTED,
		/**
		 * @uml.property name="trash"
		 * @uml.associationEnd
		 */
		TRASH,
		/**
		 * @uml.property name="trashed"
		 * @uml.associationEnd
		 */
		TRASHED
	}

	/**
	 * @author torkild
	 */
	public enum Mark {
		/**
		 * @uml.property name="first_priority"
		 * @uml.associationEnd
		 */
		FIRST_PRIORITY,
		/**
		 * @uml.property name="important"
		 * @uml.associationEnd
		 */
		IMPORTANT,
		/**
		 * @uml.property name="none"
		 * @uml.associationEnd
		 */
		NONE,
		/**
		 * @uml.property name="second_proiority"
		 * @uml.associationEnd
		 */
		SECOND_PRIORITY,
		/**
		 * @uml.property name="third_priority"
		 * @uml.associationEnd
		 */
		THIRD_PRIORITY,
		/**
		 * @uml.property name="todo"
		 * @uml.associationEnd
		 */
		TODO
	}

	private EnumSet<Flag> fFlags = EnumSet.noneOf(Flag.class);

	/**
	 * @uml.property name="fMark"
	 * @uml.associationEnd
	 */
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
	 * @return
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
