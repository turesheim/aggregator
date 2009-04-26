/*******************************************************************************
 * Copyright (c) 2008-2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui;

import java.util.HashMap;
import java.util.regex.Matcher;

import no.resheim.aggregator.core.data.MediaContent;

import org.eclipse.core.runtime.Assert;

/**
 * 
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ContentHandler {
	/** Code to use in an browser for displaying the content type */
	private String code;
	/** The content type MIME string */
	private String contentType;
	/** The name of the content type */
	private String name;
	/** The suffix associated with the content type */
	private String suffix;

	/**
	 * Creates a new content handler instance for the given content type,
	 * suffix, name and code.
	 * 
	 * @param contentType
	 *            the content type to handle
	 * @param suffix
	 *            the suffix associated with the content type
	 * @param name
	 *            the name of the content type
	 * @param code
	 *            the HTML code to embed in a browser
	 */
	public ContentHandler(String contentType, String suffix, String name,
			String code) {
		this.contentType = contentType;
		this.suffix = suffix;
		this.name = name;
		this.code = code;
	}

	public String getContentType() {
		return contentType;
	}

	public String getEmbedCode(HashMap<String, String> properties) {
		String result = code;
		for (String property : properties.keySet()) {
			String replacement = properties.get(property);
			Assert.isNotNull(replacement, "Illegal value in property \""
					+ property + "\"");
			String value = Matcher.quoteReplacement(properties.get(property));
			result = result.replaceAll("\\$\\{" + property + "\\}", value); //$NON-NLS-1$ //$NON-NLS-2$

		}
		return result;

	}

	/**
	 * Returns a string representing the given content.
	 * 
	 * @param content
	 *            the content to handle
	 * @return the content name
	 */
	public String getFormattedContentName(MediaContent content) {
		StringBuffer sb = new StringBuffer();
		sb.append(name);
		return sb.toString();
	}

	public String getSuffix() {
		return suffix;
	}
}
