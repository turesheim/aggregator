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
package no.resheim.aggregator.core.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "no.resheim.aggregator.core.ui.messages"; //$NON-NLS-1$
	public static String AggregatorUIPlugin_Browser_Title;
	public static String AggregatorUIPlugin_Browser_Tooltip;
	public static String ArticleViewer_ObjectHTMLCode;
	public static String ArticleViewer_ObjectHTMLCode_File;
	public static String ArticleViewer_Published;
	public static String ArticleViewer_UnknownPublicationDate;
	public static String ArticleViewer_Updated;
	public static String NewFeedWizard_Title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
