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
package no.resheim.aggregator.core.ui.internal;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.model.FeedRegistry;
import no.resheim.aggregator.model.FeedWorkingCopy;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewFeedWizardGeneralPage extends WizardPage {
	private Combo combo;
	private Text text;
	private Label urlLabel;
	private Label titleLabel;
	FeedWorkingCopy workingCopy;
	FeedRegistry registry;

	/**
	 * Create the wizard
	 */
	public NewFeedWizardGeneralPage(FeedRegistry registry,
			FeedWorkingCopy workingCopy) {
		super(Messages.NewFeedWizardGeneralPage_Title);
		setTitle(Messages.NewFeedWizardGeneralPage_Title);
		setDescription(Messages.NewFeedWizardGeneralPage_Description);
		setImageDescriptor(AggregatorUIPlugin
				.getImageDescriptor("icons/wizban/new_feed_wizard.png")); //$NON-NLS-1$
		this.workingCopy = workingCopy;
		this.registry = registry;
		// The wizard page starts out as incomplete
		setPageComplete(false);
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);
		setControl(container);

		titleLabel = new Label(container, SWT.NONE);
		titleLabel.setText(Messages.NewFeedWizardGeneralPage_Label_Title);

		combo = new Combo(container, SWT.NONE);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (combo.getSelectionIndex() >= 0) {
					text.setText(AggregatorPlugin.DEFAULT_FEEDS.get(combo
							.getSelectionIndex())[1]);
				}
				validate();
			}
		});
		for (String[] item : AggregatorPlugin.DEFAULT_FEEDS) {
			combo.add(item[0]);
		}
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setTitle(combo.getText());
				validate();
			}
		});
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		urlLabel = new Label(container, SWT.NONE);
		urlLabel.setText(Messages.NewFeedWizardGeneralPage_Label_URL);

		text = new Text(container, SWT.BORDER);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setURL(text.getText());
				validate();
			}
		});
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	private void validate() {
		if (workingCopy.getTitle().length() == 0) {
			setMessage(Messages.NewFeedWizardGeneralPage_Error_Missing_title);
			setPageComplete(false);
			return;
		}
		if (workingCopy.getURL().length() == 0) {
			setMessage(Messages.NewFeedWizardGeneralPage_Error_Missing_URL);
			setPageComplete(false);
			return;
		}
		if (registry.hasFeed(workingCopy.getURL())) {
			setErrorMessage(Messages.NewFeedWizardGeneralPage_Error_Existing_Feed);
			setPageComplete(false);
			return;
		}
		// Everything is in order.
		setMessage(null);
		setErrorMessage(null);
		setPageComplete(true);
	}
}
