/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.synch;

import java.text.MessageFormat;

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Abstract implementation of a subscription synchroniser. Subclasses of this
 * type are responsible for using information from the supplied subscription for
 * obtaining data and populate the collection.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public abstract class AbstractSynchronizer extends Job {
	protected Feed feed;
	protected FeedCollection collection;

	protected static final String EMPTY_STRING = "";

	protected static final String CORE_NET_BUNDLE = "org.eclipse.core.net";

	public AbstractSynchronizer() {
		super(EMPTY_STRING);
		setPriority(Job.DECORATE);
		setUser(false);
	}

	@Override
	protected abstract IStatus run(IProgressMonitor monitor);

	public void setCollection(FeedCollection collection) {
		this.collection = collection;
	}

	public void setSubscription(Feed feed) {
		this.feed = feed;
		setName(MessageFormat.format(Messages.FeedUpdateJob_Title,
				new Object[] { feed.getTitle() }));
	}

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	protected void cleanUp(Feed site) {
		// First find the folder
		try {
			for (Folder folder : collection.getDescendingFolders()) {
				if (folder.getUUID().equals(site.getLocation())) {
					folder.cleanUp(site);
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

}
