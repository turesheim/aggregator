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
import no.resheim.aggregator.model.Feed.Archiving;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Specifies aggregator archiving preferences.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ArchivingPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {

	public ArchivingPreferencePage() {
		super(GRID);
		setPreferenceStore(AggregatorUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.ArchivingPreferencePage_Description);
		setTitle(Messages.ArchivingPreferencePage_Title);
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void createFieldEditors() {
		{
			addField(new RadioGroupFieldEditor(
					PreferenceConstants.P_ARCHIVING_METHOD,
					Messages.ArchivingPreferencePage_Method,
					1,
					new String[][] {
							new String[] {
									Messages.ArchivingPreferencePage_Keep_All,
									Archiving.KEEP_ALL.toString()
							},
							new String[] {
									Messages.ArchivingPreferencePage_Keep_Some,
									Archiving.KEEP_SOME.toString()
							},
							new String[] {
									Messages.ArchivingPreferencePage_Keep_Newest,
									Archiving.KEEP_NEWEST.toString()
							},
							new String[] {
									Messages.ArchivingPreferencePage_Keep_None,
									Archiving.KEEP_NONE.toString()
							}
					}, getFieldEditorParent(), true));
		}

		{
			addField(new IntegerFieldEditor(
					PreferenceConstants.P_ARCHIVING_DAYS,
					Messages.ArchivingPreferencePage_Days,
					getFieldEditorParent()));
		}
		{
			addField(new IntegerFieldEditor(
					PreferenceConstants.P_ARCHIVING_ITEMS,
					Messages.ArchivingPreferencePage_Items,
					getFieldEditorParent()));
		}

	}

}
