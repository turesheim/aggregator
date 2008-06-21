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

import java.text.MessageFormat;
import java.util.Date;

import no.resheim.aggregator.core.ui.internal.FeedDescriptionFormatter;
import no.resheim.aggregator.core.ui.internal.FeedItemTitle;
import no.resheim.aggregator.core.ui.internal.FeedViewWidgetFactory;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Type to render articles. A {@link Browser} instance will be used to render
 * the HTML.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ArticleViewer extends Composite implements IPropertyChangeListener {
	private FeedItemTitle title;
	private Browser browser;
	/** Preference: Name of the font used in the details pane */
	private String pPresentationFontFamily;

	/** Preference: The size of the font used in the details pane (pixels) */
	private int pPresentationFontSize;

	// HTML/CSS code for specifying the description font when using the
	// integrated web browser.
	private static final String FONT_FIX_5 = "</div>"; //$NON-NLS-1$
	private static final String FONT_FIX_4 = "pt\">"; //$NON-NLS-1$
	private static final String FONT_FIX_3 = "';font-size: "; //$NON-NLS-1$
	private static final String FONT_FIX_2 = " style=\"font-family: '"; //$NON-NLS-1$
	private static final String FONT_FIX_1 = "<div"; //$NON-NLS-1$

	@Override
	public void dispose() {
		super.dispose();
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.removePropertyChangeListener(this);
	}

	/**
	 * @param parent
	 * @param style
	 */
	public ArticleViewer(Composite parent, int style) {
		super(parent, style);
		final FeedViewWidgetFactory factory = new FeedViewWidgetFactory();
		// Special widget for item title
		title = new FeedItemTitle(this, factory);
		title.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING,
				true, false));

		browser = new Browser(this, SWT.NONE);
		browser.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
				true));
		setLayout();
		updateFromPreferences();
		AggregatorUIPlugin.getDefault().getPreferenceStore()
				.addPropertyChangeListener(this);
	}

	private void setLayout() {
		GridLayout layout = new GridLayout(1, true);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		setLayout(layout);
	}

	public void show(IAggregatorItem item) {
		if (item instanceof Article) {
			showDescription((Article) item);
		} else if (item instanceof Feed) {
			showDescription((Feed) item);
		}
	}

	/**
	 * Shows the description of the selected item
	 * 
	 * @param item
	 */
	private void showDescription(Article item) {
		if (item == null)
			return;
		StringBuffer description = new StringBuffer();
		description.append(FONT_FIX_1);
		description.append(FONT_FIX_2);
		description.append(pPresentationFontFamily);
		description.append(FONT_FIX_3);
		description.append(pPresentationFontSize);
		description.append(FONT_FIX_4);
		description.append(item.getCollection().getDescription(item));
		description.append(FONT_FIX_5);
		title.setTitle(item.getTitle(), null);
		browser.setText(description.toString());
		if (AggregatorUIPlugin.getDefault().isDebugging()) {
			System.out.println(item);
		}
		if (item.getPublicationDate() > 0) {
			setStatusText(MessageFormat.format(
					Messages.ArticleViewer_Published, new Object[] {
							new Date(item.getPublicationDate()),
							item.getCreator()
					}));
		} else {
			setStatusText(MessageFormat.format(
					Messages.ArticleViewer_UnknownPublicationDate,
					new Object[] {
						new Date(item.getAdded())
					}));
		}
	}

	private void showDescription(Feed feed) {
		if (feed == null)
			return;
		browser.setText(FeedDescriptionFormatter.format(feed,
				pPresentationFontFamily, pPresentationFontSize));
		title.setTitle(feed.getTitle(), null);
		setStatusText(MessageFormat.format(Messages.ArticleViewer_Updated,
				new Object[] {
						feed.getTitle(), new Date(feed.getLastUpdate()),
						new Date(feed.getLastUpdate() + feed.getUpdateTime())
				}));
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

	private void setStatusText(String text) {
		// IStatusLineManager mgr = getViewSite().getActionBars()
		// .getStatusLineManager();
		// if (mgr != null) {
		// mgr.setMessage(text);
		// }

	}

}
