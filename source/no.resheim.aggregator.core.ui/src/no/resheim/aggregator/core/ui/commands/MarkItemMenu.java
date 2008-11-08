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

import no.resheim.aggregator.core.data.AggregatorItem.Mark;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.menus.CommandContributionItemParameter;
import org.eclipse.ui.services.IServiceLocator;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class MarkItemMenu extends CompoundContributionItem {

	private static final String COMMAND_ID = "no.resheim.aggregator.core.ui.markItem"; //$NON-NLS-1$

	@Override
	protected IContributionItem[] getContributionItems() {
		IContributionItem[] marks = new IContributionItem[6];
		marks[0] = createAction(Mark.NONE, "None"); //$NON-NLS-1$
		marks[1] = createAction(Mark.IMPORTANT, "Important"); //$NON-NLS-1$
		marks[2] = createAction(Mark.TODO, "To Do"); //$NON-NLS-1$
		marks[3] = createAction(Mark.FIRST_PRIORITY, "1. Priority"); //$NON-NLS-1$
		marks[4] = createAction(Mark.SECOND_PRIORITY, "2. Priority"); //$NON-NLS-1$
		marks[5] = createAction(Mark.THIRD_PRIORITY, "3. Priority"); //$NON-NLS-1$
		return marks;
	}

	@SuppressWarnings("unchecked")
	private IContributionItem createAction(Mark mark, String title) {
		IServiceLocator locator = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		Map parms = new HashMap();
		parms.put(MarkItemSelectionHandler.PARM_MARK, mark.toString());
		CommandContributionItemParameter item = new CommandContributionItemParameter(
				locator, mark.toString(), COMMAND_ID, parms, null, null, null,
				title, null, null, SWT.RADIO, null, false);
		return new CommandContributionItem(item);
	}
}
