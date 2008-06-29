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
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.NewFeedWizard;
import no.resheim.aggregator.core.ui.PreferenceConstants;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.FeedWorkingCopy;
import no.resheim.aggregator.data.IAggregatorItem;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command to add a new feed to the registry. The view where this command is
 * added must implement {@link IFeedView} for the command to be able to
 * determine the active feed registry.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * @see IFeedView
 */
public class AddFeedCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection registry = ((IFeedView) part).getFeedCollection();
			if (registry == null) {
				return null;
			}
			NewFeedWizard wizard = new NewFeedWizard(registry);
			IAggregatorItem parent = registry;
			IAggregatorItem item = getSelection(event);
			String selectionRoot = event.getParameter("selectionRoot"); //$NON-NLS-1$
			if (selectionRoot != null && selectionRoot.equals("true")) { //$NON-NLS-1$
				if (item != null)
					parent = registry;
			}
			FeedWorkingCopy wc = getNewFeedWorkingCopy(parent);
			wizard.setFeed(wc);
			IDialogSettings workbenchSettings = AggregatorUIPlugin.getDefault()
					.getDialogSettings();
			IDialogSettings wizardSettings = workbenchSettings
					.getSection("NewWizardAction");//$NON-NLS-1$
			if (wizardSettings == null) {
				wizardSettings = workbenchSettings
						.addNewSection("NewWizardAction");//$NON-NLS-1$
			}
			wizard.setDialogSettings(wizardSettings);
			WizardDialog dialog = new WizardDialog(HandlerUtil
					.getActiveShell(event), wizard);
			dialog.create();
			if (dialog.open() == Window.OK) {
				registry.addNew(wizard.getFeed());
			}
		}
		return null;
	}

	private FeedWorkingCopy getNewFeedWorkingCopy(IAggregatorItem parent) {
		FeedWorkingCopy wc = FeedWorkingCopy.newInstance(parent);
		// Initialise with default values from the preference store.
		// This is done here as the preference system is a UI component.
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		wc.setArchiving(Archiving.valueOf(store
				.getString(PreferenceConstants.P_ARCHIVING_METHOD)));
		wc.setArchivingDays(store.getInt(PreferenceConstants.P_ARCHIVING_DAYS));
		wc.setArchivingItems(store
				.getInt(PreferenceConstants.P_ARCHIVING_ITEMS));
		wc.setUpdateInterval(store
				.getInt(PreferenceConstants.P_UPDATING_INTERVAL));
		wc.setUpdatePeriod(UpdatePeriod.valueOf(store
				.getString(PreferenceConstants.P_UPDATING_PERIOD)));
		return wc;
	}

}
