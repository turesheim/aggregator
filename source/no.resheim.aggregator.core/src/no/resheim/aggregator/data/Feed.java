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

/**
 * Feeds are created by the user and immediately inserted into the database,
 * thus it's not the parsing of the feed stream that will result in a feed being
 * created as with an feed item.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Feed extends AbstractAggregatorItem {

	private static final String BLANK_STRING = ""; //$NON-NLS-1$

	/**
	 * 
	 */
	public enum Archiving {
		/** Do not archive items */
		KEEP_NONE,
		/** Archive all items */
		KEEP_ALL,
		/** Archive a specified number of items */
		KEEP_SOME,
		/** Archive items no older than a number of days */
		KEEP_NEWEST
	}

	public enum UpdatePeriod {
		/** Update every n minutes */
		MINUTES,
		/** Update every n hours */
		HOURS,
		/** Update every n days */
		DAYS
	}

	protected String title = BLANK_STRING;

	protected String url = BLANK_STRING;

	protected Archiving archiving = Archiving.KEEP_ALL;

	protected int archivingItems = 100;

	protected int archivingDays = 30;

	protected int updateInterval = 1;

	protected UpdatePeriod updatePeriod;

	protected long lastUpdate;

	boolean updating;

	boolean hidden;

	private IAggregatorItem parent;
	private String description;
	private String link;
	private String webmaster;
	private String editor;
	private String copyright;
	private String type;

	/**
	 * @return the link
	 */
	public String getURL() {
		return url;
	}

	public void setURL(String link) {
		this.url = link;
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

	public void setTitle(String title) {
		this.title = title;
	}

	public Feed() {
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
	 * Sets the time and date of the last feed update.
	 * 
	 * @param lastUpdate
	 *            The System.currentTimeMillis() of the last update.
	 */
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	public Archiving getArchiving() {
		return archiving;
	}

	public void setArchiving(Archiving archiving) {
		this.archiving = archiving;
	}

	public int getArchivingDays() {
		return archivingDays;
	}

	public void setArchivingDays(int archivingDays) {
		this.archivingDays = archivingDays;
	}

	public int getArchivingItems() {
		return archivingItems;
	}

	public void setArchivingItems(int archivingItems) {
		this.archivingItems = archivingItems;
	}

	public UpdatePeriod getUpdatePeriod() {
		return updatePeriod;
	}

	public void setUpdatePeriod(UpdatePeriod updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	public int getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * Set to -1 to get the default update interval.
	 * 
	 * @param updateInterval
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
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

	public boolean isUpdating() {
		return updating;
	}

	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public IAggregatorItem getParent() {
		return parent;
	}

	void setParent(IAggregatorItem parent) {
		this.parent = parent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getWebmaster() {
		return webmaster;
	}

	public void setWebmaster(String webmaster) {
		this.webmaster = webmaster;
	}

	public String getEditor() {
		return editor;
	}

	public void setEditor(String editor) {
		this.editor = editor;
	}

	public String getCopyright() {
		return copyright;
	}

	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return getTitle();
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

}
