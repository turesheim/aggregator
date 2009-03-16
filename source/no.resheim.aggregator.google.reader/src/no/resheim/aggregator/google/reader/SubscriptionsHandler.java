/**
 * 
 */
package no.resheim.aggregator.google.reader;

import java.util.ArrayList;

import no.resheim.aggregator.core.catalog.IFeedCatalog;
import no.resheim.aggregator.core.data.Feed;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class SubscriptionsHandler implements IElementHandler {
	private ArrayList<Feed> feeds;
	private IFeedCatalog catalog;

	public SubscriptionsHandler(IFeedCatalog catalog, ArrayList<Feed> feeds) {
		super();
		this.feeds = feeds;
		this.catalog = catalog;
	}

	public SubscriptionsHandler() {

	}

	public void endElement(String qName) throws SAXException {
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals("object")) {
			return new FeedHandler(new Feed(catalog), feeds);
		}
		return this;
	}
}