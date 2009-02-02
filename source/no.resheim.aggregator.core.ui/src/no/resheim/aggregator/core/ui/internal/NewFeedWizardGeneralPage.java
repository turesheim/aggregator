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

import java.util.ArrayList;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedWorkingCopy;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.NewFeedWizard;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page for the general settings of a new feed.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class NewFeedWizardGeneralPage extends WizardPage {
	private Combo combo;
	private Text urlText;
	private Label urlLabel;
	private Label titleLabel;
	private NewFeedWizard wizard;
	private Label userLabel;
	private Text userText;
	private Label passwordLabel;
	private Text passwordText;

	/**
	 * Create the wizard
	 */
	public NewFeedWizardGeneralPage(NewFeedWizard wizard) {
		super(Messages.NewFeedWizardGeneralPage_Title);
		setTitle(Messages.NewFeedWizardGeneralPage_Title);
		setDescription(Messages.NewFeedWizardGeneralPage_Description);
		setImageDescriptor(AggregatorUIPlugin
				.getImageDescriptor("icons/wizban/new_feed_wizard.png")); //$NON-NLS-1$
		this.wizard = wizard;
		// The wizard page starts out as incomplete
		setPageComplete(false);
	}

	ArrayList<Feed> defaults = new ArrayList<Feed>();

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

		final FeedWorkingCopy workingCopy = wizard.getWorkingCopy();

		titleLabel = new Label(container, SWT.NONE);
		titleLabel.setText(Messages.NewFeedWizardGeneralPage_Label_Title);

		combo = new Combo(container, SWT.NONE);
		final ArrayList<Feed> feeds = AggregatorPlugin.getDefault()
				.getDefaultFeeds();
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (combo.getSelectionIndex() >= 0) {
					workingCopy.copy(defaults.get(combo.getSelectionIndex()));
					urlText.setText(workingCopy.getURL());

				}
				validate();
			}
		});
		for (Feed feed : feeds) {
			if (!wizard.getCollection().hasFeed(feed.getURL())) {
				combo.add(feed.getTitle());
				defaults.add(feed);
			}

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

		urlText = new Text(container, SWT.BORDER);
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setURL(urlText.getText());
				validate();
			}
		});
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		final Button loginButton = new Button(container, SWT.CHECK);
		loginButton.setText(Messages.NewFeedWizardGeneralPage_Anonymous);
		GridData gd2 = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd2.horizontalSpan = 2;
		loginButton.setLayoutData(gd2);
		loginButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = !(loginButton.getSelection());
				workingCopy.setAnonymousAccess(loginButton.getSelection());
				updateCredentialsFields(state);
			}

		});

		userLabel = new Label(container, SWT.NONE);
		userLabel.setText(Messages.NewFeedWizardGeneralPage_Login);
		userText = new Text(container, SWT.BORDER);
		userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		userText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setUsername(userText.getText());
				validate();
			}
		});
		passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setText(Messages.NewFeedWizardGeneralPage_Password);
		passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setPassword(passwordText.getText());
				validate();
			}
		});
		loginButton.setSelection(true);
		updateCredentialsFields(false);
	}

	private void updateCredentialsFields(boolean state) {
		userLabel.setEnabled(state);
		userText.setEnabled(state);
		passwordLabel.setEnabled(state);
		passwordText.setEnabled(state);
	}

	private void validate() {
		FeedWorkingCopy workingCopy = wizard.getWorkingCopy();

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
		// Everything is in order.
		setMessage(null);
		setErrorMessage(null);
		setPageComplete(true);
	}
}
