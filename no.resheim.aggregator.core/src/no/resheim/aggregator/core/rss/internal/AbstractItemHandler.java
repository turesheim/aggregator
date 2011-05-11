/*******************************************************************************
 * Copyright (c) 2007-2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.rss.internal;

import no.resheim.aggregator.core.data.Article;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AbstractItemHandler extends AbstractElementHandler {

	private boolean hasArticle(Article article) {
		for (Article item : feed.getTempItems()) {
			if (item.getGuid().equals(article.getGuid())) {
				return true;
			}
		}
		return false;
	}

	protected void addArticle(Article article) {
		if (!hasArticle(article)) {
			feed.getTempItems().add(article);
		}
	}

}
