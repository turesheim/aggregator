package no.resheim.aggregator.sdk.ui;

import no.resheim.aggregator.core.ui.commands.AbstractAggregatorCommandHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;

public class FixDatabaseCommandHandler extends AbstractAggregatorCommandHandler
		implements IHandler {

	public FixDatabaseCommandHandler(boolean disallowSystemItems) {
		super(disallowSystemItems);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

}
