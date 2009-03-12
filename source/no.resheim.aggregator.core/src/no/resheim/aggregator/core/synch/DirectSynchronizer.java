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
package no.resheim.aggregator.core.synch;

import java.text.MessageFormat;
import java.util.Collections;


import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Feed.Archiving;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DirectSynchronizer extends AbstractSynchronizer {

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		synchronized (feed) {
			feed.setUpdating(true);
			feed.getTempItems().clear();
		}
		boolean debug = AggregatorPlugin.getDefault().isDebugging();
		collection.notifyListerners(new Feed[] { feed }, EventType.CHANGED);
		// If the feed does not use archiving it's better to remove all items
		// before downloading new ones.
		if (feed.getArchiving() == Archiving.KEEP_NONE) {
			// TODO: Implement cleanup.
		}
		MultiStatus ms = new MultiStatus(AggregatorPlugin.PLUGIN_ID,
				IStatus.OK, MessageFormat.format(
						Messages.FeedUpdateJob_StatusTitle, new Object[] { feed
								.getTitle() }), null);
		try {
			if (!feed.getURL().startsWith("test://")) { //$NON-NLS-1$
				ms.add(download(feed, debug));
			}
			if (ms.isOK()) {
				setName(MessageFormat.format(Messages.FeedUpdateJob_CleaningUp,
						new Object[] { feed.getTitle() }));
				cleanUp(feed);
			}
			synchronized (feed) {
				feed.setUpdating(false);
			}
			Collections.sort(feed.getTempItems());
			if (feed.getTempItems().size() > 0) {
				ms.addAll(collection.addNew(feed.getTempItems().toArray(
						new AggregatorItem[feed.getTempItems().size()])));
			}
			feed.setLastUpdate(System.currentTimeMillis());
			feed.setLastStatus(ms);
			// Store changes to the feed
			collection.feedUpdated(feed);
			collection.notifyListerners(new Feed[] { feed }, EventType.CHANGED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ms;

	}
}
