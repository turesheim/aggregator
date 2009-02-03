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
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Criterion {
	public enum Field {
		/** The author of the article */
		AUTHOR,
		/** Whether or not the article has been read */
		READ,
		/** The text of the article */
		TEXT,
		/** The title of the article */
		TITLE,
		/** The enclosure type of the article */
		TYPE
	}

	public enum Operator {
		CONTAINS,
		DOES_NOT_CONTAIN,
		DOES_NOT_EQUAL,
		DOES_NOT_MATCH_REGEXP,
		EQUALS,
		MATCHES_REGEXP
	}

	public static String[] getFieldNames() {
		return new String[] {
				Messages.Criterion_Field_Title, Messages.Criterion_Field_Text,
				Messages.Criterion_Field_Author, Messages.Criterion_Field_Type,
				Messages.Criterion_Field_Read
		};
	}

	public static String[] getOperatorNames() {
		return new String[] {
				Messages.Criterion_Op_Contains,
				Messages.Criterion_Op_Does_Not_Contain,
				Messages.Criterion_Op_Equals,
				Messages.Criterion_Op_Does_Not_Equal,
				Messages.Criterion_Op_Matches_Regexp,
				Messages.Criterion_Op_Does_Not_Match_Regexp
		};
	}

	/** The field to test */
	protected Field field;

	/** The comparison operation */
	protected Operator operator;

	/** The value to verify against */
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
	 * 
	 * @return the field
	 */
	public Field getField() {
		return field;
	}

	public Operator getOperator() {
		return operator;
	}

	public String getValue() {
		return value;
	}

	public Criterion getWorkingCopy() {
		return new Criterion(this);
	}

	/**
	 * Sets the field that the comparison applies to.
	 * 
	 * @param field
	 *            the field
	 */
	public void setField(Field field) {
		this.field = field;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
