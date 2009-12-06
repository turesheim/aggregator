/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
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

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This preference page is used for specifying which labels should be allows for
 * labelling articles.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class LabelsPreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private class LabelsListEditor extends ListEditor {

		public LabelsListEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
			GridData data = new GridData();
			data.verticalAlignment = GridData.FILL;
			data.horizontalAlignment = GridData.FILL;
			data.grabExcessVerticalSpace = true;
			data.grabExcessHorizontalSpace = true;
			this.getListControl(parent).setLayoutData(data);
		}

		@Override
		protected String createList(String[] items) {
			StringBuffer list = new StringBuffer();
			for (int i = 0; i < items.length; i++) {
				list.append(items[i]);
				if (i < items.length - 1)
					list.append(',');
			}
			return list.toString();
		}

		@Override
		protected String getNewInputObject() {
			InputDialog id = new InputDialog(getShell(), "New label",
					"Enter the name of the new label", "<new label>", null);
			id.open();
			return id.getValue();
		}

		@Override
		protected String[] parseString(String stringList) {
			return stringList.split(",");
		}

	}

	private static LabelsListEditor parser_macros;

	public LabelsPreferencePage() {
		super(GRID);
		setPreferenceStore(AggregatorUIPlugin.getDefault().getPreferenceStore());
		setDescription("Labels");
		setTitle("Labels");
	}

	@Override
	public void createFieldEditors() {
		addField(parser_macros = new LabelsListEditor(
				PreferenceConstants.P_ITEM_LABELS,
				"Items in the list can be used to label articles:", getFieldEditorParent())); //$NON-NLS-1$
	}

	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		parser_macros.loadDefault();
	}

}
