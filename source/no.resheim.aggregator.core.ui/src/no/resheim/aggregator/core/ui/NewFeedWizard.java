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

import no.resheim.aggregator.core.ui.internal.NewFeedWizardGeneralPage;
import no.resheim.aggregator.core.ui.internal.NewFeedWizardOptionsPage;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.FeedWorkingCopy;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;

/**
 * Wizard for creating new RSS feed connections.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class NewFeedWizard extends Wizard {

	IWizardPage general;
	IWizardPage archiving;
	FeedWorkingCopy workingCopy;
	FeedCollection registry;

	/**
	 * 
	 */
	public NewFeedWizard(FeedCollection registry) {
		super();
		this.registry = registry;
		setWindowTitle(Messages.NewFeedWizard_Title);
	}

	@Override
	public boolean performFinish() {
		return true;
	}

	@Override
	public void addPages() {
		super.addPages();
		archiving = new NewFeedWizardOptionsPage(registry, workingCopy);
		general = new NewFeedWizardGeneralPage(registry, workingCopy);
		addPage(general);
		addPage(archiving);
	}

	public void setFeed(FeedWorkingCopy workingCopy) {
		this.workingCopy = workingCopy;
	}

	public Feed getFeed() {
		return workingCopy.getFeed();
	}

}
