package no.resheim.aggregator.model;

import java.util.UUID;

public interface IAggregatorItem {

	public abstract IAggregatorItem getParent();

	public abstract UUID getUUID();

}
