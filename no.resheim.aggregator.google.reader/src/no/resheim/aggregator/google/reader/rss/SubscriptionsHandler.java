/**
 * 
 */
package no.resheim.aggregator.google.reader.rss;

import java.util.ArrayList;

import no.resheim.aggregator.core.catalog.IFeedCatalog;
import no.resheim.aggregator.core.data.Subscription;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class SubscriptionsHandler implements IGoogleElementHandler {
	private ArrayList<Object> feeds;
	private IFeedCatalog catalog;

	public SubscriptionsHandler(IFeedCatalog catalog, ArrayList<Object> feeds) {
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

	public IGoogleElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals("object")) {
			return new FeedHandler(new Subscription(catalog), feeds);
		}
		return this;
	}
}