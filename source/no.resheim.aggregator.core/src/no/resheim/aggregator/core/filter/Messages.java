package no.resheim.aggregator.core.filter;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.filter.messages"; //$NON-NLS-1$
	public static String Criterion_Field_Author;
	public static String Criterion_Field_Read;
	public static String Criterion_Field_Text;
	public static String Criterion_Field_Title;
	public static String Criterion_Field_Type;
	public static String Criterion_Op_Contains;
	public static String Criterion_Op_Does_Not_Contain;
	public static String Criterion_Op_Does_Not_Equal;
	public static String Criterion_Op_Does_Not_Match_Regexp;
	public static String Criterion_Op_Equals;
	public static String Criterion_Op_Matches_Regexp;
	public static String FilterJob_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
