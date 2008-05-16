/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/

package no.resheim.aggregator.core.ui.internal;

import no.resheim.aggregator.model.Feed;

public class FeedDescriptionFormatter {

	private static final String LINEFEED = System.getProperty("line.separator"); //$NON-NLS-1$

	private static String formatCell(String contents, String font, int size) {
		StringBuilder sb = new StringBuilder();
		sb.append("<td style=\"font-family: '"); //$NON-NLS-1$
		sb.append(font);
		sb.append("';font-size: "); //$NON-NLS-1$
		sb.append(size);
		sb.append("pt\">"); //$NON-NLS-1$
		sb.append(contents);
		sb.append("</td>"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String formatHeader(String contents, int span, String font,
			int size) {
		StringBuilder sb = new StringBuilder();
		sb.append("<th colspan=\""); //$NON-NLS-1$
		sb.append(span);
		sb.append("\" style=\"font-family: '"); //$NON-NLS-1$
		sb.append(font);
		sb.append("';font-size: "); //$NON-NLS-1$
		sb.append(size);
		sb.append("pt; text-align: left; text-decoration: underline;\">"); //$NON-NLS-1$
		sb.append(contents);
		sb.append("</th>"); //$NON-NLS-1$
		return sb.toString();
	}

	private static String formatRow(String c1, String c2, String font, int size) {
		StringBuilder sb = new StringBuilder();
		sb.append("<tr>"); //$NON-NLS-1$
		sb.append(formatCell(c1, font, size));
		sb.append(formatCell(c2, font, size));
		sb.append("</tr>"); //$NON-NLS-1$
		sb.append(LINEFEED);
		return sb.toString();
	}

	public static String format(Feed feed, String font, int size) {
		StringBuffer sb = new StringBuffer();
		sb.append("<div>"); //$NON-NLS-1$
		sb.append("<table width=\"100%\"><tr>"); //$NON-NLS-1$
		sb.append(formatHeader(Messages.FeedDescriptionFormatter_Description,
				1, font, size));
		sb.append("</tr><tr>"); //$NON-NLS-1$
		sb.append(formatCell(feed.getDescription(), font, size));
		sb.append("</tr></table>"); //$NON-NLS-1$

		sb.append("<table width=\"100%\">"); //$NON-NLS-1$
		sb.append("<tr>"); //$NON-NLS-1$
		sb.append(formatHeader(Messages.FeedDescriptionFormatter_Properties, 2,
				font, size));
		sb.append("</tr>"); //$NON-NLS-1$
		sb.append(formatRow(Messages.FeedDescriptionFormatter_Type, feed
				.getType(), font, size));
		sb.append(formatRow(Messages.FeedDescriptionFormatter_URL, feed
				.getURL(), font, size));
		sb.append(formatRow(Messages.FeedDescriptionFormatter_Site, feed
				.getLink(), font, size));
		sb.append(formatRow(Messages.FeedDescriptionFormatter_Copyright, feed
				.getCopyright(), font, size));
		sb.append(formatRow(Messages.FeedDescriptionFormatter_Editor, feed
				.getEditor(), font, size));
		sb.append(formatRow(Messages.FeedDescriptionFormatter_Webmaster, feed
				.getWebmaster(), font, size));
		sb.append("</table>"); //$NON-NLS-1$
		sb.append("</div>"); //$NON-NLS-1$
		return sb.toString();
	}
}
