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

import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class FeedCollectionSelectionHandler extends
		AbstractAggregatorCommandHandler {
	public FeedCollectionSelectionHandler() {
		super(false);
	}

	public static final String PARM_COLLECTION = "collectionId"; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			AggregatorCollection registry = (AggregatorCollection) event
					.getObjectParameterForExecution(PARM_COLLECTION);
			((IFeedView) part).setFeedCollection(registry);
			if (event.getTrigger() instanceof Event) {
				Widget w = ((Event) event.getTrigger()).widget;
				if (w instanceof MenuItem) {
					((MenuItem) w).setSelection(true);
				}
			}
		}
		return null;
	}

}
