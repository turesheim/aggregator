/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui.views;

import java.text.MessageFormat;
import java.util.HashMap;

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.ui.CollectionViewerLabelProvider;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.Messages;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.mylyn.internal.provisional.commons.ui.AbstractNotificationPopup;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class NotificationPopup extends AbstractNotificationPopup {

	private static final int MAX_ITEMS = 5;
	int count;

	CollectionViewerLabelProvider labelProvider;
	private IFeedView fView;
	private HashMap<String, AggregatorItem> map;

	public NotificationPopup(IFeedView view, AggregatorItem[] items) {
		super(view.getFeedViewer().getControl().getDisplay());
		labelProvider = new CollectionViewerLabelProvider();
		setDelayClose(5000);
		setBlockOnOpen(true);
		setFadingEnabled(true);
		fView = view;
		open(items);
	}

	private void createArticleLabel(Composite parent, Article item) {
		if (count < MAX_ITEMS) {
			Composite c = new Composite(parent, SWT.NONE);
			c.setBackground(parent.getBackground());
			GridLayout gl = new GridLayout(2, false);
			gl.marginHeight = 0;
			c.setLayout(gl);
			Label image = new Label(c, SWT.NONE);
			image.setBackground(parent.getBackground());
			image.setImage(labelProvider.getImage(item.getFeed()));
			image.setSize(16, 16);
			Link link = new Link(c, SWT.NONE);
			link.setText(MessageFormat.format(Messages.NotificationPopup_Link,
					new Object[] {
							((Article) item).getTitle(),
							((Article) item).getUUID()
					}));
			link.setBackground(parent.getBackground());
			link.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					final AggregatorItem item = map.get(event.text);
					final ISelection selection = new StructuredSelection(item);
					Display.getCurrent().asyncExec(new Runnable() {
						public void run() {
							fView.getFeedViewer().setSelection(selection, true);
						}
					});
				}
			});
		}
		count++;
	}

	@Override
	protected void createContentArea(Composite parent) {
		for (AggregatorItem item : map.values()) {
			if (item instanceof Article) {
				createArticleLabel(parent, (Article) item);
			}
		}
		if (count > MAX_ITEMS) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(MessageFormat.format(Messages.NotificationPopup_More,
					new Object[] {
						new Integer(count - MAX_ITEMS)
					}));
			label.setBackground(parent.getBackground());
		}
	}

	private void open(AggregatorItem[] items) {
		map = new HashMap<String, AggregatorItem>();
		for (AggregatorItem item : items) {
			map.put(item.getUUID().toString(), item);
		}
		open();
	}
}
