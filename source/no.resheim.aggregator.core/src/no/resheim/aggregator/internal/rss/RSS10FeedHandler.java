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

import no.resheim.aggregator.model.Feed;
import no.resheim.aggregator.model.FeedRegistry;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS version 1.0 streams. See the specifications at
 * http://web.resource.org/rss/1.0/spec.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RSS10FeedHandler extends AbstractElementHandler {

	public RSS10FeedHandler(FeedRegistry registry, Feed feed) {
		super();
		this.registry = registry;
		this.feed = feed;
	}

	public void endElement(String qName) throws SAXException {
		if (qName.equals(TITLE)) {
			feed.setTitle(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(DESCRIPTION)) {
			feed.setDescription(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(LINK)) {
			feed.setLink(getBuffer().toString());
			setCapture(false);
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals(TITLE) || qName.equals(DESCRIPTION)
				|| qName.equals(LINK)) {
			setCapture(true);
		}
		if (qName.equals(ITEM)) {
			return new RSS10ItemHandler(registry, feed);
		}
		return this;
	}
}
