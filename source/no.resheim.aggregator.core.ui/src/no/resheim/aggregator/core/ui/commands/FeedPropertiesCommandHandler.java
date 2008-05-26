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

import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.FeedPropertiesDialog;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.FeedWorkingCopy;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class FeedPropertiesCommandHandler extends
		AbstractAggregatorCommandHandler implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection registry = ((IFeedView) part).getFeedRegistry();
			IAggregatorItem o = getSelection(event);
			if (o instanceof Feed) {
				Feed feed = (Feed) o;
				FeedWorkingCopy wc = new FeedWorkingCopy(feed);
				FeedPropertiesDialog dialog = new FeedPropertiesDialog(
						HandlerUtil.getActiveShell(event),
						Messages.FeedPropertiesCommand_Title,
						AggregatorUIPlugin.getDefault().getImageRegistry().get(
								AggregatorUIPlugin.IMG_FEED_OBJ),
						Messages.FeedPropertiesCommand_Description, 0,
						new String[] {
								Messages.FeedPropertiesCommand_OK,
								Messages.FeedPropertiesCommand_CANCEL
						}, 0, wc);
				if (dialog.open() == Window.OK) {
					feed.updateFromWorkingCopy(wc);
					registry.updateFeedData(feed);
				}
			}
		}
		return null;
	}

}
