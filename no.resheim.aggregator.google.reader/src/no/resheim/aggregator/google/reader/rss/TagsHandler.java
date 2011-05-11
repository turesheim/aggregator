package no.resheim.aggregator.google.reader.rss;

import java.util.ArrayList;

import no.resheim.aggregator.core.catalog.IFeedCatalog;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TagsHandler implements IGoogleElementHandler {
	private ArrayList<Object> elements;
	private IFeedCatalog catalog;

	public TagsHandler(IFeedCatalog catalog, ArrayList<Object> elements) {
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
		if (qName.equals("object")) {
			return new TagHandler(elements);
		}
		return this;
	}

}
