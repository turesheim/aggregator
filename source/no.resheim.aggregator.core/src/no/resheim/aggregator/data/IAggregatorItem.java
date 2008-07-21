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

import java.util.UUID;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IAggregatorItem {

	/**
	 * Sets a new title for the item.
	 * 
	 * @param title
	 *            the new title
	 */
	public abstract void setTitle(String title);

	/**
	 * Returns the title of the item.
	 * 
	 * @return the title string
	 */
	public abstract String getTitle();

	/**
	 * Returns the parent of the item.
	 * 
	 * @return the parent item
	 */
	public abstract IAggregatorItem getParent();

	/**
	 * Returns the unique identifier of the item.
	 * 
	 * @return the unique identifier
	 */
	public abstract UUID getUUID();

	/**
	 * Returns the order of the item.
	 * 
	 * @return the order
	 */
	public int getOrdering();

}
