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

import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CriteriaContentProvider implements IContentProvider,
		IStructuredContentProvider {

	private Filter fFilter;

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (fFilter != null) {
			return fFilter.getCriteria();
		}
		return new Object[0];
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Filter) {
			fFilter = (Filter) newInput;
		}

	}

}
