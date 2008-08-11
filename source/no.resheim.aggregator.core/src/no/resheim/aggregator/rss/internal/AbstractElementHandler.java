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
import java.util.Date;
import java.util.TimeZone;

import no.resheim.aggregator.data.AggregatorUIItem;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
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

	static String[] date_formats = {
			"yyyy-MM-dd'T'kk:mm:ssZ", // ISO //$NON-NLS-1$
			"yyyy-MM-dd'T'kk:mm:ss'Z'", // ISO //$NON-NLS-1$
			"yyyy-MM-dd'T'kk:mm:ssz", // ISO //$NON-NLS-1$
			"yyyy-MM-dd'T'kk:mm:ss", // ISO //$NON-NLS-1$
			"EEE, d MMM yy kk:mm:ss z", // RFC822 //$NON-NLS-1$
			"EEE, d MMM yyyy kk:mm:ss z", // RFC2882 //$NON-NLS-1$
			"EEE MMM  d kk:mm:ss zzz yyyy", // ASC //$NON-NLS-1$
			"EEE, dd MMMM yyyy kk:mm:ss", // Mon, 26 January 2004 //$NON-NLS-1$
			// 16:31:00 ET
			"yyyy-MM-dd kk:mm:ss.0", "-yy-MM", "-yyMM", "yy-MM-dd", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			"yyyy-MM-dd", "yyyy-MM", "yyyy-D", "-yyMM", "yyyyMMdd", "yyMMdd", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
			"yyyy", "yyD" //$NON-NLS-1$ //$NON-NLS-2$

	};

	public static Date parse(String d) {
		Date date = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat();
		formatter.setTimeZone(TimeZone.getTimeZone("GMT")); //$NON-NLS-1$
		d = d.replaceAll("([-+]\\d\\d:\\d\\d)", "GMT$1"); // Correct W3C times //$NON-NLS-1$ //$NON-NLS-2$
		d = d.replaceAll(" ([ACEMP])T$", " $1ST"); // Correct Disney times //$NON-NLS-1$ //$NON-NLS-2$
		for (int i = 0; i < date_formats.length; i++) {
			try {
				formatter.applyPattern(date_formats[i]);
				date = formatter.parse(d);
				break;
			} catch (Exception e) {
			}
		}

		return date;
	}

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
