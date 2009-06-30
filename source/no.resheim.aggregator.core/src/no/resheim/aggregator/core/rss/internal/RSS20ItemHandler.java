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
package no.resheim.aggregator.core.rss.internal;

import java.util.UUID;

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.MediaContent;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.MediaContent.Medium;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS 2.0 stream items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class RSS20ItemHandler extends AbstractItemHandler {

	private static final String MEDIUM = "medium"; //$NON-NLS-1$

	private static final String DURATION = "duration"; //$NON-NLS-1$

	private static final String TYPE = "type"; //$NON-NLS-1$

	private static final String ENCLOSURE = "enclosure"; //$NON-NLS-1$

	private static final String URL = "url"; //$NON-NLS-1$

	private static final String MEDIA_PLAYER = "media:player"; //$NON-NLS-1$

	private static final String MEDIA_CONTENT = "media:content"; //$NON-NLS-1$

	private static final String AUTHOR = "author"; //$NON-NLS-1$

	public static final String PUBDATE = "pubDate"; //$NON-NLS-1$

	public static final String GUID = "guid"; //$NON-NLS-1$

	/** xmlns:media="http://search.yahoo.com/mrss/" */

	/** WordPress full content element */
	public static final String CONTENT_ENCODED = "content:encoded"; //$NON-NLS-1$

	public RSS20ItemHandler(AggregatorCollection registry, Subscription feed) {
		this.collection = registry;
		this.feed = feed;
		item = new Article(feed, UUID.randomUUID());
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
		if (qName.equals(DESCRIPTION)) {
			// Make sure we don't overwrite description added by content:encoded
			if (item.internalGetText() == null) {
				item.internalSetText(getBuffer().toString());
			}
			setCapture(false);
		}
		if (qName.equals(CONTENT_ENCODED)) {
			item.internalSetText(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(GUID)) {
			item.setGuid(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(AUTHOR)) {
			item.setCreator(getBuffer().toString());
			setCapture(false);
		}
		// RFC822 date specification
		if (qName.equals(PUBDATE)) {
			item.setPublicationDate(parse(getBuffer().toString()).getTime());
			setCapture(false);
		}

		if (qName.equals(ITEM)) {
			// We don't have a GUID so we need to fake it if possible. If not
			// we'll bail out as this feed is useless
			if (item.getGuid() == null) {
				if (item.getLink() == null) {
					throw new SAXException(
							"Feed item is missing both \"guid\" and \"link\" elements."); //$NON-NLS-1$
				}
				item.setGuid(item.getLink());
			}
			if (!collection.hasArticle(item.getGuid())) {
				addArticle(item);
			}
		}
	}

	// 2009-05-13T16:58:37+02:00
	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		try {
			if (qName.equals(TITLE) || qName.equals(LINK)
					|| qName.equals(AUTHOR) || qName.equals(DESCRIPTION)
					|| qName.equals(PUBDATE) || qName.equals(GUID)
					|| qName.equals(CONTENT_ENCODED)) {
				setCapture(true);
			}
			if (qName.equals(MEDIA_PLAYER)) {
				item.setMediaPlayerURL(atts.getValue(URL));
			}
			if (qName.equals(MEDIA_CONTENT)) {
				// TODO: Parse the rest of the item
				MediaContent media = new MediaContent();
				if (atts.getValue(TYPE) != null) {
					media.setContentType(atts.getValue(TYPE));
				}
				if (atts.getValue(URL) != null) {
					media.setContentURL(atts.getValue(URL));
				}
				if (atts.getValue(MEDIUM) != null) {
					media.setMedium(Medium.valueOf(atts.getValue(MEDIUM)
							.toUpperCase()));
				}
				item.addMediaContent(media);
			}
			if (qName.equals(ENCLOSURE)) {
				MediaContent media = new MediaContent();
				if (atts.getValue(DURATION) != null) {
					media
							.setDuration(Integer.parseInt(atts
									.getValue(DURATION)));
				}
				if (atts.getValue(TYPE) != null) {
					media.setContentType(atts.getValue(TYPE));
				}
				if (atts.getValue(URL) != null) {
					media.setContentURL(atts.getValue(URL));
				}
				item.addMediaContent(media);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return this;
	}
}
