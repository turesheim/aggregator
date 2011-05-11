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
package no.resheim.aggregator.core.ui.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A FormToolkit customised for use by the aggregator view. Adapted from a
 * similar type for the tabbed properties view.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedViewWidgetFactory extends FormToolkit {

	/**
	 * These horizontal margin around the composite. Each section should use a
	 * margin of 0, 0.
	 */
	public static final int HMARGIN = 6;

	/**
	 * These horizontal margin around the composite.
	 */
	public static final int VMARGIN = 6;

	/**
	 * Horizontal space to leave between related widgets. Each section should
	 * use these values for spacing its widgets. For example, you can use +/-
	 * HSPACE as the offset of a left or right FlatFormAttachment.
	 * 
	 * The tabbed property composite also inserts VSPACE pixels between section
	 * composites if more than one section is displayed.
	 */
	public static final int HSPACE = 5;

	/**
	 * Horizontal space to leave between related widgets.
	 */
	public static final int VSPACE = 4;

	/**
	 * Space to leave between the centre of the property tab and the closest
	 * widget to the left or right. I.e. for a property tab whose widgets are
	 * logically divided into two halves, the total space between the halves
	 * should be 2*CENTER_SPACE.
	 */
	public static final int CENTER_SPACE = 10;

	/**
	 * private constructor.
	 */
	public FeedViewWidgetFactory() {
		super(Display.getCurrent());
	}

	/**
	 * Creates a label as a part of the form.
	 * 
	 * @param parent
	 *            the label parent.
	 * @param text
	 *            the label text.
	 * @return the label.
	 */
	public CLabel createCLabel(Composite parent, String text) {
		return createCLabel(parent, text, SWT.NONE);
	}

	/**
	 * Creates a label as a part of the form.
	 * 
	 * @param parent
	 *            the label parent.
	 * @param text
	 *            the label text.
	 * @param style
	 *            the label style.
	 * @return the label.
	 */
	public CLabel createCLabel(Composite parent, String text, int style) {
		final CLabel label = new CLabel(parent, style);
		label.setBackground(parent.getBackground());
		label.setText(text);
		return label;
	}

	public void dispose() {
		if (getColors() != null) {
			super.dispose();
		}
	}
}
