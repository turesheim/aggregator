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

import java.util.ArrayList;
import java.util.UUID;

import no.resheim.aggregator.core.data.Folder;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * A filter contains associated folders, criteria and actions to perform. The filter only applies to the associated folders.
 * @author   Torkild Ulvøy Resheim
 * @since   1.0
 */
public class Filter {

	/**
	 * Returns <code>true</code> if the filter shall only be manually executed. The default is <code>false</code>.
	 * @return   <code>true</code> if the filter should only be executed manually
	 * @uml.property  name="manualOnly"
	 */
	public boolean isManualOnly() {
		return manualOnly;
	}

	/**
	 * Specifies if the filter should only be applied when executed manually.
	 * @param manualOnly   whether the filter shall only be executed manually
	 * @uml.property  name="manualOnly"
	 */
	public void setManualOnly(boolean manualOnly) {
		this.manualOnly = manualOnly;
	}

	/** Actions to perform */
	protected ArrayList<Action> actions;

	/**
	 * Criteria to trigger the action
	 * @uml.property  name="criteria"
	 */
	protected ArrayList<Criterion> criteria;

	/**
	 * Folders this filter applies to
	 * @uml.property  name="folders"
	 */
	protected ArrayList<Folder> folders;

	/**
	 * Match one or all criteria for action
	 * @uml.property  name="matchAllCriteria"
	 */
	private boolean matchAllCriteria;

	/**
	 * Whether or not to only apply manually
	 * @uml.property  name="manualOnly"
	 */
	private boolean manualOnly;

	/**
	 * The title of the filter
	 * @uml.property  name="title"
	 */
	private String title;

	/**
	 * The unique identifier for the filter
	 * @uml.property  name="uuid"
	 */
	private UUID uuid;

	/**
	 * Whether or not this instance is a working copy
	 * @uml.property  name="workingCopy"
	 */
	private boolean workingCopy;

	/**
	 * Adds a new criterion to the filter.
	 * 
	 * @param criterion
	 *            the criterion to add
	 */
	public void addCriterion(Criterion criterion) {
		criteria.add(criterion);
	}

	private Filter() {
		folders = new ArrayList<Folder>();
		actions = new ArrayList<Action>();
		criteria = new ArrayList<Criterion>();
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
	 *            the filter to create a copy of
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

	/**
	 * Applies the filter.
	 * 
	 * @return
	 */
	public IStatus execute() {
		return Status.OK_STATUS;
	}

	/**
	 * Returns the folders that the filter applies to. If the list is empty, the filter applies to all folder.
	 * @return   the
	 * @uml.property  name="folders"
	 */
	public Folder[] getFolders() {
		return folders.toArray(new Folder[folders.size()]);
	}

	/**
	 * Returns all the criteria for the filter.
	 * @return
	 * @uml.property  name="criteria"
	 */
	public Criterion[] getCriteria() {
		return criteria.toArray(new Criterion[criteria.size()]);
	}

	/**
	 * @return
	 * @uml.property  name="title"
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return
	 * @uml.property  name="uuid"
	 */
	public UUID getUuid() {
		return uuid;
	}

	public Filter getWorkingCopy() {
		Filter filter = new Filter(this);
		filter.workingCopy = true;
		return filter;
	}

	/**
	 * If all the criteria must match before the filter action(s) can be executed, this method will return <code>true</code>.
	 * @return   <code>true</code> if all criteria must match
	 * @uml.property  name="matchAllCriteria"
	 */
	public boolean isMatchAllCriteria() {
		return matchAllCriteria;
	}

	/**
	 * @return   <code>true</code> if the instance is a working copy
	 * @uml.property  name="workingCopy"
	 */
	public boolean isWorkingCopy() {
		return workingCopy;
	}

	/**
	 * @param  matchAllCriteria
	 * @uml.property  name="matchAllCriteria"
	 */
	public void setMatchAllCriteria(boolean matchAllCriteria) {
		this.matchAllCriteria = matchAllCriteria;
	}

	/**
	 * @param  title
	 * @uml.property  name="title"
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * @param  uuid
	 * @uml.property  name="uuid"
	 */
	public void setUuid(UUID uuid) {
		this.uuid = uuid;
	}
}
