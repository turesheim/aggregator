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
package no.resheim.aggregator.core.ui.wizards;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.ui.wizards.messages"; //$NON-NLS-1$
	public static String NewFeedWizardGeneralPage_Anonymous;
	public static String NewFeedWizardGeneralPage_Description;
	public static String NewFeedWizardGeneralPage_Error_Bad_URL;
	public static String NewFeedWizardGeneralPage_Error_Existing_Feed;
	public static String NewFeedWizardGeneralPage_Error_Missing_title;
	public static String NewFeedWizardGeneralPage_Error_Missing_URL;
	public static String NewFeedWizardGeneralPage_Label_Image;
	public static String NewFeedWizardGeneralPage_Label_Select;
	public static String NewFeedWizardGeneralPage_Label_Title;
	public static String NewFeedWizardGeneralPage_Label_URL;
	public static String NewFeedWizardGeneralPage_Login;
	public static String NewFeedWizardGeneralPage_Password;
	public static String NewFeedWizardGeneralPage_Title;
	public static String NewFeedWizardOptionsPage_Description;
	public static String NewFeedWizardOptionsPage_Label_Archiving_Group;
	public static String NewFeedWizardOptionsPage_Label_days;
	public static String NewFeedWizardOptionsPage_Label_Days;
	public static String NewFeedWizardOptionsPage_Label_Hours;
	public static String NewFeedWizardOptionsPage_Label_items;
	public static String NewFeedWizardOptionsPage_Label_Keep_All;
	public static String NewFeedWizardOptionsPage_Label_Keep_newest;
	public static String NewFeedWizardOptionsPage_Label_Keep_none;
	public static String NewFeedWizardOptionsPage_Label_Keep_Some;
	public static String NewFeedWizardOptionsPage_Label_Minutes;
	public static String NewFeedWizardOptionsPage_Label_Update_every;
	public static String NewFeedWizardOptionsPage_Label_Updating_Group;
	public static String NewFeedWizardOptionsPage_Label_Use_Preferences;
	public static String NewFeedWizardOptionsPage_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
