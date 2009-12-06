package no.resheim.aggregator.core.data;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.data.messages"; //$NON-NLS-1$
	public static String Article_Downloaded;
	public static String Article_PublishedAndDownloaded;
	public static String Article_PublishedByAndDownloaded;
	public static String FeedCollection_NoDelete_Locked;
	public static String FeedCollection_UpdateInProgress;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
