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
package no.resheim.aggregator.core.catalog;

import java.net.URL;

import no.resheim.aggregator.core.data.Feed;

/**
 * Describes a feed catalog.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IFeedCatalog {

	/**
	 * Returns the feeds that this catalogue is aware of.
	 * 
	 * @return the feeds list
	 */
	public Feed[] getFeeds();

	/**
	 * Returns the enabled state of the catalogue.
	 * 
	 * @return the enabled state
	 */
	public boolean isEnabled();

	/**
	 * Returns the bundle relative path to the catalog's icon.
	 * 
	 * @return the icon path
	 */
	public URL getIcon();

	/**
	 * Returns the name of the catalogue.
	 * 
	 * @return the catalogue name
	 */
	public String getName();

	/**
	 * Returns the identifier of the catalogue.
	 * 
	 * @return the catalogue identifier
	 */
	public String getId();
}