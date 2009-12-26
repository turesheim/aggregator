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
package no.resheim.aggregator.core.ui;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.SubscriptionWorkingCopy;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
import no.resheim.aggregator.core.ui.wizards.NewFeedWizardGeneralPage;
import no.resheim.aggregator.core.ui.wizards.NewFeedWizardSynchronizationPage;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;

/**
 * Wizard for creating new RSS feed connections.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class NewFeedWizard extends Wizard implements IWorkbenchWizard {

	IWizardPage archiving;
	IWizardPage general;
	AggregatorCollection collection;

	public AggregatorCollection getCollection() {
		return collection;
	}

	SubscriptionWorkingCopy workingCopy;

	public NewFeedWizard() {
		super();
		this.collection = AggregatorPlugin.getDefault().getFeedCollection(null);
		setWindowTitle(Messages.NewFeedWizard_Title);
	}

	/**
	 * 
	 */
	public NewFeedWizard(AggregatorCollection collection) {
		super();
		this.collection = collection;
		setWindowTitle(Messages.NewFeedWizard_Title);
	}

	@Override
	public void addPages() {
		super.addPages();
		archiving = new NewFeedWizardSynchronizationPage(this);
		general = new NewFeedWizardGeneralPage(this);
		addPage(general);
		addPage(archiving);
	}

	public Subscription getFeed() {
		Subscription feed = workingCopy.getFeed();
		return feed;
	}

	public SubscriptionWorkingCopy getWorkingCopy() {
		return workingCopy;
	}

	@Override
	public boolean performFinish() {
		if (!workingCopy.isAnonymousAccess()) {
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(workingCopy.getUUID()
					.toString());
			try {
				feedNode.put(AggregatorPlugin.SECURE_STORAGE_USERNAME,
						workingCopy.getUsername(), true);
				feedNode.put(AggregatorPlugin.SECURE_STORAGE_PASSWORD,
						workingCopy.getPassword(), true);
			} catch (StorageException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		collection.addNew(workingCopy.getFeed());
		return true;
	}

	public void setFeed(SubscriptionWorkingCopy workingCopy) {
		this.workingCopy = workingCopy;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		AggregatorItem parent = collection;
		if (selection instanceof IStructuredSelection) {
			if (selection.getFirstElement() instanceof AggregatorItem) {
				parent = (AggregatorItem) selection.getFirstElement();
			}
		}
		SubscriptionWorkingCopy wc = getNewFeedWorkingCopy(parent);
		setFeed(wc);
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
