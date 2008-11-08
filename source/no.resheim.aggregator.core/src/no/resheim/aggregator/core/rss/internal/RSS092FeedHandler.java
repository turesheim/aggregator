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

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS version 0.92 streams.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RSS092FeedHandler extends AbstractElementHandler {

	public RSS092FeedHandler(FeedCollection registry, Feed feed) {
		super();
		this.collection = registry;
		this.feed = feed;
	}

	public void endElement(String qName) throws SAXException {
		if (qName.equals(TITLE)) {
			// Only set the title if it has not already been specified
			if (feed.getTitle() == null || feed.getTitle().length() == 0) {
				feed.setTitle(getBuffer().toString());
			}
			setCapture(false);
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals(TITLE)) {
			setCapture(true);
		}
		if (qName.equals(ITEM)) {
			return new RSS092ItemHandler(collection, feed);
		}
		return this;
	}

}
