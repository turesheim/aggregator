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
package no.resheim.aggregator.core.ui.commands;

import no.resheim.aggregator.core.AggregatorPlugin;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CollectionParameterValueConverter extends
		AbstractParameterValueConverter {

	public CollectionParameterValueConverter() {
	}

	/**
	 * Returns the feed collection with the given identifier.
	 */
	@Override
	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {
		return AggregatorPlugin.getDefault().getFeedCollection(parameterValue);
	}

	@Override
	public String convertToString(Object parameterValue)
			throws ParameterValueConversionException {
		return parameterValue.toString();
	}
}
