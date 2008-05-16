package no.resheim.aggregator.core.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.ui.messages"; //$NON-NLS-1$
	public static String ArticleViewer_Published;
	public static String ArticleViewer_UnknownPublicationDate;
	public static String ArticleViewer_Updated;
	public static String NewFeedWizard_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
