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

import no.resheim.aggregator.core.data.SubscriptionWorkingCopy;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * Wizard page for specifying extra feed options.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class NewFeedWizardOptionsPage extends WizardPage {

	private Combo periodCombo;
	private Spinner intervalField;
	private Label updateEveryLabel;
	private Group group;
	private Label itemsLabel;
	private Spinner itemCount;
	private Label daysLabel;
	private Spinner days;
	private Button radioKeepNone;
	private Button radioKeepNewest;
	private Button radioKeepSome;
	private Button radioKeepAll;
	private Group aGroup;

	private NewFeedWizard wizard;

	/**
	 * Create the wizard
	 */
	public NewFeedWizardOptionsPage(NewFeedWizard wizard) {
		super(Messages.NewFeedWizardOptionsPage_Title);
		setTitle(Messages.NewFeedWizardOptionsPage_Title);
		setDescription(Messages.NewFeedWizardOptionsPage_Description);
		setImageDescriptor(AggregatorUIPlugin
				.getImageDescriptor("icons/wizban/new_feed_wizard.png")); //$NON-NLS-1$
		this.wizard = wizard;
	}

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final SubscriptionWorkingCopy workingCopy = wizard.getWorkingCopy();
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		container.setLayout(gridLayout_1);
		setControl(container);

		aGroup = new Group(container, SWT.NONE);
		aGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		aGroup.setText(Messages.NewFeedWizardOptionsPage_Label_Archiving_Group);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		aGroup.setLayout(gridLayout);

		radioKeepAll = new Button(aGroup, SWT.RADIO);
		radioKeepAll.setText(Messages.NewFeedWizardOptionsPage_Label_Keep_All);
		radioKeepAll.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				workingCopy.setArchiving(Archiving.KEEP_ALL);
				itemCount.setEnabled(false);
				days.setEnabled(false);
			}
		});
		new Label(aGroup, SWT.NONE);
		new Label(aGroup, SWT.NONE);

		radioKeepSome = new Button(aGroup, SWT.RADIO);
		radioKeepSome
				.setText(Messages.NewFeedWizardOptionsPage_Label_Keep_Some);
		radioKeepSome.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				workingCopy.setArchiving(Archiving.KEEP_SOME);
				itemCount.setEnabled(true);
				days.setEnabled(false);
			}
		});

		itemCount = new Spinner(aGroup, SWT.BORDER);
		itemCount.setDigits(0);
		itemCount.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setArchivingItems(itemCount.getSelection());
			}
		});

		itemsLabel = new Label(aGroup, SWT.NONE);
		itemsLabel.setText(Messages.NewFeedWizardOptionsPage_Label_items);

		radioKeepNewest = new Button(aGroup, SWT.RADIO);
		radioKeepNewest
				.setText(Messages.NewFeedWizardOptionsPage_Label_Keep_newest);
		radioKeepNewest.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				workingCopy.setArchiving(Archiving.KEEP_NEWEST);
				itemCount.setEnabled(false);
				days.setEnabled(true);
			}
		});

		days = new Spinner(aGroup, SWT.BORDER);
		days.setDigits(0);
		days.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setArchivingDays(days.getSelection());
			}
		});

		daysLabel = new Label(aGroup, SWT.NONE);
		daysLabel.setText(Messages.NewFeedWizardOptionsPage_Label_days);

		radioKeepNone = new Button(aGroup, SWT.RADIO);
		radioKeepNone
				.setText(Messages.NewFeedWizardOptionsPage_Label_Keep_none);
		radioKeepNone.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				workingCopy.setArchiving(Archiving.KEEP_NONE);
				itemCount.setEnabled(false);
				days.setEnabled(false);
			}
		});
		new Label(aGroup, SWT.NONE);
		new Label(aGroup, SWT.NONE);

		group = new Group(container, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		group.setText(Messages.NewFeedWizardOptionsPage_Label_Updating_Group);
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 3;
		group.setLayout(gridLayout_2);

		updateEveryLabel = new Label(group, SWT.NONE);
		updateEveryLabel
				.setText(Messages.NewFeedWizardOptionsPage_Label_Update_every);

		intervalField = new Spinner(group, SWT.BORDER);

		periodCombo = new Combo(group, SWT.READ_ONLY);
		periodCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		periodCombo.add(Messages.NewFeedWizardOptionsPage_Label_Minutes);
		periodCombo.add(Messages.NewFeedWizardOptionsPage_Label_Hours);
		periodCombo.add(Messages.NewFeedWizardOptionsPage_Label_Days);
		periodCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int index = periodCombo.getSelectionIndex();
				switch (index) {
				case 0:
					workingCopy.setUpdatePeriod(UpdatePeriod.MINUTES);
					break;
				case 1:
					workingCopy.setUpdatePeriod(UpdatePeriod.HOURS);
					break;
				case 2:
					workingCopy.setUpdatePeriod(UpdatePeriod.DAYS);
					break;

				}
			}
		});
		updateArchivingWidgets();
		updateRefreshWidgets();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		updateArchivingWidgets();
		updateRefreshWidgets();
	}

	private void updateArchivingWidgets() {
		SubscriptionWorkingCopy workingCopy = wizard.getWorkingCopy();
		switch (workingCopy.getArchiving()) {
		case KEEP_ALL:
			radioKeepAll.setSelection(true);
			itemCount.setEnabled(false);
			days.setEnabled(false);
			break;
		case KEEP_NEWEST:
			radioKeepNewest.setSelection(true);
			itemCount.setEnabled(false);
			days.setEnabled(true);
			break;
		case KEEP_SOME:
			radioKeepSome.setSelection(true);
			itemCount.setEnabled(true);
			days.setEnabled(false);
			break;
		case KEEP_NONE:
			radioKeepNone.setSelection(true);
			itemCount.setEnabled(false);
			days.setEnabled(false);
			break;
		}
		days.setSelection(workingCopy.getArchivingDays());
		itemCount.setSelection(workingCopy.getArchivingItems());
	}

	private void updateRefreshWidgets() {
		SubscriptionWorkingCopy workingCopy = wizard.getWorkingCopy();
		switch (workingCopy.getUpdatePeriod()) {
		case MINUTES:
			periodCombo.setEnabled(true);
			periodCombo.select(0);
			intervalField.setEnabled(true);
			break;
		case HOURS:
			periodCombo.setEnabled(true);
			periodCombo.select(1);
			intervalField.setEnabled(true);
			break;
		case DAYS:
			periodCombo.select(2);
			periodCombo.setEnabled(true);
			intervalField.setEnabled(true);
			break;
		}
		intervalField.setSelection(workingCopy.getUpdateInterval());
	}

}
