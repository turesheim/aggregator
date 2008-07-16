/*******************************************************************************
 * Copyright (c) 2007-2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.data;

import no.resheim.aggregator.data.internal.AggregatorItem;

/**
 * Feeds are created by the user and immediately inserted into the database,
 * thus it's not the parsing of the feed stream that will result in a feed being
 * created as with an feed item.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Feed extends AggregatorItem {

	/**
	 * 
	 */
	public enum Archiving {
		/** Archive all items */
		KEEP_ALL,
		/** Archive items no older than a number of days */
		KEEP_NEWEST,
		/** Do not archive items */
		KEEP_NONE,
		/** Archive a specified number of items */
		KEEP_SOME
	}

	public enum UpdatePeriod {
		MINUTES, HOURS, DAYS
	}

	private static final String BLANK_STRING = ""; //$NON-NLS-1$

	protected Archiving archiving = Archiving.KEEP_ALL;

	protected int archivingDays = 30;

	protected int archivingItems = 100;

	private String copyright;

	private String description;

	private String editor;

	boolean hidden;

	protected long lastUpdate;

	private String link;

	private String password;
	boolean threaded;
	protected String title = BLANK_STRING;

	private String type;
	protected int updateInterval = 1;
	protected UpdatePeriod updatePeriod;
	boolean updating;
	protected String url = BLANK_STRING;
	private String username;
	private String webmaster;

	/**
	 * @param parent
	 */
	Feed(IAggregatorItem parent) {
		super(parent);
	}

	public Archiving getArchiving() {
		return archiving;
	}

	public int getArchivingDays() {
		return archivingDays;
	}

	public int getArchivingItems() {
		return archivingItems;
	}

	public String getCopyright() {
		return copyright;
	}

	public String getDescription() {
		return description;
	}

	public String getEditor() {
		return editor;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public String getLink() {
		return link;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		if (title != null) {
			return title;
		} else
			return BLANK_STRING;
	}

	public String getType() {
		return type;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	public UpdatePeriod getUpdatePeriod() {
		return updatePeriod;
	}

	/**
	 * Returns the calculated update interval of the feed in milliseconds. This
	 * field should not be serialized.
	 * 
	 * @return The update interval
	 */
	public long getUpdateTime() {
		long time = -1;
		switch (updatePeriod) {
		case MINUTES:
			time = updateInterval * 60 * 1000;
			break;
		case HOURS:
			time = updateInterval * 60 * 1000 * 60;
			break;
		case DAYS:
			time = updateInterval * 60 * 1000 * 60 * 24;
			break;
		}
		return time;
	}

	/**
	 * @return the link
	 */
	public String getURL() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getWebmaster() {
		return webmaster;
	}

	public boolean isHidden() {
		return hidden;
	}

	public boolean isThreaded() {
		return threaded;
	}

	public boolean isUpdating() {
		return updating;
	}

	public void setArchiving(Archiving archiving) {
		this.archiving = archiving;
	}

	public void setArchivingDays(int archivingDays) {
		this.archivingDays = archivingDays;
	}

	public void setArchivingItems(int archivingItems) {
		this.archivingItems = archivingItems;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * Sets the time and date of the last feed update.
	 * 
	 * @param lastUpdate
	 *            The System.currentTimeMillis() of the last update.
	 */
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set to -1 to get the default update interval.
	 * 
	 * @param updateInterval
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	public void setUpdatePeriod(UpdatePeriod updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public void setURL(String link) {
		this.url = link;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setWebmaster(String webmaster) {
		this.webmaster = webmaster;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		sb.append(" ["); //$NON-NLS-1$
		sb.append(getOrdering());
		sb.append(']');
		return sb.toString();
	}

	/**
	 * Updates this feed instance with values from the working copy.
	 * 
	 * @param wc
	 */
	public void updateFromWorkingCopy(FeedWorkingCopy wc) {
		this.title = wc.title;
		this.url = wc.url;
		this.archiving = wc.archiving;
		this.archivingItems = wc.archivingItems;
		this.archivingDays = wc.archivingDays;
		this.updateInterval = wc.updateInterval;
		this.updatePeriod = wc.updatePeriod;
		this.hidden = wc.hidden;
	}

}
