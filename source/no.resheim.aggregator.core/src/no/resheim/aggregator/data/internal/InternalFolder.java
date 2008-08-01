package no.resheim.aggregator.data.internal;

import java.util.UUID;

import no.resheim.aggregator.data.Folder;

public class InternalFolder extends Folder {

	public InternalFolder(AggregatorUIItem parent) {
		super(parent);
	}

	public void setFeed(UUID feed) {
		this.feed = feed;
	}

}
