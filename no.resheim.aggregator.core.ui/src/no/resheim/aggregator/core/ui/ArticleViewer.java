/*******************************************************************************
 * Copyright (c) 2007-2009 Torkild Ulvøy Resheim.
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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.HashMap;

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.MediaContent;
import no.resheim.aggregator.core.ui.internal.FeedItemTitle;
import no.resheim.aggregator.core.ui.internal.FeedViewWidgetFactory;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * A control that is used to show an aggregator item. A {@link Browser} instance
 * will be used to render the HTML.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ArticleViewer extends Composite implements IPropertyChangeListener {
	private static final String CONTENT_PROPERTY = "content";

	/** The default content type */
	private static final String DEFAULT_CONTENT_TYPE = "text/html"; //$NON-NLS-1$

	/** A component rendering the title of the item */
	private FeedItemTitle title;

	/** A component rendering the contents of the item */
	private Browser browser;

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
		contentProperties = new HashMap<String, String>();
		final FeedViewWidgetFactory factory = new FeedViewWidgetFactory();
		setLayout();
		// Special widget for item title
		title = new FeedItemTitle(this, factory);
		title.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		listeners = new ListenerList();
		browser = new Browser(this, SWT.NONE);
		browser.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		updateFromPreferences();
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(this);
		JFaceResources.getFontRegistry().addListener(this);
		fBrowserListener = new BrowserStatusTextListener();
		browser.addStatusTextListener(fBrowserListener);
		browser.addLocationListener(new LocationListener() {

			public void changed(LocationEvent event) {
			}

			public void changing(LocationEvent event) {
				if (fInterceptBrowser) {
					// Handle MSIE problem. Bug 645
					if (event.location.equals("about:blank")) {
						return;
					}
					IPreferenceStore store = AggregatorUIPlugin.getDefault()
							.getPreferenceStore();
					String setting = store
							.getString(PreferenceConstants.P_OPEN_LINK);
					switch (PreferenceConstants.LinkOpen.valueOf(setting)) {
					case EDITOR:
						event.doit = false;
						try {
							AggregatorUIPlugin.getBrowser().openURL(
									new URL(event.location));
						} catch (PartInitException e1) {
							e1.printStackTrace();
						} catch (MalformedURLException e1) {
							System.err.println("Bad URL :" + event.location);
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

	private class PlayMediaAction extends Action {

		/**
		 * @param mediaIndex
		 */
		public PlayMediaAction(int mediaIndex) {
			super();
			this.mediaIndex = mediaIndex;
		}

		int mediaIndex = 0;

		@Override
		public void run() {
			if (selectedArticle != null) {
				viewContent(selectedArticle, true, mediaIndex);
			}
		}
	}

	private class PlayMediaMenuAction extends Action implements IMenuCreator {
		/**
		 * @param text
		 * @param style
		 */
		public PlayMediaMenuAction(String text, int style) {
			super(text, style);
			setMenuCreator(this);
		}

		@Override
		public void run() {
			if (selectedArticle != null) {
				viewContent(selectedArticle, true, 0);
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
				int count = 0;
				for (MediaContent mediaContent : media) {
					PlayMediaAction action = new PlayMediaAction(count++);
					ContentHandler handler = AggregatorUIPlugin.getDefault()
							.getContentHandler(mediaContent.getContentType(),
									mediaContent.getContentURL());
					if (handler != null) {
						action.setText(handler
								.getFormattedContentName(mediaContent));
					} else {
						action.setText(Messages.ArticleViewer_UnhandledContent);
					}
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
		playMediaAction = new PlayMediaMenuAction(
				Messages.ArticleViewer_PlayMediaActionLabel,
				IAction.AS_DROP_DOWN_MENU);
		playMediaAction.setImageDescriptor(AggregatorUIPlugin.getDefault()
				.getImageRegistry().getDescriptor(
						AggregatorUIPlugin.IMG_PLAY_MEDIA));

		setStarredAction = new Action() {
			@Override
			public void run() {
				selectedArticle.setStarred(!selectedArticle.isStarred());
				final String key = selectedArticle.isStarred() ? AggregatorUIPlugin.IMG_STARRED
						: AggregatorUIPlugin.IMG_UNSTARRED;
				setImageDescriptor(AggregatorUIPlugin.getDefault()
						.getImageRegistry().getDescriptor(key));
				// Make sure everyone knows about the change

				try {
					selectedArticle.getCollection().writeBack(selectedArticle);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		};
		setStarredAction.setImageDescriptor(AggregatorUIPlugin.getDefault()
				.getImageRegistry().getDescriptor(
						AggregatorUIPlugin.IMG_UNSTARRED));

		playMediaItem = new ActionContributionItem(playMediaAction);
		playMediaItem.setVisible(false);
		title.getToolBarManager().add(playMediaItem);
		title.getToolBarManager().add(setStarredAction);
		title.getToolBarManager().update(false);

	}

	private BrowserStatusTextListener fBrowserListener;

	private class BrowserStatusTextListener implements StatusTextListener {
		public void changed(StatusTextEvent event) {
			String text = event.text;
			// BAD HACK
			if (text.equals("Done")) {
				text = "";
			}
			for (Object listener : listeners.getListeners()) {
				((IArticleViewerListener) listener).statusTextChanged(text);
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
		if (item instanceof Article) {
			selectedArticle = (Article) item;

			title.setItem(selectedArticle);
			viewContent(selectedArticle, false, 0);
			if (selectedArticle.hasMedia()) {
				MediaContent content = selectedArticle.getMediaContent()[0];
				ContentHandler handler = AggregatorUIPlugin.getDefault()
						.getContentHandler(content.getContentType(),
								content.getContentURL());
				if (handler != null) {
					playMediaAction.setToolTipText(MessageFormat.format(
							Messages.ArticleViewer_PlayActionTitle,
							new Object[] { handler
									.getFormattedContentName(content) }));
				} else {
					playMediaAction
							.setText(Messages.ArticleViewer_UnhandledContent);
				}
			}
			playMediaItem.setVisible(selectedArticle.hasMedia());
			final String key = selectedArticle.isStarred() ? AggregatorUIPlugin.IMG_STARRED
					: AggregatorUIPlugin.IMG_UNSTARRED;
			setStarredAction.setImageDescriptor(AggregatorUIPlugin.getDefault()
					.getImageRegistry().getDescriptor(key));
			title.getToolBarManager().update(true);
		}
	}

	private void viewContent(Article item, boolean showMedia, int mediaIndex) {
		fInterceptBrowser = false;
		String text = null;
		try {
			if (showMedia) {
				if (item.getMediaContent().length >= mediaIndex) {
					MediaContent media = item.getMediaContent()[mediaIndex];
					text = getContentHandlerHTML(media.getContentType(), media
							.getContentURL(), media.getContentURL());
				}
			} else {
				text = getContentHandlerHTML(DEFAULT_CONTENT_TYPE, null, item
						.getText());
			}
			browser.setText(text);
			fInterceptBrowser = true;
		} catch (CoreException e) {
			StatusManager.getManager().handle(e.getStatus());
		}
	}

	final IExtensionRegistry ereg = Platform.getExtensionRegistry();

	HashMap<String, String> contentProperties;
	private Action setStarredAction;

	private String getContentHandlerHTML(String contentType, String url,
			String content) throws CoreException {
		String code = MessageFormat.format(
				Messages.ArticleViewer_NoContentHandler,
				new Object[] { contentType });
		ContentHandler handler = AggregatorUIPlugin.getDefault()
				.getContentHandler(contentType, url);
		if (handler == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					AggregatorUIPlugin.PLUGIN_ID,
					"No handler for content type " + contentType));
		}
		contentProperties.put(CONTENT_PROPERTY, content); //$NON-NLS-1$
		code = handler.getEmbedCode(contentProperties);
		return code;
	}

	/**
	 * Updates the content property set with values from preference settings.
	 */
	private void updateFromPreferences() {
		if (!JFaceResources.getFontRegistry().hasValueFor(
				"no.resheim.aggregator.core.ui.articleTextFont")) {
			FontData[] fontData = JFaceResources.getHeaderFont().getFontData();
			fontData[0].setHeight(10);
			JFaceResources.getFontRegistry().put(
					"no.resheim.aggregator.core.ui.articleTextFont", fontData);
		}
		FontData[] fd = JFaceResources.getFont(
				"no.resheim.aggregator.core.ui.articleTextFont").getFontData();

		contentProperties.put("font-family", fd[0].getName()); //$NON-NLS-1$
		contentProperties.put("font-size", String.valueOf(fd[0].getHeight())); //$NON-NLS-1$
		show(selectedArticle);

	}

	public void propertyChange(PropertyChangeEvent event) {
		updateFromPreferences();
	}
}
