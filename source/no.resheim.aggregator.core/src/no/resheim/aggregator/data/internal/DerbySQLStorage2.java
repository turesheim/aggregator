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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.IAggregatorItem;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;

import org.eclipse.core.resources.ISaveContext;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

/**
 * This type is a "merge" between the DerbySQLStorage and the MemoryStorage. Not
 * quite ready for full use yet, but can be used to test out performance
 * enhancements.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DerbySQLStorage2 extends MemoryStorage {

	/** Connection options */
	private static final String CONNECT_OPTIONS = ";create=true"; //$NON-NLS-1$

	/** Database driver */
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$

	/** Disconnection options */
	private static final String DISCONNECT_OPTIONS = ";shutdown=true"; //$NON-NLS-1$

	private static final String JDBC_DERBY = "jdbc:derby:"; //$NON-NLS-1$

	/** Name of the SQL file used to create the tables */
	private static final String TABLES_SQL = "tables.sql"; //$NON-NLS-1$

	/** The database connection */
	private Connection connection;

	public DerbySQLStorage2(FeedCollection collection, IPath path) {
		super(collection, path);
	}

	public IStatus shutdown() {
		try {
			connection = DriverManager.getConnection(JDBC_DERBY
					+ path.toOSString() + DISCONNECT_OPTIONS);
			connection.close();
		} catch (SQLException e) {
			// Should throw ERROR 08006 which we will ignore.
		}
		return Status.OK_STATUS;
	}

	public HashMap<UUID, Feed> getFeeds() {
		try {
			feeds.clear();
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery("select * from feeds"); //$NON-NLS-1$
			while (rs.next()) {
				Feed f = composeFeed(collection, rs);
				feeds.put(f.getUUID(), f);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return feeds;
	}

	public IStatus startup(IProgressMonitor monitor) {
		try {
			monitor.subTask(Messages.DerbySQLStorage_Creating_Storage);
			Class.forName(DB_DRIVER).newInstance();
			connection = DriverManager.getConnection(JDBC_DERBY
					+ path.toOSString() + CONNECT_OPTIONS);
			connection.setAutoCommit(true);
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet rs = metadata.getTables(null, "APP", "FEEDS", null); //$NON-NLS-1$ //$NON-NLS-2$
			if (!rs.next()) {
				createTables(new NullProgressMonitor());
			}
		} catch (SQLException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not database", e); //$NON-NLS-1$
		} catch (InstantiationException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		} catch (IllegalAccessException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		} catch (ClassNotFoundException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}

	public void prepareToSave(ISaveContext context) throws CoreException {
	}

	private IStatus createTables(IProgressMonitor monitor) throws SQLException {
		monitor.subTask(Messages.DerbySQLStorage_Initializing_Database);
		InputStream is = DerbySQLStorage.class.getResourceAsStream(TABLES_SQL);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Statement s = connection.createStatement();
		StringBuffer create = new StringBuffer();
		String in = null;
		try {
			monitor.subTask(Messages.DerbySQLStorage_Creating_Tables);
			while ((in = br.readLine()) != null) {
				if (in.contains("/*")) { //$NON-NLS-1$
					while (!in.contains("*/")) { //$NON-NLS-1$
						in = br.readLine();
					}
					in = br.readLine();
				}
				if (in.endsWith(";")) { //$NON-NLS-1$
					create.append(in.substring(0, in.length() - 1));
					try {
						s.executeUpdate(create.toString());
					} catch (SQLException sqle) {
						System.out.println(sqle.getMessage());
						// In case the table already exists
					}
					create.setLength(0);
				} else {
					create.append(in);
					create.append('\n');
				}
			}
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		}
	}

	public void saving(ISaveContext context) throws CoreException {
		for (ItemHolder itemHolder : items.values()) {
			AggregatorItem item = itemHolder.item;
			if (item instanceof Article)
				insert((Article) item);
			if (item instanceof Folder)
				insert((Folder) item);
			if (item instanceof Feed)
				insert((Feed) item);
			item.setSerialized(true);
		}
	}

	/**
	 * Inserts a feed item into the database.
	 * 
	 * @param item
	 *            The item to insert.
	 */
	private void insert(Article item) {
		try {
			PreparedStatement ps = connection
					.prepareStatement("insert into articles values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
			ps.setEscapeProcessing(true);
			ps.setString(1, item.getUUID().toString());
			ps.setString(2, ((AggregatorItem) item.getParent()).getUUID()
					.toString());
			ps.setLong(3, item.getOrdering());
			ps.setString(4, item.getFeedUUID().toString());
			ps.setString(5, item.getGuid());
			ps.setString(6, item.getTitle());
			ps.setString(7, item.getLink());
			ps.setString(8, encode(item.getMarks()));
			ps.setInt(9, item.isRead() ? 1 : 0);
			ps.setLong(10, item.getPublicationDate());
			ps.setLong(11, item.getReadDate());
			ps.setLong(12, item.getAdded());
			ps.setString(13, item.getDescription());
			ps.setString(14, item.getCreator());
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a new feed into the database.
	 * 
	 * @param feed
	 *            The feed to insert
	 */
	private void insert(Feed feed) {
		try {
			PreparedStatement ps = connection
					.prepareStatement("insert into feeds values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
			ps.setEscapeProcessing(true);
			ps.setString(1, feed.getUUID().toString());
			ps.setString(2, ((AggregatorItem) feed.getParent()).getUUID()
					.toString());
			ps.setLong(3, feed.getOrdering());
			ps.setString(4, feed.getTitle());
			ps.setString(5, feed.getURL());
			ps.setString(6, encode(feed.getMarks()));
			ps.setString(7, feed.getArchiving().toString());
			ps.setInt(8, feed.getArchivingItems());
			ps.setInt(9, feed.getArchivingDays()); // archiving_days
			ps.setInt(10, feed.getUpdateInterval()); // update_interval
			ps.setString(11, feed.getUpdatePeriod().toString()); // update_period
			ps.setLong(12, feed.getLastUpdate()); // last_update
			ps.setString(13, feed.getDescription()); // description
			ps.setString(14, feed.getLink()); // link
			ps.setString(15, feed.getWebmaster()); // webmaster
			ps.setString(16, feed.getEditor()); // editor
			ps.setString(17, feed.getCopyright()); // copyright
			ps.setString(18, feed.getType()); // feed_type
			ps.setInt(19, feed.isHidden() ? 1 : 0);
			ps.setString(20, feed.getUsername());
			ps.setString(21, feed.getPassword());
			ps.setInt(22, feed.isThreaded() ? 1 : 0);
			ps.executeUpdate();
			ps.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a folder into the database.
	 * 
	 * @param item
	 *            The item to insert.
	 */
	private void insert(Folder category) {
		try {
			PreparedStatement ps = connection
					.prepareStatement("insert into folders values(?,?,?,?,?) "); //$NON-NLS-1$
			ps.setEscapeProcessing(true);
			ps.setString(1, category.getUUID().toString());
			ps.setString(2, ((AggregatorItem) category.getParent()).getUUID()
					.toString());
			ps.setLong(3, category.getOrdering());
			ps.setString(4, category.getTitle());
			ps.setString(5, encode(category.getMarks()));
			ps.executeUpdate();
			ps.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Feed composeFeed(AggregatorItem parent, ResultSet rs)
			throws SQLException {
		Feed feed = collection.newFeedInstance(parent);
		feed.setUUID(UUID.fromString(rs.getString(1)));
		feed.setParent(parent);
		feed.setOrdering(rs.getInt(3));
		feed.setTitle(rs.getString(4));
		feed.setURL(rs.getString(5));
		feed.setMarks(decode(rs.getString(6)));
		feed.setArchiving(Archiving.valueOf(rs.getString(7)));
		feed.setArchivingItems(rs.getInt(8));
		feed.setArchivingDays(rs.getInt(9));
		feed.setUpdateInterval(rs.getInt(10));
		feed.setUpdatePeriod(UpdatePeriod.valueOf(rs.getString(11)));
		feed.setLastUpdate(rs.getLong(12));
		feed.setDescription(rs.getString(13));
		feed.setLink(rs.getString(14));
		feed.setWebmaster(rs.getString(15));
		feed.setEditor(rs.getString(16));
		feed.setCopyright(rs.getString(17));
		feed.setType(rs.getString(18));
		feed.setHidden(rs.getInt(19) != 0);
		feed.setUsername(rs.getString(20));
		feed.setPassword(rs.getString(21));
		feed.setThreaded(rs.getInt(22) != 0);
		return feed;
	}

	/**
	 * Composes an Item instance from the result set but leaves out the
	 * description field.
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private Article composeArticle(IAggregatorItem parent, ResultSet rs)
			throws SQLException {
		Article item = collection.newArticleInstance(parent);
		item.setUUID(UUID.fromString(rs.getString(1)));
		item.setParent(parent);
		item.setOrdering(rs.getInt(3));
		item.setFeedUUID(UUID.fromString(rs.getString(4)));
		item.setGuid(rs.getString(5));
		item.setTitle(rs.getString(6));
		item.setLink(rs.getString(7));
		item.setMarks(decode(rs.getString(8)));
		item.setRead(rs.getInt(9) != 0);
		item.setPublicationDate(rs.getLong(10));
		item.setReadDate(rs.getLong(11));
		item.setAddedDate(rs.getLong(12));
		item.setDescription(rs.getString(13));
		item.setCreator(rs.getString(14));
		return item;
	}

	private Folder composeFolder(IAggregatorItem parent, ResultSet rs)
			throws SQLException {
		Folder item = collection.newFolderInstance(parent);
		item.setUUID(UUID.fromString(rs.getString(1)));
		item.setParent(parent);
		item.setOrdering(rs.getInt(3));
		item.setTitle(rs.getString(4));
		item.setMarks(decode(rs.getString(5)));
		return item;
	}

	private AggregatorItem selectFeed(AggregatorItem parent, int index)
			throws SQLException {
		Statement s = connection.createStatement();
		String query = null;
		AggregatorItem feed = null;
		if (index == -1) {
			query = "select * from feeds where uuid='" //$NON-NLS-1$
					+ parent.getUUID().toString() + "'"; //$NON-NLS-1$
		} else {
			query = "select * from feeds where parent_uuid='" //$NON-NLS-1$
					+ parent.getUUID().toString() + "' and ordering=" + index; //$NON-NLS-1$
		}

		ResultSet rs = s.executeQuery(query);
		while (rs.next()) {
			feed = composeFeed(parent, rs);
		}
		rs.close();
		return feed;
	}

	private AggregatorItem selectArticle(AggregatorItem parent, int index)
			throws SQLException {
		Statement s = connection.createStatement();
		Article article = null;
		ResultSet rs = s
				.executeQuery("select * from articles where parent_uuid='" //$NON-NLS-1$
						+ ((AggregatorItem) parent).getUUID().toString()
						+ "' and ordering=" + index); //$NON-NLS-1$);
		while (rs.next()) {
			article = composeArticle(parent, rs);
		}
		rs.close();
		return article;
	}

	private AggregatorItem selectCategory(AggregatorItem parent, int index)
			throws SQLException {
		Statement s = connection.createStatement();
		String query = null;
		Folder folder = null;
		if (index == -1) {
			query = "select * from folders where uuid='" //$NON-NLS-1$
					+ parent.getUUID().toString() + "'"; //$NON-NLS-1$
		} else {
			query = "select * from folders where parent_uuid='" //$NON-NLS-1$
					+ parent.getUUID().toString() + "' and ordering=" + index; //$NON-NLS-1$

		}
		ResultSet rs = s.executeQuery(query);
		while (rs.next()) {
			folder = composeFolder(parent, rs);
		}
		rs.close();
		return folder;
	}

	public AggregatorItem getItem(AggregatorItem parent, int index) {
		AggregatorItem item = super.getItem(parent, index);
		// If the item was not found in the cache we'll look for it in the
		// database.
		try {
			if (item == null) {
				item = selectArticle(parent, index);
			}
			if (item == null) {
				item = selectFeed(parent, index);
			}
			if (item == null) {
				item = selectCategory(parent, index);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// If the item does not exist in the memory cache we will add it
		if (item != null && items.get(item.getUUID()) == null) {
			super.add(item);
		}
		return item;
	}

	public boolean hasArticle(String guid) {
		boolean found = false;
		for (ItemHolder itemHolder : items.values()) {
			final AggregatorItem item = itemHolder.item;
			if (item instanceof Article
					&& ((Article) item).getGuid().equals(guid)) {
				return true;
			}
		}
		try {
			Statement s = connection.createStatement();
			String query = "select * from articles where guid='" //$NON-NLS-1$
					+ guid + "'"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			found = rs.next();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return found;
	}

	public synchronized int getChildCount(AggregatorItem parent) {
		ItemHolder holder = items.get(parent.getUUID());
		int count = 0;
		// First count all the children that has not yet been serialised.
		for (AggregatorItem item : holder.children) {
			if (!item.isSerialized())
				count++;
		}
		// Then retrieve the number of child items that is stored in the
		// database.
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s
					.executeQuery("select count(uuid) from articles where parent_uuid='" //$NON-NLS-1$
							+ parent.getUUID() + "'"); //$NON-NLS-1$
			if (rs.next())
				count += rs.getInt(1);
			rs.close();
			rs = s
					.executeQuery("select count(uuid) from folders where parent_uuid='" //$NON-NLS-1$
							+ parent.getUUID() + "'"); //$NON-NLS-1$
			if (rs.next())
				count += rs.getInt(1);
			rs.close();
			rs = s
					.executeQuery("select count(uuid) from feeds where parent_uuid='" //$NON-NLS-1$
							+ parent.getUUID() + "'"); //$NON-NLS-1$
			if (rs.next())
				count += rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	public int getUnreadCount(Feed feed) {
		ItemHolder holder = items.get(feed.getUUID());
		int count = 0;
		// First count all the children that has not yet been serialised.
		for (AggregatorItem item : holder.children) {
			if (!item.isSerialized() && item instanceof Article
					&& !((Article) item).isRead()) {
				count++;
			}
		}
		// Then count the database items.
		try {
			if (connection.isClosed())
				return 0;
			Statement s = connection.createStatement();
			String query = "select count(title) from articles where parent_uuid='" //$NON-NLS-1$
					+ feed.getUUID() + "' and is_read=0"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			if (rs.next())
				count += rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
}
