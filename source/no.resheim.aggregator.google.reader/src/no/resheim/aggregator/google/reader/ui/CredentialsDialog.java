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
package no.resheim.aggregator.google.reader.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Simple dialog for obtaining user name and password to use when logging in to
 * the Google account.
 * 
 * @author Torkild Ulvøy Resheim
 * 
 */
public class CredentialsDialog extends Dialog {

	private String login;
	private String password;

	/**
	 * Create the dialog
	 * 
	 * @param parentShell
	 */
	public CredentialsDialog(Shell parentShell) {
		super(parentShell);
		setBlockOnOpen(true);
	}

	public String getPassword() {
		return password;
	}

	public String getLogin() {
		return login;
	}

	/**
	 * Create contents of the dialog
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		container.setLayout(gridLayout);

		final Label pleaseEnterTheLabel = new Label(container, SWT.WRAP);
		final GridData gd_pleaseEnterTheLabel = new GridData(SWT.LEFT,
				SWT.CENTER, false, false, 2, 1);
		pleaseEnterTheLabel.setLayoutData(gd_pleaseEnterTheLabel);
		pleaseEnterTheLabel
				.setText("Please enter the credentials for logging in to your Google account.\nThis information will be safely stored in an encrypted location.");

		final Label userNameemailLabel = new Label(container, SWT.NONE);
		userNameemailLabel.setText("User name (e-mail):");

		final Text loginText = new Text(container, SWT.BORDER);
		loginText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				login = loginText.getText();
			}
		});
		final GridData gd_login = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		loginText.setLayoutData(gd_login);

		final Label passwordLabel = new Label(container, SWT.NONE);
		passwordLabel.setLayoutData(new GridData());
		passwordLabel.setText("Password:");

		final Text passwordText = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(final ModifyEvent e) {
				password = passwordText.getText();
			}
		});
		final GridData gd_password = new GridData(SWT.FILL, SWT.CENTER, true,
				false);
		passwordText.setLayoutData(gd_password);
		//
		return container;
	}

	/**
	 * Create contents of the button bar
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(346, 175);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Google Log-In");
	}

}
