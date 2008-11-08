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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;

import no.resheim.aggregator.core.ui.internal.FeedDescriptionFormatter;
import no.resheim.aggregator.core.ui.internal.FeedItemTitle;
import no.resheim.aggregator.core.ui.internal.FeedViewWidgetFactory;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * A control that is used to show aggregator articles. A {@link Browser}
 * instance will be used to render the HTML.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ArticleViewer extends Composite implements IPropertyChangeListener {
	private static final String DEFAULT_CONTENT_TYPE = "text/html"; //$NON-NLS-1$
	private static final String MEDIAPLAYERS_ID = "no.resheim.aggregator.core.ui.mediaHandlers"; //$NON-NLS-1$
	private FeedItemTitle title;
	private Browser browser;
	/** Preference: Name of the font used in the details pane */
	private String pPresentationFontFamily;

	/** Preference: The size of the font used in the details pane (pixels) */
	private int pPresentationFontSize;

	private ListenerList listeners;

	public void addListener(IArticleViewerListener listener) {
		listeners.add(listener);
	}

	public void removeListener(IArticleViewerListener listener) {
		listeners.remove(listener);
	}

	boolean fInterceptBrowser = false;

	@Override
	public void dispose() {
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this);
		browser.removeStatusTextListener(fBrowserListener);
		super.dispose();
	}

	/**
	 * @param parent
	 * @param style
	 */
	public ArticleViewer(Composite parent, int style) {
		super(parent, style);
		final FeedViewWidgetFactory factory = new FeedViewWidgetFactory();
		setLayout();
		// Special widget for item title
		title = new FeedItemTitle(this, factory);
		title.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		listeners = new ListenerList();
		browser = new Browser(this, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Button playButton = new Button(parent, SWT.NONE);
		// playButton.setText(">");

		updateFromPreferences();
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(this);
		fBrowserListener = new BrowserStatusTextListener();
		browser.addStatusTextListener(fBrowserListener);
		browser.addLocationListener(new LocationListener() {

			public void changed(LocationEvent event) {
			}

			public void changing(LocationEvent event) {
				if (fInterceptBrowser) {
					IPreferenceStore store = AggregatorUIPlugin.getDefault()
							.getPreferenceStore();
					String setting = store
							.getString(PreferenceConstants.P_OPEN_LINK);
					switch (PreferenceConstants.LinkOpen.valueOf(setting)) {
					case EDITOR:
						event.doit = false;
						try {
							AggregatorUIPlugin.getSharedBrowser().openURL(
									new URL(event.location));
						} catch (PartInitException e1) {
							e1.printStackTrace();
						} catch (MalformedURLException e1) {
							e1.printStackTrace();
						}
						break;
					case EXTERNAL:
						event.doit = false;
						try {
							PlatformUI.getWorkbench().getBrowserSupport()
									.getExternalBrowser().openURL(
											new URL(event.location));
						} catch (PartInitException e) {
							e.printStackTrace();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}
						break;
					default:
						break;
					}
				}
			}
		});
	}

	private BrowserStatusTextListener fBrowserListener;

	private class BrowserStatusTextListener implements StatusTextListener {
		public void changed(StatusTextEvent event) {
			for (Object listener : listeners.getListeners()) {
				((IArticleViewerListener) listener)
						.statusTextChanged(event.text);
			}
		}
	}

	private void setLayout() {
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.marginBottom = 0;
		layout.marginTop = 0;
		setLayout(layout);
	}

	public void show(Object item) {
		fInterceptBrowser = false;
		if (item instanceof Article) {
			showDescription((Article) item);
		} else if (item instanceof Feed) {
			showDescription((Feed) item);
		}
		fInterceptBrowser = true;
	}

	/**
	 * Shows the description of the selected item
	 * 
	 * @param item
	 */
	private void showDescription(Article item) {
		if (item == null)
			return;
		title.setTitle(item.getTitle(), null);
		try {
			if (item.getMediaEnclosureType().length() == 0) {
				String text = getMediaPlayerHTML(DEFAULT_CONTENT_TYPE, item
						.getText());
				browser.setText(text);
			} else {
				String text = getMediaPlayerHTML(item.getMediaEnclosureType(),
						item.getMediaEnclosureURL());
				browser.setText(text);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showDescription(Feed feed) {
		if (feed == null)
			return;
		browser.setText(FeedDescriptionFormatter.format(feed,
				pPresentationFontFamily, pPresentationFontSize));
		title.setTitle(feed.getTitle(), null);
	}

	final IExtensionRegistry ereg = Platform.getExtensionRegistry();

	private String getMediaPlayerHTML(String contentType, String content)
			throws IOException {
		String code = MessageFormat.format(
				Messages.ArticleViewer_NoContentHandler, new Object[] {
					contentType
				});

		IConfigurationElement[] players = ereg
				.getConfigurationElementsFor(MEDIAPLAYERS_ID);
		for (IConfigurationElement player : players) {
			IConfigurationElement[] types = player.getChildren("type"); //$NON-NLS-1$
			for (IConfigurationElement type : types) {
				if (type.getAttribute("id").equals(contentType)) { //$NON-NLS-1$
					// There has to be one code element
					code = player.getChildren("code")[0].getValue(); //$NON-NLS-1$
					// If a file is specified, we must merge in the location of
					// that.
					if (player.getAttribute("file") != null) { //$NON-NLS-1$
						Bundle bundle = Platform.getBundle(player
								.getContributor().getName());
						Path path = new Path(player.getAttribute("file")); //$NON-NLS-1$
						URL url = FileLocator.find(bundle, path, null);
						code = code.replaceAll("\\$\\{file\\}", FileLocator //$NON-NLS-1$
								.resolve(url).toExternalForm());
					}
					code = code.replaceAll("\\$\\{content\\}", content); //$NON-NLS-1$
					code = code.replaceAll(
							"\\$\\{font-family\\}", pPresentationFontFamily); //$NON-NLS-1$
					code = code
							.replaceAll(
									"\\$\\{font-size\\}", String.valueOf(pPresentationFontSize)); //$NON-NLS-1$
					return code;
				}
			}
		}
		return code;
	}

	private void updateFromPreferences() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		FontData fd = PreferenceConverter.getFontData(store,
				PreferenceConstants.P_PREVIEW_FONT);
		pPresentationFontFamily = fd.getName();
		pPresentationFontSize = fd.getHeight();
	}

	public void propertyChange(PropertyChangeEvent event) {
		updateFromPreferences();
	}
}
