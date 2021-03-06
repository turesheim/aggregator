/*******************************************************************************
 * Copyright (c) 2008-2009 Torkild Ulvøy Resheim.
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

import java.io.ByteArrayInputStream;
import java.text.DateFormat;
import java.util.Calendar;

import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.BrokenItem;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItem.Flag;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * This label provider will listen to any preference changes and update it's
 * internal settings accordingly.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CollectionViewerLabelProvider extends ColumnLabelProvider
		implements ILabelProvider, IColorProvider, IPropertyChangeListener,
		ITableLabelProvider {

	/** The image registry we're using */
	private static ImageRegistry registry = AggregatorUIPlugin.getDefault()
			.getImageRegistry();

	/** Font to use when indicating that a feed has unread items */
	private Font bold;

	/**
	 * Font to use when indicating that a feed has unread items and is being
	 * updated
	 */
	private Font boldItalic;

	final Calendar calendar = Calendar.getInstance();

	final DateFormat dateFormat = DateFormat.getDateTimeInstance();

	/** Font to use when indicating that a feed is being updated */
	private Font italic;

	/** Preference: show unread items in header */
	private boolean pShowUnreadCount = true;;

	private Color pUnreadItemColor;

	/**
	 * 
	 */
	public CollectionViewerLabelProvider() {
		super();
		initialize();
	}

	@Override
	public void dispose() {
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this);
		pUnreadItemColor.dispose();
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
	 */
	public Color getBackground(Object element) {
		return null;
	}

	private Image getBaseFeedImageDescriptor(Subscription feed) {
		// The feed has a custom image
		if (feed.getImageData() != null) {
			String id = feed.getUUID().toString();
			ImageDescriptor descriptor = registry.getDescriptor(id);
			// It's not in the registry so we need to make an instance
			if (descriptor == null) {
				ByteArrayInputStream bis = new ByteArrayInputStream(feed
						.getImageData());
				ImageData data = new ImageData(bis);
				Image image = new Image(getDisplay(), data.scaledTo(16, 16));
				return image;
			}
			return registry.get(id);
		} else {
			// Use the default feed image
			return registry.get(AggregatorUIPlugin.IMG_FEED_OBJ);
		}
	}

	private String getBaseId(AggregatorItem item) {
		String baseId = null;
		if (item instanceof Folder) {
			baseId = AggregatorUIPlugin.IMG_FOLDER_OBJ;
			if (item.getFlags().contains(Flag.TRASH)) {
				baseId = AggregatorUIPlugin.IMG_TRASH_OBJ;
			}
		}
		if (item instanceof Article) {
			if (((Article) item).isStarred()) {
				baseId = AggregatorUIPlugin.IMG_STARRED;
			} else {
				baseId = AggregatorUIPlugin.IMG_ARTICLE_OBJ;
			}
		}
		if (item instanceof BrokenItem) {
			baseId = AggregatorUIPlugin.IMG_ARTICLE_OBJ;
		}
		return baseId;
	}

	private AggregatorCollection getCollection(AggregatorItem item) {
		try {
			return item.getCollection();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (columnIndex == 0) {
			return getImage(element);
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		switch (columnIndex) {
		case 0:
			return getText(element);
		case 1:
			if (element instanceof Article)
				return ((Article) element).getLabelString();
			else
				return null;
		default:
			break;
		}
		return null;
	}

	private Display getDisplay() {
		return Display.getCurrent();
	}

	private Subscription getFeed(Object element) {
		if (element instanceof Folder) {
			return ((Folder) element).getFeed();
		}
		return null;

	}

	@Override
	public Font getFont(Object element) {
		Font font = JFaceResources.getDialogFont();
		if (element == null)
			return font;
		if (element instanceof Folder) {
			Folder folder = ((Folder) element);
			int unread = getCollection(folder).getUnreadItemCount(folder);
			Subscription feed = ((Folder) element).getFeed();
			if (feed != null && feed.isUpdating()) {
				if (unread > 0) {
					return boldItalic;
				} else {
					return italic;
				}
			}
			if (unread > 0) {
				return bold;
			}
		}
		return font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
	 */
	public Color getForeground(Object element) {
		if (element instanceof Article) {
			if (((Article) element).isRead()) {
				return Display.getDefault().getSystemColor(
						SWT.COLOR_LIST_FOREGROUND);
			} else {
				return pUnreadItemColor;

			}
		}
		return null;
	}

	/**
	 * Return an image for the aggregator item and decorate if the status is not
	 * null
	 * <p>
	 * Overlays should be 7x8 pixels. See
	 * http://wiki.eclipse.org/User_Interface_Guidelines
	 * </p>
	 * 
	 * @param item
	 * @param status
	 * @return an image representing the item
	 */
	private Image getImage(AggregatorItem item) {
		String label = "NONE";
		String id = getBaseId(item) + "_" + label; //$NON-NLS-1$
		ImageDescriptor type = null;

		// Add status overlays
		ImageDescriptor si = null;
		if (item instanceof BrokenItem) {
			si = registry.getDescriptor(AggregatorUIPlugin.IMG_DEC_ERROR);
			id += "_ERROR"; //$NON-NLS-1$
		}
		// Add MIME type overlays
		if (item instanceof Article) {
			if (((Article) item).hasMedia()) {
				type = registry.getDescriptor(AggregatorUIPlugin.IMG_DEC_MEDIA);
				id += "_flash"; //$NON-NLS-1$
			}
		}
		// If we already have the image in the registry return it.
		if (registry.get(id) != null) {
			return registry.get(id);
		}
		// Otherwise we have to compose a new instance
		Image baseImage = registry.get(getBaseId(item));
		Point size = new Point(16, 16);
		DecorationOverlayIcon icon = new DecorationOverlayIcon(baseImage,
				new ImageDescriptor[] { type, null, null, si, null }, size) {
		};
		registry.put(id, icon);
		return registry.get(id);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
	 */
	public Image getImage(Object element) {
		if (element == null) {
			return null;
		}
		if (element instanceof Subscription) {
			return getImage((Subscription) element, null, null);
		}
		if (element instanceof Folder
				&& ((Folder) element).getFeedUUID() != null) {
			Subscription feed = getCollection((Folder) element).getFeeds().get(
					((Folder) element).getFeedUUID());
			if (feed != null) {
				return getImage(feed, feed.getLastStatus(), (Folder) element);
			} else {
				return null;
			}
		} else if (element instanceof AggregatorItem) {
			return getImage((AggregatorItem) element);
		}
		return null;
	}

	private Image getImage(Subscription feed, IStatus status, Folder folder) {
		String id = AggregatorUIPlugin.IMG_FEED_OBJ;
		String label = "NONE";
		// The feed has a custom image
		if (feed.getImageData() != null) {
			id = feed.getUUID().toString();
		}
		if (folder != null) {
			id += "_" + label; //$NON-NLS-1$
		}
		// Add status overlays
		ImageDescriptor si = null;
		if (status != null) {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				si = registry.getDescriptor(AggregatorUIPlugin.IMG_DEC_ERROR);
				id += "_ERROR"; //$NON-NLS-1$
				break;
			case IStatus.WARNING:
				si = registry.getDescriptor(AggregatorUIPlugin.IMG_DEC_WARNING);
				id += "_WARNING"; //$NON-NLS-1$
				break;
			}
		}
		// If we already have a variant, use this.
		if (registry.get(id) != null) {
			return registry.get(id);
		}
		// Otherwise we'll have to create an image using the status overlay
		Image baseImage = getBaseFeedImageDescriptor(feed);
		Point size = new Point(16, 16);
		DecorationOverlayIcon icon = new DecorationOverlayIcon(baseImage,
				new ImageDescriptor[] { null, null, null, si, null }, size) {
		};
		// Store the image for the next time
		registry.put(id, icon);
		return registry.get(id);
	}

	public String getText(Object element) {
		if (element instanceof AggregatorItem) {
			AggregatorItem item = (AggregatorItem) element;
			if (element instanceof Subscription || element instanceof Folder) {
				StringBuffer sb = new StringBuffer();
				sb.append(item.getTitle());
				if (pShowUnreadCount && getCollection(item) != null) {
					int unread = getCollection(item).getUnreadItemCount(item);
					if (unread > 0) {
						sb.append(" ("); //$NON-NLS-1$
						sb.append(unread);
						sb.append(")"); //$NON-NLS-1$
					}
				}
				return sb.toString();
			}
			return item.getTitle();
		}
		// Fallback, should never happen
		return element.toString();
	}

	@Override
	public String getToolTipText(Object element) {
		// Fix for bug 561
		if (element == null)
			return null;
		Subscription f = getFeed(element);
		if (f != null) {
			IStatus s = f.getLastStatus();
			if (s != null && !s.isOK()) {
				if (s.getException() != null) {
					return s.getException().getLocalizedMessage();
				} else
					return s.getMessage();
			}
		}
		return null;
	}

	/**
	 * Initialises certain presentation values such as fonts and colours.
	 */
	private void initialize() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.addPropertyChangeListener(this);
		FontDescriptor fdItalic = JFaceResources.getDialogFontDescriptor()
				.setStyle(SWT.ITALIC);
		italic = fdItalic.createFont(getDisplay());
		FontDescriptor fdBold = JFaceResources.getDialogFontDescriptor()
				.setStyle(SWT.BOLD);
		bold = fdBold.createFont(getDisplay());
		FontDescriptor fdBoldItalic = JFaceResources.getDialogFontDescriptor()
				.setStyle(SWT.BOLD | SWT.ITALIC);
		boldItalic = fdBoldItalic.createFont(getDisplay());
		pUnreadItemColor = new Color(getDisplay(), PreferenceConverter
				.getColor(store, PreferenceConstants.P_UNREAD_ITEM_COLOR));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse
	 * .jface.util.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent event) {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		pShowUnreadCount = store
				.getBoolean(PreferenceConstants.P_SHOW_UNREAD_COUNT);
		if (pUnreadItemColor != null) {
			pUnreadItemColor.dispose();
		}
		pUnreadItemColor = new Color(getDisplay(), PreferenceConverter
				.getColor(store, PreferenceConstants.P_UNREAD_ITEM_COLOR));

	}
}
