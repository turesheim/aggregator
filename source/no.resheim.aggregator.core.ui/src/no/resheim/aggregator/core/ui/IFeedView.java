/*******************************************************************************
 * Copyright (c) 2007-2009 Torkild Ulvøy Resheim.
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

import no.resheim.aggregator.core.data.AggregatorCollection;

import org.eclipse.jface.viewers.Viewer;

/**
 * Interface describing views that display feed contents.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IFeedView {

	public enum Layout {
		/** Feed contents are displayed using a undefined layout */
		UNDEFINED,
		/** Feed contents are displayed using a vertical layout. */
		VERTICAL,
		/** Feed contents are displayed using a horizontal layout. */
		HORIZONTAL
	};

	/**
	 * Returns the feed registry that the view is getting it's feeds from.
	 * 
	 * @return The feed registry
	 */
	public AggregatorCollection getFeedCollection();

	/**
	 * Sets the feed collection that the view is obtaining it's data from.
	 * 
	 * @param registry
	 */
	public void setFeedCollection(AggregatorCollection registry);

	/**
	 * Returns the viewer that is actually displaying the feed contents.
	 * 
	 * @return the viewer
	 */
	public Viewer getFeedViewer();

	/**
	 * Returns the layout of the view.
	 * 
	 * @return the layout
	 */
	public Layout getLayout();

	/**
	 * Sets the layout of the view.
	 * 
	 * @param layout
	 *            the layout to set.
	 */
	public void setLayout(Layout layout);
}
