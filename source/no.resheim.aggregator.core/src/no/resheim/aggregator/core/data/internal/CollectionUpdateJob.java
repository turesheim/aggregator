/*******************************************************************************
 * Copyright (c) 2007-2009 Torkild Ulvøy Resheim.
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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.FeedUpdateJob;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job that when run will iterate through all feeds that are held by the
 * collection, determine if the feed needs to be updated and if that is the
 * case; create a new job to update the feed. Multiple feed update jobs may
 * execute concurrently.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CollectionUpdateJob extends Job {

	/**
	 * The feed collection that we're updating
	 * 
	 */
	private FeedCollection fCollection;

	/**
	 * Compares feeds by next update time.
	 * 
	 * @author Torkild Ulvøy Resheim
	 */
	private static class FeedComparator implements Comparator<Feed>,
			Serializable {

		private static final long serialVersionUID = 5298242739294116541L;

		public int compare(Feed o1, Feed o2) {
			long val1 = o1.getLastUpdate() + o1.getUpdateTime();
			long val2 = o2.getLastUpdate() + o2.getUpdateTime();
			return (int) (val1 - val2);
		}

	}

	/**
	 * Creates a new collection update job that will update the feeds of the
	 * collection.
	 * 
	 * @param collection
	 */
	public CollectionUpdateJob(FeedCollection collection) {
		super(Messages.RegistryUpdateJob_Label);
		this.fCollection = collection;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Stack<Feed> feeds = getSortedFeeds();
		while (!feeds.empty()) {
			Feed feed = feeds.pop();
			// Update if we're not already doing so unless the last update was
			// bad.
			if (!feed.isUpdating() && feed.getLastStatus().isOK()) {
				long last = feed.getLastUpdate();
				long interval = feed.getUpdateTime();
				// Schedule the job to run
				if ((last + interval) <= System.currentTimeMillis()) {
					FeedUpdateJob j = new FeedUpdateJob(fCollection, feed);
					j.schedule();
				}
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Returns a stack of sorted feeds. The feed eligible to be updated first is
	 * on top of the stack.
	 * 
	 * @return a stack of sorted feeds
	 */
	private Stack<Feed> getSortedFeeds() {
		Stack<Feed> feeds = new Stack<Feed>();
		for (Feed feed : fCollection.getFeeds().values()) {
			feeds.add(feed);
		}
		Collections.sort(feeds, new FeedComparator());
		return feeds;
	}

}
