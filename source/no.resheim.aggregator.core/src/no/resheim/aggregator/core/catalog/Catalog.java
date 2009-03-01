package no.resheim.aggregator.core.catalog;

import java.util.ArrayList;
import java.util.List;

import no.resheim.aggregator.core.data.Feed;

/**
 * FIXME: Use the declared instance instance instead.
 * 
 * @author turesheim
 * @deprecated
 */
public class Catalog {
	private String name;
	private String icon;
	private String bundle;
	private ArrayList<Feed> feeds;

	public Catalog(String name, List<Feed> feeds, String icon, String bundle) {
		super();
		this.name = name;
		this.feeds = new ArrayList<Feed>();
		this.feeds.addAll(feeds);
		this.icon = icon;
		this.bundle = bundle;
	}

	public String getIcon() {
		return icon;
	}

	public String getBundle() {
		return bundle;
	}

	public Feed[] getFeeds() {
		return feeds.toArray(new Feed[feeds.size()]);
	}

	public String getName() {
		return name;
	}

}
