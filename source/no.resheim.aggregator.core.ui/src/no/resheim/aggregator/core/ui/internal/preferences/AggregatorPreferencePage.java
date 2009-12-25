/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui.internal.preferences;

import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.PreferenceConstants;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * @author Torkild Ulvøy Resheim
 */

public class AggregatorPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public AggregatorPreferencePage() {
		super(GRID);
		setPreferenceStore(AggregatorUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.AggregatorPreferencePage_Description);
		setTitle(Messages.AggregatorPreferencePage_Title);
	}

	public void createFieldEditors() {

		{
			addField(new BooleanFieldEditor(
					PreferenceConstants.P_PREVIEW_IS_READ,
					Messages.AggregatorPreferencePage_Preview_is_read,
					getFieldEditorParent()));
		}

		{
			addField(new BooleanFieldEditor(
					PreferenceConstants.P_SHOW_UNREAD_COUNT,
					Messages.AggregatorPreferencePage_Show_Unread_Count,
					getFieldEditorParent()));
		}

		{
			addField(new ColorFieldEditor(
					PreferenceConstants.P_UNREAD_ITEM_COLOR,
					Messages.AggregatorPreferencePage_UnreadItemColor,
					getFieldEditorParent()));
		}

		{
			addField(new RadioGroupFieldEditor(PreferenceConstants.P_OPEN_LINK,
					Messages.AggregatorPreferencePage_Open_Links_In, 1,
					new String[][] {
							new String[] {
									Messages.AggregatorPreferencePage_View,
									PreferenceConstants.LinkOpen.VIEW
											.toString() },
							new String[] {
									Messages.AggregatorPreferencePage_Editor,
									PreferenceConstants.LinkOpen.EDITOR
											.toString() },
							new String[] {
									Messages.AggregatorPreferencePage_External,
									PreferenceConstants.LinkOpen.EXTERNAL
											.toString() } },
					getFieldEditorParent(), true));
		}

		{
			addField(new BooleanFieldEditor(
					PreferenceConstants.P_BROWSER_LOCATION_BAR,
					Messages.AggregatorPreferencePage_Location_Bar,
					getFieldEditorParent()));
		}

		{
			addField(new BooleanFieldEditor(
					PreferenceConstants.P_BROWSER_NAVIGATION_BAR,
					Messages.AggregatorPreferencePage_Navigation_Bar,
					getFieldEditorParent()));
		}

		{
			addField(new BooleanFieldEditor(
					PreferenceConstants.P_BROWSER_CREATE_NEW_WINDOW,
					Messages.AggregatorPreferencePage_Reuse_Window,
					getFieldEditorParent()));
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}