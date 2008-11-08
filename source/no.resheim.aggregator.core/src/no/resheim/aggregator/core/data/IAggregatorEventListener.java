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

package no.resheim.aggregator.core.data;

/**
 * An interface for aggregator components that will be notified of aggregator
 * events.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IAggregatorEventListener {
	/**
	 * Notifies the listener that an aggregator item has changed. The event
	 * object contains details about the change.
	 * 
	 * @param event
	 *            the change event
	 */
	public void aggregatorItemChanged(AggregatorItemChangedEvent event);
}
