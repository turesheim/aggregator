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

import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.ui.internal.FilterPropertiesComposite;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * This properties dialog allows the user to change and apply feed properties.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FilterPropertiesDialog extends MessageDialog {

	FeedCollection fCollection;

	public FilterPropertiesDialog(Shell parentShell, String dialogTitle,
			Image dialogTitleImage, String dialogMessage, int dialogImageType,
			String[] dialogButtonLabels, int defaultIndex,
			FeedCollection collection) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
		fCollection = collection;
	}

	@Override
	protected Control createCustomArea(Composite parent) {
		return new FilterPropertiesComposite(parent, SWT.NONE, fCollection);
	}

}
