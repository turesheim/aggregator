/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/

package no.resheim.aggregator.google.reader.rss;

import java.util.ArrayList;

import no.resheim.aggregator.core.data.Subscription;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.1
 */
class FeedHandler implements IGoogleElementHandler {
	private Subscription feed;
	private ArrayList<Object> feeds;
	private StringBuffer buffer = new StringBuffer();
	private boolean capture;
	private boolean fIgnore;

	private Capturing fCapturing;

	private enum Capturing {
		NOTHING, TITLE, URL
	}

	// Since we can have several objects embedded into this one we must track
	// this and add the feed when we reach the end of the initial object.
	private int fObjectLevel;

	/**
	 * This method will also clear the buffer.
	 * 
	 * @param capture
	 *            the capture to set
	 */
	public void setCapture(boolean capture) {
		this.capture = capture;
		buffer.setLength(0);
	}

	public FeedHandler(Subscription feed, ArrayList<Object> feeds) {
		super();
		this.feed = feed;
		this.feeds = feeds;
		fObjectLevel++;
	}

	public FeedHandler() {
	}

	public void endElement(String qName) throws SAXException {

		if (qName.equals("list")) {
			fIgnore = false;
		}

		if (qName.equals("object")) {
			fObjectLevel--;
			if (fObjectLevel == 0) {
				feeds.add(feed);
			}
		}
		if (!fCapturing.equals(Capturing.NOTHING) && qName.equals("string")) {
			String data = buffer.toString();
			switch (fCapturing) {
			case TITLE:
				feed.setTitle(data);
				break;
			case URL:
				feed.setURL(data.replaceFirst("feed/", ""));
				break;
			default:
				break;
			}
			fCapturing = Capturing.NOTHING;
			setCapture(false);
		}
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (capture) {
			buffer.append(ch, start, length);
		}
	}

	public IGoogleElementHandler startElement(String qName, Attributes atts)
			throws SAXException {

		if (fIgnore) {
			return this;
		}
		// Ignore this list!
		if (qName.equals("list")) {
			fIgnore = true;
		}
		if (qName.equals("string") && fObjectLevel == 1) {
			String id = atts.getValue("name");
			if (id != null) {
				if (id.equals("id")) {
					fCapturing = Capturing.URL;
					setCapture(true);
				}
				if (id.equals("title")) {
					fCapturing = Capturing.TITLE;
					setCapture(true);
				}
			}
		}
		return this;
	}
}