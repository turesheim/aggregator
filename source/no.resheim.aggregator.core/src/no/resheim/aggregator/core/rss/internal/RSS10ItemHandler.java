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

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.internal.InternalArticle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS 1.0 stream items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class RSS10ItemHandler extends AbstractItemHandler {

	public RSS10ItemHandler(FeedCollection registry, Feed feed) {
		this.collection = registry;
		this.feed = feed;
		item = new InternalArticle(feed, UUID.randomUUID());
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
			// RSS 1.0 does not have a GUID
			item.setGuid(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(DESCRIPTION)) {
			item.internalSetText(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(ITEM)) {
			if (!collection.hasArticle(item.getGuid())) {
				addArticle(item);
			}
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		if (qName.equals(TITLE) || qName.equals(LINK)
				|| qName.equals(DESCRIPTION)) {
			setCapture(true);
		}
		return this;
	}

}
