package no.resheim.aggregator.ui.test;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.ui.test.messages"; //$NON-NLS-1$
	public static String CreateArticlesHandler_NewArticle_Description;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
