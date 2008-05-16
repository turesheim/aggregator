/****************************************************************************
 *      Copyright (c) 2007 Torkild Ulvøy Resheim. All rights reserved.      *
 ****************************************************************************/
package no.resheim.aggregator.model;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public interface FeedListener {
	public void feedChanged(AggregatorItemChangedEvent event);
}
