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
package no.resheim.aggregator.ui.test.regression;

import java.util.UUID;

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.test.TestUtils;
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
public class Bug636_CreateArticlesHandler extends
		AbstractAggregatorCommandHandler {

	public Bug636_CreateArticlesHandler() {
		super(false, true);
	}

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			final AggregatorCollection collection = ((IFeedView) part)
					.getFeedCollection();
			if (collection == null) {
				return null;
			}
			Job job = new Job("Adding test data") { //$NON-NLS-1$
				@SuppressWarnings("restriction")
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Subscription feed = TestUtils.createNewFeed("Bug 636");
					StringBuilder sb = new StringBuilder();
					for (int a = 0; a < 1024; a++) {
						sb.append("x"); //$NON-NLS-1$
					}
					Folder folder = collection.addNew(feed);

					// First insert a good article.
					Article article_1 = new Article(folder, UUID.randomUUID(),
							feed.getUUID());
					article_1.setTitle("Article 1 (good)"); //$NON-NLS-1$
					article_1.setGuid(article_1.getUUID().toString());
					article_1.internalSetText("description"); //$NON-NLS-1$
					article_1.setLink(EMPTY_STRING);

					// This article should fail to be inserted into the database
					// as the URL field is way too long.
					Article article_2 = new Article(folder, UUID.randomUUID(),
							feed.getUUID());
					article_2.setTitle("Article 2 (bad)"); //$NON-NLS-1$
					article_2.setGuid(article_2.getUUID().toString());
					article_2.internalSetText("description"); //$NON-NLS-1$
					article_2.setLink(EMPTY_STRING);

					Article article_3 = new Article(folder, UUID.randomUUID(),
							feed.getUUID());
					article_3.setTitle("Article 3 (good)"); //$NON-NLS-1$
					article_3.setGuid(article_3.getUUID().toString());
					article_3.internalSetText("description"); //$NON-NLS-1$
					article_3.setLink(sb.toString());
					collection.addNew(new Article[] {
							article_1, article_2, article_3
					});
					// This article should fail to be inserted into the database
					// as the URL field is way too long.
					Article article_4 = new Article(folder, UUID.randomUUID(),
							feed.getUUID());
					article_4.setTitle("Article 4 (bad)"); //$NON-NLS-1$
					article_4.setGuid(article_4.getUUID().toString());
					article_4.internalSetText("description"); //$NON-NLS-1$
					article_4.setLink(sb.toString());
					collection.addNew(new Article[] {
						article_4
					});
					return Status.OK_STATUS;
				}
			};
			job.schedule();
		}
		return null;
	}
}
