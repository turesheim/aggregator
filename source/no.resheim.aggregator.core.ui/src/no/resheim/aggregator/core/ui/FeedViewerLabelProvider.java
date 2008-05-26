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

import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.Folder;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * This label provider will listen to any preference changes and update it's
 * internal settings accordingly.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
/**
 * @author torkildr
 * 
 */
public class FeedViewerLabelProvider extends LabelProvider implements
		ILabelProvider, IColorProvider, IPropertyChangeListener {

	/** Preference: show unread items in header */
	private boolean pShowUnreadCount = true;

	/**
	 * 
	 */
	public FeedViewerLabelProvider() {
		super();
		initialize();
	}

	private void initialize() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.addPropertyChangeListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element instanceof Folder) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FOLDER);
		}
		if (element instanceof Feed) {
			return AggregatorUIPlugin.getDefault().getImage(
					AggregatorUIPlugin.IMG_FEED_OBJ);
		}
		if (element instanceof Article) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(
					ISharedImages.IMG_OBJ_FILE);
		}
		return PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJ_ELEMENT);
	}

	public String getText(Object element) {
		if (element instanceof Feed) {
			Feed feed = (Feed) element;
			StringBuffer sb = new StringBuffer();
			sb.append(feed.getTitle());
			if (pShowUnreadCount) {
				int unread = feed.getRegistry().getItemCount(feed);
				if (unread > 0) {
					sb.append(" ("); //$NON-NLS-1$
					sb.append(unread);
					sb.append(")"); //$NON-NLS-1$
				}
			}
			return sb.toString();
		}
		if (element instanceof Article)
			return ((Article) element).getTitle();

		if (element instanceof Folder)
			return ((Folder) element).getTitle();
		// Fallback, should never happen
		return element.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof Article) {
			if (((Article) element).isRead()) {
				return Display.getDefault().getSystemColor(
						SWT.COLOR_LIST_FOREGROUND);
			} else {
				return Display.getDefault().getSystemColor(SWT.COLOR_RED);

			}
		}
		return null;
	}

	@Override
	public void dispose() {
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		pShowUnreadCount = store
				.getBoolean(PreferenceConstants.P_SHOW_UNREAD_COUNT);

	}

}
