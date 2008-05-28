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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
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

	public static final String IMG_NEW_FEED_WIZBAN = "new_feed_banner"; //$NON-NLS-1$

	private static AggregatorUIPlugin plugin;

	/**
	 * The constructor
	 */
	public AggregatorUIPlugin() {
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
		reg.put(IMG_NEW_FEED_WIZBAN, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/wizban/new_feed_wizard.png")); //$NON-NLS-1$
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
		if (plugin == null) {
			plugin = new AggregatorUIPlugin();
		}
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
