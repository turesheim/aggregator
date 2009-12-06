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
package no.resheim.aggregator.core;

import no.resheim.aggregator.core.data.AggregatorCollection;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IFeedCollectionEventListener {
	/**
	 * Called only once just after a feed collection has been created, it's
	 * storage is ready and any declared feeds has been added to it.
	 * 
	 * @param collection
	 *            the initialised collection
	 */
	public void collectionInitialized(AggregatorCollection collection);

}
