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
package no.resheim.aggregator.core.data;

import java.util.ArrayList;
import java.util.UUID;

import no.resheim.aggregator.core.catalog.Feed;
import no.resheim.aggregator.core.catalog.IFeedCatalog;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A {@link Subscription} is a representation of the subscription to a feed.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Subscription implements Comparable<Subscription> {
	/**
	 * @author Torkild Ulvøy Resheim
	 * @since 1.0
	 */
	public enum Archiving {
		/** Keep all items */
		KEEP_ALL,
		/** Keep only items no older than a given date */
		KEEP_NEWEST,
		/** Never keep any old items */
		KEEP_NONE,
		/** Keep only a given number of items */
		KEEP_SOME
	}

	/**
	 * @author Torkild Ulvøy Resheim
	 */
	public enum UpdatePeriod {
		DAYS, HOURS, MINUTES
	}

	private static final String BLANK_STRING = ""; //$NON-NLS-1$

	protected boolean anonymousAccess = true;

	protected Archiving archiving = Archiving.KEEP_ALL;

	protected int archivingDays = 30;

	protected int archivingItems = 100;

	/** The catalogue used to create this instance or <code>null</code> */
	private IFeedCatalog catalog;

	private String copyright;

	private String description;

	private String editor;

	boolean hidden;

	byte[] imageData;

	protected boolean keepUnread;

	/**
	 * The status code received the last time the feed was attempted updated.
	 */
	private IStatus lastStatus = Status.OK_STATUS;

	private long lastUpdate;

	private String link;

	private UUID location;

	private String synchronizerId;

	private ArrayList<Article> tempItems;

	private boolean threaded;

	private String title;

	private String type;

	/**
	 * The default is to update every hour.
	 */
	private int updateInterval = 1;

	/**
	 * The default is to update every hour.
	 */
	private UpdatePeriod updatePeriod = UpdatePeriod.HOURS;

	private boolean updating;

	private String url = BLANK_STRING;

	private UUID uuid;

	private String webmaster;

	/**
	 * A unique identifier will be created for the feed as it is instantiated.
	 */
	public Subscription() {
		uuid = UUID.randomUUID();
		tempItems = new ArrayList<Article>();
		// Use the default synchronizer and replace later if required.
		synchronizerId = IFeedCatalog.DEFAULT_SYNCHRONIZER_ID;
	}

	public Subscription(IFeedCatalog catalog) {
		this();
		synchronizerId = catalog.getSynchronizerId();
		this.catalog = catalog;
	}

	public int compareTo(Subscription arg) {
		return this.getTitle().compareTo(arg.getTitle());
	}

	/**
	 * @return
	 * @uml.property name="archiving"
	 */
	public Archiving getArchiving() {
		return archiving;
	}

	/**
	 * @return
	 * @uml.property name="archivingDays"
	 */
	public int getArchivingDays() {
		return archivingDays;
	}

	/**
	 * @return
	 * @uml.property name="archivingItems"
	 */
	public int getArchivingItems() {
		return archivingItems;
	}

	/**
	 * Returns the catalogue used when creating this feed.
	 * 
	 * @return the feed catalogue
	 */
	public IFeedCatalog getCatalog() {
		return catalog;
	}

	/**
	 * @return
	 * @uml.property name="copyright"
	 */
	public String getCopyright() {
		return copyright;
	}

	/**
	 * @return
	 * @uml.property name="description"
	 */
	public String getDescription() {
		if (description == null) {

		}
		return description;
	}

	/**
	 * @return
	 * @uml.property name="editor"
	 */
	public String getEditor() {
		return editor;
	}

	/**
	 * @return
	 * @uml.property name="imageData"
	 */
	public byte[] getImageData() {
		return imageData;
	}

	/**
	 * @return
	 * @uml.property name="lastStatus"
	 */
	public IStatus getLastStatus() {
		return lastStatus;
	}

	/**
	 * Returns the time this subscription was last updated.
	 * 
	 * @return last update date
	 * @uml.property name="lastUpdate"
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @return
	 * @uml.property name="link"
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @return
	 * @uml.property name="location"
	 */
	public UUID getLocation() {
		return location;
	}

	/**
	 * Returns the identifier of the synchronisation mechanism to use. "feed"
	 * means the internal feed update mechanism. While "google-reader" use the
	 * Google Reader synchroniser.
	 * 
	 * 
	 * @return the identifier of the synchronisation mechanism.
	 */
	public String getSynchronizer() {
		return synchronizerId;
	}

	/**
	 * @return
	 */
	public ArrayList<Article> getTempItems() {
		return tempItems;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return
	 */
	public int getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * @return
	 */
	public UpdatePeriod getUpdatePeriod() {
		return updatePeriod;
	}

	/**
	 * Returns the calculated update interval of the feed in milliseconds. This
	 * field should not be serialised.
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

	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @return
	 */
	public String getWebmaster() {
		return webmaster;
	}

	/**
	 * @return
	 */
	public boolean isAnonymousAccess() {
		return anonymousAccess;
	}

	/**
	 * @return
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * Indicates whether or not the feed items are threaded.
	 * 
	 * @return whether or not the feed items are threaded.
	 */
	public boolean isThreaded() {
		return threaded;
	}

	/**
	 * @return
	 */
	public boolean isUpdating() {
		return updating;
	}

	public boolean keepUnread() {
		return keepUnread;
	}

	/**
	 * @param anonymousAccess
	 */
	public void setAnonymousAccess(boolean anonymousAccess) {
		this.anonymousAccess = anonymousAccess;
	}

	/**
	 * @param archiving
	 */
	public void setArchiving(Archiving archiving) {
		this.archiving = archiving;
	}

	/**
	 * @param archivingDays
	 */
	public void setArchivingDays(int archivingDays) {
		this.archivingDays = archivingDays;
	}

	/**
	 * @param archivingItems
	 */
	public void setArchivingItems(int archivingItems) {
		this.archivingItems = archivingItems;
	}

	/**
	 * @param copyright
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param editor
	 */
	public void setEditor(String editor) {
		this.editor = editor;
	}

	/**
	 * @param hidden
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param imageData
	 */
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public void setKeepUread(boolean keep) {
		keepUnread = keep;
	}

	/**
	 * @param lastStatus
	 */
	public void setLastStatus(IStatus lastStatus) {
		this.lastStatus = lastStatus;
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

	/**
	 * @param link
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Sets the identifier of the folder where articles found in this feed will
	 * be placed when downloaded.
	 * 
	 * @param location
	 *            UUID of the folder
	 */
	public void setLocation(UUID location) {
		this.location = location;
	}

	/**
	 * Specifies the mechanism to use for the updating the feed. The value here
	 * must match the <b>id</b> of an
	 * <b>no.resheim.aggregator.core.synchronizers</b> extension declaration.
	 * 
	 * @param synchronizer
	 */
	public void setSynchronizer(String synchronizer) {
		this.synchronizerId = synchronizer;
	}

	/**
	 * @param threaded
	 */
	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	/**
	 * @param title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param type
	 */
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

	/**
	 * @param updatePeriod
	 */
	public void setUpdatePeriod(UpdatePeriod updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	/**
	 * @param updating
	 */
	public void setUpdating(boolean updating) {
		this.updating = updating;
	}

	public void setURL(String link) {
		this.url = link;
	}

	public void setUUID(UUID uuid) {
		this.uuid = uuid;
	}

	/**
	 * @param webmaster
	 *            TODO: Move to {@link Feed}
	 */
	public void setWebmaster(String webmaster) {
		this.webmaster = webmaster;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(title);
		return sb.toString();
	}

	/**
	 * Updates this feed instance with values from the working copy.
	 * 
	 * @param wc
	 */
	public void updateFromWorkingCopy(SubscriptionWorkingCopy wc) {
		title = wc.getTitle();
		url = wc.getURL();
		archiving = wc.archiving;
		archivingItems = wc.archivingItems;
		archivingDays = wc.archivingDays;
		updateInterval = wc.getUpdateInterval();
		updatePeriod = wc.getUpdatePeriod();
		hidden = wc.hidden;
		anonymousAccess = wc.anonymousAccess;
		keepUnread = wc.keepUnread;
	}

	void validate() {
		Assert.isNotNull(getUUID(), "Unspecified feed UUID"); //$NON-NLS-1$

	}
}
