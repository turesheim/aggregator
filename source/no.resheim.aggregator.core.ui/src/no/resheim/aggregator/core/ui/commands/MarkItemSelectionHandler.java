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

import java.util.HashMap;
import java.util.Map;

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItem.Mark;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * Command handler for marking an aggregator item.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class MarkItemSelectionHandler extends AbstractAggregatorCommandHandler
		implements IElementUpdater {

	public static final String PARM_MARK = "markId"; //$NON-NLS-1$

	public MarkItemSelectionHandler() {
		super(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {

		AggregatorItem[] items = getSelectedItems(event);
		Object m = event.getObjectParameterForExecution(PARM_MARK);
		if (m != null) {
			Mark mark = (Mark) m;
			items[0].setMark(mark);
			try {
				getCollection(event).update(items[0]);
				IWorkbenchWindow window = HandlerUtil
						.getActiveWorkbenchWindowChecked(event);
				ICommandService commandService = (ICommandService) window
						.getService(ICommandService.class);
				Map filter = new HashMap();
				filter.put(PARM_MARK, event
						.getObjectParameterForExecution(PARM_MARK));
				commandService.refreshElements(event.getCommand().getId(),
						filter);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
	 */
	@SuppressWarnings("unchecked")
	public void updateElement(UIElement element, Map parameters) {
		ISelection selection = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getSelectionService()
				.getSelection();
		AggregatorItem[] items = getSelectedItems(selection);
		if (items.length == 1) {
			Object m = parameters.get(PARM_MARK);
			if (m != null) {
				Mark mark = Mark.valueOf(m.toString());
				element.setChecked(items[0].getMark().equals(mark));
			}
		}
	}
}
