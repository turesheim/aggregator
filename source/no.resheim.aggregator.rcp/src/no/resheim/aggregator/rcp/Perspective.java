package no.resheim.aggregator.rcp;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {
	public static final String BROWSER_ID = "org.eclipse.ui.browser.view";

	private static final String MAIN_VIEW_ID = "no.resheim.aggregator.core.ui.RSSView"; //$NON-NLS-1$
	private static final String ARTICLES_VIEW_ID = "no.resheim.aggregator.core.ui.ArticlesView"; //$NON-NLS-1$
	private static final String ARTICLE_VIEW_ID = "no.resheim.aggregator.core.ui.ArticleView"; //$NON-NLS-1$

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		// We don't need the editor area.
		layout.setEditorAreaVisible(false);
		layout.setFixed(false);
		layout.addStandaloneView(MAIN_VIEW_ID, false, IPageLayout.LEFT, 0.3f,
				editorArea);
		layout.addStandaloneView(ARTICLES_VIEW_ID, true, IPageLayout.LEFT,
				0.5f, editorArea);
		layout.addStandaloneView(ARTICLE_VIEW_ID, false, IPageLayout.BOTTOM,
				0.5f, ARTICLES_VIEW_ID);
	}
}
