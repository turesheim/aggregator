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

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Folder extends AbstractAggregatorItem {
	String title;

	private IAggregatorItem parent;

	/**
	 * @param registryId
	 * @param title
	 */
	public Folder(UUID uuid, UUID parentUuid, String title) {
		super();
		this.parent_uuid = parentUuid;
		this.uuid = uuid;
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFeedId() {
		return null;
	}

	public IAggregatorItem getParentItem() {
		return parent;
	}

	public void setParentItem(IAggregatorItem parent) {
		this.parent = parent;
	}
}
