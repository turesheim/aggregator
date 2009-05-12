package no.resheim.aggregator.core.ui;

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Article;

import org.eclipse.jface.viewers.IElementComparer;

/**
 * The purpose of using this type is to handle situations where for instance the
 * selection is set using an {@link Article} that has been created by a feed
 * update. That will not be the same instance as presented in the viewer as
 * these are normally created from the database.
 */
public class AggregatorItemComparer implements IElementComparer {

	public boolean equals(Object a, Object b) {
		if (a instanceof AggregatorItem && b instanceof AggregatorItem) {
			return (((AggregatorItem) a).getUUID().equals(((AggregatorItem) b)
					.getUUID()));
		}
		return false;
	}

	public int hashCode(Object element) {
		if (element instanceof AggregatorItem) {
			return ((AggregatorItem) element).getUUID().hashCode();
		}
		return element.hashCode();
	}
}