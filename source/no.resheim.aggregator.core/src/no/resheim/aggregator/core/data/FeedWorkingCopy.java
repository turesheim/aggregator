/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.core.data;

import java.util.UUID;

/**
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public final class FeedWorkingCopy extends Feed {
	public static FeedWorkingCopy newInstance(AggregatorItem parent) {
		Feed feed = new Feed();
		feed.setUUID(UUID.randomUUID());
		return new FeedWorkingCopy(feed);
	}

	/**
	 * @uml.property  name="feed"
	 * @uml.associationEnd  
	 */
	Feed feed;

	/**
	 * @uml.property  name="password"
	 */
	protected String password;

	/**
	 * @uml.property  name="username"
	 */
	protected String username;

	public FeedWorkingCopy(Feed feed) {
		super();
		this.feed = feed;
		this.uuid = feed.uuid;
		copy(feed);
	}

	public void copy(Feed feed) {
		title = feed.getTitle();
		url = feed.url;
		archiving = feed.archiving;
		archivingItems = feed.archivingItems;
		archivingDays = feed.archivingDays;
		updateInterval = feed.updateInterval;
		updatePeriod = feed.updatePeriod;
		hidden = feed.hidden;
		anonymousAccess = feed.anonymousAccess;
		keepUnread = feed.keepUnread;
	}

	/**
	 * @return
	 * @uml.property  name="feed"
	 */
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
			feed.anonymousAccess = anonymousAccess;
			feed.keepUnread = keepUnread;
		}
		return feed;
	}

	/**
	 * @return
	 * @uml.property  name="password"
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 * @uml.property  name="username"
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param  password
	 * @uml.property  name="password"
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param  username
	 * @uml.property  name="username"
	 */
	public void setUsername(String username) {
		this.username = username;
	}

}
