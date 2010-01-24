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
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class MediaContent {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * @author torkild
	 */
	public enum Expression {
		FULL, NONSTOP, SAMPLE
	}

	/**
	 * @author torkild
	 */
	public enum Medium {
		AUDIO, DOCUMENT, EXECUTABLE, IMAGE, VIDEO
	}

	protected int bitrate = 0;

	protected int channels = 0;

	protected String contentType = EMPTY_STRING;

	protected String contentURL = EMPTY_STRING;

	protected int duration;

	protected Expression expression = Expression.FULL;

	protected long filesSize = 0;

	protected int framerate = 0;

	protected String height = EMPTY_STRING;

	protected boolean isDefault = false;

	protected String lang = EMPTY_STRING;

	protected String mediaPlayer = EMPTY_STRING;

	protected Medium medium = Medium.VIDEO;

	protected int samplingrate = 0;

	protected String thumbnailURL = EMPTY_STRING;

	protected String width = EMPTY_STRING;

	public int getBitrate() {
		return bitrate;
	}

	public int getChannels() {
		return channels;
	}

	public String getContentType() {
		return contentType;
	}

	public String getContentURL() {
		return contentURL;
	}

	public int getDuration() {
		return duration;
	}

	public Expression getExpression() {
		return expression;
	}

	public long getFilesSize() {
		return filesSize;
	}

	public int getFramerate() {
		return framerate;
	}

	public String getHeight() {
		return height;
	}

	public String getLang() {
		return lang;
	}

	public String getMediaPlayer() {
		return mediaPlayer;
	}

	public Medium getMedium() {
		return medium;
	}

	public int getSamplingrate() {
		return samplingrate;
	}

	public String getThumbnailURL() {
		return thumbnailURL;
	}

	public String getWidth() {
		return width;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setBitrate(int bitrate) {
		this.bitrate = bitrate;
	}

	public void setChannels(int channels) {
		this.channels = channels;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setContentURL(String contentURL) {
		this.contentURL = contentURL;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public void setExpression(Expression expression) {
		this.expression = expression;
	}

	public void setFilesSize(long filesSize) {
		this.filesSize = filesSize;
	}

	public void setFramerate(int framerate) {
		this.framerate = framerate;
	}

	public void setHeight(String height) {
		this.height = height;
	}

	public void setLang(String lang) {
		this.lang = lang;
	}

	public void setMediaPlayer(String mediaPlayer) {
		this.mediaPlayer = mediaPlayer;
	}

	public void setMedium(Medium medium) {
		this.medium = medium;
	}

	public void setSamplingrate(int samplingrate) {
		this.samplingrate = samplingrate;
	}

	public void setThumbnailURL(String thumbnailURL) {
		this.thumbnailURL = thumbnailURL;
	}

	public void setWidth(String width) {
		this.width = width;
	}
}
