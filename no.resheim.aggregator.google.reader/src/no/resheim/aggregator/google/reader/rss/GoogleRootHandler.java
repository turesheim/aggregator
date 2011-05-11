/**
 * 
 */
package no.resheim.aggregator.google.reader.rss;

import java.util.ArrayList;

import no.resheim.aggregator.core.catalog.IFeedCatalog;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

class GoogleRootHandler implements IGoogleElementHandler {
	StringBuffer buffer = new StringBuffer();
	ArrayList<Object> elements;

	private IFeedCatalog catalog;

	public GoogleRootHandler(IFeedCatalog catalog, ArrayList<Object> elements) {
		super();
		this.elements = elements;
		this.catalog = catalog;
	}

	public void endElement(String qName) throws SAXException {
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
	}

	public IGoogleElementHandler startElement(String qName, Attributes atts)
			throws SAXException {
		if (qName.equals("list")) {
			if (atts.getValue("name").equals("subscriptions")) {
				return new SubscriptionsHandler(catalog, elements);
			} else if (atts.getValue("name").equals("tags")) {
				return new TagsHandler(catalog, elements);
			} else {
				System.out.println(atts.getValue("name"));
			}
		}
		return this;
	}
}