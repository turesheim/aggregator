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

import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.PreferenceConstants;

import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UpdatingPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public UpdatingPreferencePage() {
		super(GRID);
		setPreferenceStore(AggregatorUIPlugin.getDefault().getPreferenceStore());
		setDescription(Messages.UpdatingPreferencePage_0);
		setTitle(Messages.UpdatingPreferencePage_1);
	}

	@Override
	protected void createFieldEditors() {
		{
			addField(new ComboFieldEditor(
					PreferenceConstants.P_UPDATING_PERIOD,
					Messages.UpdatingPreferencePage_2, new String[][] {
							new String[] {
									Messages.UpdatingPreferencePage_3,
									UpdatePeriod.MINUTES.toString()
							},
							new String[] {
									Messages.UpdatingPreferencePage_4,
									UpdatePeriod.HOURS.toString()
							},
							new String[] {
									Messages.UpdatingPreferencePage_5,
									UpdatePeriod.DAYS.toString()
							},
					}, getFieldEditorParent()));
		}

		{
			addField(new IntegerFieldEditor(
					PreferenceConstants.P_UPDATING_INTERVAL,
					Messages.UpdatingPreferencePage_6, getFieldEditorParent()));
		}
	}

	public void init(IWorkbench workbench) {
	}

}
