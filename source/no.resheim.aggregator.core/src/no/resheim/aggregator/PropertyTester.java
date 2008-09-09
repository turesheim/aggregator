package no.resheim.aggregator;

public class PropertyTester extends org.eclipse.core.expressions.PropertyTester {

	public PropertyTester() {
		// TODO Auto-generated constructor stub
	}

	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		System.out.println(receiver.getClass().toString());
		if (property != null && property.equals("multipleCollections")) { //$NON-NLS-1$
			return (AggregatorPlugin.getDefault().getCollections().size() > 1);
		}
		return false;
	}
}
