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

import java.util.Stack;

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;

import org.eclipse.core.runtime.Assert;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses RDF/RSS feeds and puts the result in the feed registry database.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedParser extends DefaultHandler {
	private Stack<IElementHandler> handlers;

	public FeedParser(FeedCollection collection, Feed feed) {
		Assert.isNotNull(collection,
				"Cannot parse feed with a \"null\" collection"); //$NON-NLS-1$
		Assert.isNotNull(feed, "Cannot parse feed that is \"null\""); //$NON-NLS-1$
		handlers = new Stack<IElementHandler>();
		handlers.push(new RSSFeedHandler(collection, feed));
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		handlers.lastElement().characters(ch, start, length);
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		handlers.pop().endElement(qName);
	}

	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		IElementHandler nextHandler;
		nextHandler = handlers.lastElement().startElement(qName, atts);
		handlers.push(nextHandler);
	}
}
