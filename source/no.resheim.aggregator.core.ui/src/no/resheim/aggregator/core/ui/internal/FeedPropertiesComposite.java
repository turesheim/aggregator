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

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class FeedPropertiesComposite extends Composite {

	private Feed feed = null;

	private Label titleLabel = null;

	private Label urlLabel = null;

	private Text titleText = null;

	private Text urlText = null;

	private Group archiveGroup = null;

	private Button radioKeepAll = null;

	private Button radioDeleteItems = null;

	private Button radioKeepMax = null;

	private Spinner days = null;

	private Label label = null;

	private Spinner itemCount = null;

	private Label label1 = null;

	private Button radioNoArchiving = null;

	private Group updateGroup = null;

	private Label label2 = null;

	private Spinner intervalField = null;

	private Combo periodCombo = null;

	public FeedPropertiesComposite(Composite parent, Feed feed) {
		super(parent, SWT.NONE);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		this.setLayoutData(gridData);
		this.feed = feed;
		initialize();
	}

	private void initialize() {
		GridData gridData1 = new GridData();
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData1.horizontalSpan = 2;
		gridData1.grabExcessHorizontalSpace = true;
		gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridData gridData = new GridData();
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData.horizontalSpan = 2;
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		gridLayout.makeColumnsEqualWidth = false;
		titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText(Messages.FeedPropertiesComposite_0);
		titleText = new Text(this, SWT.BORDER);
		titleText.setToolTipText(Messages.FeedPropertiesComposite_1);
		titleText.setLayoutData(gridData);
		titleText
				.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
					public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
						feed.setTitle(titleText.getText());
					}
				});
		urlLabel = new Label(this, SWT.NONE);
		urlLabel.setText(Messages.FeedPropertiesComposite_2);
		urlText = new Text(this, SWT.BORDER);
		urlText.setToolTipText(Messages.FeedPropertiesComposite_3);
		urlText.setLayoutData(gridData1);
		urlText.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				feed.setURL(urlText.getText());
			}
		});
		this.setLayout(gridLayout);
		createArchiveGroup();
		createUpdateGroup();
		setSize(new Point(457, 230));
		// Update with the values we've already got from the feed.
		urlText.setText(feed.getURL());
		titleText.setText(feed.getTitle());
		intervalField.setSelection(feed.getUpdateInterval());
		days.setSelection(feed.getArchivingDays());
		itemCount.setSelection(feed.getArchivingItems());
		// XXX: The order better not change
		periodCombo.select(feed.getUpdatePeriod().ordinal());
	}

	/**
	 * This method initializes archiveGroup
	 * 
	 */
	private void createArchiveGroup() {
		GridData gridData4 = new GridData();
		gridData4.widthHint = 20;
		GridData gridData3 = new GridData();
		gridData3.widthHint = 20;
		GridLayout gridLayout1 = new GridLayout();
		gridLayout1.numColumns = 3;
		GridData gridData2 = new GridData();
		gridData2.horizontalSpan = 2;
		gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		archiveGroup = new Group(this, SWT.NONE);
		archiveGroup.setText(Messages.FeedPropertiesComposite_4);
		archiveGroup.setLayout(gridLayout1);
		archiveGroup.setLayoutData(gridData2);
		radioKeepAll = new Button(archiveGroup, SWT.RADIO);
		radioKeepAll.setText(Messages.FeedPropertiesComposite_7);
		radioKeepAll.setToolTipText(Messages.FeedPropertiesComposite_8);
		radioKeepAll
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						itemCount.setEnabled(false);
						days.setEnabled(false);
						feed.setArchiving(Archiving.KEEP_ALL);
					}
				});
		new Label(archiveGroup, SWT.NONE);
		new Label(archiveGroup, SWT.NONE);
		radioKeepMax = new Button(archiveGroup, SWT.RADIO);
		radioKeepMax.setText(Messages.FeedPropertiesComposite_9);
		radioKeepMax.setToolTipText(Messages.FeedPropertiesComposite_10);
		radioKeepMax
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						itemCount.setEnabled(true);
						days.setEnabled(false);
						feed.setArchiving(Archiving.KEEP_SOME);
					}
				});
		itemCount = new Spinner(archiveGroup, SWT.BORDER);
		itemCount.setToolTipText(Messages.FeedPropertiesComposite_11);
		itemCount.setLayoutData(gridData4);
		itemCount
				.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
					public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
						try {
							feed.setArchivingItems(itemCount.getSelection());
						} catch (NumberFormatException err) {
						}
					}
				});
		label1 = new Label(archiveGroup, SWT.NONE);
		label1.setText(Messages.FeedPropertiesComposite_12);
		radioDeleteItems = new Button(archiveGroup, SWT.RADIO);
		radioDeleteItems.setText(Messages.FeedPropertiesComposite_13);
		radioDeleteItems.setToolTipText(Messages.FeedPropertiesComposite_14);
		radioDeleteItems
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						itemCount.setEnabled(false);
						days.setEnabled(true);
						feed.setArchiving(Archiving.KEEP_NEWEST);
					}
				});
		days = new Spinner(archiveGroup, SWT.BORDER);
		days.setToolTipText(Messages.FeedPropertiesComposite_15);
		days.setLayoutData(gridData3);
		days.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
			public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
				try {
					feed.setArchivingDays(days.getSelection());
				} catch (NumberFormatException err) {
				}
			}
		});
		label = new Label(archiveGroup, SWT.NONE);
		label.setText(Messages.FeedPropertiesComposite_16);
		radioNoArchiving = new Button(archiveGroup, SWT.RADIO);
		radioNoArchiving.setText(Messages.FeedPropertiesComposite_17);
		radioNoArchiving.setToolTipText(Messages.FeedPropertiesComposite_18);
		radioNoArchiving
				.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						itemCount.setEnabled(false);
						days.setEnabled(false);
						feed.setArchiving(Archiving.KEEP_NONE);
					}
				});
		updateArchivingWidgets();
	}

	private void updateArchivingWidgets() {
		switch (feed.getArchiving()) {
		case KEEP_ALL:
			radioKeepAll.setSelection(true);
			itemCount.setEnabled(false);
			days.setEnabled(false);
			break;
		case KEEP_NEWEST:
			radioDeleteItems.setSelection(true);
			itemCount.setEnabled(false);
			days.setEnabled(true);
			break;
		case KEEP_SOME:
			radioKeepMax.setSelection(true);
			itemCount.setEnabled(true);
			days.setEnabled(false);
			break;
		case KEEP_NONE:
			radioNoArchiving.setSelection(true);
			itemCount.setEnabled(false);
			days.setEnabled(false);
			break;
		}
	}

	private void updateRefreshWidgets() {
		switch (feed.getUpdatePeriod()) {
		case MINUTES:
			periodCombo.setEnabled(true);
			intervalField.setEnabled(true);
			break;
		case HOURS:
			periodCombo.setEnabled(true);
			intervalField.setEnabled(true);
			break;
		case DAYS:
			periodCombo.setEnabled(true);
			intervalField.setEnabled(true);
			break;
		}
	}

	/**
	 * This method initializes updateGroup
	 * 
	 */
	private void createUpdateGroup() {
		GridData gridData7 = new GridData();
		gridData7.widthHint = 30;
		GridData gridData6 = new GridData();
		gridData6.horizontalSpan = 3;
		gridData6.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData6.grabExcessHorizontalSpace = true;
		GridLayout gridLayout2 = new GridLayout();
		gridLayout2.numColumns = 3;
		GridData gridData5 = new GridData();
		gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
		gridData5.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
		updateGroup = new Group(this, SWT.NONE);
		updateGroup.setText(Messages.FeedPropertiesComposite_19);
		updateGroup.setLayout(gridLayout2);
		updateGroup.setLayoutData(gridData5);
		label2 = new Label(updateGroup, SWT.NONE);
		label2.setText(Messages.FeedPropertiesComposite_22);
		intervalField = new Spinner(updateGroup, SWT.BORDER);
		intervalField.setToolTipText(Messages.FeedPropertiesComposite_23);
		intervalField.setLayoutData(gridData7);
		intervalField
				.addModifyListener(new org.eclipse.swt.events.ModifyListener() {
					public void modifyText(org.eclipse.swt.events.ModifyEvent e) {
						try {
							feed
									.setUpdateInterval(intervalField
											.getSelection());
						} catch (NumberFormatException err) {
						}
					}
				});
		createPeriodCombo();
		updateRefreshWidgets();
	}

	/**
	 * This method initializes periodCombo
	 * 
	 */
	private void createPeriodCombo() {
		GridData gridData8 = new GridData();
		gridData8.widthHint = 100;
		periodCombo = new Combo(updateGroup, SWT.NONE);
		periodCombo.setToolTipText(Messages.FeedPropertiesComposite_24);
		periodCombo.setLayoutData(gridData8);
		periodCombo
				.addSelectionListener(new org.eclipse.swt.events.SelectionListener() {
					public void widgetSelected(
							org.eclipse.swt.events.SelectionEvent e) {
						switch (periodCombo.getSelectionIndex()) {
						case 0:
							feed.setUpdatePeriod(UpdatePeriod.MINUTES);
							break;
						case 1:
							feed.setUpdatePeriod(UpdatePeriod.HOURS);
							break;
						case 2:
							feed.setUpdatePeriod(UpdatePeriod.DAYS);
							break;
						}
					}

					public void widgetDefaultSelected(
							org.eclipse.swt.events.SelectionEvent e) {
					}
				});
		periodCombo.add(Messages.FeedPropertiesComposite_25);
		periodCombo.add(Messages.FeedPropertiesComposite_26);
		periodCombo.add(Messages.FeedPropertiesComposite_27);

	}

	public Feed getFeed() {
		return feed;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
	}

} // @jve:decl-index=0:visual-constraint="10,10"
