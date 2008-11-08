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
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.FeedWorkingCopy;
import no.resheim.aggregator.core.ui.internal.NewFeedWizardGeneralPage;
import no.resheim.aggregator.core.ui.internal.NewFeedWizardOptionsPage;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for creating new RSS feed connections.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class NewFeedWizard extends Wizard {

	IWizardPage archiving;
	IWizardPage general;
	FeedCollection collection;

	public FeedCollection getCollection() {
		return collection;
	}

	FeedWorkingCopy workingCopy;

	/**
	 * 
	 */
	public NewFeedWizard(FeedCollection collection) {
		super();
		this.collection = collection;
		setWindowTitle(Messages.NewFeedWizard_Title);
	}

	@Override
	public void addPages() {
		super.addPages();
		archiving = new NewFeedWizardOptionsPage(this);
		general = new NewFeedWizardGeneralPage(this);
		addPage(general);
		addPage(archiving);
	}

	public Feed getFeed() {
		Feed feed = workingCopy.getFeed();
		return feed;
	}

	public FeedWorkingCopy getWorkingCopy() {
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

	public void setFeed(FeedWorkingCopy workingCopy) {
		this.workingCopy = workingCopy;
	}

}
