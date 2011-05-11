/*******************************************************************************
 * Copyright (c) 2008-2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.data.internal;

import java.util.EnumSet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.IAggregatorStorage;
import no.resheim.aggregator.core.data.AggregatorItem.Flag;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.resources.ISaveParticipant;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Implementation of {@link IAggregatorStorage} with some common features.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractAggregatorStorage implements IAggregatorStorage {

	public Lock readLock() {
		return lock.readLock();
	}

	public Lock writeLock() {
		return lock.writeLock();
	}

	/**
	 * Handles concurrency for the database, making sure that no readers get
	 * access while writing and only one writer gets access at the time.
	 */
	private final ReadWriteLock lock = new ReentrantReadWriteLock();

	/**
	 * The feed collection that data is being handled for
	 */
	protected AggregatorCollection collection;

	/** The path to a folder where data can be stored */
	protected IPath path;

	public AbstractAggregatorStorage(AggregatorCollection collection, IPath path) {
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

	/**
	 * Called when the workbench want's the {@link ISaveParticipant} to store
	 * it's state. We use this method to clear the trash folder.
	 */
	public void saving(ISaveContext context) throws CoreException {
		if (context.getKind() == ISaveContext.FULL_SAVE) {
			AggregatorItem[] children = collection.getTrashFolder()
					.getChildren(EnumSet.allOf(ItemType.class));
			for (AggregatorItem aggregatorItem : children) {
				collection.deleteChild(aggregatorItem);
			}
			collection.notifyListerners(children, EventType.REMOVED);
		}
	}

	protected String encodeFlags(EnumSet<Flag> flags) {
		StringBuffer sb = new StringBuffer();
		for (Flag flag : flags) {
			sb.append(flag.toString());
			sb.append(',');
		}
		return sb.toString();
	}

	protected EnumSet<Flag> decodeFlags(String flagsString) {
		EnumSet<Flag> marks = EnumSet.noneOf(Flag.class);
		for (String mark : flagsString.split(",")) { //$NON-NLS-1$
			if (mark.trim().length() > 0)
				marks.add(Flag.valueOf(mark));
		}
		return marks;
	}
}
