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

import java.text.SimpleDateFormat;
import java.util.UUID;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.internal.InternalArticle;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Handles RSS 2.0 stream items.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 * 
 */
public class AtomItemHandler extends AbstractElementHandler {
	private static final String UPDATED = "updated"; //$NON-NLS-1$

	/*
	The "atom:entry" element represents an individual entry, acting as a
	container for metadata and data associated with the entry.  This
	element can appear as a child of the atom:feed element, or it can
	appear as the document (i.e., top-level) element of a stand-alone
	Atom Entry Document.
	
	   atomEntry =
	      element atom:entry {
	         atomCommonAttributes,
	         (atomAuthor*
	          & atomCategory*
	          & atomContent?
	          & atomContributor*
	          & atomId
	          & atomLink*
	          & atomPublished?
	          & atomRights?
	          & atomSource?
	          & atomSummary?
	          & atomTitle
	          & atomUpdated
	          & extensionElement*)
	      }

	This specification assigns no significance to the order of appearance
	of the child elements of atom:entry.

	The following child elements are defined by this specification (note
	that it requires the presence of some of these elements):

	o  atom:entry elements MUST contain one or more atom:author elements,
	  unless the atom:entry contains an atom:source element that
	  contains an atom:author element or, in an Atom Feed Document, the
	  atom:feed element contains an atom:author element itself.
	o  atom:entry elements MAY contain any number of atom:category
	  elements.
	o  atom:entry elements MUST NOT contain more than one atom:content
	  element.
	o  atom:entry elements MAY contain any number of atom:contributor
	  elements.
	o  atom:entry elements MUST contain exactly one atom:id element.
	o  atom:entry elements that contain no child atom:content element
	  MUST contain at least one atom:link element with a rel attribute
	  value of "alternate".
	o  atom:entry elements MUST NOT contain more than one atom:link
	  element with a rel attribute value of "alternate" that has the
	  same combination of type and hreflang attribute values.
	o  atom:entry elements MAY contain additional atom:link elements
	  beyond those described above.
	o  atom:entry elements MUST NOT contain more than one atom:published
	  element.
	o  atom:entry elements MUST NOT contain more than one atom:rights
	  element.
	o  atom:entry elements MUST NOT contain more than one atom:source
	  element.
	o  atom:entry elements MUST contain an atom:summary element in either
	  of the following cases:
	  *  the atom:entry contains an atom:content that has a "src"
	     attribute (and is thus empty).
	  *  the atom:entry contains content that is encoded in Base64;
	     i.e., the "type" attribute of atom:content is a MIME media type
	     [MIMEREG], but is not an XML media type [RFC3023], does not
	     begin with "text/", and does not end with "/xml" or "+xml".
	o  atom:entry elements MUST NOT contain more than one atom:summary
	  element.
	o  atom:entry elements MUST contain exactly one atom:title element.
	o  atom:entry elements MUST contain exactly one atom:updated element.
	  
	*/
	private static final String ENTRY = "entry"; //$NON-NLS-1$

	private static final String ISSUED = "issued"; //$NON-NLS-1$

	private static final String ID = "id"; //$NON-NLS-1$

	private static final String SUMMARY = "summary"; //$NON-NLS-1$

	/** WordPress full content element */
	private static final String CONTENT = "content"; //$NON-NLS-1$

	static final SimpleDateFormat date = new SimpleDateFormat(
			"yyyy-MM-ddEEE, d MMM yyyy HH:mm:ss Z"); //$NON-NLS-1$

	public AtomItemHandler(FeedCollection registry, Feed feed) {
		this.collection = registry;
		this.feed = feed;
		item = new InternalArticle(feed, UUID.randomUUID());
		item.setAddedDate(System.currentTimeMillis());
	}

	public void endElement(String qName) throws SAXException {
		super.endElement(qName);
		// Mandatory
		if (qName.equals(TITLE)) {
			item.setTitle(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(LINK)) {
			item.setLink(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(SUMMARY)) {
			item.internalSetText(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(CONTENT)) {
			item.internalSetText(getBuffer().toString());
			setCapture(false);
		}
		if (qName.equals(ID)) {
			item.setGuid(getBuffer().toString());
			setCapture(false);
		}
		// RFC822 date specification
		if (qName.equals(ISSUED)) {
			item.setPublicationDate(parse(getBuffer().toString()).getTime());
			setCapture(false);
		}
		if (qName.equals(ENTRY)) {
			if (!collection.hasArticle(item.getGuid())) {
				collection.addNew(item);
			}
		}
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		super.startElement(qName, atts);
		if (qName.equals(TITLE) || qName.equals(SUMMARY)
				|| qName.equals(ISSUED) || qName.equals(ID)
				|| qName.equals(CONTENT) || qName.equals(UPDATED)) {
			setCapture(true);
		}
		// Must exist if there is not a "content" element
		if (qName.equals(LINK)) {
			item.setLink(atts.getValue("href")); //$NON-NLS-1$
		}
		return this;
	}
}
