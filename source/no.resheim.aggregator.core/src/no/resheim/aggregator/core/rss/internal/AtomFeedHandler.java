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
 * Handles Atom streams. http://www.ietf.org/rfc/rfc4287.txt
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AtomFeedHandler extends AbstractElementHandler {

	private static final String TITLE = "title"; //$NON-NLS-1$
	private static final String DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String WEBMASTER = "webmaster"; //$NON-NLS-1$
	private static final String EDITOR = "managingEditor"; //$NON-NLS-1$
	private static final String COPYRIGHT = "copyright"; //$NON-NLS-1$
	private static final String ENTRY = "entry"; //$NON-NLS-1$

	public AtomFeedHandler(FeedCollection registry, Feed feed) {
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
		if (qName.equals(DESCRIPTION)) {
			feed.setDescription(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(LINK)) {
			feed.setLink(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(WEBMASTER)) {
			feed.setWebmaster(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(COPYRIGHT)) {
			feed.setCopyright(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(EDITOR)) {
			feed.setEditor(getBuffer().toString());
			setCapture(false);
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals(TITLE) || qName.equals(DESCRIPTION)
				|| qName.equals(LINK) || qName.equals(WEBMASTER)
				|| qName.equals(EDITOR) || qName.equals(COPYRIGHT)) {
			setCapture(true);
		}
		if (qName.equals(ENTRY)) {
			return new AtomItemHandler(collection, feed);
		}
		return this;
	}

}
