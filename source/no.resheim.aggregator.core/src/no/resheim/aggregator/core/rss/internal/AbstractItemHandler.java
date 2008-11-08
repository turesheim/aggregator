package no.resheim.aggregator.core.rss.internal;

import no.resheim.aggregator.core.data.Article;

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
