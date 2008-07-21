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

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * Common node handler.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface IElementHandler {
	// Common element and attribute names
	static final String LINK = "link"; //$NON-NLS-1$
	static final String TITLE = "title"; //$NON-NLS-1$
	static final String ITEM = "item"; //$NON-NLS-1$
	static final String DESCRIPTION = "description"; //$NON-NLS-1$

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException;

	public void endElement(String qName) throws SAXException;

	public void characters(char[] ch, int start, int length)
			throws SAXException;
}