package no.resheim.aggregator.rcp.commands;

import java.util.EnumSet;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.commands.AbstractAggregatorCommandHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class UpdateAllCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public UpdateAllCommandHandler() {
		super(false, true);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			AggregatorCollection collection = ((IFeedView) part)
					.getFeedCollection();
			if (collection == null) {
				return null;
			}

			try {
				AggregatorItem[] items = collection.getChildren(EnumSet
						.of(ItemType.FOLDER));
				for (AggregatorItem item : items) {
					IStatus s = collection.synchronize(item);
					if (!s.isOK()) {
						StatusManager.getManager().handle(s,
								StatusManager.BLOCK);
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		// Do not enable action if the collection has not initialised yet.
		return AggregatorPlugin.isInitialized();
	}
}
