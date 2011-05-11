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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Parses a <b>tag</b> item in a google feed query.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.1
 */
class TagHandler implements IGoogleElementHandler {
	private static final String OBJECT = "object";
	private static final String LIST = "list";
	private static final String ID = "id";
	private static final String STRING = "string";
	private static final String NAME = "name";
	private String label;
	private ArrayList<Object> items;
	private StringBuffer buffer = new StringBuffer();
	private boolean capture;
	private boolean fIgnore;

	private Capturing fCapturing;

	private enum Capturing {
		NOTHING, TITLE, URL, ID
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

	public TagHandler(ArrayList<Object> items) {
		super();
		this.items = items;
		fObjectLevel++;
	}

	public TagHandler() {
	}

	public void endElement(String qName) throws SAXException {

		if (qName.equals(LIST)) {
			fIgnore = false;
		}

		if (qName.equals(OBJECT)) {
			fObjectLevel--;
			if (fObjectLevel == 0) {
				items.add(label);
			}
		}
		if (!fCapturing.equals(Capturing.NOTHING) && qName.equals(STRING)) {
			String data = buffer.toString();
			switch (fCapturing) {
			case ID:
				label = data.substring(data.lastIndexOf('/') + 1);
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
		if (qName.equals(LIST)) {
			fIgnore = true;
		}
		if (qName.equals(STRING) && fObjectLevel == 1) {
			String id = atts.getValue(NAME);
			if (id != null) {
				if (id.equals(ID)) {
					fCapturing = Capturing.ID;
					setCapture(true);
				}
			}
		}
		return this;
	}
}