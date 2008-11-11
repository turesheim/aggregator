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

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.HashMap;

import no.resheim.aggregator.core.AggregatorPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
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

	/** Flash MIME type */
	public static final String MIME_FLASH = "application/x-shockwave-flash"; //$NON-NLS-1$

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

	public static final String IMG_PLAY_MEDIA = "play_media"; //$NON-NLS-1$

	/** Identifier for feed item image descriptor */
	public static final String IMG_ADD_OBJ = "add_obj"; //$NON-NLS-1$

	/** Identifier for feed item image descriptor */
	public static final String IMG_FEED_OBJ = "feed_obj"; //$NON-NLS-1$

	public static final String IMG_FOLDER_OBJ = "folder_obj"; //$NON-NLS-1$

	public static final String IMG_ARTICLE_OBJ = "article_obj"; //$NON-NLS-1$

	public static final String IMG_TRASH_OBJ = "trash_obj"; //$NON-NLS-1$

	public static final String IMG_NEW_FEED_WIZBAN = "new_feed_banner"; //$NON-NLS-1$

	public static final String IMG_DEC_WARNING = "warning_dec"; //$NON-NLS-1$

	public static final String IMG_DEC_ERROR = "error_dec"; //$NON-NLS-1$

	public static final String IMG_DEC_MEDIA = "media_dec"; //$NON-NLS-1$

	public static final String IMG_MARK_IMPORTANT = "mark_important"; //$NON-NLS-1$

	public static final String IMG_MARK_1PRI = "mark_1pri"; //$NON-NLS-1$
	public static final String IMG_MARK_2PRI = "mark_2pri"; //$NON-NLS-1$
	public static final String IMG_MARK_3PRI = "mark_3pri"; //$NON-NLS-1$
	public static final String IMG_MARK_TODO = "mark_todo"; //$NON-NLS-1$
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

	final IExtensionRegistry ereg = Platform.getExtensionRegistry();

	private static final String MEDIAPLAYERS_ID = "no.resheim.aggregator.core.ui.contentHandlers"; //$NON-NLS-1$

	private HashMap<String, ArrayList<ContentHandler>> contentHandlers;

	public ContentHandler getContentHandler(String type) {
		if (contentHandlers == null) {
			initContentHandlers();
		}
		ArrayList<ContentHandler> typeHandlers = contentHandlers.get(type);
		if (typeHandlers != null && typeHandlers.size() > 0) {
			return typeHandlers.get(0);
		}
		return null;
	}

	private void initContentHandlers() {
		contentHandlers = new HashMap<String, ArrayList<ContentHandler>>();
		synchronized (contentHandlers) {
			IConfigurationElement[] players = ereg
					.getConfigurationElementsFor(MEDIAPLAYERS_ID);
			for (IConfigurationElement player : players) {
				String code = player.getChildren("code")[0].getValue(); //$NON-NLS-1$
				String type = player.getAttribute("type"); //$NON-NLS-1$
				String name = player.getAttribute("name"); //$NON-NLS-1$
				ArrayList<ContentHandler> typeHandlers = contentHandlers
						.get(type);
				if (typeHandlers == null)
					typeHandlers = new ArrayList<ContentHandler>();
				typeHandlers.add(new ContentHandler(type, name, code));
				contentHandlers.put(type, typeHandlers);
			}
		}
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
		reg.put(IMG_PLAY_MEDIA, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/etool16/play_media.gif")); //$NON-NLS-1$
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
		reg.put(IMG_DEC_ERROR, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/error_co.gif")); //$NON-NLS-1$
		reg.put(IMG_DEC_MEDIA, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/media_co.gif")); //$NON-NLS-1$
		reg.put(IMG_MARK_IMPORTANT, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/mark_important.gif")); //$NON-NLS-1$
		reg.put(IMG_MARK_TODO, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/mark_todo.gif")); //$NON-NLS-1$
		reg.put(IMG_MARK_1PRI, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/mark_1.gif")); //$NON-NLS-1$
		reg.put(IMG_MARK_2PRI, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/mark_2.gif")); //$NON-NLS-1$
		reg.put(IMG_MARK_3PRI, imageDescriptorFromPlugin(PLUGIN_ID,
				"icons/ovr16/mark_3.gif")); //$NON-NLS-1$
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

	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage
					.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(),
					colorModel.getGreenMask(), colorModel.getBlueMask());
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0],
							pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage
					.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF,
						blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(bufferedImage.getWidth(),
					bufferedImage.getHeight(), colorModel.getPixelSize(),
					palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
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
