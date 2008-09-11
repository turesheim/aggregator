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
package no.resheim.aggregator;

import org.eclipse.core.expressions.PropertyTester;

// FIXME: Do not extend java.lang.Object
public class AggregatorPropertyTester extends PropertyTester {

	private static final String MULTIPLE_COLLECTIONS = "multipleCollections"; //$NON-NLS-1$

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		if (property != null && property.equals(MULTIPLE_COLLECTIONS)) {
			return (AggregatorPlugin.getDefault().getCollections().size() > 1);
		}
		return false;
	}
}
