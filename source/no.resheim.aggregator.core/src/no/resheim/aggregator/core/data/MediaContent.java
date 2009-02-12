/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.data;

/**
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public class MediaContent {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * @author   torkild
	 */
	public enum Expression {
		/**
		 * @uml.property  name="fULL"
		 * @uml.associationEnd  
		 */
		FULL, /**
		 * @uml.property  name="nONSTOP"
		 * @uml.associationEnd  
		 */
		NONSTOP, /**
		 * @uml.property  name="sAMPLE"
		 * @uml.associationEnd  
		 */
		SAMPLE
	}

	/**
	 * @author   torkild
	 */
	public enum Medium {
		/**
		 * @uml.property  name="aUDIO"
		 * @uml.associationEnd  
		 */
		AUDIO, /**
		 * @uml.property  name="dOCUMENT"
		 * @uml.associationEnd  
		 */
		DOCUMENT, /**
		 * @uml.property  name="eXECUTABLE"
		 * @uml.associationEnd  
		 */
		EXECUTABLE, /**
		 * @uml.property  name="iMAGE"
		 * @uml.associationEnd  
		 */
		IMAGE, /**
		 * @uml.property  name="vIDEO"
		 * @uml.associationEnd  
		 */
		VIDEO
	}

	/**
	 * @uml.property  name="bitrate"
	 */
	protected int bitrate = 0;

	/**
	 * @uml.property  name="channels"
	 */
	protected int channels = 0;

	/**
	 * @uml.property  name="contentType"
	 */
	protected String contentType = EMPTY_STRING;

	/**
	 * @uml.property  name="contentURL"
	 */
	protected String contentURL = EMPTY_STRING;

	/**
	 * @uml.property  name="duration"
	 */
	protected int duration;

	/**
	 * @uml.property  name="expression"
	 * @uml.associationEnd  
	 */
	protected Expression expression = Expression.FULL;

	/**
	 * @uml.property  name="filesSize"
	 */
	protected long filesSize = 0;

	/**
	 * @uml.property  name="framerate"
	 */
	protected int framerate = 0;

	/**
	 * @uml.property  name="height"
	 */
	protected String height = EMPTY_STRING;

	/**
	 * @uml.property  name="isDefault"
	 */
	protected boolean isDefault = false;

	/**
	 * @uml.property  name="lang"
	 */
	protected String lang = EMPTY_STRING;

	/**
	 * @uml.property  name="mediaPlayer"
	 */
	protected String mediaPlayer = EMPTY_STRING;

	/**
	 * @uml.property  name="medium"
	 * @uml.associationEnd  
	 */
	protected Medium medium = Medium.VIDEO;

	/**
	 * @uml.property  name="samplingrate"
	 */
	protected int samplingrate = 0;

	/**
	 * @uml.property  name="thumbnailURL"
	 */
	protected String thumbnailURL = EMPTY_STRING;

	/**
	 * @uml.property  name="width"
	 */
	protected String width = EMPTY_STRING;

	/**
	 * @return
	 * @uml.property  name="bitrate"
	 */
	public int getBitrate() {
		return bitrate;
	}

	/**
	 * @return
	 * @uml.property  name="channels"
	 */
	public int getChannels() {
		return channels;
	}

	/**
	 * @return
	 * @uml.property  name="contentType"
	 */
	public String getContentType() {
		return contentType;
	}

	/**
	 * @return
	 * @uml.property  name="contentURL"
	 */
	public String getContentURL() {
		return contentURL;
	}

	/**
	 * @return
	 * @uml.property  name="duration"
	 */
	public int getDuration() {
		return duration;
	}

	/**
	 * @return
	 * @uml.property  name="expression"
	 */
	public Expression getExpression() {
		return expression;
	}

	/**
	 * @return
	 * @uml.property  name="filesSize"
	 */
	public long getFilesSize() {
		return filesSize;
	}

	/**
	 * @return
	 * @uml.property  name="framerate"
	 */
	public int getFramerate() {
		return framerate;
	}

	/**
	 * @return
	 * @uml.property  name="height"
	 */
	public String getHeight() {
		return height;
	}

	/**
	 * @return
	 * @uml.property  name="lang"
	 */
	public String getLang() {
		return lang;
	}

	/**
	 * @return
	 * @uml.property  name="mediaPlayer"
	 */
	public String getMediaPlayer() {
		return mediaPlayer;
	}

	/**
	 * @return
	 * @uml.property  name="medium"
	 */
	public Medium getMedium() {
		return medium;
	}

	/**
	 * @return
	 * @uml.property  name="samplingrate"
	 */
	public int getSamplingrate() {
		return samplingrate;
	}

	/**
	 * @return
	 * @uml.property  name="thumbnailURL"
	 */
	public String getThumbnailURL() {
		return thumbnailURL;
	}

	/**
	 * @return
	 * @uml.property  name="width"
	 */
	public String getWidth() {
		return width;
	}

	/**
	 * @return
	 * @uml.property  name="isDefault"
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * @param  bitrate
	 * @uml.property  name="bitrate"
	 */
	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	/**
	 * @param  channels
	 * @uml.property  name="channels"
	 */
	public void setChannels(int channels) {
		this.channels = channels;
	}

	/**
	 * @param  contentType
	 * @uml.property  name="contentType"
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	/**
	 * @param  contentURL
	 * @uml.property  name="contentURL"
	 */
	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}

	/**
	 * @param  isDefault
	 * @uml.property  name="isDefault"
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * @param  duration
	 * @uml.property  name="duration"
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}

	/**
	 * @param  expression
	 * @uml.property  name="expression"
	 */
	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	/**
	 * @param  filesSize
	 * @uml.property  name="filesSize"
	 */
	public void setFilesSize(long filesSize) {
		this.filesSize = filesSize;
	}

	/**
	 * @param  framerate
	 * @uml.property  name="framerate"
	 */
	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}

	/**
	 * @param  height
	 * @uml.property  name="height"
	 */
	public void setHeight(String height) {
		this.height = height;
	}

	/**
	 * @param  lang
	 * @uml.property  name="lang"
	 */
	public void setLang(String lang) {
		this.lang = lang;
	}

	/**
	 * @param  mediaPlayer
	 * @uml.property  name="mediaPlayer"
	 */
	public void setMediaPlayer(String mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}

	/**
	 * @param  medium
	 * @uml.property  name="medium"
	 */
	public void setMedium(Medium medium) {
		this.medium = medium;
	}

	/**
	 * @param  samplingrate
	 * @uml.property  name="samplingrate"
	 */
	public void setSamplingrate(int samplingrate) {
		this.samplingrate = samplingrate;
	}

	/**
	 * @param  thumbnailURL
	 * @uml.property  name="thumbnailURL"
	 */
	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}

	/**
	 * @param  width
	 * @uml.property  name="width"
	 */
	public void setWidth(String width) {
		this.width = width;
	}
}
