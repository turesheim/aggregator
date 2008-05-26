package no.resheim.aggregator.core.ui.commands;

import no.resheim.aggregator.AggregatorPlugin;

import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.ParameterValueConversionException;

public class CollectionParameterValueConverter extends
		AbstractParameterValueConverter {

	public CollectionParameterValueConverter() {
	}

	@Override
	public Object convertToObject(String parameterValue)
			throws ParameterValueConversionException {
		return AggregatorPlugin.getDefault().getFeedCollection(parameterValue);
	}

	@Override
	public String convertToString(Object parameterValue)
			throws ParameterValueConversionException {
		System.out.println(parameterValue.getClass().toString());
		return parameterValue.toString();
	}

}
