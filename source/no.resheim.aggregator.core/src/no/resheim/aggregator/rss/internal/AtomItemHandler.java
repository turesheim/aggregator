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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.internal.InternalArticle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS 2.0 stream items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class AtomItemHandler extends AbstractElementHandler {

	private static final String ENTRY = "entry"; //$NON-NLS-1$

	private static final String ISSUED = "issued"; //$NON-NLS-1$

	private static final String ID = "id"; //$NON-NLS-1$

	private static final String SUMMARY = "summary"; //$NON-NLS-1$

	/** WordPress full content element */
	private static final String CONTENT = "content"; //$NON-NLS-1$

	static final SimpleDateFormat date = new SimpleDateFormat(
			"yyyy-MM-ddEEE, d MMM yyyy HH:mm:ss Z"); //$NON-NLS-1$

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
				e.printStackTrace();
			}
		}

		return date;
	}

	public AtomItemHandler(FeedCollection registry, Feed feed) {
		this.collection = registry;
		this.feed = feed;
		item = new InternalArticle(null, UUID.randomUUID());
		item.setLocation(feed.getLocation());
		item.setFeedUUID(feed.getUUID());
		item.setAddedDate(System.currentTimeMillis());
	}

	public void endElement(String qName) throws SAXException {
		super.endElement(qName);
		if (qName.equals(TITLE)) {
			item.setTitle(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(LINK)) {
			item.setLink(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(SUMMARY)) {
			// Make sure we don't overwrite description added by content:encoded
			if (item.getDescription() == null) {
				item.setDescription(getBuffer().toString());
			}
			setCapture(false);
		}
		if (qName.equals(CONTENT)) {
			item.setDescription(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(ID)) {
			item.setGuid(getBuffer().toString());
			setCapture(false);
		}
		// RFC822 date specification
		if (qName.equals(ISSUED)) {
			item.setPublicationDate(parse(getBuffer().toString()).getTime());
			setCapture(false);
		}
		if (qName.equals(ENTRY)) {
			if (!collection.hasArticle(item.getGuid())) {
				collection.addNew(item);
			}
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		if (qName.equals(TITLE) || qName.equals(LINK)
				|| qName.equals(DESCRIPTION) || qName.equals(ISSUED)
				|| qName.equals(ID) || qName.equals(CONTENT)) {
			setCapture(true);
		}
		return this;
	}

}
