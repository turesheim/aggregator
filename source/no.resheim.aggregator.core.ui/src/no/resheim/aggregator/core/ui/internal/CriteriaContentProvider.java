package no.resheim.aggregator.core.ui.internal;

import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class CriteriaContentProvider implements IContentProvider,
		IStructuredContentProvider {

	private Filter fFilter;

	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (newInput instanceof Filter) {
			fFilter = (Filter) newInput;
		}

	}

	public Object[] getElements(Object inputElement) {
		if (fFilter != null) {
			return fFilter.getCriteria();
		}
		return new Object[0];
	}

}
