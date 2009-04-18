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
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.data.Subscription.Archiving;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;

/**
 * Synchronizer that connects directly to the feed URL and downloads from there.
 * This is the normal way of operation.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DirectSynchronizer extends AbstractSynchronizer {

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		synchronized (subscription) {
			subscription.setUpdating(true);
			subscription.getTempItems().clear();
		}
		boolean debug = AggregatorPlugin.getDefault().isDebugging();
		collection.notifyListerners(new Subscription[] { subscription },
				EventType.CHANGED);
		// If the feed does not use archiving it's better to remove all items
		// before downloading new ones.
		if (subscription.getArchiving() == Archiving.KEEP_NONE) {
			// TODO: Implement cleanup.
		}
		MultiStatus ms = new MultiStatus(AggregatorPlugin.PLUGIN_ID,
				IStatus.OK, MessageFormat.format(
						Messages.FeedUpdateJob_StatusTitle,
						new Object[] { subscription.getTitle() }), null);
		try {
			if (!subscription.getURL().startsWith("test://")) { //$NON-NLS-1$
				ms.add(download());
			}
			if (ms.isOK()) {
				setName(MessageFormat.format(Messages.FeedUpdateJob_CleaningUp,
						new Object[] { subscription.getTitle() }));
				cleanUp();
			}
			synchronized (subscription) {
				subscription.setUpdating(false);
			}
			Collections.sort(subscription.getTempItems());
			if (subscription.getTempItems().size() > 0) {
				ms.addAll(collection.addNew(subscription.getTempItems()
						.toArray(
								new AggregatorItem[subscription.getTempItems()
										.size()])));
			}
			subscription.setLastUpdate(System.currentTimeMillis());
			subscription.setLastStatus(ms);
			// Store changes to the feed
			collection.feedUpdated(subscription);
			collection.notifyListerners(new Subscription[] { subscription },
					EventType.CHANGED);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ms;

	}
}
