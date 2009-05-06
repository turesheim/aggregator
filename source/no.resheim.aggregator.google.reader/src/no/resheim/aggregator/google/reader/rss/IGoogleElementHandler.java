/**
 * 
 */
package no.resheim.aggregator.google.reader.rss;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

interface IGoogleElementHandler {

	public IGoogleElementHandler startElement(String qName, Attributes atts)
			throws SAXException;

	public void endElement(String qName) throws SAXException;

	public void characters(char[] ch, int start, int length)
			throws SAXException;
}