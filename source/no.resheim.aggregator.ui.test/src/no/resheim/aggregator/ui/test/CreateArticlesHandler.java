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
package no.resheim.aggregator.ui.test;

import java.util.UUID;

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.commands.AbstractAggregatorCommandHandler;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command to simulate the create a feed and the insertion of a number of
 * articles.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class CreateArticlesHandler extends AbstractAggregatorCommandHandler {

	public CreateArticlesHandler() {
		super(false, true);
	}

	private static final String COUNT_PARAMETER_ID = "count"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			final AggregatorCollection collection = ((IFeedView) part)
					.getFeedCollection();
			if (collection == null) {
				return null;
			}
			final int count = Integer.parseInt(event
					.getParameter(COUNT_PARAMETER_ID));
			Job job = new Job("Adding test data") { //$NON-NLS-1$
				@SuppressWarnings("restriction")
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Subscription feed = createNewFeed(collection, "** Test feed **"); //$NON-NLS-1$
					Folder folder = collection.addNew(feed);
					for (int a = 0; a < count; a++) {
						Article article = new Article(folder,
								UUID.randomUUID(), feed.getUUID());
						article.setTitle("Article #" + a); //$NON-NLS-1$
						article.setGuid(article.getUUID().toString());
						article
								.internalSetText(Messages.CreateArticlesHandler_NewArticle_Description);
						article.setLink(EMPTY_STRING);
						collection.addNew(new Article[] {
							article
						});
					}
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
		return null;
	}

	private Subscription createNewFeed(AggregatorCollection parent, String title) {
		Subscription feed = new Subscription();
		// Initialise with default values from the preference store.
		// This is done here as the preference system is a UI component.
		feed.setTitle(title);
		feed.setURL("test://"); //$NON-NLS-1$
		feed.setArchiving(Archiving.KEEP_ALL);
		feed.setArchivingDays(30);
		feed.setArchivingItems(1000);
		feed.setUpdateInterval(0);
		feed.setUpdatePeriod(UpdatePeriod.DAYS);
		return feed;
	}
}
