package no.resheim.aggregator.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	private static final String MAIN_VIEW_ID = "no.resheim.aggregator.core.ui.RSSView"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		layout.setFixed(true);

		layout.addStandaloneView(MAIN_VIEW_ID, false, IPageLayout.LEFT, 1.0f,
				editorArea);
	}

}