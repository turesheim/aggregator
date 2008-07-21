/*******************************************************************************
 * Copyright (c) 2007-2008 Torkild Ulvøy Resheim.
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

import java.util.Collections;
import java.util.Comparator;
import java.util.Stack;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.FeedUpdateJob;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

/**
 * Job that when run will iterate through all feeds that are held by the
 * registry, determine if the feed needs to be updated and if that's the case
 * create a new job to update the feed.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CollectionUpdateJob extends Job {

	/** The feed registry that we're updating */
	private FeedCollection registry;

	private class FeedComparator implements Comparator<Feed> {

		public int compare(Feed o1, Feed o2) {
			return 0;
		}

	}

	public CollectionUpdateJob(FeedCollection registry) {
		super(Messages.RegistryUpdateJob_Label);
		this.registry = registry;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		Stack<Feed> feeds = getSortedFeeds();
		while (!feeds.empty()) {
			Feed f = feeds.pop();
			// Update if we're not already doing so.
			if (!f.isUpdating()) {
				long last = f.getLastUpdate();
				long interval = f.getUpdateTime();
				// Schedule the job to run
				if ((last + interval) <= System.currentTimeMillis()) {
					FeedUpdateJob j = new FeedUpdateJob(registry, f);
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
		for (Feed feed : registry.getFeeds()) {
			feeds.add(feed);
		}
		Collections.sort(feeds, new FeedComparator());
		return feeds;
	}

}
