package no.resheim.aggregator.core.data.internal;

import java.util.UUID;

import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Folder;

public class InternalFolder extends Folder {

	public InternalFolder(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
	}

	public void setFeed(UUID feed) {
		this.feed = feed;
	}

}
