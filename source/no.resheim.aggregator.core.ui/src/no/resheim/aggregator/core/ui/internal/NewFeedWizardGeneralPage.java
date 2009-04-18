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

import java.util.ArrayList;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.catalog.IFeedCatalog;
import no.resheim.aggregator.core.data.FeedWorkingCopy;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.NewFeedWizard;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;

/**
 * Wizard page for the general settings of a new feed.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class NewFeedWizardGeneralPage extends WizardPage {
	private static final String WIZARD_BANNER = "icons/wizban/new_feed_wizard.png";
	private Text combo;
	private Text urlText;
	private Label urlLabel;
	private Label titleLabel;
	private NewFeedWizard wizard;
	private Label userLabel;
	private Text userText;
	private Label passwordLabel;
	private Text passwordText;

	/**
	 * Create the wizard
	 */
	public NewFeedWizardGeneralPage(NewFeedWizard wizard) {
		super(Messages.NewFeedWizardGeneralPage_Title);
		setTitle(Messages.NewFeedWizardGeneralPage_Title);
		setDescription(Messages.NewFeedWizardGeneralPage_Description);
		setImageDescriptor(AggregatorUIPlugin.getImageDescriptor(WIZARD_BANNER)); //$NON-NLS-1$
		this.wizard = wizard;
		// The wizard page starts out as incomplete
		setPageComplete(false);
	}

	ArrayList<Subscription> defaults = new ArrayList<Subscription>();

	/**
	 * Create contents of the wizard
	 * 
	 * @param parent
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		final GridLayout gridLayout = new GridLayout();
		container.setLayout(gridLayout);
		setControl(container);

		final FeedWorkingCopy workingCopy = wizard.getWorkingCopy();

		final SashForm sashForm = new SashForm(container, SWT.NONE);
		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final TreeViewer treeViewer = new TreeViewer(sashForm, SWT.BORDER);
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(final SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (selection instanceof IStructuredSelection) {
					Object selected = ((IStructuredSelection) selection)
							.getFirstElement();
					if (selected instanceof Subscription) {
						workingCopy.copy((Subscription) selected);
						urlText.setText(workingCopy.getURL());
						combo.setText(workingCopy.getTitle());
					}
				}
			}
		});
		final Tree tree = treeViewer.getTree();
		final GridData gd_tree = new GridData(SWT.FILL, SWT.FILL, false, false,
				1, 5);
		treeViewer.setContentProvider(new ITreeContentProvider() {

			private IFeedCatalog[] catalogs;

			public Object[] getElements(Object inputElement) {
				return catalogs;
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput,
					Object newInput) {
				catalogs = AggregatorPlugin.getDefault().getCatalogs();
			}

			public Object[] getChildren(Object parentElement) {
				if (parentElement instanceof IFeedCatalog) {
					return ((IFeedCatalog) parentElement).getFeeds();
				}
				if (parentElement instanceof IFeedCatalog[]) {
					return catalogs;
				}
				return new Object[0];
			}

			public Object getParent(Object element) {
				return null;
			}

			public boolean hasChildren(Object element) {
				if (element instanceof Subscription) {
					return false;
				}
				return true;
			}
		});
		treeViewer.setInput(this);
		treeViewer.setLabelProvider(new LabelProvider() {

			@Override
			public String getText(Object element) {
				if (element instanceof IFeedCatalog) {
					return ((IFeedCatalog) element).getName();
				}
				if (element instanceof Subscription) {
					return ((Subscription) element).getTitle();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof IFeedCatalog) {
					IFeedCatalog catalog = (IFeedCatalog) element;
					ImageRegistry registry = AggregatorUIPlugin.getDefault()
							.getImageRegistry();
					String id = "catalog." + catalog.getId() + "."
							+ catalog.getIcon();
					if (registry.get(id) == null) {
						ImageDescriptor img = ImageDescriptor
								.createFromURL(catalog.getIcon());
						registry.put(id, img);
					}
					return registry.get(id);
				}
				if (element instanceof Subscription) {
					ImageRegistry registry = AggregatorUIPlugin.getDefault()
							.getImageRegistry();
					return registry.get(AggregatorUIPlugin.IMG_FEED_OBJ);
				}
				return null;
			}

		});
		gd_tree.heightHint = 50;
		tree.setLayoutData(gd_tree);

		final Group group = new Group(sashForm, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		group.setLayout(gridLayout_1);

		titleLabel = new Label(group, SWT.NONE);
		titleLabel.setText(Messages.NewFeedWizardGeneralPage_Label_Title);

		combo = new Text(group, SWT.BORDER);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setTitle(combo.getText());
				validate();
			}
		});

		urlLabel = new Label(group, SWT.NONE);
		urlLabel.setText(Messages.NewFeedWizardGeneralPage_Label_URL);

		urlText = new Text(group, SWT.BORDER);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		urlText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setURL(urlText.getText());
				validate();
			}
		});

		final Button button = new Button(group, SWT.CHECK);
		button.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2,
				1));
		button.setText(Messages.NewFeedWizardGeneralPage_Anonymous);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean state = !(button.getSelection());
				workingCopy.setAnonymousAccess(button.getSelection());
				updateCredentialsFields(state);
			}

		});
		button.setSelection(true);

		final Group group_1 = new Group(group, SWT.NONE);
		group_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		final GridLayout gridLayout_2 = new GridLayout();
		gridLayout_2.numColumns = 2;
		group_1.setLayout(gridLayout_2);

		userLabel = new Label(group_1, SWT.NONE);
		userLabel.setText(Messages.NewFeedWizardGeneralPage_Login);
		userText = new Text(group_1, SWT.BORDER);
		userText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		userText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setUsername(userText.getText());
				validate();
			}
		});
		passwordLabel = new Label(group_1, SWT.NONE);
		passwordLabel.setText(Messages.NewFeedWizardGeneralPage_Password);
		passwordText = new Text(group_1, SWT.BORDER | SWT.PASSWORD);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
				false));
		passwordText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				workingCopy.setPassword(passwordText.getText());
				validate();
			}
		});
		sashForm.setWeights(new int[] { 1, 1 });
		updateCredentialsFields(false);
	}

	private void updateCredentialsFields(boolean state) {
		userLabel.setEnabled(state);
		userText.setEnabled(state);
		passwordLabel.setEnabled(state);
		passwordText.setEnabled(state);
	}

	private void validate() {
		FeedWorkingCopy workingCopy = wizard.getWorkingCopy();

		if (workingCopy.getTitle().length() == 0) {
			setMessage(Messages.NewFeedWizardGeneralPage_Error_Missing_title);
			setPageComplete(false);
			return;
		}
		if (workingCopy.getURL().length() == 0) {
			setMessage(Messages.NewFeedWizardGeneralPage_Error_Missing_URL);
			setPageComplete(false);
			return;
		}
		// Everything is in order.
		setMessage(null);
		setErrorMessage(null);
		setPageComplete(true);
	}
}
