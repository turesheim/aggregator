/*******************************************************************************
 * Copyright (c) 2008-2009 Torkild Ulvøy Resheim.
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
 */
public abstract class AggregatorItem {
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
	 * The type of aggregator item. This enumeration is normally used when
	 * wanting to define a set of item types to handle. In some situations only
	 * folders are wanted, in others all types are wanted.
	 */
	public enum ItemType {
		ARTICLE, FOLDER
	};

	/** The collection this item belongs to */
	private AggregatorCollection collection;

	private EnumSet<Flag> fFlags = EnumSet.noneOf(Flag.class);

	private String[] fLabels;

	private boolean fSystem = false;

	protected AggregatorItemParent parent;

	/**
	 * The folder title
	 */
	protected String title = ""; //$NON-NLS-1$

	/**
	 * The unique identifier of the item.
	 */
	protected UUID uuid = null;

	/**
	 * @param parent
	 *            the parent item
	 * @param uuid
	 *            the unique identifier of the item
	 */
	protected AggregatorItem(AggregatorItemParent parent, UUID uuid) {
		this.parent = parent;
		this.uuid = uuid;
		fLabels = new String[0];
	}

	/**
	 * Adds a new label to the aggregator item.
	 * 
	 * @param label
	 *            the label to add.
	 */
	public void addLabel(String label) {
		if (!hasLabel(label)) {
			try {
				String[] newLabels = new String[fLabels.length + 1];
				System.arraycopy(fLabels, 0, newLabels, 1, fLabels.length);
				newLabels[0] = label.trim();
				fLabels = newLabels;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Iterates upwards in order to find the collection instance for the item
	 * and returns this.
	 * 
	 * @return the collection of the item.
	 * @throws CoreException
	 */
	public AggregatorCollection getCollection() throws CoreException {
		if (collection == null) {
			AggregatorItem p = this;
			while (!(p instanceof AggregatorCollection)) {
				p = p.getParent();
			}
			collection = (AggregatorCollection) p;
		}
		return collection;

	}

	/**
	 * Returns the system flags of this aggregator item.
	 * 
	 * @return the flags
	 */
	public EnumSet<Flag> getFlags() {
		return fFlags;
	}

	public String[] getLabels() {
		return fLabels;
	}

	public String getLabelString() {
		StringBuilder sb = new StringBuilder();
		synchronized (fLabels) {
			boolean comma = false;
			for (String string : fLabels) {
				if (comma) {
					sb.append(", ");
				}
				sb.append(string);
				comma = true;
			}
		}
		return sb.toString();
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
	 * Tests if the given label is already assigned to the item and returns
	 * <code>true</code> if this is the case.
	 * 
	 * @param label
	 *            the label to test for.
	 * @return <code>true</code> if the label is assigned.
	 */
	public boolean hasLabel(String label) {
		boolean found = false;
		synchronized (fLabels) {
			for (String l : fLabels) {
				if (l != null && l.equalsIgnoreCase(label.trim())) {
					found = true;
				}
			}
		}
		return found;
	}

	public boolean isSystem() {
		return fSystem;
	};

	/**
	 * 
	 * @param label
	 */
	public void removeLabel(String label) {
		if (hasLabel(label)) {
			int c = 0;
			String[] newLabels = new String[fLabels.length - 1];
			for (String string : fLabels) {
				if (!string.equalsIgnoreCase(label.trim())) {
					newLabels[c++] = string;
				}
			}
			fLabels = newLabels;
		}
	}

	public void setFlag(Flag flag) {
		fFlags.add(flag);
	}

	public void setFlags(EnumSet<Flag> flags) {
		this.fFlags = flags;
	}

	public void setLabels(String[] labels) {
		this.fLabels = labels;
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
