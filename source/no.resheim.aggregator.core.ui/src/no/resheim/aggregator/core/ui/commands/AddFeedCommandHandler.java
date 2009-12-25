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

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.SubscriptionWorkingCopy;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.NewFeedWizard;
import no.resheim.aggregator.core.ui.PreferenceConstants;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardDialog;
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

	private static final String NEW_WIZARD_ACTION = "NewWizardAction"; //$NON-NLS-1$
	private static final String SELECTION_ROOT = "selectionRoot"; //$NON-NLS-1$

	public AddFeedCommandHandler() {
		super(false, true);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {

		AggregatorCollection collection = getCollection(event);
		if (collection == null) {
			return null;
		}
		NewFeedWizard wizard = new NewFeedWizard(collection);
		AggregatorItem parent = collection;
		AggregatorItem item = getSelection(event);
		String selectionRoot = event.getParameter(SELECTION_ROOT);
		if (selectionRoot != null
				&& selectionRoot.equals(Boolean.TRUE.toString())) {
			if (item != null)
				parent = collection;
		}
		SubscriptionWorkingCopy wc = getNewFeedWorkingCopy(parent);
		wizard.setFeed(wc);
		IDialogSettings workbenchSettings = AggregatorUIPlugin.getDefault()
				.getDialogSettings();
		IDialogSettings wizardSettings = workbenchSettings
				.getSection(NEW_WIZARD_ACTION);
		if (wizardSettings == null) {
			wizardSettings = workbenchSettings.addNewSection(NEW_WIZARD_ACTION);
		}
		wizard.setDialogSettings(wizardSettings);
		WizardDialog dialog = new WizardDialog(HandlerUtil
				.getActiveShell(event), wizard);
		dialog.setMinimumPageSize(550, 300);
		dialog.create();
		dialog.open();
		return null;
	}

	private SubscriptionWorkingCopy getNewFeedWorkingCopy(AggregatorItem parent) {
		SubscriptionWorkingCopy wc = SubscriptionWorkingCopy.newInstance(parent);
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
