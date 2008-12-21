/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
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
 * Type to describe a filter action.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Action {

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	/** Operation to perform */
	public enum Operation {
		/** Mark as something */
		MARK,
		/** Move to a specific folder */
		MOVE,
		/** Move to trash */
		TRASH,
		/** Delete directly */
		DELETE,
	}

	private Action(Action action) {
		this.operation = action.operation;
		this.operator = action.operator;
	}

	/** The operation to perform */
	protected Operation operation;

	/** Folder to move to or mark to apply */
	protected String operator;

	public Action getWorkingCopy() {
		return new Action(this);
	}

}
