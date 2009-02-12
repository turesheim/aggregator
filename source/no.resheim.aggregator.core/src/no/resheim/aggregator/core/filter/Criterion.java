/*******************************************************************************
 * Copyright (c) 2008-2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.filter;

/**
 * Criterion for a filter to be applied.
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public class Criterion {
	/**
	 * @author   torkild
	 */
	public enum Field {
		/**
		 * @uml.property  name="aUTHOR"
		 * @uml.associationEnd  
		 */
		AUTHOR,
		/**
		 * @uml.property  name="rEAD"
		 * @uml.associationEnd  
		 */
		READ,
		/**
		 * @uml.property  name="tEXT"
		 * @uml.associationEnd  
		 */
		TEXT,
		/**
		 * @uml.property  name="tITLE"
		 * @uml.associationEnd  
		 */
		TITLE,
		/**
		 * @uml.property  name="tYPE"
		 * @uml.associationEnd  
		 */
		TYPE
	}

	/**
	 * @author   torkild
	 */
	public enum Operator {
		/**
		 * @uml.property  name="cONTAINS"
		 * @uml.associationEnd  
		 */
		CONTAINS,
		/**
		 * @uml.property  name="dOES_NOT_CONTAIN"
		 * @uml.associationEnd  
		 */
		DOES_NOT_CONTAIN,
		/**
		 * @uml.property  name="eQUALS"
		 * @uml.associationEnd  
		 */
		EQUALS,
		/**
		 * @uml.property  name="dOES_NOT_EQUAL"
		 * @uml.associationEnd  
		 */
		DOES_NOT_EQUAL,
		/**
		 * @uml.property  name="mATCHES_REGEXP"
		 * @uml.associationEnd  
		 */
		MATCHES_REGEXP,
		/**
		 * @uml.property  name="dOES_NOT_MATCH_REGEXP"
		 * @uml.associationEnd  
		 */
		DOES_NOT_MATCH_REGEXP
	}

	/**
	 * The field to test
	 * @uml.property  name="field"
	 * @uml.associationEnd  
	 */
	protected Field field;

	/**
	 * The comparison operation
	 * @uml.property  name="operator"
	 * @uml.associationEnd  
	 */
	protected Operator operator;

	/**
	 * The value to verify against
	 * @uml.property  name="value"
	 */
	protected String value;

	public Criterion() {
		field = Field.TITLE;
		operator = Operator.CONTAINS;
		value = ""; //$NON-NLS-1$
	}

	private Criterion(Criterion criterion) {
		this.field = criterion.field;
		this.operator = criterion.operator;
		this.value = criterion.value;
	}

	/**
	 * Returns the field that the comparison applies to.
	 * @return   the field
	 * @uml.property  name="field"
	 */
	public Field getField() {
		return field;
	}

	/**
	 * @return
	 * @uml.property  name="operator"
	 */
	public Operator getOperator() {
		return operator;
	}

	/**
	 * @return
	 * @uml.property  name="value"
	 */
	public String getValue() {
		return value;
	}

	public Criterion getWorkingCopy() {
		return new Criterion(this);
	}

	/**
	 * Sets the field that the comparison applies to.
	 * @param field   the field
	 * @uml.property  name="field"
	 */
	public void setField(Field field) {
		this.field = field;
	}

	/**
	 * @param  operator
	 * @uml.property  name="operator"
	 */
	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	/**
	 * @param  value
	 * @uml.property  name="value"
	 */
	public void setValue(String value) {
		this.value = value;
	}
}
