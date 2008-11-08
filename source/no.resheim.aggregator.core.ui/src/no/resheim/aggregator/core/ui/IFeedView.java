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
package no.resheim.aggregator.core.ui;

import org.eclipse.jface.viewers.Viewer;

import no.resheim.aggregator.core.data.FeedCollection;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IFeedView {
	/**
	 * Returns the feed registry that the view is getting it's feeds from.
	 * 
	 * @return The feed registry
	 */
	public FeedCollection getFeedCollection();

	/**
	 * Sets the feed collection that the view is obtaining it's data from.
	 * 
	 * @param registry
	 */
	public void setFeedCollection(FeedCollection registry);

	public Viewer getFeedViewer();
}
