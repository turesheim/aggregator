/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.core.data;

import java.util.UUID;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public final class SubscriptionWorkingCopy extends Subscription {
	public static SubscriptionWorkingCopy newInstance(AggregatorItem parent) {
		Subscription feed = new Subscription();
		feed.setUUID(UUID.randomUUID());
		return new SubscriptionWorkingCopy(feed);
	}

	Subscription feed;

	protected String password;

	protected String username;

	public SubscriptionWorkingCopy(Subscription feed) {
		super();
		this.feed = feed;
		setUUID(feed.getUUID());
		copy(feed);
	}

	// FIXME: Use clone instead?
	public void copy(Subscription feed) {
		setTitle(feed.getTitle());
		setURL(feed.getURL());
		archiving = feed.archiving;
		archivingItems = feed.archivingItems;
		archivingDays = feed.archivingDays;
		setUpdateInterval(feed.getUpdateInterval());
		setUpdatePeriod(feed.getUpdatePeriod());
		hidden = feed.hidden;
		anonymousAccess = feed.anonymousAccess;
		keepUnread = feed.keepUnread;
		setLastStatus(feed.getLastStatus());
		setSynchronizer(feed.getSynchronizer());
	}

	/**
	 * @return
	 */
	public Subscription getFeed() {
		if (feed != null) {
			feed.setTitle(getTitle());
			feed.setURL(getURL());
			feed.archiving = archiving;
			feed.archivingItems = archivingItems;
			feed.archivingDays = archivingDays;
			feed.setUpdateInterval(getUpdateInterval());
			feed.setUpdatePeriod(getUpdatePeriod());
			feed.hidden = hidden;
			feed.anonymousAccess = anonymousAccess;
			feed.keepUnread = keepUnread;
			feed.setLastStatus(getLastStatus());
			feed.setSynchronizer(getSynchronizer());
		}
		return feed;
	}

	/**
	 * @return
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param password
	 */
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @param username
	 */
	public void setUsername(String username) {
		this.username = username;
	}

}
