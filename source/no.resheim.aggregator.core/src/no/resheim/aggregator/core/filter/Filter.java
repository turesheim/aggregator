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

import no.resheim.aggregator.core.data.Folder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A filter contains associated folders, criteria and actions to perform. The
 * filter only applies to the associated folders.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class Filter {

	/** Actions to perform */
	protected ArrayList<Action> actions;

	/** Criteria to trigger the action */
	protected ArrayList<Criterion> criteria;

	/** Folders this filter applies to */
	protected ArrayList<Folder> folders;

	/** Match one or all criteria for action */
	private boolean matchAllCriteria;

	/** The title of the filter */
	private String title;

	/** The unique identifier for the filter */
	private UUID uuid;

	/** Whether or not this instance is a working copy */
	private boolean workingCopy;

	private Filter() {
		folders = new ArrayList<Folder>();
		actions = new ArrayList<Action>();
		criteria = new ArrayList<Criterion>();
		criteria.add(new Criterion());
	}

	public Filter(UUID uuid, String title) {
		this();
		this.title = title;
		this.uuid = uuid;
	}

	/**
	 * Creates a new filter instance using another filter as the source. This
	 * constructor is only used when creating a new working copy of a filter.
	 * 
	 * @param filter
	 */
	private Filter(Filter filter) {
		this();
		this.title = filter.title;
		this.uuid = filter.uuid;
		// Copy the folders list
		for (Folder folder : filter.folders) {
			folders.add(folder);
		}
		// Copy the actions list. This uses a working copy of the action as it
		// may be modified.
		for (Action action : filter.actions) {
			actions.add(action.getWorkingCopy());
		}

	}

	/**
	 * @param title
	 * @param uuid
	 */
	public Filter(String title, UUID uuid) {
		this();
		this.title = title;
		this.uuid = uuid;
	}

	public IStatus execute() {
		return Status.OK_STATUS;
	}

	public Folder[] getFolders() {
		return folders.toArray(new Folder[folders.size()]);
	}

	public Criterion[] getCriteria() {
		return criteria.toArray(new Criterion[criteria.size()]);
	}

	public String getTitle() {
		return title;
	}

	public UUID getUuid() {
		return uuid;
	}

	public Filter getWorkingCopy() {
		Filter filter = new Filter(this);
		filter.workingCopy = true;
		return filter;
	}

	/**
	 * If all the criteria must match before the filter action(s) can be
	 * executed, this method will return <code>true</code>.
	 * 
	 * @return <code>true</code> if all criteria must match
	 */
	public boolean isMatchAllCriteria() {
		return matchAllCriteria;
	}

	/**
	 * 
	 * @return <code>true</code> if the instance is a working copy
	 */
	public boolean isWorkingCopy() {
		return workingCopy;
	}

	public void setMatchAllCriteria(boolean matchAllCriteria) {
		this.matchAllCriteria = matchAllCriteria;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
