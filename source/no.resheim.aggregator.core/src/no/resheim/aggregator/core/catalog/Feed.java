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

import no.resheim.aggregator.core.data.Subscription;

/**
 * 
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Feed {

	private Subscription subscription;

	public Feed(Subscription subscription) {
		this.subscription = subscription;
	}

	public Subscription getSubscription() {
		return subscription;
	}

}
