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
package no.resheim.aggregator.core.ui.views;

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.IFeedView.Layout;
import no.resheim.aggregator.core.ui.commands.AbstractAggregatorCommandHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * 
 */
public class SetLayoutCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	private static final String PARAMETER_ID = "no.resheim.aggregator.core.ui.layoutCommandParameter"; //$NON-NLS-1$

	public SetLayoutCommandHandler() {
		super(false);
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			boolean horz = event.getParameter(PARAMETER_ID)
					.equals("horizontal"); //$NON-NLS-1$
			if (horz) {
				((IFeedView) part).setLayout(Layout.HORIZONTAL);
			} else {
				((IFeedView) part).setLayout(Layout.VERTICAL);
			}
		}
		return 0;
	}
}
