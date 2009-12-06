package no.resheim.aggregator.core.synch;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.synch.messages"; //$NON-NLS-1$
	public static String FeedUpdateJob_CleaningUp;
	public static String FeedUpdateJob_CredentialsError;
	public static String FeedUpdateJob_Error_Title;
	public static String FeedUpdateJob_HostError;
	public static String FeedUpdateJob_StatusTitle;
	public static String FeedUpdateJob_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
