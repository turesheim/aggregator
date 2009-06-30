/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.data;

import java.util.UUID;

/**
 * This type is used as a place holder for cases where a proper item should have
 * been created/returned. Typically this happens when there is a hole in the
 * folder contents.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class BrokenItem extends AggregatorItem {

	protected BrokenItem(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
		setTitle("Broken item"); //$NON-NLS-1$
	}

	public BrokenItem(AggregatorItemParent parent) {
		this(parent, UUID.randomUUID());
	}
}
