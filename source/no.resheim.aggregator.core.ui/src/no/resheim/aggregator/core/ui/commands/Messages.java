package no.resheim.aggregator.core.ui.commands;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.ui.commands.messages"; //$NON-NLS-1$
	public static String FeedPropertiesCommand_CANCEL;
	public static String FeedPropertiesCommand_Description;
	public static String FeedPropertiesCommand_OK;
	public static String FeedPropertiesCommand_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
