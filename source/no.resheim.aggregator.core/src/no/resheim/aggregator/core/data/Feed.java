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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Feeds are
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public class Feed implements Comparable<Feed> {

	/**
	 * @author   torkild
	 */
	public enum Archiving {
		/**
		 * @uml.property  name="kEEP_ALL"
		 * @uml.associationEnd  
		 */
		KEEP_ALL,
		/**
		 * @uml.property  name="kEEP_NEWEST"
		 * @uml.associationEnd  
		 */
		KEEP_NEWEST,
		/**
		 * @uml.property  name="kEEP_NONE"
		 * @uml.associationEnd  
		 */
		KEEP_NONE,
		/**
		 * @uml.property  name="kEEP_SOME"
		 * @uml.associationEnd  
		 */
		KEEP_SOME
	}

	/**
	 * @author   torkild
	 */
	public enum UpdatePeriod {
		/**
		 * @uml.property  name="dAYS"
		 * @uml.associationEnd  
		 */
		DAYS, /**
		 * @uml.property  name="hOURS"
		 * @uml.associationEnd  
		 */
		HOURS, /**
		 * @uml.property  name="mINUTES"
		 * @uml.associationEnd  
		 */
		MINUTES
	}

	private static final String BLANK_STRING = ""; //$NON-NLS-1$

	/**
	 * @uml.property  name="anonymousAccess"
	 */
	protected boolean anonymousAccess = true;

	/**
	 * @uml.property  name="archiving"
	 * @uml.associationEnd  
	 */
	protected Archiving archiving = Archiving.KEEP_ALL;

	/**
	 * @uml.property  name="archivingDays"
	 */
	protected int archivingDays = 30;

	/**
	 * @uml.property  name="archivingItems"
	 */
	protected int archivingItems = 100;

	/**
	 * @uml.property  name="copyright"
	 */
	private String copyright;

	/**
	 * @uml.property  name="description"
	 */
	private String description;

	/**
	 * @uml.property  name="editor"
	 */
	private String editor;

	/**
	 * @uml.property  name="hidden"
	 */
	boolean hidden;

	/**
	 * @uml.property  name="imageData"
	 */
	byte[] imageData;

	protected boolean keepUnread;

	/**
	 * Default is OK
	 * @uml.property  name="lastStatus"
	 */
	private IStatus lastStatus = Status.OK_STATUS;

	/**
	 * @uml.property  name="lastUpdate"
	 */
	protected long lastUpdate;

	/**
	 * @uml.property  name="link"
	 */
	private String link;

	/**
	 * @uml.property  name="location"
	 */
	protected UUID location;

	/**
	 * @uml.property  name="tempItems"
	 */
	ArrayList<Article> tempItems;

	/**
	 * @uml.property  name="threaded"
	 */
	protected boolean threaded;

	/**
	 * @uml.property  name="title"
	 */
	protected String title;

	/**
	 * @uml.property  name="type"
	 */
	private String type;

	/**
	 * @uml.property  name="updateInterval"
	 */
	protected int updateInterval = 1;

	/**
	 * @uml.property  name="updatePeriod"
	 * @uml.associationEnd  
	 */
	protected UpdatePeriod updatePeriod;

	/**
	 * @uml.property  name="updating"
	 */
	boolean updating;

	protected String url = BLANK_STRING;

	protected UUID uuid;

	/**
	 * @uml.property  name="webmaster"
	 */
	private String webmaster;

	/**
	 * A unique identifier will be created for the feed as it is instantiated.
	 */
	public Feed() {
		uuid = UUID.randomUUID();
		tempItems = new ArrayList<Article>();
	}

	public int compareTo(Feed arg) {
		return this.getTitle().compareTo(arg.getTitle());
	}

	/**
	 * @return
	 * @uml.property  name="archiving"
	 */
	public Archiving getArchiving() {
		return archiving;
	}

	/**
	 * @return
	 * @uml.property  name="archivingDays"
	 */
	public int getArchivingDays() {
		return archivingDays;
	}

	/**
	 * @return
	 * @uml.property  name="archivingItems"
	 */
	public int getArchivingItems() {
		return archivingItems;
	}

	/**
	 * @return
	 * @uml.property  name="copyright"
	 */
	public String getCopyright() {
		return copyright;
	}

	/**
	 * @return
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		if (description == null) {

		}
		return description;
	}

	/**
	 * @return
	 * @uml.property  name="editor"
	 */
	public String getEditor() {
		return editor;
	}

	/**
	 * @return
	 * @uml.property  name="imageData"
	 */
	public byte[] getImageData() {
		return imageData;
	}

	/**
	 * @return
	 * @uml.property  name="lastStatus"
	 */
	public IStatus getLastStatus() {
		return lastStatus;
	}

	/**
	 * @return
	 * @uml.property  name="lastUpdate"
	 */
	public long getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @return
	 * @uml.property  name="link"
	 */
	public String getLink() {
		return link;
	}

	/**
	 * @return
	 * @uml.property  name="location"
	 */
	public UUID getLocation() {
		return location;
	}

	/**
	 * @return
	 * @uml.property  name="tempItems"
	 */
	public ArrayList<Article> getTempItems() {
		return tempItems;
	}

	/**
	 * @return
	 * @uml.property  name="title"
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return
	 * @uml.property  name="type"
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return
	 * @uml.property  name="updateInterval"
	 */
	public int getUpdateInterval() {
		return updateInterval;
	}

	/**
	 * @return
	 * @uml.property  name="updatePeriod"
	 */
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

	public UUID getUUID() {
		return uuid;
	}

	/**
	 * @return
	 * @uml.property  name="webmaster"
	 */
	public String getWebmaster() {
		return webmaster;
	}

	/**
	 * @return
	 * @uml.property  name="anonymousAccess"
	 */
	public boolean isAnonymousAccess() {
		return anonymousAccess;
	}

	/**
	 * @return
	 * @uml.property  name="hidden"
	 */
	public boolean isHidden() {
		return hidden;
	}

	/**
	 * @return
	 * @uml.property  name="threaded"
	 */
	public boolean isThreaded() {
		return threaded;
	}

	/**
	 * @return
	 * @uml.property  name="updating"
	 */
	public boolean isUpdating() {
		return updating;
	}

	public boolean keepUnread() {
		return keepUnread;
	}

	/**
	 * @param  anonymousAccess
	 * @uml.property  name="anonymousAccess"
	 */
	public void setAnonymousAccess(boolean anonymousAccess) {
		this.anonymousAccess = anonymousAccess;
	}

	/**
	 * @param  archiving
	 * @uml.property  name="archiving"
	 */
	public void setArchiving(Archiving archiving) {
		this.archiving = archiving;
	}

	/**
	 * @param  archivingDays
	 * @uml.property  name="archivingDays"
	 */
	public void setArchivingDays(int archivingDays) {
		this.archivingDays = archivingDays;
	}

	/**
	 * @param  archivingItems
	 * @uml.property  name="archivingItems"
	 */
	public void setArchivingItems(int archivingItems) {
		this.archivingItems = archivingItems;
	}

	/**
	 * @param  copyright
	 * @uml.property  name="copyright"
	 */
	public void setCopyright(String copyright) {
		this.copyright = copyright;
	}

	/**
	 * @param  description
	 * @uml.property  name="description"
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @param  editor
	 * @uml.property  name="editor"
	 */
	public void setEditor(String editor) {
		this.editor = editor;
	}

	/**
	 * @param  hidden
	 * @uml.property  name="hidden"
	 */
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @param  imageData
	 * @uml.property  name="imageData"
	 */
	public void setImageData(byte[] imageData) {
		this.imageData = imageData;
	}

	public void setKeepUread(boolean keep) {
		keepUnread = keep;
	}

	/**
	 * @param  lastStatus
	 * @uml.property  name="lastStatus"
	 */
	public void setLastStatus(IStatus lastStatus) {
		this.lastStatus = lastStatus;
	}

	/**
	 * Sets the time and date of the last feed update.
	 * @param lastUpdate   The System.currentTimeMillis() of the last update.
	 * @uml.property  name="lastUpdate"
	 */
	public void setLastUpdate(long lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	/**
	 * @param  link
	 * @uml.property  name="link"
	 */
	public void setLink(String link) {
		this.link = link;
	}

	/**
	 * Sets the identifier of the folder where articles found in this feed will be placed when downloaded.
	 * @param location   UUID of the folder
	 * @uml.property  name="location"
	 */
	public void setLocation(UUID location) {
		this.location = location;
	}

	/**
	 * @param  threaded
	 * @uml.property  name="threaded"
	 */
	public void setThreaded(boolean threaded) {
		this.threaded = threaded;
	}

	/**
	 * @param  title
	 * @uml.property  name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param  type
	 * @uml.property  name="type"
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set to -1 to get the default update interval.
	 * @param  updateInterval
	 * @uml.property  name="updateInterval"
	 */
	public void setUpdateInterval(int updateInterval) {
		this.updateInterval = updateInterval;
	}

	/**
	 * @param  updatePeriod
	 * @uml.property  name="updatePeriod"
	 */
	public void setUpdatePeriod(UpdatePeriod updatePeriod) {
		this.updatePeriod = updatePeriod;
	}

	/**
	 * @param  updating
	 * @uml.property  name="updating"
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
	 * @param  webmaster
	 * @uml.property  name="webmaster"
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
	public void updateFromWorkingCopy(FeedWorkingCopy wc) {
		title = wc.title;
		url = wc.url;
		archiving = wc.archiving;
		archivingItems = wc.archivingItems;
		archivingDays = wc.archivingDays;
		updateInterval = wc.updateInterval;
		updatePeriod = wc.updatePeriod;
		hidden = wc.hidden;
		anonymousAccess = wc.anonymousAccess;
		keepUnread = wc.keepUnread;
	}
}
