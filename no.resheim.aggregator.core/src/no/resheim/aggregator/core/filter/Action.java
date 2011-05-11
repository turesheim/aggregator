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
 * Action to execute when filter criteria matches.
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public class Action {

	/**
	 * Operation to perform
	 */
	public enum Operation {
		/**
		 * @uml.property  name="dELETE"
		 * @uml.associationEnd  
		 */
		DELETE,
		/**
		 * @uml.property  name="mARK"
		 * @uml.associationEnd  
		 */
		MARK,
		/**
		 * @uml.property  name="mOVE"
		 * @uml.associationEnd  
		 */
		MOVE,
		/**
		 * @uml.property  name="tRASH"
		 * @uml.associationEnd  
		 */
		TRASH,
	}

	/**
	 * The operation to perform
	 * @uml.property  name="operation"
	 * @uml.associationEnd  
	 */
	protected Operation operation;

	/**
	 * Folder to move to or mark to apply
	 * @uml.property  name="operator"
	 */
	protected String operator;

	private Action(Action action) {
		this.operation = action.operation;
		this.operator = action.operator;
	}

	/**
	 * @return
	 * @uml.property  name="operation"
	 */
	public Operation getOperation() {
		return operation;
	}

	/**
	 * @return
	 * @uml.property  name="operator"
	 */
	public String getOperator() {
		return operator;
	}

	/**
	 * Returns a working copy for the action.
	 * 
	 * @return the working copy
	 */
	public Action getWorkingCopy() {
		return new Action(this);
	}

	/**
	 * @param  operation
	 * @uml.property  name="operation"
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	/**
	 * @param  operator
	 * @uml.property  name="operator"
	 */
	public void setOperator(String operator) {
		this.operator = operator;
	}

}
