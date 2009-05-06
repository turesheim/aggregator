package no.resheim.aggregator.google.reader.ui;

import no.resheim.aggregator.google.reader.GoogleReaderPlugin;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class GooglePreferencePage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	public GooglePreferencePage() {
		super(GRID);
		setPreferenceStore(GoogleReaderPlugin.getDefault().getPreferenceStore());
		setDescription("Settings for the Google Reader Integration");
		setTitle("Google Reader Integration");
	}

	protected void createFieldEditors() {

		{
			addField(new IntegerFieldEditor(PreferenceConstants.P_AMOUNT,
					"Number of items to fetch initially:",
					getFieldEditorParent()));
		}
	}

	public void init(IWorkbench workbench) {
	}

}
