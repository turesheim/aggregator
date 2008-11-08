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

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.MediaContent;
import no.resheim.aggregator.core.ui.internal.FeedDescriptionFormatter;
import no.resheim.aggregator.core.ui.internal.FeedItemTitle;
import no.resheim.aggregator.core.ui.internal.FeedViewWidgetFactory;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
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
	private static final String MEDIAPLAYERS_ID = "no.resheim.aggregator.core.ui.contentHandlers"; //$NON-NLS-1$
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

	private IAction playMediaAction;

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

		createActions();
	}

	ActionContributionItem playMediaItem;

	class PlayMediaAction extends Action implements IMenuCreator {
		/**
		 * @param text
		 * @param style
		 */
		public PlayMediaAction(String text, int style) {
			super(text, style);
			setMenuCreator(this);
		}

		@Override
		public void run() {
			if (selectedArticle != null) {
				viewContent(selectedArticle, true);
			}
		}

		public void dispose() {
			if (fMenu != null) {
				fMenu.dispose();
			}
		}

		protected void addActionToMenu(Menu parent, IAction action) {
			ActionContributionItem item = new ActionContributionItem(action);
			item.fill(parent, -1);
		}

		Menu fMenu;

		public Menu getMenu(Control parent) {
			if (fMenu != null) {
				fMenu.dispose();
			}
			fMenu = new Menu(parent);
			if (selectedArticle.hasMedia()) {
				MediaContent[] media = selectedArticle.getMediaContent();
				for (MediaContent mediaContent : media) {
					Action action = new Action() {

					};
					action.setText(mediaContent.getContentURL());
					addActionToMenu(fMenu, action);
				}
			}
			return fMenu;
		}

		public Menu getMenu(Menu parent) {
			return null;
		}

	}

	private void createActions() {
		playMediaAction = new PlayMediaAction("Play media",
				IAction.AS_DROP_DOWN_MENU);
		playMediaAction.setImageDescriptor(AggregatorUIPlugin.getDefault()
				.getImageRegistry().getDescriptor(
						AggregatorUIPlugin.IMG_PLAY_MEDIA));

		playMediaItem = new ActionContributionItem(playMediaAction);
		title.getToolBarManager().add(playMediaItem);
		title.getToolBarManager().update(true);

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

	private Article selectedArticle;

	public void show(Object item) {
		fInterceptBrowser = false;
		if (item instanceof Article) {
			selectedArticle = (Article) item;
			viewArticle(selectedArticle);
		} else if (item instanceof Feed) {
			showDescription((Feed) item);
		}
		fInterceptBrowser = true;
	}

	/**
	 * Displays the selected article. If the article contains media, the first
	 * media content is being used. If it's plain text the "text/html" content
	 * handler is used, and if it contains a closure, this is being used.
	 * 
	 * @param item
	 *            the article to display
	 */
	private void viewArticle(Article item) {
		if (item == null)
			return;
		title.setTitle(item.getTitle(), null);
		playMediaItem.setVisible(item.hasMedia());
		title.getToolBarManager().update(true);
		viewContent(item, false);
	}

	private void viewContent(Article item, boolean showMedia) {
		fInterceptBrowser = false;
		String text = null;
		if (showMedia) {
			if (item.getMediaContent().length > 0) {
				MediaContent media = item.getMediaContent()[0];
				text = getContentHandlerHTML(media.getContentType(), media
						.getContentURL());
				browser.setText(text);
				return;
			}
		}
		// Revert to default (we may not be able to set media content
		if (text == null) {
			text = getContentHandlerHTML(DEFAULT_CONTENT_TYPE, item.getText());
		}
		browser.setText(text);
		fInterceptBrowser = true;
	}

	private void showDescription(Feed feed) {
		if (feed == null)
			return;
		browser.setText(FeedDescriptionFormatter.format(feed,
				pPresentationFontFamily, pPresentationFontSize));
		title.setTitle(feed.getTitle(), null);
	}

	final IExtensionRegistry ereg = Platform.getExtensionRegistry();

	private String getContentHandlerHTML(String contentType, String content) {
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
						try {
							code = code.replaceAll("\\$\\{file\\}", FileLocator //$NON-NLS-1$
									.resolve(url).toExternalForm());
						} catch (IOException e) {
							e.printStackTrace();
						}
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
