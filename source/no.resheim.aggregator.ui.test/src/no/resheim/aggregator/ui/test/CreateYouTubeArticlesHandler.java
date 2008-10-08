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

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.commands.AbstractAggregatorCommandHandler;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;
import no.resheim.aggregator.data.internal.InternalArticle;

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
public class CreateYouTubeArticlesHandler extends
		AbstractAggregatorCommandHandler {

	public CreateYouTubeArticlesHandler() {
		super(false, true);
	}

	private static final String COUNT_PARAMETER_ID = "count"; //$NON-NLS-1$
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private static final String[][] ARTICLES = new String[][] {
			{
					"KdkPcMWynKk", "New battery management parts"
			}, {
					"Mk102mPkQuc", "STK500"
			}, {
					"RODTFfEX31I", "XMEGA!"
			}, {
					"mRgpYyP6e-E", "The first issue"
			}, {
					"0c9Kgusa2O4", "UC3 Introduction"
			}, {
					"7kV7XFPO_es", "NGW100 part II: Demo"
			}, {
					"qv8Vq15-mRM", "DB101, BC100, EVK525, Buildroot"
			}, {
					"g8SB9NQSEGc", "STK600, AVR ONE!, Raven"
			}, {
					"148_M6N6Opk", "Wireless sensor in 90s"
			}
	};

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			final FeedCollection collection = ((IFeedView) part)
					.getFeedCollection();
			if (collection == null) {
				return null;
			}
			Job job = new Job("Adding test data") { //$NON-NLS-1$
				@SuppressWarnings("restriction")
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					Feed feed = createNewFeed(collection,
							"YouTube Support Test"); //$NON-NLS-1$
					Folder folder = collection.addNew(feed);
					for (int a = 0; a < ARTICLES.length; a++) {
						InternalArticle article = new InternalArticle(folder,
								UUID.randomUUID(), feed.getUUID());
						article.setTitle(ARTICLES[a][1]); //$NON-NLS-1$
						article.setGuid(article.getUUID().toString());
						article.setLink("http://www.youtube.com/watch?v="
								+ ARTICLES[a][0]);
						// article
						// .internalSetText(Messages.CreateArticlesHandler_NewArticle_Description);
						article
								.internalSetText("<object width=\"100%\" height=\"100%\" bgcolor=\"#000000\"><param name=\"movie\" "
										+ "value=\"http://www.youtube.com/v/"
										+ ARTICLES[a][0]
										+ "&hl=en&fs=1\"></param><param "
										+ "name=\"allowFullScreen\" value=\"false\"></param>"
										+ "<embed src=\"http://www.youtube.com/v/"
										+ ARTICLES[a][0]
										+ "&hl=en&fs=1\" "
										+ "type=\"application/x-shockwave-flash\" allowfullscreen=\"false\" enablejavascript=\"true\" width=\"100%\" "
										+ "height=\"100%\"></embed></object>");
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

	private Feed createNewFeed(FeedCollection parent, String title) {
		Feed feed = new Feed();
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
