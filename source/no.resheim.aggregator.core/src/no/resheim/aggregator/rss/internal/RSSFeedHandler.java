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

import java.text.MessageFormat;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;

/**
 * This type is the general feed handler. It will detect which sub-type of RSS
 * the feed is and utilize specific feed handlers accordingly.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RSSFeedHandler extends AbstractElementHandler {
	private static final String RDF_TYPE = "RSS 1.0/RDF"; //$NON-NLS-1$
	private static final String RSS_TYPE = "RSS"; //$NON-NLS-1$
	// http:// cyber.law.harvard.edu/rss/rss.html
	private static final String VERSION_2_0 = "2.0"; //$NON-NLS-1$
	private static final String VERSION_0_92 = "0.92"; //$NON-NLS-1$
	private static final String ELEMENT_RSS = "rss"; //$NON-NLS-1$
	// http://web.resource.org/rss/1.0/spec
	private static final String ELEMENT_RDF = "rdf:RDF"; //$NON-NLS-1$
	private static final String ATTR_VERSION = "version"; //$NON-NLS-1$

	public RSSFeedHandler(FeedCollection registry, Feed feed) {
		super();
		this.registry = registry;
		this.feed = feed;
	}

	public void endElement(String qName) throws SAXException {
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		// Do we have a RSS feed
		if (qName.equals(ELEMENT_RSS)) {
			if (atts.getValue(ATTR_VERSION).equals(VERSION_0_92)) {
				feed.setType(RSS_TYPE + atts.getValue(ATTR_VERSION));
				return new RSS092FeedHandler(registry, feed);
			}
			if (atts.getValue(ATTR_VERSION).equals(VERSION_2_0)) {
				feed.setType(RSS_TYPE + atts.getValue(ATTR_VERSION));
				return new RSS20FeedHandler(registry, feed);
			}
		}
		// rdf:RDF is basically the same as RSS1
		if (qName.equals(ELEMENT_RDF)) {
			feed.setType(RDF_TYPE);
			return new RSS10FeedHandler(registry, feed);
		}
		throw new SAXNotRecognizedException(MessageFormat.format(
				Messages.RSSFeedHandler_Unrecognized_Feed_Type, new Object[] {
					qName
				}));
	}
}
