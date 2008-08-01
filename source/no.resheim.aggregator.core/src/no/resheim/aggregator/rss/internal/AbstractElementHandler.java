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
package no.resheim.aggregator.rss.internal;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.internal.AggregatorUIItem;
import no.resheim.aggregator.data.internal.InternalArticle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/*
 * <sy:updatePeriod> ( 'hourly' | 'daily' | 'weekly' | 'monthly' | 'yearly' )
 * <sy:updateFrequency> ( a positive integer ) <sy:updateBase> ( #PCDATA )
 * [W3CDTF]
 */
/**
 * Handles capturing of XML element text and Dublin Core elements. Dublin Core
 * elements may or may not have been added to the feed being parsed. These are
 * normally used when the feed standard used does not supply required elements,
 * for instance the publication date (dc:date). We don't care if the header of
 * the feed declares that it's offering DC, we'll just parse the elements if
 * they are found.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractElementHandler implements IElementHandler {

	static final SimpleDateFormat dc_date = new SimpleDateFormat(
			"yyyy-MM-dd'T'hh:mm:ss'Z'"); //$NON-NLS-1$	

	// Dublin Core Elements
	//http://homepage.univie.ac.at/horst.prillinger/blog/archives/2005/01/000922.
	// html
	// http://purl.org/dc/elements/1.1/
	static final String DC_DATE = "dc:date"; //$NON-NLS-1$
	private static final String DC_CREATOR = "dc:creator"; //$NON-NLS-1$

	/** Temporary text storage */
	private StringBuffer buffer;

	/** Whether or not to capture text */
	private boolean capture;

	/** The current feed */
	protected Feed feed;

	/**
	 * The current article. We use {@link InternalArticle} instead of
	 * {@link Article} as we need to be able to set certain values.
	 */
	protected InternalArticle item;

	protected AggregatorUIItem location;

	/** The feed registry we're working for */
	protected FeedCollection collection;

	public AbstractElementHandler() {
		buffer = new StringBuffer();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (capture) {
			buffer.append(ch, start, length);
		}

	}

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

	/**
	 * @return the buffer
	 */
	public StringBuffer getBuffer() {
		return buffer;
	}

	public void endElement(String qName) throws SAXException {
		if (qName.equals(DC_CREATOR)) {
			item.setCreator(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(DC_DATE)) {
			try {
				item.setPublicationDate(dc_date.parse(getBuffer().toString())
						.getTime());
			} catch (ParseException e) {
			}
			setCapture(false);
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals(DC_CREATOR) || qName.equals(DC_DATE)) {
			setCapture(true);
		}
		return this;

	}

}
