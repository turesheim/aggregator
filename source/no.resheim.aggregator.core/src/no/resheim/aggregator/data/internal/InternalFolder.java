package no.resheim.aggregator.data.internal;

import java.util.UUID;

import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.AggregatorItemParent;

public class InternalFolder extends Folder {

	public InternalFolder(AggregatorItemParent parent, UUID uuid) {
		super(parent, uuid);
	}

	public void setFeed(UUID feed) {
		this.feed = feed;
	}

}
