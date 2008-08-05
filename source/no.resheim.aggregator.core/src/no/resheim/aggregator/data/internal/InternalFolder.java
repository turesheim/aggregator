package no.resheim.aggregator.data.internal;

import java.util.UUID;

import no.resheim.aggregator.data.AggregatorUIItem;
import no.resheim.aggregator.data.Folder;

public class InternalFolder extends Folder {

	public InternalFolder(AggregatorUIItem parent, UUID uuid) {
		super(parent, uuid);
	}

	public void setFeed(UUID feed) {
		this.feed = feed;
	}

}
