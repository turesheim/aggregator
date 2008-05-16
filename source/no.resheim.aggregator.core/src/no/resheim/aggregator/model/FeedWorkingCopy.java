/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.model;

import java.util.UUID;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedWorkingCopy extends Feed {
	Feed feed;

	public FeedWorkingCopy(UUID uuid, UUID parentUuid) {
		this.uuid = uuid;
		this.parent_uuid = parentUuid;
	}

	public FeedWorkingCopy(Feed feed) {
		this.feed = feed;
		this.title = feed.title;
		this.url = feed.url;
		this.archiving = feed.archiving;
		this.archivingItems = feed.archivingItems;
		this.archivingDays = feed.archivingDays;
		this.updateInterval = feed.updateInterval;
		this.updatePeriod = feed.updatePeriod;
		this.hidden = feed.hidden;
	}

	public Feed getFeed() {
		if (feed == null) {
			feed = new Feed();
		}
		feed.title = title;
		feed.url = url;
		feed.archiving = archiving;
		feed.archivingItems = archivingItems;
		feed.archivingDays = archivingDays;
		feed.updateInterval = updateInterval;
		feed.updatePeriod = updatePeriod;
		feed.uuid = uuid;
		feed.parent_uuid = parent_uuid;
		feed.hidden = hidden;
		return feed;
	}

}
