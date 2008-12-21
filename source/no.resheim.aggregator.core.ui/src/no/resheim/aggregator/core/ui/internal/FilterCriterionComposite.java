package no.resheim.aggregator.core.ui.internal;

import no.resheim.aggregator.core.filter.Criterion;
import no.resheim.aggregator.core.filter.Criterion.Field;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public class FilterCriterionComposite extends Composite {

	private Binding field;
	private DataBindingContext m_bindingContext;
	private Combo combo;
	private Criterion fCriterion;

	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 */
	public FilterCriterionComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout());

		combo = new Combo(this, SWT.NONE);
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		initValues();
		m_bindingContext = initDataBindings();
	}

	private void initValues() {
		fCriterion = new Criterion();
		for (Field field : Field.values()) {
			combo.add(field.toString());
			System.out.println(field.toString());
		}
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	protected DataBindingContext initDataBindings() {
		IObservableValue comboTextObserveWidget = SWTObservables
				.observeText(combo);
		IObservableValue fCriterionFieldObserveValue = BeansObservables
				.observeValue(fCriterion, "field");
		//
		//
		DataBindingContext bindingContext = new DataBindingContext();
		//
		field = bindingContext.bindValue(comboTextObserveWidget,
				fCriterionFieldObserveValue, null, null);
		//
		return bindingContext;
	}

}
