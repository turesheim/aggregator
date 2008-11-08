package no.resheim.aggregator.core.rss.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.rss.internal.messages"; //$NON-NLS-1$
	public static String RSSFeedHandler_Unrecognized_Feed_Type;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
