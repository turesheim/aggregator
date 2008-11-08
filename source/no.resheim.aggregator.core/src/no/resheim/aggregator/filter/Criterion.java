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
package no.resheim.aggregator.filter;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Criterion {

	public enum Field {
		/** The title of the article */
		TITLE,
		/** The text of the article */
		TEXT,
		/** The author of the article */
		AUTHOR,
		/** The enclosure type of the article */
		TYPE,
		/** Whether or not the article has been read */
		READ
	}

	public enum Operator {
		CONTAINS,
		DOES_NOT_CONTAIN,
		EQUALS,
		DOES_NOT_EQUAL,
		MATCHES_REGEXP,
		DOES_NOT_MATCH_REGEXP
	}

	/** The field to test */
	protected Field field;

	/** The comparison operation */
	protected Operator operator;

	/** The value to verify against */
	protected String value;
}
