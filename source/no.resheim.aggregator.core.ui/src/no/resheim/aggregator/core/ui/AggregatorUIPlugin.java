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

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorItem;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.AggregatorItem.Flag;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AggregatorUIPlugin extends AbstractUIPlugin {
	/** Identifier for the select feed collection command */
	public static final String CMD_SELECT_COLLECTION = "no.resheim.aggregator.core.ui.selectCollection"; //$NON-NLS-1$

	/** The plug-in ID */
	public static final String PLUGIN_ID = "no.resheim.aggregator.core.ui"; //$NON-NLS-1$

	/** Identifier for the view icon */
	private static final String IMG_VIEW_ICON = "LOGO"; //$NON-NLS-1$

	/** Identifier for disabled refresh button image descriptor */
	public static final String IMG_DISABLED_REFRESH = "disabled_refresh"; //$NON-NLS-1$

	/** Identifier for enabled refresh button image descriptor */
	public static final String IMG_ENABLED_REFRESH = "enabled_refresh"; //$NON-NLS-1$

	/** Identifier for feed item image descriptor */
	public static final String IMG_ADD_OBJ = "add_obj"; //$NON-NLS-1$

	/** Identifier for feed item image descriptor */
	public static final String IMG_FEED_OBJ = "feed_obj"; //$NON-NLS-1$

	public static final String IMG_FOLDER_OBJ = "folder_obj"; //$NON-NLS-1$

	public static final String IMG_ARTICLE_OBJ = "article_obj"; //$NON-NLS-1$

	public static final String IMG_TRASH_OBJ = "trash_obj"; //$NON-NLS-1$

	public static final String IMG_NEW_FEED_WIZBAN = "new_feed_banner"; //$NON-NLS-1$

	public static final String IMG_DEC_WARNING = "warning_dec"; //$NON-NLS-1$

	public static final String IMG_DEC_ERRROR = "error_dec"; //$NON-NLS-1$

	private static AggregatorUIPlugin plugin;

	/**
	 * The constructor
	 */
	public AggregatorUIPlugin() {
		super();
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (fEditorBrowser != null) {
			fEditorBrowser.close();
		}
		super.stop(context);
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(IMG_VIEW_ICON, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/feed-icon-16x16.png")); //$NON-NLS-1$
		reg.put(IMG_ENABLED_REFRESH, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/clcl16/nav_refresh.gif")); //$NON-NLS-1$
		reg.put(IMG_DISABLED_REFRESH, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/dlcl16/nav_refresh.gif")); //$NON-NLS-1$
		reg.put(IMG_ADD_OBJ, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/obj16/add_obj.gif")); //$NON-NLS-1$
		reg.put(IMG_FEED_OBJ, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/obj16/feed_obj.gif")); //$NON-NLS-1$
		reg.put(IMG_TRASH_OBJ, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/obj16/trashfolder_obj.gif")); //$NON-NLS-1$
		reg.put(IMG_NEW_FEED_WIZBAN, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/wizban/new_feed_wizard.png")); //$NON-NLS-1$
		reg.put(IMG_DEC_WARNING, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/warning_co.gif")); //$NON-NLS-1$
		reg.put(IMG_DEC_ERRROR, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/error_co.gif")); //$NON-NLS-1$
		// Copy some stuff from the shared
		reg.put(IMG_ARTICLE_OBJ, PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
		reg.put(IMG_FOLDER_OBJ, PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));

	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	private static IWebBrowser fEditorBrowser;

	public static IWebBrowser getSharedBrowser() {
		if (fEditorBrowser == null) {
			try {
				fEditorBrowser = PlatformUI.getWorkbench().getBrowserSupport()
						.createBrowser(
								IWorkbenchBrowserSupport.NAVIGATION_BAR
										| IWorkbenchBrowserSupport.LOCATION_BAR
										| IWorkbenchBrowserSupport.AS_EDITOR,
								AggregatorPlugin.PLUGIN_ID,
								Messages.AggregatorUIPlugin_Browser_Title,
								Messages.AggregatorUIPlugin_Browser_Tooltip);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return fEditorBrowser;
	}

	/**
	 * Return an image for the aggregator item and decorate if the status is not
	 * null
	 * 
	 * @param item
	 * @param status
	 * @return an image representing the item
	 */
	Image getImage(Object item, IStatus status) {
		String baseId = null;

		if (item instanceof Feed)
			baseId = IMG_FEED_OBJ;
		if (item instanceof Folder) {
			baseId = IMG_FOLDER_OBJ;
			if (((AggregatorItem) item).getFlags().contains(Flag.TRASH)) {
				baseId = IMG_TRASH_OBJ;
			}
		}
		if (item instanceof Article)
			baseId = IMG_ARTICLE_OBJ;
		if (baseId == null)
			return null;

		String id = baseId;

		ImageDescriptor si = null;
		if (status != null) {
			switch (status.getSeverity()) {
			case IStatus.ERROR:
				si = getImageRegistry().getDescriptor(IMG_DEC_ERRROR);
				break;
			case IStatus.WARNING:
				si = getImageRegistry().getDescriptor(IMG_DEC_WARNING);
				break;
			}
			if (si != null) {
				id = id + "_" + Integer.toString(status.getSeverity()); //$NON-NLS-1$
			}
		}
		if (getImageRegistry().get(id) != null) {
			return getImageRegistry().get(id);
		}
		Image baseImage = getImageRegistry().get(baseId);
		DecorationOverlayIcon icon = new DecorationOverlayIcon(baseImage, si,
				IDecoration.BOTTOM_LEFT);
		getImageRegistry().put(id, icon);
		return getImage(id);
	}

	/**
	 * Returns the instance of the image with the given key.
	 * 
	 * @param key
	 *            the key identifying the image
	 * @return the image
	 */
	public Image getImage(String key) {
		return this.getImageRegistry().get(key);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AggregatorUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the
	 * thread calling this method has an associated display. If so, this display
	 * is returned. Otherwise the method returns the default display.
	 */
	private static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	public static void openMessageDialog(String title, IStatus status) {
		displayErrorMessage(title, status.getMessage());
	}

	private static void displayErrorMessage(final String title,
			final String message) {
		getStandardDisplay().asyncExec(new Runnable() {
			public void run() {
				Shell shell = getStandardDisplay().getActiveShell();
				if (shell == null) {
					shell = new Shell(new Display());
				}
				MessageDialog.openError(shell, title, message);
			}
		});

	}
}
