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
import no.resheim.aggregator.core.data.Subscription;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS 2.0 stream items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class AtomItemHandler extends AbstractItemHandler {
	private static final String UPDATED = "updated"; //$NON-NLS-1$
	// TODO: Properly handle links within content
	private static final String ENTRY = "entry"; //$NON-NLS-1$

	private static final String PUBLISHED = "published"; //$NON-NLS-1$

	private static final String ID = "id"; //$NON-NLS-1$

	private static final String SUMMARY = "summary"; //$NON-NLS-1$

	/** WordPress full content element */
	private static final String CONTENT = "content"; //$NON-NLS-1$

	public AtomItemHandler(AggregatorCollection registry, Subscription feed) {
		this.collection = registry;
		this.feed = feed;
		item = new Article(feed, UUID.randomUUID());
		item.setAddedDate(System.currentTimeMillis());
	}

	public void endElement(String qName) throws SAXException {
		super.endElement(qName);
		// Mandatory
		if (qName.equals(TITLE)) {
			item.setTitle(getBuffer().toString());
		}
		if (qName.equals(SUMMARY)) {
			item.internalSetText(getBuffer().toString());
		}
		if (qName.equals(CONTENT)) {
			item.internalSetText(getBuffer().toString());
		}
		if (qName.equals(ID)) {
			item.setGuid(getBuffer().toString());
		}
		// RFC822 date specification
		if (qName.equals(PUBLISHED)) {
			item.setPublicationDate(parse(getBuffer().toString()).getTime());
		}
		if (qName.equals(ENTRY)) {
			if (!collection.hasArticle(item.getGuid())) {
				addArticle(item);
			}
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		if (qName.equals(TITLE) || qName.equals(SUMMARY)
				|| qName.equals(PUBLISHED) || qName.equals(ID)
				|| qName.equals(CONTENT) || qName.equals(UPDATED)) {
			setCapture(true);
		} else {
			setCapture(false);
		}
		if (qName.equals("source")) {
			return new AtomItemSourceHandler(collection, feed);
		}
		if (qName.equals("category")) {
			// FIXME: This should strictly be handled by the reader plug-in
			String term = atts.getValue("term");
			String label = atts.getValue("label");
			if (term != null && term.endsWith("/state/com.google/read")) {
				if (label != null && label.equals("read")) {
					item.setRead(true);
				}
			}
			if (term != null && term.endsWith("/state/com.google/starred")) {
				if (label != null && label.equals("starred")) {
					item.setStarred(true);
				}
			}
		}
		// Must exist if there is not a "content" element
		if (qName.equals(LINK)) {
			item.setLink(atts.getValue("href")); //$NON-NLS-1$
		}
		return this;
	}
}
