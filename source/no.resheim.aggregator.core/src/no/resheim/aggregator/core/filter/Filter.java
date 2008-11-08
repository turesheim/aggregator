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

import java.util.ArrayList;
import java.util.UUID;

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Folder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Filter {
	/**
	 * @param title
	 * @param uuid
	 */
	public Filter(String title, UUID uuid) {
		super();
		this.title = title;
		this.uuid = uuid;
	}

	/** Folders this filter applies to */
	protected ArrayList<Folder> folders;
	/** Criteria to trigger the action */
	protected ArrayList<Criterion> criteria;
	/** Actions to perform */
	protected ArrayList<Action> actions;
	/** The title of the filter */
	private String title;
	/** The unique identifier for the filter */
	private UUID uuid;

	public String getTitle() {
		return title;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}

	public IStatus execute() {
		return Status.OK_STATUS;
	}

	private boolean match(Article item) {

		return false;
	}
}
