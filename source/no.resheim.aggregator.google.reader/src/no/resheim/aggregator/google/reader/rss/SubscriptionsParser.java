/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.google.reader.rss;

import java.util.ArrayList;
import java.util.Stack;

import no.resheim.aggregator.core.catalog.IFeedCatalog;
import no.resheim.aggregator.core.data.Subscription;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This type is used to parse the content of the Google Reader subscriptions
 * list. http://www.google.com/reader/api/0/subscription/list?output=xml
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class SubscriptionsParser extends DefaultHandler {
	protected final Stack<IGoogleElementHandler> handlers;
	StringBuffer buffer;
	boolean capture;
	IFeedCatalog catalog;

	public SubscriptionsParser(IFeedCatalog catalog,
			ArrayList<Subscription> feeds) {
		handlers = new Stack<IGoogleElementHandler>();
		buffer = new StringBuffer();
		this.catalog = catalog;
		// Root element is "object"
		handlers.push(new SubscriptionsRootHandler(catalog, feeds));
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
		IGoogleElementHandler nextHandler;
		nextHandler = handlers.lastElement().startElement(qName, atts);
		handlers.push(nextHandler);
	}
}
