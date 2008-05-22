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

import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedRegistry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS version 0.92 feed items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RSS092ItemHandler extends AbstractElementHandler {

	public RSS092ItemHandler(FeedRegistry registry, Feed feed) {
		this.registry = registry;
		this.feed = feed;
		this.item = new Article(feed);
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
			item.setDescription(getBuffer().toString());
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
				|| qName.equals(DESCRIPTION)) {
			setCapture(true);
		}
		return this;
	}

}
