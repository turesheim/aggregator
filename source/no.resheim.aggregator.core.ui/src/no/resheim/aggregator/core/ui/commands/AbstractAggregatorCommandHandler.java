/**
 * 
 */
package no.resheim.aggregator.core.ui.commands;

import no.resheim.aggregator.model.IAggregatorItem;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractAggregatorCommandHandler extends AbstractHandler {

	protected IAggregatorItem getSelection(ExecutionEvent event) {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection) {
			Object o = ((IStructuredSelection) selection).getFirstElement();
			if (o != null && o instanceof IAggregatorItem) {
				return ((IAggregatorItem) o);
			}
		}
		return null;
	}
}
