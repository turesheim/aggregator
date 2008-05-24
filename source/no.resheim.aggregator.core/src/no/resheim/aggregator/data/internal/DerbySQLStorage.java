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
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.IAggregatorStorage;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCategory;
import no.resheim.aggregator.data.FeedRegistry;
import no.resheim.aggregator.data.IAggregatorItem;
import no.resheim.aggregator.data.AbstractAggregatorItem.Mark;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DerbySQLStorage implements IAggregatorStorage {
	/** Connection options */
	private static final String CONNECT_OPTIONS = ";create=true"; //$NON-NLS-1$

	/** Database driver */
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$

	/** Disconnection options */
	private static final String DISCONNECT_OPTIONS = ";shutdown=true"; //$NON-NLS-1$

	private static final String JDBC_DERBY = "jdbc:derby:"; //$NON-NLS-1$

	/** Name of the SQL file used to create the tables */
	private static final String TABLES_SQL = "tables.sql"; //$NON-NLS-1$

	private Connection connection;

	private FeedRegistry registry;

	private IPath path;

	/**
	 * 
	 */
	public DerbySQLStorage(FeedRegistry registry, IPath path) {
		super();
		this.registry = registry;
		this.path = path;
	}

	private void addInt(StringBuffer sb, long value, boolean comma) {
		if (comma)
			sb.append(',');
		sb.append(value);
	}

	private void addLong(StringBuffer sb, long value, boolean comma) {
		if (comma)
			sb.append(',');
		sb.append(value);
	}

	private void addNull(StringBuffer sb, boolean comma) {
		if (comma)
			sb.append(',');
		sb.append("NULL"); //$NON-NLS-1$
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.IAggregatorStorage#shutdown()
	 */
	public IStatus shutdown() {
		try {
			System.out.println("Shutting down database"); //$NON-NLS-1$
			connection = DriverManager.getConnection(JDBC_DERBY
					+ path.toOSString() + DISCONNECT_OPTIONS);
			connection.close();
		} catch (SQLException e) {
			// Should throw ERROR 08006 which we will ignore.
		}
		return Status.OK_STATUS;
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

	private IStatus createTables() throws SQLException {
		System.out.println("Creating tables"); //$NON-NLS-1$
		InputStream is = DerbySQLStorage.class.getResourceAsStream(TABLES_SQL);
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
			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		}
	}

	private EnumSet<Mark> decode(String markString) {
		EnumSet<Mark> marks = EnumSet.noneOf(Mark.class);
		for (String mark : markString.split(",")) { //$NON-NLS-1$
			if (mark.trim().length() > 0)
				marks.add(Mark.valueOf(mark));
		}
		return marks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#delete(no.resheim.aggregator.model.IAggregatorItem)
	 */
	public void delete(IAggregatorItem item) {
		if (item instanceof FeedCategory) {
			try {
				Statement s = connection.createStatement();
				s.executeUpdate("delete from categories where uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from articles where parent_uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from categories where parent_uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from feeds where parent_uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (item instanceof Feed) {
			try {
				Statement s = connection.createStatement();
				s.executeUpdate("delete from feeds where uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from articles where parent_uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from categories where parent_uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.executeUpdate("delete from feeds where parent_uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"); //$NON-NLS-1$
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		if (item instanceof Article) {
			try {
				Statement s = connection.createStatement();
				String query = "delete from articles where uuid='" //$NON-NLS-1$
						+ item.getUUID().toString() + "'"; //$NON-NLS-1$
				s.executeUpdate(query);
				s.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#deleteOutdated(no.resheim.aggregator.model.Feed,
	 *      long)
	 */
	public void deleteOutdated(Feed feed, long date) {
		try {
			executeUpdate("delete from articles where feed_uuid='" //$NON-NLS-1$
					+ feed.getUUID().toString() + "' and is_read=1" //$NON-NLS-1$
					+ " and ((publication_date>0 and publication_date<=" + date //$NON-NLS-1$
					+ ") or (publication_date=0 and added_date<=" + date + "))"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private String encode(EnumSet<Mark> marks) {
		StringBuffer sb = new StringBuffer();
		for (Mark mark : marks) {
			sb.append(mark.toString());
			sb.append(',');
		}
		return sb.toString();
	}

	private void executeUpdate(String query) throws SQLException {
		Statement s = connection.createStatement();
		s.executeUpdate(query);
		s.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#getChildren(no.resheim.aggregator.model.IAggregatorItem)
	 */
	public IAggregatorItem[] getChildren(IAggregatorItem item) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#getItem(java.lang.String)
	 */
	public Article getItem(String guid) {
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
	 * @see no.resheim.aggregator.model.IAggregatorStorage#initializeFeeds()
	 */
	public HashMap<UUID, Feed> initializeFeeds() {
		HashMap<UUID, Feed> feeds = new HashMap<UUID, Feed>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery("select * from feeds"); //$NON-NLS-1$
			while (rs.next()) {
				Feed f = composeFeed(rs);
				f.setParent(registry);
				feeds.put(f.getUUID(), f);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return feeds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#add(no.resheim.aggregator.model.IAggregatorItem)
	 */
	public void add(IAggregatorItem item) {
		if (item instanceof Article)
			insert((Article) item);
		if (item instanceof FeedCategory)
			insert((FeedCategory) item);
		if (item instanceof Feed)
			insert((Feed) item);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#keepMaximum(no.resheim.aggregator.model.Feed,
	 *      int)
	 */
	public void keepMaximum(Feed feed, int keep) {
		try {
			Statement s = connection.createStatement();
			String query = "select * from articles where feed_uuid='" //$NON-NLS-1$
					+ feed.getUUID().toString() + "' order by added_date desc"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			int count = 0;
			// Browse through the ones that we want to keep
			while (rs.next() && count < keep) {
				// Do nothing.
			}
			// And delete the rest
			while (rs.next()) {
				Article item = composeItem(rs);
				delete(item);
			}
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#move(no.resheim.aggregator.model.IAggregatorItem,
	 *      no.resheim.aggregator.model.IAggregatorItem)
	 */
	public void move(IAggregatorItem item, IAggregatorItem newParent) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.internal.IRegistryExternalizer#open(java.lang.String)
	 */
	public IStatus startup(IProgressMonitor monitor) {
		System.out.println("Opening database at " + path.toOSString()); //$NON-NLS-1$
		try {
			Class.forName(DB_DRIVER).newInstance();
			connection = DriverManager.getConnection(JDBC_DERBY
					+ path.toOSString() + CONNECT_OPTIONS);
			DatabaseMetaData metadata = connection.getMetaData();
			ResultSet rs = metadata.getTables(null, "APP", "FEEDS", null); //$NON-NLS-1$ //$NON-NLS-2$
			if (!rs.next()) {
				return createTables();
			}
		} catch (Exception e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#rename(no.resheim.aggregator.model.IAggregatorItem)
	 */
	public void rename(IAggregatorItem item) {
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
			folder.setRegistry(registry);
			feeds.add(folder);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#selectDescription(no.resheim.aggregator.model.Article)
	 */
	public String getDescription(Article item) {
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
			f.setRegistry(registry);
			feeds.add(f);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#selectItemCount(no.resheim.aggregator.model.Feed)
	 */
	public int getUnreadCount(Feed feed) {
		int count = 0;
		try {
			Statement s = connection.createStatement();
			String query = "select count(title) from articles where parent_uuid='" //$NON-NLS-1$
					+ feed.getUUID() + "' and is_read=0"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			if (rs.next())
				count += rs.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
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
				i.setRegistry(registry);
				feeds.add(i);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#updateFeed(no.resheim.aggregator.model.Feed)
	 */
	public void updateFeed(Feed feed) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#updateReadFlag(no.resheim.aggregator.model.Article)
	 */
	public void updateReadFlag(IAggregatorItem item) {
		try {
			Statement s = connection.createStatement();
			s.setEscapeProcessing(true);
			if (item instanceof Article) {
				s.executeUpdate("update articles set is_read=1 where uuid='" //$NON-NLS-1$
						+ item.getUUID() + "'"); //$NON-NLS-1$
			} else if (item instanceof FeedCategory || item instanceof Feed) {
				s
						.executeUpdate("update articles set is_read=1 where parent_uuid='" //$NON-NLS-1$
								+ item.getUUID() + "'"); //$NON-NLS-1$				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
