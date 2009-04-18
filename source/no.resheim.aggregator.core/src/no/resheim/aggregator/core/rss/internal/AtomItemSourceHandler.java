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

import java.text.SimpleDateFormat;

import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.FeedCollection;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles the <b>source</b> element of Atom streams (by ignoring the content).
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class AtomItemSourceHandler extends AbstractItemHandler {

	private static final String ID = "id"; //$NON-NLS-1$

	static final SimpleDateFormat date = new SimpleDateFormat(
			"yyyy-MM-ddEEE, d MMM yyyy HH:mm:ss Z"); //$NON-NLS-1$

	public AtomItemSourceHandler(FeedCollection registry, Subscription feed) {
		this.collection = registry;
		this.feed = feed;
	}

	public void endElement(String qName) throws SAXException {
		super.endElement(qName);
		// Mandatory
		if (qName.equals(TITLE)) {
			setCapture(false);
		}
		if (qName.equals(LINK)) {
			setCapture(false);
		}
		if (qName.equals(ID)) {
			setCapture(false);
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		if (qName.equals(TITLE) || qName.equals(ID) || qName.equals(LINK)) {
			setCapture(true);
		}
		return this;
	}
}
