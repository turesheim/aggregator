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
package no.resheim.aggregator.core.ui.internal;

import java.util.UUID;

import no.resheim.aggregator.core.filter.Criterion;
import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FilterPropertiesComposite extends Composite {

	private Button removeButton;
	private Button addButton;
	private Label criteriaLabel;
	private Table table;
	private TableViewer tableViewer;
	private Label titleLabel;
	private Text text;
	private Filter filter;
	private CriteriaContentProvider criteriaContentProvider;

	final class CriterionCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
			System.out.println(property);
			return true;
		}

		private int getIndex(String property) {
			if (property.equals("Field"))
				return 0;
			if (property.equals("Operator"))
				return 1;
			if (property.equals("Value"))
				return 2;
			else
				return -1;
		}

		public Object getValue(Object element, String property) {
			System.out.println(element);
			// Find the index of the column
			int columnIndex = getIndex(property);

			Object result = null;
			Criterion task = (Criterion) element;

			switch (columnIndex) {
			case 0: // COMPLETED_COLUMN
				result = task.getField();
				break;
			case 1: // DESCRIPTION_COLUMN
				result = task.getOperator();
				break;
			case 2: // OWNER_COLUMN
				result = task.getValue();
				break;
			}
			return result;
		}

		public void modify(Object element, String property, Object value) {
			// TODO Auto-generated method stub

		}

	}

	final class CriterionLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			Criterion criterion = (Criterion) element;
			switch (columnIndex) {
			case 0:
				return criterion.getField().toString();
			case 1:
				return criterion.getOperator().toString();
			case 2:
				return criterion.getValue();
			}
			return "";
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

		public void dispose() {
			// TODO Auto-generated method stub

		}

		public boolean isLabelProperty(Object element, String property) {
			System.out.println(property);
			// TODO Auto-generated method stub
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub

		}

	}

	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 */
	public FilterPropertiesComposite(Composite parent, int style) {

		super(parent, style);

		filter = new Filter(UUID.randomUUID(), "New filter");

		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("Title:");

		text = new Text(this, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		new Label(this, SWT.NONE);

		criteriaLabel = new Label(this, SWT.NONE);
		criteriaLabel.setText("Criteria:");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		tableViewer = new TableViewer(this, SWT.BORDER);
		tableViewer.setUseHashlookup(true);
		criteriaContentProvider = new CriteriaContentProvider();
		tableViewer.setContentProvider(criteriaContentProvider);
		tableViewer.setLabelProvider(new CriterionLabelProvider());
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		final GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true,
				2, 2);
		gd_table.heightHint = 309;
		table.setLayoutData(gd_table);
		for (int i = 0; i < 3; i++) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setWidth(100);
		}
		table.getColumn(0).setText("Field");
		table.getColumn(1).setText("Operator");
		table.getColumn(2).setText("Value");
		tableViewer.setInput(filter);
		initTableEditing();

		addButton = new Button(this, SWT.NONE);
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				filter.addCriterion(new Criterion());
				tableViewer.refresh();
			}
		});
		final GridData gd_addButton = new GridData(SWT.FILL, SWT.TOP, false,
				false);
		addButton.setLayoutData(gd_addButton);
		addButton.setText("Add");

		removeButton = new Button(this, SWT.NONE);
		final GridData gd_removeButton = new GridData(SWT.FILL, SWT.TOP, false,
				false);
		removeButton.setLayoutData(gd_removeButton);
		removeButton.setText("Remove");
	}

	private void initTableEditing() {
		CellEditor[] editors = new CellEditor[3];
		editors[0] = new ComboBoxCellEditor(table, new String[] {},
				SWT.READ_ONLY);
		editors[1] = new ComboBoxCellEditor(table, new String[] {},
				SWT.READ_ONLY);
		editors[2] = new TextCellEditor(table);
		tableViewer.setCellEditors(editors);
		tableViewer.setCellModifier(new CriterionCellModifier());
	}

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());
		FilterPropertiesComposite props = new FilterPropertiesComposite(shell,
				SWT.NONE);
		shell.pack();
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
