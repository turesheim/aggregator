/**
 * 
 */
package no.resheim.aggregator.google.reader;

import java.util.ArrayList;

import no.resheim.aggregator.core.catalog.IFeedCatalog;
import no.resheim.aggregator.core.data.Feed;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class SubscriptionsRootHandler implements IElementHandler {
	StringBuffer buffer = new StringBuffer();
	ArrayList<Feed> feeds;

	private IFeedCatalog catalog;

	public SubscriptionsRootHandler(IFeedCatalog catalog, ArrayList<Feed> feeds) {
		super();
		this.feeds = feeds;
		this.catalog = catalog;
	}

	public void endElement(String qName) throws SAXException {
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	public IElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals("list")) {
			if (atts.getValue("name").equals("subscriptions")) {
				return new SubscriptionsHandler(catalog, feeds);
			}
		}
		return this;
	}
}