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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FilterPropertiesComposite extends Composite {

	private Table filtersTable;
	private TableViewer tableViewer_1;
	private Composite composite;
	private TabItem generalTabItem;
	private TabFolder tabFolder;
	private TableColumn newColumnTableColumn_2;
	private TableColumn newColumnTableColumn_1;
	private TableColumn newColumnTableColumn;
	private Button removeButton;
	private Button addButton;
	private Label criteriaLabel;
	private Table table;
	private TableViewer tableViewer;
	private Filter filter;
	private CriteriaContentProvider criteriaContentProvider;

	final class CriterionCellModifier implements ICellModifier {

		public boolean canModify(Object element, String property) {
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
				return 2;
		}

		public Object getValue(Object element, String property) {
			// Find the index of the column
			int columnIndex = getIndex(property);

			Object result = null;
			Criterion task = (Criterion) element;

			switch (columnIndex) {
			case 0: // COMPLETED_COLUMN
				result = new Integer(task.getField().ordinal());
				break;
			case 1: // DESCRIPTION_COLUMN
				result = new Integer(task.getOperator().ordinal());
				break;
			case 2: // OWNER_COLUMN
				result = task.getValue();
				break;
			}
			System.out.println(result);
			return result;
		}

		public void modify(Object element, String property, Object value) {
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
		gridLayout.numColumns = 2;
		setLayout(gridLayout);
		criteriaContentProvider = new CriteriaContentProvider();
		initTableEditing();

		tableViewer_1 = new TableViewer(this, SWT.BORDER);
		tableViewer_1.setContentProvider(new FilterContentProvider());
		filtersTable = tableViewer_1.getTable();
		filtersTable.setLinesVisible(true);
		filtersTable.setHeaderVisible(true);
		final GridData gd_filtersTable = new GridData(SWT.FILL, SWT.FILL, true,
				true);
		filtersTable.setLayoutData(gd_filtersTable);

		tabFolder = new TabFolder(this, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		generalTabItem = new TabItem(tabFolder, SWT.NONE);
		generalTabItem.setText("General");

		composite = new Composite(tabFolder, SWT.NONE);
		final GridLayout gridLayout_1 = new GridLayout();
		gridLayout_1.numColumns = 2;
		composite.setLayout(gridLayout_1);
		generalTabItem.setControl(composite);

		criteriaLabel = new Label(composite, SWT.NONE);
		criteriaLabel.setLayoutData(new GridData());
		criteriaLabel.setText("Criteria:");
		new Label(composite, SWT.NONE);

		tableViewer = new TableViewer(composite, SWT.BORDER);
		tableViewer.setUseHashlookup(true);
		tableViewer.setContentProvider(criteriaContentProvider);
		tableViewer.setLabelProvider(new CriterionLabelProvider());
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		final GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true,
				1, 2);
		gd_table.heightHint = 200;
		table.setLayoutData(gd_table);

		newColumnTableColumn = new TableColumn(table, SWT.NONE);
		newColumnTableColumn.setWidth(100);
		newColumnTableColumn.setText("Field");

		newColumnTableColumn_1 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_1.setWidth(100);
		newColumnTableColumn_1.setText("Operator");

		newColumnTableColumn_2 = new TableColumn(table, SWT.NONE);
		newColumnTableColumn_2.setWidth(100);
		newColumnTableColumn_2.setText("Value");

		tableViewer.setColumnProperties(new String[] {
				"Field", "Operator", "Value"
		});
		tableViewer.setInput(filter);

		addButton = new Button(composite, SWT.NONE);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				filter.addCriterion(new Criterion());
				tableViewer.refresh();
			}
		});
		addButton.setText("Add");

		removeButton = new Button(composite, SWT.NONE);
		removeButton
				.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		removeButton.setText("Remove");
	}

	private void initTableEditing() {
		CellEditor[] editors = new CellEditor[3];
		String[] fieldNames = new String[] {
				"Author", "Read", "Text", "Title", "Type"
		};
		String[] operatorNames = new String[] {
				"Contains", "Does not contain", "Equals", "Does not equal",
				"Matches regexp", "Does not match regexp"
		};
		editors[0] = new ComboBoxCellEditor(table, fieldNames, SWT.NONE);
		editors[1] = new ComboBoxCellEditor(table, operatorNames, SWT.READ_ONLY);
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
