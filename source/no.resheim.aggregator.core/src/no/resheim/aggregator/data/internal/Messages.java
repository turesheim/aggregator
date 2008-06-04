package no.resheim.aggregator.data.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.data.internal.messages"; //$NON-NLS-1$
	public static String DerbySQLStorage_Creating_Storage;
	public static String DerbySQLStorage_Creating_Tables;
	public static String DerbySQLStorage_Initializing_Database;
	public static String RegistryUpdateJob_Label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
