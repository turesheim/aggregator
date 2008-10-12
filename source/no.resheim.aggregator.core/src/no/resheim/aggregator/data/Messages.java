package no.resheim.aggregator.data;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.data.messages"; //$NON-NLS-1$
	public static String Article_Downloaded;
	public static String Article_PublishedAndDownloaded;
	public static String Article_PublishedByAndDownloaded;
	public static String FeedCollection_NoDelete_Locked;
	public static String FeedCollection_UpdateInProgress;
	public static String FeedUpdateJob_CleaningUp;
	public static String FeedUpdateJob_Error_Title;
	public static String FeedUpdateJob_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
