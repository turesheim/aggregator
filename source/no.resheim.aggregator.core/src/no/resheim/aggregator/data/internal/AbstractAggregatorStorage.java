/*******************************************************************************
 * Copyright (c) 2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.data.internal;

import java.util.EnumSet;

import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorStorage;
import no.resheim.aggregator.data.AggregatorUIItem.Mark;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Implementation of {@link IAggregatorStorage} with some common features.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractAggregatorStorage implements IAggregatorStorage {

	/** The feed collection that data is being handled for */
	protected FeedCollection collection;

	/** The path to a folder where data can be stored */
	protected IPath path;

	public AbstractAggregatorStorage(FeedCollection collection, IPath path) {
		this.collection = collection;
		this.path = path;
	}

	public void doneSaving(ISaveContext context) {
		// Does nothing per default. It is up to subclasses to implement.
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
		// Does nothing per default. It is up to subclasses to implement.
	}

	public void rollback(ISaveContext context) {
		// Does nothing per default. It is up to subclasses to implement.
	}

	public void saving(ISaveContext context) throws CoreException {
		// Does nothing per default. It is up to subclasses to implement.
	}

	/**
	 * Creates a comma separated string containing a list of all enabled <q>
	 * marks</q>.
	 * 
	 * @param marks
	 * @return
	 */
	protected String encode(EnumSet<Mark> marks) {
		StringBuffer sb = new StringBuffer();
		for (Mark mark : marks) {
			sb.append(mark.toString());
			sb.append(',');
		}
		return sb.toString();
	}

	/**
	 * Decodes a comma separated string containing a list of all enabled <q>
	 * marks</q> into a enumeration set.
	 * 
	 * @param markString
	 *            the string to decode
	 * @return the enumeration set
	 */
	protected EnumSet<Mark> decode(String markString) {
		EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);
		for (String mark : markString.split(",")) { //$NON-NLS-1$
			if (mark.trim().length() > 0)
				marks.add(Mark.valueOf(mark));
		}
		return marks;
	}
}
