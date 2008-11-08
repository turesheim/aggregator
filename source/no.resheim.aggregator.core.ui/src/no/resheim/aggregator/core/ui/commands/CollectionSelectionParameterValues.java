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
package no.resheim.aggregator.core.ui.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.FeedCollection;

import org.eclipse.core.commands.IParameterValues;

public class CollectionSelectionParameterValues implements IParameterValues {

	public Map<String, String> getParameterValues() {
		HashMap<String, String> map = new HashMap<String, String>();
		Collection<FeedCollection> list = AggregatorPlugin.getDefault()
				.getCollections();
		for (FeedCollection feedRegistry : list) {
			map.put(feedRegistry.getTitle(), feedRegistry.getId());
		}
		return map;
	}

}
