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

import java.util.UUID;

import no.resheim.aggregator.data.internal.AggregatorUIItem;

/**
 * Aggregator item representing a folder. Folders are used to contain other type
 * of aggregator items such as articles and other folders. A folder may also
 * <i>point</i> to a {@link Feed}, meaning that the folder is the default
 * location for the feed articles. In this case the folder can be used to obtain
 * the feed instance.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Folder extends AggregatorUIItem {

	protected UUID feed;

	public UUID getFeed() {
		return feed;
	}

	/**
	 * @param registryId
	 * @param title
	 */
	public Folder(AggregatorUIItem parent) {
		super(parent);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(" ["); //$NON-NLS-1$
		sb.append(getOrdering());
		sb.append(']');
		return sb.toString();
	}
}
