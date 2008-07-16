/*******************************************************************************
 * Copyright (c) 2007-2008 Torkild Ulvøy Resheim.
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

import no.resheim.aggregator.data.internal.AggregatorItem;

/**
 * Aggregator item representing a folder. Folders are used to hold any other
 * type of aggregator items such as feeds, articles and other folders.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Folder extends AggregatorItem {

	/** The folder title */
	private String title;

	/**
	 * @param registryId
	 * @param title
	 */
	Folder(IAggregatorItem parent) {
		super(parent);
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(" ["); //$NON-NLS-1$
		sb.append(getOrdering());
		sb.append(']');
		return sb.toString();
	}
}
