/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui.internal;

import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.internal.forms.widgets.FormsResources;

/**
 * The title in the aggregator view title. Adapted from a type in the tabbed
 * properties view since this was internal.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
@SuppressWarnings("restriction")
public class FeedItemTitle extends Composite {

	@Override
	public void dispose() {
		super.dispose();
		backgroundImage.dispose();
	}

	private CLabel label;

	private static final String BLANK = ""; //$NON-NLS-1$

	private static final String TITLE_FONT = "org.eclipse.ui.internal.views.properties.tabbed.view.TabbedPropertyTitle"; //$NON-NLS-1$

	private FeedViewWidgetFactory factory;

	/**
	 * Constructor for TabbedPropertyTitle.
	 * 
	 * @param parent
	 *            the parent composite.
	 * @param factory
	 *            the widget factory for the tabbed property sheet
	 */
	public FeedItemTitle(Composite parent, FeedViewWidgetFactory factory) {
		// super(parent, SWT.NO_FOCUS);
		super(parent, SWT.NONE);
		setBackgroundMode(SWT.INHERIT_DEFAULT);

		this.factory = factory;

		factory.getColors().initializeSectionToolBarColors();
		setBackground(factory.getColors().getBackground());
		setForeground(factory.getColors().getForeground());

		GridLayout layout = new GridLayout(2, false);

		// FormLayout layout = new FormLayout();
		// layout.marginWidth = 1;
		// layout.marginHeight = 2;
		setLayout(layout);

		Font font;
		if (!JFaceResources.getFontRegistry().hasValueFor(TITLE_FONT)) {
			FontData[] fontData = JFaceResources.getHeaderFont().getFontData();
			fontData[0].setHeight(10);
			JFaceResources.getFontRegistry().put(TITLE_FONT, fontData);
		}
		font = JFaceResources.getFont(TITLE_FONT);

		label = factory.createCLabel(this, BLANK);
		label.setBackground(new Color[] {
				factory.getColors().getColor(IFormColors.H_GRADIENT_END),
				factory.getColors().getColor(IFormColors.H_GRADIENT_START)
		}, new int[] {
			100
		}, true);
		label.setFont(font);
		label.setForeground(factory.getColors().getColor(IFormColors.TITLE));
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		label.setLayoutData(gd);

		addListener(SWT.Resize, new Listener() {

			public void handleEvent(Event event) {
				createBackground();
				setBackgroundImage(backgroundImage);
			}
		});

	}

	ToolBarManager toolBarManager;
	Image backgroundImage;

	/**
	 * Returns the tool bar manager that is used to manage tool items in the
	 * form's title area.
	 * 
	 * @return form tool bar manager
	 */
	public IToolBarManager getToolBarManager() {
		if (toolBarManager == null) {
			toolBarManager = new ToolBarManager(SWT.FLAT);
			final ToolBar toolbar = toolBarManager.createControl(this);
			toolbar.setBackground(getBackground());
			toolbar.setForeground(getForeground());
			toolbar.setCursor(FormsResources.getHandCursor());
			addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (toolBarManager != null) {
						toolBarManager.dispose();
						toolBarManager = null;
					}
				}
			});
			toolbar.addListener(SWT.Paint, new Listener() {

				public void handleEvent(Event event) {
					toolbar.setBackgroundImage(backgroundImage);
				}

			});
		}
		return toolBarManager;
	}

	private void createBackground() {
		Color bg = factory.getColors().getColor(IFormColors.H_GRADIENT_END);
		Color gbg = factory.getColors().getColor(IFormColors.H_GRADIENT_START);
		Rectangle rect = getClientArea();
		Display display = getDisplay();
		Image newImage = new Image(display, 1, Math.max(1, rect.height));
		Rectangle bounds = newImage.getBounds();
		GC gc = new GC(newImage);
		gc.setForeground(bg);
		gc.setBackground(gbg);
		gc.fillGradientRectangle(bounds.x, bounds.y, bounds.width,
				bounds.height, true);
		// background bottom separator
		gc.setForeground(factory.getColors().getColor(
				IFormColors.H_BOTTOM_KEYLINE1));
		gc.drawLine(bounds.x, bounds.height - 2, bounds.x + bounds.width - 1,
				bounds.height - 2);
		gc.setForeground(factory.getColors().getColor(
				IFormColors.H_BOTTOM_KEYLINE2));
		gc.drawLine(bounds.x, bounds.height - 1, bounds.x + bounds.width - 1,
				bounds.height - 1);
		if (backgroundImage != null) {
			backgroundImage.dispose();
		}
		backgroundImage = newImage;
	}

	/**
	 * Set the text label.
	 * 
	 * @param text
	 *            the text label.
	 * @param image
	 *            the image for the label.
	 */
	public void setTitle(String text, Image image) {
		if (text != null) {
			label.setText(text);
		} else {
			label.setText(BLANK);
		}
		label.setImage(image);
		redraw();
	}
}
