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
package no.resheim.aggregator.core.ui;

/**
 * Constant definitions for plug-in preferences
 * 
 * @author Torkild Ulvøy Resheim
 */
public class PreferenceConstants {
	public enum LinkOpen {
		/** Links open in the aggregator view */
		VIEW,
		/** Links open in an internal browser in the editor area */
		EDITOR,
		/** Links open in an external browser */
		EXTERNAL
	};

	public static final String P_OPEN_LINK = "link_open"; //$NON-NLS-1$
	/** Automatically mark previewed items as read */
	public static final String P_PREVIEW_IS_READ = "preview_is_read"; //$NON-NLS-1$
	/** Show the number of unread articles in the feed label */
	public static final String P_SHOW_UNREAD_COUNT = "show_unread_count"; //$NON-NLS-1$
	/** The font to use when showing the article */
	public static final String P_PREVIEW_FONT = "preview_font"; //$NON-NLS-1$
	/** The default archiving method to use */
	public static final String P_ARCHIVING_METHOD = "archiving_method"; //$NON-NLS-1$
	/** The default number of days to keep articles */
	public static final String P_ARCHIVING_DAYS = "archiving_days"; //$NON-NLS-1$
	/** The default number of items to keep */
	public static final String P_ARCHIVING_ITEMS = "archiving_items"; //$NON-NLS-1$
	/** The default update period */
	public static final String P_UPDATING_PERIOD = "updating_period"; //$NON-NLS-1$
	/** The update interval */
	public static final String P_UPDATING_INTERVAL = "updating_interval"; //$NON-NLS-1$
	/** Show the browser location bar or not */
	public static final String P_BROWSER_LOCATION_BAR = "browser_location_bar"; //$NON-NLS-1$
	/** Show the browser navigation bar or not */
	public static final String P_BROWSER_NAVIGATION_BAR = "browser_navigation_bar"; //$NON-NLS-1$
	/** Open each article in a new frame or reuse the preview window */
	public static final String P_BROWSER_REUSE_WINDOW = "browser_reuse_window"; //$NON-NLS-1$
}
