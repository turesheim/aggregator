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
package no.resheim.aggregator.internal.rss;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import no.resheim.aggregator.model.Feed;
import no.resheim.aggregator.model.FeedRegistry;
import no.resheim.aggregator.model.Article;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS 2.0 stream items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class RSS20ItemHandler extends AbstractElementHandler {

	public static final String PUBDATE = "pubDate"; //$NON-NLS-1$

	public static final String GUID = "guid"; //$NON-NLS-1$

	static final SimpleDateFormat date = new SimpleDateFormat(
			"EEE, d MMM yyyy HH:mm:ss Z"); //$NON-NLS-1$

	public RSS20ItemHandler(FeedRegistry registry, Feed feed) {
		this.registry = registry;
		this.item = new Article(feed);
		this.feed = feed;
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
		if (qName.equals(DESCRIPTION)) {
			item.setDescription(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(GUID)) {
			item.setGuid(getBuffer().toString());
			setCapture(false);
		}
		// RFC822 date specification
		if (qName.equals(PUBDATE)) {
			try {
				item.setPublicationDate(date.parse(getBuffer().toString())
						.getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			setCapture(false);
		}
		if (qName.equals(ITEM)) {
			if (!registry.hasArticle(item)) {
				registry.add(item);
			}
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		if (qName.equals(TITLE) || qName.equals(LINK)
				|| qName.equals(DESCRIPTION) || qName.equals(PUBDATE)
				|| qName.equals(GUID)) {
			setCapture(true);
		}
		return this;
	}

}
