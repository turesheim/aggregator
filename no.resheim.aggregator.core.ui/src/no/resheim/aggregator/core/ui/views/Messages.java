package no.resheim.aggregator.core.ui.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.ui.views.messages"; //$NON-NLS-1$
	public static String RSSView_BrowserTitle;
	public static String RSSView_HorizontalActionTitle;
	public static String RSSView_LayoutMenuTitle;
	public static String RSSView_VerticalActionTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
