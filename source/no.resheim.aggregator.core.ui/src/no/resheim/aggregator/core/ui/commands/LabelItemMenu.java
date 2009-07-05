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
public class LabelItemMenu extends CompoundContributionItem {

	private static final String COMMAND_ID = "no.resheim.aggregator.core.ui.markItem"; //$NON-NLS-1$

	@Override
	protected IContributionItem[] getContributionItems() {
		IContributionItem[] marks = new IContributionItem[6];
		marks[0] = createAction("None"); //$NON-NLS-1$
		marks[1] = createAction("Important"); //$NON-NLS-1$
		marks[2] = createAction("To Do"); //$NON-NLS-1$
		marks[3] = createAction("1. Priority"); //$NON-NLS-1$
		marks[4] = createAction("2. Priority"); //$NON-NLS-1$
		marks[5] = createAction("3. Priority"); //$NON-NLS-1$
		return marks;
	}

	@SuppressWarnings("unchecked")
	private IContributionItem createAction(String title) {
		IServiceLocator locator = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		Map parms = new HashMap();
		parms.put(LabelItemSelectionHandler.PARM_MARK, title);
		/*
		 * CommandContributionItemParameter item = new
		 * CommandContributionItemParameter( locator, COMMAND_ID, "",
		 * SWT.CHECK); item.parameters = parms; item.label = title;
		 */
		CommandContributionItemParameter item = new CommandContributionItemParameter(
				locator, title, COMMAND_ID, parms, null, null, null, title,
				null, null, SWT.CHECK, null, false);
		return new CommandContributionItem(item);
	}
}
