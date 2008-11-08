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

import no.resheim.aggregator.core.data.AggregatorItem.Mark;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

/**
 * Converts to and from {@link Mark} which is used to mark aggregator items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class MarkItemParameterValueConverter extends
		AbstractParameterValueConverter {

	public MarkItemParameterValueConverter() {
	}

	/**
	 * Returns the feed collection with the given identifier.
	 */
	@Override
	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {
		return Mark.valueOf(parameterValue);
	}

	@Override
	public String convertToString(Object parameterValue)
			throws ParameterValueConversionException {
		return parameterValue.toString();
	}
}
