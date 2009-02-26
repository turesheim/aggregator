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
package no.resheim.aggregator.core.ui.internal;

import java.util.UUID;

import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FilterContentProvider implements IContentProvider,
		IStructuredContentProvider {

	private FeedCollection fCollection;

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (fCollection != null) {
			return fCollection.getFilters().toArray();
		} else {
			return new Filter[] {
					new Filter(UUID.randomUUID(), "My first filter"),
					new Filter(UUID.randomUUID(), "My second filter"),
					new Filter(UUID.randomUUID(), "My third filter")
			};
		}
		// return new Object[0];
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof FeedCollection) {
			fCollection = (FeedCollection) newInput;
		}
		System.out.println("FilterContentProvider.inputChanged()");

	}

}
