package no.resheim.aggregator.core.ui.internal;

import java.util.UUID;

import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

public class FilterPropertiesComposite extends Composite {

	private Table table;
	private TableViewer tableViewer;
	private DataBindingContext m_bindingContext;
	private Binding title;
	private Label titleLabel;
	private Text text;
	private Filter filter;
	private CriteriaContentProvider criteriaContentProvider;

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

		titleLabel = new Label(this, SWT.NONE);
		titleLabel.setText("Title:");

		text = new Text(this, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		m_bindingContext = initDataBindings();
		new Label(this, SWT.NONE);

		criteriaContentProvider = new CriteriaContentProvider();

		tableViewer = new TableViewer(this, SWT.BORDER);
		table = tableViewer.getTable();
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableViewer.setContentProvider(criteriaContentProvider);
		tableViewer.setInput(filter);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected DataBindingContext initDataBindings() {
		IObservableValue textTextObserveWidget = SWTObservables.observeText(
				text, SWT.Modify);
		IObservableValue filterTitleObserveValue = BeansObservables
				.observeValue(filter, "title");
		//
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		title = bindingContext.bindValue(textTextObserveWidget,
				filterTitleObserveValue, null, null);
		//
		return bindingContext;
	}

}
