package no.resheim.aggregator.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.model.messages"; //$NON-NLS-1$
	public static String FeedUpdateJob_Error_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
