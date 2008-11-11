/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui;

import no.resheim.aggregator.core.data.MediaContent;

/**
 * 
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ContentHandler {
	public String getSuffix() {
		return suffix;
	}

	public String getContentType() {
		return contentType;
	}

	private String code;
	private String contentType;
	private String name;
	private String suffix;

	public ContentHandler(String contentType, String suffix, String name,
			String code) {
		this.contentType = contentType;
		this.suffix = suffix;
		this.name = name;
		this.code = code;
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

	/**
	 * FIXME: Use properties and a proper variable replacer.
	 * 
	 * @param content
	 * @param fontFace
	 * @param fontFamily
	 * 
	 * 
	 * @return
	 */
	public String getEmbedCode(String content, String fontFace, int fontFamily) {
		// // There has to be one code element
		//		code = player.getChildren("code")[0].getValue(); //$NON-NLS-1$
		// // If a file is specified, we must merge in the location of
		// // that.
		//		if (player.getAttribute("file") != null) { //$NON-NLS-1$
		// Bundle bundle = Platform.getBundle(player.getContributor()
		// .getName());
		//			Path path = new Path(player.getAttribute("file")); //$NON-NLS-1$
		// URL url = FileLocator.find(bundle, path, null);
		// try {
		//				code = code.replaceAll("\\$\\{file\\}", FileLocator //$NON-NLS-1$
		// .resolve(url).toExternalForm());
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		String result = code;
		result = result.replaceAll("\\$\\{content\\}", content); //$NON-NLS-1$
		result = result.replaceAll("\\$\\{font-family\\}", fontFace); //$NON-NLS-1$
		result = result.replaceAll(
				"\\$\\{font-size\\}", String.valueOf(fontFamily)); //$NON-NLS-1$
		return result;

	}
}
