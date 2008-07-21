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
package no.resheim.aggregator.data.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.IAggregatorItem;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * An implementation of the aggregator storage that only keeps the items in
 * memory. The data is not persisted in any way.
 * 
 * FIXME: Implement missing methods
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class MemoryStorage extends AbstractAggregatorStorage {

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	/**
	 * Simple holder of aggregator items. The main purpose of the type is to
	 * maintain the relations between the items.
	 */
	protected class ItemHolder {
		ArrayList<AggregatorItem> children;

		AggregatorItem item;

		/**
		 * @param item
		 */
		public ItemHolder(AggregatorItem item) {
			super();
			this.item = item;
			children = new ArrayList<AggregatorItem>();
		}
	}

	HashMap<UUID, Feed> feeds;

	HashMap<UUID, ItemHolder> items;

	public MemoryStorage(FeedCollection collection, IPath path) {
		super(collection, path);
		items = new HashMap<UUID, ItemHolder>();
		feeds = new HashMap<UUID, Feed>();
		items.put(collection.getUUID(), new ItemHolder(collection));
	}

	public void add(AggregatorItem item) {
		ItemHolder holder = new ItemHolder(item);
		items.put(item.getUUID(), holder);
		ItemHolder parentHolder = items.get(item.getParent().getUUID());
		parentHolder.children.add(item);
		if (item instanceof Feed) {
			feeds.put(item.getUUID(), (Feed) item);
		}
	}

	public void delete(AggregatorItem item) {
		ItemHolder parentHolder = items.get(item.getParent().getUUID());
		Assert.isTrue(parentHolder.children.remove(item));
		Assert.isNotNull(items.remove(item.getUUID()));
		if (item instanceof Feed) {
			feeds.remove(item.getUUID());
		}
	}

	public void deleteOutdated(Feed feed, long date) {
		// TODO Auto-generated method stub
	}

	public int getChildCount(AggregatorItem parent) {
		ItemHolder holder = items.get(parent.getUUID());
		if (holder != null) {
			return holder.children.size();
		} else
			return 0;
	}

	public AggregatorItem[] getChildren(AggregatorItem parent) {
		ItemHolder holder = items.get(parent.getUUID());
		if (holder != null) {
			return holder.children.toArray(new AggregatorItem[holder.children
					.size()]);
		}
		return new AggregatorItem[0];
	}

	public String getDescription(Article item) {
		ItemHolder holder = items.get(item.getUUID());
		if (holder != null) {
			return ((Article) holder.item).getDescription();
		}
		return EMPTY_STRING;
	}

	public HashMap<UUID, Feed> getFeeds() {
		return feeds;
	}

	public AggregatorItem getItem(AggregatorItem parent, int index) {
		ItemHolder holder = items.get(parent.getUUID());
		if (holder != null) {
			for (AggregatorItem child : holder.children) {
				if (child.getOrdering() == index)
					return child;
			}
		}
		return null;
	}

	public int getUnreadCount(Feed parent) {
		int count = 0;
		ItemHolder holder = items.get(parent.getUUID());
		if (holder != null) {
			for (IAggregatorItem item : holder.children) {
				if (item instanceof Article && !((Article) item).isRead()) {
					count++;
				}
			}
		}
		return count;
	}

	public boolean hasArticle(String guid) {
		for (ItemHolder item : items.values()) {
			if (item.item instanceof Article) {
				if (((Article) item.item).getGuid().equals(guid))
					return true;
			}
		}
		return false;
	}

	public boolean hasFeed(String url) {
		for (Feed feed : feeds.values()) {
			if (feed.getURL().equals(url)) {
				return true;
			}
		}
		return false;
	}

	public void keepMaximum(Feed feed, int keep) {
		// TODO Auto-generated method stub

	}

	public void move(AggregatorItem item, AggregatorItem parent, int order) {
		ItemHolder oldHolder = items.get(item.getParent().getUUID());
		ItemHolder newHolder = items.get(parent.getUUID());
		oldHolder.children.remove(item);
		item.setOrdering(order);
		item.setParent(parent);
		newHolder.children.add(item);
	}

	public void rename(AggregatorItem item) {
		// TODO Auto-generated method stub

	}

	public IStatus shutdown() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Does nothing. Whatever initialisation required is already done in the
	 * constructor.
	 */
	public IStatus startup(IProgressMonitor monitor) {
		return Status.OK_STATUS;
	}

	public void updateFeed(Feed feed) {
		// TODO Auto-generated method stub

	}

	public void updateReadFlag(AggregatorItem item) {
		// TODO Auto-generated method stub

	}
}
