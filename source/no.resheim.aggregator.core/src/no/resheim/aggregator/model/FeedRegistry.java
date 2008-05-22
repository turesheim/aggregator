/*******************************************************************************
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.model.AbstractAggregatorItem.Mark;
import no.resheim.aggregator.model.AggregatorItemChangedEvent.FeedChangeEventType;
import no.resheim.aggregator.model.Feed.Archiving;
import no.resheim.aggregator.model.Feed.UpdatePeriod;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class FeedRegistry implements IAggregatorItem {

	public class AggregatorDatabase {
		/** Connection options */
		private static final String CONNECT_OPTIONS = ";create=true"; //$NON-NLS-1$

		/** Disconnection options */
		private static final String DISCONNECT_OPTIONS = ";shutdown=true"; //$NON-NLS-1$

		/** Database driver */
		private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$

		/** Database name */
		private static final String DB_NAME = "/database"; //$NON-NLS-1$

		private static final String JDBC_DERBY = "jdbc:derby:"; //$NON-NLS-1$

		/** Name of the SQL file used to create the tables */
		private static final String TABLES_SQL = "tables.sql"; //$NON-NLS-1$

		private Connection connection;

		private String location;

		/**
		 * 
		 */
		public AggregatorDatabase() {
			super();
		}

		private void addInt(StringBuffer sb, long value, boolean comma) {
			if (comma)
				sb.append(',');
			sb.append(value);
		}

		private void addNull(StringBuffer sb, boolean comma) {
			if (comma)
				sb.append(',');
			sb.append("NULL"); //$NON-NLS-1$
		}

		private void addLong(StringBuffer sb, long value, boolean comma) {
			if (comma)
				sb.append(',');
			sb.append(value);
		}

		private void addString(StringBuffer sb, String value, boolean comma) {
			if (comma)
				sb.append(',');
			if (value == null) {
				sb.append("''"); //$NON-NLS-1$
			} else {
				sb.append('\'');
				sb.append(value.replaceAll("'", "''")); //$NON-NLS-1$ //$NON-NLS-2$
				sb.append('\'');
			}
		}

		private IStatus close() {
			try {
				System.out.println("Shutting down database"); //$NON-NLS-1$
				connection = DriverManager.getConnection(JDBC_DERBY + location
						+ DISCONNECT_OPTIONS);
				connection.close();
			} catch (SQLException e) {
				// Should throw ERROR 08006 which we will ignore.
			}
			return Status.OK_STATUS;
		}

		/**
		 * Composes an Item instance from the result set but leaves out the
		 * description field.
		 * 
		 * @param rs
		 * @return
		 * @throws SQLException
		 */
		private Article composeItem(ResultSet rs) throws SQLException {
			Article item = new Article();
			item.setUUID(UUID.fromString(rs.getString(1)));
			item.setParentUUID(UUID.fromString(rs.getString(2)));
			item.setFeedUUID(UUID.fromString(rs.getString(3)));
			item.setGuid(rs.getString(4));
			item.setTitle(rs.getString(5));
			item.setLink(rs.getString(6));
			item.setMarks(decode(rs.getString(7)));
			item.setRead(rs.getInt(8) != 0);
			item.setPublicationDate(rs.getLong(9));
			item.setReadDate(rs.getLong(10));
			item.setAddedDate(rs.getLong(11));
			item.setCreator(rs.getString(13));
			return item;
		}

		private void deleteCategory(FeedCategory category) {
			try {
				Statement s = connection.createStatement();
				s.executeUpdate("delete from categories where uuid='" //$NON-NLS-1$
						+ category.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from articles where parent_uuid='" //$NON-NLS-1$
						+ category.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from categories where parent_uuid='" //$NON-NLS-1$
						+ category.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from feeds where parent_uuid='" //$NON-NLS-1$
						+ category.getUUID().toString() + "'"); //$NON-NLS-1$
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void deleteFeed(Feed feed) {
			try {
				Statement s = connection.createStatement();
				s.executeUpdate("delete from feeds where uuid='" //$NON-NLS-1$
						+ feed.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from articles where parent_uuid='" //$NON-NLS-1$
						+ feed.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from categories where parent_uuid='" //$NON-NLS-1$
						+ feed.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from feeds where parent_uuid='" //$NON-NLS-1$
						+ feed.getUUID().toString() + "'"); //$NON-NLS-1$
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private Article deleteItem(Article item) {
			Article deleted = getItem(item.getGuid());
			try {
				Statement s = connection.createStatement();
				String query = "delete from articles where uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"; //$NON-NLS-1$
				s.executeUpdate(query);
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return deleted;
		}

		/**
		 * Deletes all articles from the specified feed which publication date
		 * is order than the given date.
		 * 
		 * @param feed
		 *            The feed to delete articles from
		 * @param date
		 *            The limit date
		 */
		private void deleteOutdated(Feed feed, long date) {
			try {
				executeUpdate("delete from articles where feed_uuid='" //$NON-NLS-1$
						+ feed.getUUID().toString()
						+ "' and is_read=1" //$NON-NLS-1$
						+ " and ((publication_date>0 and publication_date<=" + date //$NON-NLS-1$
						+ ") or (publication_date=0 and added_date<=" + date + "))"); //$NON-NLS-1$ //$NON-NLS-2$
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private void executeUpdate(String query) throws SQLException {
			Statement s = connection.createStatement();
			s.executeUpdate(query);
			s.close();
		}

		/**
		 * 
		 * @param feed
		 * @param keep
		 */
		private void keep(Feed feed, int keep) {
			try {
				Statement s = connection.createStatement();
				String query = "select * from articles where feed_uuid='" //$NON-NLS-1$
						+ feed.getUUID().toString()
						+ "' order by added_date desc"; //$NON-NLS-1$
				ResultSet rs = s.executeQuery(query);
				int count = 0;
				// Browse through the ones that we want to keep
				while (rs.next() && count < keep) {
					// Do nothing.
				}
				// And delete the rest
				while (rs.next()) {
					Article item = composeItem(rs);
					deleteItem(item);
				}
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Retrieves all the child articles of the given parent node. If the
		 * node is <i>null</i>; all registries are returned. if it's a
		 * registry; categories and feeds are returned, if it's a category;
		 * categories and feeds are returned. And if it's a feed; feed articles
		 * are returned.
		 * 
		 * @param parent
		 *            The parent item
		 * @return An array of aggregator articles
		 */
		private IAggregatorItem[] getChildren(IAggregatorItem item) {
			Assert.isNotNull(item);
			ArrayList<IAggregatorItem> feeds = new ArrayList<IAggregatorItem>();
			try {
				selectCategories(item, feeds);
				selectFeeds(item, feeds);
				selectItems(item, feeds);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return feeds.toArray(new IAggregatorItem[feeds.size()]);
		}

		/**
		 * 
		 * 
		 * @param guid
		 *            The globally unique identifier
		 * @return The FeedItem or <i>null</i>
		 */
		private Article getItem(String guid) {
			try {
				Statement s = connection.createStatement();
				String query = "select * from articles where guid='" //$NON-NLS-1$
						+ guid + "'"; //$NON-NLS-1$
				ResultSet rs = s.executeQuery(query);
				if (rs.next()) {
					return composeItem(rs);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see no.resheim.aggregator.internal.IRegistryExternalizer#open(java.lang.String)
		 */
		private IStatus open(String path) {
			this.location = AggregatorPlugin.getDefault().getConfigDir()
					.getAbsolutePath()
					+ DB_NAME;
			System.out.println("Opening database at " + this.location); //$NON-NLS-1$
			try {
				Class.forName(DB_DRIVER).newInstance();
				connection = DriverManager.getConnection(JDBC_DERBY + location
						+ CONNECT_OPTIONS);
				if (new File(location).exists()) {
					createTables();
				}
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return Status.OK_STATUS;
		}

		/**
		 * 
		 * @param item
		 *            The parent item
		 * @param feeds
		 *            The list of aggregator articles to append to
		 * @throws SQLException
		 */
		private void selectCategories(IAggregatorItem item,
				ArrayList<IAggregatorItem> feeds) throws SQLException {
			Statement s = connection.createStatement();
			String query = null;
			query = "select * from categories where parent_uuid='" //$NON-NLS-1$
					+ item.getUUID().toString() + "'"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			while (rs.next()) {
				FeedCategory folder = new FeedCategory(UUID.fromString(rs
						.getString(1)), UUID.fromString(rs.getString(2)), rs
						.getString(3));
				folder.setParent(item);
				folder.setRegistry(FeedRegistry.this);
				feeds.add(folder);
			}
		}

		/**
		 * 
		 * @param parent
		 *            The parent item
		 * @param feeds
		 *            The list of aggregator articles to append to
		 * @throws SQLException
		 */
		private void selectFeeds(IAggregatorItem parent,
				ArrayList<IAggregatorItem> feeds) throws SQLException {
			Statement s = connection.createStatement();
			String query = null;
			query = "select * from feeds where parent_uuid='" //$NON-NLS-1$
					+ parent.getUUID().toString() + "'"; //$NON-NLS-1$

			ResultSet rs = s.executeQuery(query);
			while (rs.next()) {
				Feed f = composeFeed(rs);
				f.setParent(parent);
				f.setRegistry(FeedRegistry.this);
				feeds.add(f);
			}
		}

		private HashMap<UUID, Feed> initializeFeeds() {
			HashMap<UUID, Feed> feeds = new HashMap<UUID, Feed>();
			try {
				Statement s = connection.createStatement();
				ResultSet rs = s.executeQuery("select * from feeds"); //$NON-NLS-1$
				while (rs.next()) {
					Feed f = composeFeed(rs);
					f.setParent(FeedRegistry.this);
					feeds.put(f.getUUID(), f);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return feeds;
		}

		private Feed composeFeed(ResultSet rs) throws SQLException {
			Feed feed = new Feed();
			feed.setUUID(UUID.fromString(rs.getString(1)));
			feed.setParentUUID(UUID.fromString(rs.getString(2)));
			feed.setTitle(rs.getString(3));
			feed.setURL(rs.getString(4));
			feed.setMarks(decode(rs.getString(5)));
			feed.setArchiving(Archiving.valueOf(rs.getString(6)));
			feed.setArchivingItems(rs.getInt(7));
			feed.setArchivingDays(rs.getInt(8));
			feed.setUpdateInterval(rs.getInt(9));
			feed.setUpdatePeriod(UpdatePeriod.valueOf(rs.getString(10)));
			feed.setLastUpdate(rs.getLong(11));
			feed.setDescription(rs.getString(12));
			feed.setLink(rs.getString(13));
			feed.setWebmaster(rs.getString(14));
			feed.setEditor(rs.getString(15));
			feed.setCopyright(rs.getString(16));
			feed.setType(rs.getString(17));
			feed.setHidden(rs.getInt(18) != 0);
			return feed;
		}

		private EnumSet<Mark> decode(String markString) {
			EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);
			for (String mark : markString.split(",")) { //$NON-NLS-1$
				if (mark.trim().length() > 0)
					marks.add(Mark.valueOf(mark));
			}
			return marks;
		}

		private String encode(EnumSet<Mark> marks) {
			StringBuffer sb = new StringBuffer();
			for (Mark mark : marks) {
				sb.append(mark.toString());
				sb.append(',');
			}
			return sb.toString();
		}

		/**
		 * 
		 * @param item
		 *            The parent item
		 * @param feeds
		 *            The list of aggregator articles to append to
		 * @throws SQLException
		 */
		private void selectItems(IAggregatorItem parent,
				ArrayList<IAggregatorItem> feeds) {
			try {
				Statement s = connection.createStatement();
				String query = "select * from articles where parent_uuid='" //$NON-NLS-1$
						+ parent.getUUID() + "'"; //$NON-NLS-1$
				ResultSet rs = s.executeQuery(query);
				while (rs.next()) {
					Article i = composeItem(rs);
					i.setParent(parent);
					i.setRegistry(FeedRegistry.this);
					feeds.add(i);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		private int selectItemCount(Feed feed) {
			try {
				Statement s = connection.createStatement();
				String query = "select count(title) from articles where parent_uuid='" //$NON-NLS-1$
						+ feed.getUUID() + "' and is_read=0"; //$NON-NLS-1$
				ResultSet rs = s.executeQuery(query);
				if (rs.next())
					return rs.getInt(1);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return 0;
		}

		private String selectDescription(Article item) {
			try {
				Statement s = connection.createStatement();
				String query = "select description from articles where guid='" //$NON-NLS-1$
						+ item.getGuid() + "'"; //$NON-NLS-1$
				ResultSet rs = s.executeQuery(query);
				if (rs.next()) {
					return rs.getString(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return null;

		}

		private void createTables() throws SQLException {
			System.out.println("Creating tables"); //$NON-NLS-1$

			InputStream is = AggregatorDatabase.class
					.getResourceAsStream(TABLES_SQL);
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			Statement s = connection.createStatement();
			StringBuffer create = new StringBuffer();
			String in = null;
			try {
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
			} catch (IOException e) {
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
						.prepareStatement("insert into feeds values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) "); //$NON-NLS-1$
				ps.setString(1, feed.getUUID().toString());
				ps.setString(2, feed.getParentUUID().toString());
				ps.setString(3, feed.getTitle()); // title
				ps.setString(4, feed.getURL()); // url;
				ps.setString(5, encode(feed.getMarks())); // marks
				ps.setString(6, feed.getArchiving().toString()); // archiving
				ps.setInt(7, feed.getArchivingItems()); // archiving_items
				ps.setInt(8, feed.getArchivingDays()); // archiving_days
				ps.setInt(9, feed.getUpdateInterval()); // update_interval
				ps.setString(10, feed.getUpdatePeriod().toString()); // update_period
				ps.setLong(11, feed.getLastUpdate()); // last_update
				ps.setString(12, feed.getDescription()); // description
				ps.setString(13, feed.getLink()); // link
				ps.setString(14, feed.getWebmaster()); // webmaster
				ps.setString(15, feed.getEditor()); // editor
				ps.setString(16, feed.getCopyright()); // copyright
				ps.setString(17, feed.getType()); // feed_type
				ps.setInt(18, feed.isHidden() ? 1 : 0);
				ps.executeUpdate();
				ps.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Updates the database with feed data.
		 * 
		 * @param feed
		 */
		private void updateFeed(Feed feed) {
			try {
				StringBuffer sb = new StringBuffer();
				sb.append("update feeds set description="); //$NON-NLS-1$
				addString(sb, feed.getDescription(), false);
				sb.append(", link="); //$NON-NLS-1$
				addString(sb, feed.getLink(), false);
				sb.append(", title="); //$NON-NLS-1$
				addString(sb, feed.getTitle(), false);
				sb.append(", webmaster="); //$NON-NLS-1$
				addString(sb, feed.getWebmaster(), false);
				sb.append(", editor="); //$NON-NLS-1$
				addString(sb, feed.getEditor(), false);
				sb.append(", copyright="); //$NON-NLS-1$
				addString(sb, feed.getCopyright(), false);
				sb.append(", feed_type="); //$NON-NLS-1$
				addString(sb, feed.getType(), false);
				sb.append(", last_update="); //$NON-NLS-1$
				addLong(sb, feed.getLastUpdate(), false);
				sb.append(", marks="); //$NON-NLS-1$
				addString(sb, encode(feed.getMarks()), false);
				sb.append(", archiving="); //$NON-NLS-1$
				addString(sb, feed.getArchiving().toString(), false);
				sb.append(", archiving_items="); //$NON-NLS-1$
				addInt(sb, feed.getArchivingItems(), false);
				sb.append(", archiving_days="); //$NON-NLS-1$
				addInt(sb, feed.getArchivingDays(), false);
				sb.append(", update_interval="); //$NON-NLS-1$
				addInt(sb, feed.getUpdateInterval(), false);
				sb.append(", update_period="); //$NON-NLS-1$
				addString(sb, feed.getUpdatePeriod().toString(), false);
				sb.append(" where uuid="); //$NON-NLS-1$
				addString(sb, feed.getUUID().toString(), false);
				executeUpdate(sb.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * Updates the database to indicate that the aggregator item has a new
		 * parent.
		 * 
		 * @param item
		 *            The moved item
		 * @param newParent
		 *            The new parent
		 */
		private void move(IAggregatorItem item, IAggregatorItem newParent) {
			try {
				if (item instanceof Feed) {
					Statement s = connection.createStatement();
					s.executeUpdate("update feeds set parent_uuid='" //$NON-NLS-1$
							+ newParent.getUUID().toString() + "' where uuid='" //$NON-NLS-1$
							+ item.getUUID().toString() + "'"); //$NON-NLS-1$
					s.close();

				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

		/**
		 * Inserts a feed item into the database.
		 * 
		 * @param item
		 *            The item to insert.
		 */
		private void insert(Article item) {
			Assert.isTrue(item.isValid());
			StringBuffer sb = new StringBuffer();
			sb.append("insert into articles values ("); //$NON-NLS-1$
			addString(sb, item.getUUID().toString(), false);
			addString(sb, item.getParentUUID().toString(), true);
			addString(sb, item.getFeedUUID().toString(), true);
			addString(sb, item.getGuid(), true);
			addString(sb, item.getTitle(), true);
			addString(sb, item.getLink(), true);
			addString(sb, encode(item.getMarks()), true);
			sb.append(',');
			sb.append(item.isRead() ? 1 : 0);
			addLong(sb, item.getPublicationDate(), true);
			addLong(sb, item.getReadDate(), true);
			addLong(sb, item.getAdded(), true);
			addString(sb, item.getDescription(), true);
			if (item.getCreator() != null)
				addString(sb, item.getCreator(), true);
			else
				addNull(sb, true);
			sb.append(")"); //$NON-NLS-1$
			try {
				Statement s = connection.createStatement();
				s.setEscapeProcessing(true);
				s.execute(sb.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Inserts a feed item into the database.
		 * 
		 * @param item
		 *            The item to insert.
		 */
		private void insert(FeedCategory category) {
			StringBuffer sb = new StringBuffer();
			sb.append("insert into categories values ("); //$NON-NLS-1$
			addString(sb, category.getUUID().toString(), false);
			addString(sb, category.getParentUUID().toString(), true);
			addString(sb, category.getTitle(), true);
			addString(sb, encode(category.getMarks()), true);
			sb.append(")"); //$NON-NLS-1$
			try {
				Statement s = connection.createStatement();
				s.setEscapeProcessing(true);
				s.execute(sb.toString());
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Indicates that the feed item has been read.
		 * 
		 * @param item
		 *            The item to update
		 */
		private void updateReadFlag(Article item) {
			try {
				Statement s = connection.createStatement();
				s.setEscapeProcessing(true);
				s.executeUpdate("update articles set is_read=1 where guid='" //$NON-NLS-1$
						+ item.getGuid() + "'"); //$NON-NLS-1$
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Renames the given item.
		 * 
		 * @param item
		 */
		private void rename(IAggregatorItem item) {
			String query = null;
			if (item instanceof FeedCategory) {
				query = "update categories set title='" + item.getTitle() //$NON-NLS-1$
						+ "' where uuid='" + item.getUUID().toString() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (item instanceof Feed) {
				query = "update feeds set title='" + item.getTitle() //$NON-NLS-1$
						+ "' where uuid='" + item.getUUID().toString() + "'"; //$NON-NLS-1$ //$NON-NLS-2$
			}
			if (query != null) {
				try {
					Statement s = connection.createStatement();
					s.setEscapeProcessing(true);
					s.executeUpdate(query);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static final UUID DEFAULT_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	private AggregatorDatabase database;

	/** The list of feed change listeners */
	private static ArrayList<FeedListener> feedListeners = new ArrayList<FeedListener>();

	private UUID uuid;
	/**
	 * List of <i>live</i> feeds that we must keep track of even if the any
	 * viewers has not opened the feed for viewing so that it has had a chance
	 * of being created. This list is populated at startup and maintained
	 * thereafter.
	 */
	private HashMap<UUID, Feed> sites;

	public FeedRegistry() {
		this(DEFAULT_ID);
	}

	public FeedRegistry(UUID id) {
		this.uuid = id;
		openDatabase();
		sites = database.initializeFeeds();
		// Start a new update job that will periodically wake up and create
		// FeedUpdateJobs when a feed is scheduled for an update.
		final RegistryUpdateJob job = new RegistryUpdateJob(this);
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				job.schedule(30000);
			}
		});
		job.schedule();
	}

	public void shutdown() {
		database.close();
	}

	/**
	 * Adds a new feed to the database and immediately stores it's data in the
	 * persistent storage.
	 * 
	 * @param feed
	 *            The feed to add
	 */
	public void add(IAggregatorItem item) {
		try {
			if (item instanceof Feed) {
				Feed feed = (Feed) item;
				sites.put(feed.getUUID(), feed);
				database.insert(feed);
				feed.setRegistry(this);
				// Schedule an update immediately
				FeedUpdateJob j = new FeedUpdateJob(this, feed);
				j.schedule();
			} else if (item instanceof FeedCategory) {
				FeedCategory folder = (FeedCategory) item;
				folder.setRegistry(this);
				database.insert(folder);
			} else if (item instanceof Article) {
				Article feedItem = (Article) item;
				feedItem.setAddedDate(System.currentTimeMillis());
				feedItem.setRegistry(this);
				database.insert(feedItem);
			}
			notifyListerners(new AggregatorItemChangedEvent(item,
					FeedChangeEventType.CREATED));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void move(IAggregatorItem item, IAggregatorItem newParent) {
		database.move(item, newParent);
	}

	/**
	 * Renames the given aggregator item, but does not fire an event.
	 * 
	 * @param item
	 */
	public void rename(IAggregatorItem item) {
		database.rename(item);
	}

	/**
	 * Updates the feed data in the persistent storage. Should only be called by
	 * {@link FeedUpdateJob} after the feed has be updated with new information.
	 * 
	 * @param feed
	 *            The feed to update
	 */
	void feedUpdated(Feed feed) {
		database.updateFeed(feed);
	}

	public void updateFeedData(IAggregatorItem item) {
		if (item instanceof Feed) {
			// Ensure that the local list has a copy of the same instance.
			sites.put(item.getUUID(), (Feed) item);
			database.updateFeed((Feed) item);
		}
	}

	/**
	 * 
	 * @param item
	 * @return
	 */
	public IAggregatorItem[] getChildren(IAggregatorItem item) {
		return database.getChildren(item);
	}

	public String getFeedId() {
		return null;
	}

	/**
	 * Returns the complete list of feeds that this registry is maintaining.
	 * Note
	 * 
	 * @return The list of feeds
	 */
	public Collection<Feed> getFeeds() {
		return sites.values();
	}

	public void updateAllFeeds() {
		for (Feed feed : getFeeds()) {
			updateFeed(feed);
		}
	}

	public void updateFeed(Feed feed) {
		if (!feed.isUpdating()) {
			FeedUpdateJob job = new FeedUpdateJob(this, feed);
			job.schedule();
		}
	}

	/**
	 * Tests if the feed exists in the repository.
	 * 
	 * @return <b>True</b> if the feed exists.
	 */
	public boolean hasFeed(String url) {
		return sites.containsKey(url);
	}

	/**
	 * Returns the number of <b>unread</b> articles.
	 * 
	 * @param element
	 * @return
	 */
	public int getItemCount(IAggregatorItem element) {
		if (element instanceof Feed) {
			return database.selectItemCount(((Feed) element));
		}
		return -1;
	}

	public UUID getUUID() {
		return uuid;
	}

	public String getDescription(Article item) {
		return database.selectDescription(item);
	}

	public void setRead(Article item) {
		item.setRead(true);
		database.updateReadFlag(item);
		notifyListerners(new AggregatorItemChangedEvent(item,
				FeedChangeEventType.READ));
	}

	/**
	 * Tests to see if the item already exists in the database. If this is the
	 * case <i>true</i> is returned. This method relies on the globally unique
	 * identifier of the feed item.
	 * 
	 * @param item
	 * @return
	 */
	public boolean hasArticle(Article item) {
		Assert.isNotNull(item.getGuid());
		if (database.getItem(item.getGuid()) != null) {
			return true;
		}
		return false;
	}

	/**
	 * Opens the registry database.
	 */
	private void openDatabase() {
		File db = new File(AggregatorPlugin.getDefault().getConfigDir()
				.getAbsolutePath()
				+ File.separator + uuid);
		database = new AggregatorDatabase();
		database.open(db.getAbsolutePath());
	}

	/**
	 * Removes the specified item from the database.
	 * 
	 * @param element
	 *            The element to be removed.
	 */
	public void remove(IAggregatorItem element) {
		if (element instanceof Feed) {
			sites.remove(((Feed) element).getUUID());
			database.deleteFeed((Feed) element);
			notifyListerners(new AggregatorItemChangedEvent(element,
					FeedChangeEventType.REMOVED));

		}
		if (element instanceof Article) {
			database.deleteItem((Article) element);
			notifyListerners(new AggregatorItemChangedEvent(element,
					FeedChangeEventType.REMOVED));
		}
		if (element instanceof FeedCategory) {
			database.deleteCategory((FeedCategory) element);
			notifyListerners(new AggregatorItemChangedEvent(element,
					FeedChangeEventType.REMOVED));
		}
	}

	/** The number of milliseconds in a day */
	private final long DAY = 86400000;

	/**
	 * Uses the archiving rules of the site to remove articles from the feed.
	 * Should only be called after a FeedUpdateJob has been executed.
	 * 
	 * @param site
	 */
	void cleanUp(Feed site) {
		Archiving archiving = site.getArchiving();
		int days = site.getArchivingDays();
		int articles = site.getArchivingItems();
		switch (archiving) {
		case KEEP_ALL:
			break;
		case KEEP_NEWEST:
			long lim = System.currentTimeMillis() - ((long) days * DAY);
			database.deleteOutdated(site, lim);
			break;
		case KEEP_SOME:
			database.keep(site, articles);
			break;
		default:
			break;
		}
	}

	/**
	 * The feed registry does not have a parent so this method will return
	 * <b>null</b>.
	 * 
	 * @return The parent (null).
	 */
	public IAggregatorItem getParent() {
		return null;
	}

	/**
	 * Add listener to be notified about feed changes. The added listener will
	 * be notified when feeds are added, removed and when their contents has
	 * changed.
	 * 
	 * @param listener
	 *            The listener to be notified
	 */
	public void addFeedListener(FeedListener listener) {
		feedListeners.add(listener);
	}

	public void removeFeedListener(FeedListener listener) {
		feedListeners.remove(listener);
	}

	/**
	 * Notify feed listeners about the feed change. If the feed change was
	 * caused by a update, a new update is scheduled.
	 * 
	 * @param event
	 *            The feed change event with details
	 */
	public void notifyListerners(AggregatorItemChangedEvent event) {
		System.out.println(event);
		for (FeedListener listener : feedListeners) {
			listener.feedChanged(event);
		}
	}

	public FeedRegistry getRegistry() {
		return this;
	}

	public void setTitle(String title) {
	}

	public String getTitle() {
		return null;
	}
}
