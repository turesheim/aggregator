/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.data;

import java.util.UUID;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedWorkingCopy extends Feed {
	Feed feed;

	public static FeedWorkingCopy newInstance(IAggregatorItem parent) {
		Feed feed = new Feed();
		feed.setUUID(UUID.randomUUID());
		return new FeedWorkingCopy(feed);
	}

	public FeedWorkingCopy(Feed feed) {
		super();
		this.feed = feed;
		copy(feed);
	}

	public void copy(Feed feed) {
		this.title = feed.getTitle();
		this.url = feed.url;
		this.archiving = feed.archiving;
		this.archivingItems = feed.archivingItems;
		this.archivingDays = feed.archivingDays;
		this.updateInterval = feed.updateInterval;
		this.updatePeriod = feed.updatePeriod;
		this.hidden = feed.hidden;
	}

	public Feed getFeed() {
		if (feed != null) {
			feed.setTitle(title);
			feed.url = url;
			feed.archiving = archiving;
			feed.archivingItems = archivingItems;
			feed.archivingDays = archivingDays;
			feed.updateInterval = updateInterval;
			feed.updatePeriod = updatePeriod;
			feed.hidden = hidden;
		}
		return feed;
	}

}
