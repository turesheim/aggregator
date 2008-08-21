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
import java.sql.Types;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.data.AggregatorUIItem;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.IAggregatorItem;
import no.resheim.aggregator.data.ParentingAggregatorItem;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Aggregator storage that uses the Derby embedded database to store it's data.
 * The only instantiated items are those created on request in order to keep the
 * memory use low.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DerbySQLStorage extends AbstractAggregatorStorage {

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

	/**
	 * 
	 */
	public DerbySQLStorage(FeedCollection registry, IPath path) {
		super(registry, path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.IAggregatorStorage#shutdown()
	 */
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

	private Feed composeFeed(ResultSet rs) throws SQLException {
		Feed feed = new Feed();
		feed.setUUID(UUID.fromString(rs.getString(1)));
		feed.setTitle(rs.getString(2));
		feed.setLocation(UUID.fromString(rs.getString(3)));
		feed.setURL(rs.getString(4));
		feed.setArchiving(Archiving.valueOf(rs.getString(5)));
		feed.setArchivingItems(rs.getInt(6));
		feed.setArchivingDays(rs.getInt(7));
		feed.setUpdateInterval(rs.getInt(8));
		feed.setUpdatePeriod(UpdatePeriod.valueOf(rs.getString(9)));
		feed.setLastUpdate(rs.getLong(10));
		feed.setDescription(rs.getString(11));
		feed.setLink(rs.getString(12));
		feed.setWebmaster(rs.getString(13));
		feed.setEditor(rs.getString(14));
		feed.setCopyright(rs.getString(15));
		feed.setType(rs.getString(16));
		feed.setHidden(rs.getInt(17) != 0);
		feed.setAnonymousAccess(rs.getInt(18) != 0);
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
	private InternalArticle composeArticle(ParentingAggregatorItem parent,
			ResultSet rs) throws SQLException {
		InternalArticle item = new InternalArticle(parent, UUID.fromString(rs
				.getString(1)), UUID.fromString(rs.getString(4)));
		item.setOrdering(rs.getInt(3));
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

	/**
	 * Creates a new folder instance from the data found in the supplied result
	 * set.
	 * 
	 * @param parent
	 *            the parent item
	 * @param rs
	 *            the result set
	 * @return a {@link Folder} instance composed from the result set
	 * @throws SQLException
	 */
	private AggregatorUIItem composeFolder(ParentingAggregatorItem parent,
			ResultSet rs) throws SQLException {
		InternalFolder item = new InternalFolder(parent, UUID.fromString(rs
				.getString(1)));
		item.setParent(parent);
		item.setOrdering(rs.getInt(3));
		if (rs.getString(4) != null) {
			item.setFeed(UUID.fromString(rs.getString(4)));
		}
		item.setHidden(rs.getInt(5) != 0);
		item.setTitle(rs.getString(6));
		item.setMarks(decode(rs.getString(7)));
		return item;
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
			// Create a folder to represent the collection root. This is
			// required for maintaining relation integrity.
			Folder root = new InternalFolder(null, collection.getUUID());
			root.setTitle("ROOT"); //$NON-NLS-1$
			root.setHidden(true);
			insert(root);

			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#delete(no.resheim.aggregator
	 * .model.IAggregatorItem)
	 */
	public void delete(IAggregatorItem item) {
		if (item instanceof Folder) {
			try {
				Statement s = connection.createStatement();
				s.executeUpdate("delete from folders where uuid='" //$NON-NLS-1$
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
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#deleteOutdated(no.resheim
	 * .aggregator.model.Feed, long)
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

	private void executeUpdate(String query) throws SQLException {
		Statement s = connection.createStatement();
		s.executeUpdate(query);
		s.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.AggregatorStorage#getChildren(no.resheim
	 * .aggregator.model.AggregatorItem)
	 */
	public AggregatorUIItem[] getChildren(ParentingAggregatorItem item) {
		Assert.isNotNull(item);
		ArrayList<AggregatorUIItem> items = new ArrayList<AggregatorUIItem>();
		try {
			selectFolders(item, items);
			selectItems(item, items);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items.toArray(new AggregatorUIItem[items.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.IAggregatorStorage#getChildCount(no.resheim.aggregator
	 * .data.IAggregatorItem)
	 */
	public synchronized int getChildCount(ParentingAggregatorItem parent) {
		UUID parentID = parent.getUUID();
		return getChildCount(parentID);
	}

	private int getChildCount(UUID parentID) {
		int count = 0;
		try {
			if (connection.isClosed())
				return 0;
			Statement s = connection.createStatement();
			ResultSet rs = s
					.executeQuery("select count(uuid) from articles where parent_uuid='" //$NON-NLS-1$
							+ parentID.toString() + "'"); //$NON-NLS-1$
			if (rs.next())
				count += rs.getInt(1);
			rs.close();
			rs = s
					.executeQuery("select count(uuid) from folders where parent_uuid='" //$NON-NLS-1$
							+ parentID.toString() + "'"); //$NON-NLS-1$
			if (rs.next())
				count += rs.getInt(1);
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#getItem(java.lang.String)
	 */
	public boolean hasArticle(String guid) {
		boolean found = false;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.IAggregatorStorage#initializeFeeds()
	 */
	public HashMap<UUID, Feed> getFeeds() {
		HashMap<UUID, Feed> feeds = new HashMap<UUID, Feed>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery("select * from feeds"); //$NON-NLS-1$
			while (rs.next()) {
				Feed f = composeFeed(rs);
				feeds.put(f.getUUID(), f);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return feeds;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#add(no.resheim.aggregator
	 * .model.IAggregatorItem)
	 */
	public IStatus add(IAggregatorItem item) {
		try {
			if (item instanceof Article) {
				// Set the order of the item
				((Article) item).setOrdering(getChildCount(((Article) item)
						.getLocation()));
				insert((Article) item);
			}
			if (item instanceof Folder) {
				((AggregatorUIItem) item)
						.setOrdering(getChildCount(((AggregatorUIItem) item)
								.getParent()));
				insert((Folder) item);
			}
			if (item instanceof Feed)
				insert((Feed) item);
			return Status.OK_STATUS;
		} catch (SQLException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.DerbySQLStorage_StoreError,
							item), e);
		}
	}

	/**
	 * Inserts a feed item into the database.
	 * 
	 * @param item
	 *            The item to insert.
	 * @throws SQLException
	 */
	private void insert(Article item) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into articles values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, item.getUUID().toString());
		ps.setString(2, item.getLocation().toString());
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
	}

	/**
	 * Inserts a new feed into the database.
	 * 
	 * @param feed
	 *            The feed to insert
	 * @throws SQLException
	 */
	private void insert(Feed feed) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into feeds values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, feed.getUUID().toString());
		ps.setString(2, feed.getTitle());
		ps.setString(3, feed.getLocation().toString());
		ps.setString(4, feed.getURL());
		ps.setString(5, feed.getArchiving().toString());
		ps.setInt(6, feed.getArchivingItems());
		ps.setInt(7, feed.getArchivingDays()); // archiving_days
		ps.setInt(8, feed.getUpdateInterval()); // update_interval
		ps.setString(9, feed.getUpdatePeriod().toString()); // update_period
		ps.setLong(10, feed.getLastUpdate()); // last_update
		ps.setString(11, feed.getDescription()); // description
		ps.setString(12, feed.getLink()); // link
		ps.setString(13, feed.getWebmaster()); // webmaster
		ps.setString(14, feed.getEditor()); // editor
		ps.setString(15, feed.getCopyright()); // copyright
		ps.setString(16, feed.getType()); // feed_type
		ps.setInt(17, feed.isHidden() ? 1 : 0);
		ps.setInt(18, feed.isAnonymousAccess() ? 1 : 0);
		ps.executeUpdate();
		ps.close();
	}

	/**
	 * Inserts a folder into the database.
	 * 
	 * @param item
	 *            The item to insert.
	 * @throws SQLException
	 */
	private void insert(Folder folder) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into folders values(?,?,?,?,?,?,?) "); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, folder.getUUID().toString());
		// Folders are used to represent the root aggregator item
		if (folder.getParent() != null) {
			ps.setString(2, ((AggregatorUIItem) folder.getParent()).getUUID()
					.toString());
		} else {
			ps.setNull(2, Types.CHAR);
		}
		ps.setLong(3, folder.getOrdering());
		// Folders are used to represent the root aggregator item
		if (folder.getFeed() != null) {
			ps.setString(4, folder.getFeed().toString());
		} else {
			ps.setNull(4, Types.CHAR);
		}
		ps.setInt(5, folder.isHidden() ? 1 : 0);
		ps.setString(6, folder.getTitle());
		ps.setString(7, encode(folder.getMarks()));
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#keepMaximum(no.resheim
	 * .aggregator.model.Feed, int) FIXME: THis method is totally broken
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
				// Article item = composeArticle(feed, rs);
				// delete(item);
			}
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#move(no.resheim.aggregator
	 * .model.IAggregatorItem, no.resheim.aggregator.model.IAggregatorItem)
	 */
	public void move(AggregatorUIItem item, ParentingAggregatorItem parent,
			int order) {
		try {
			item.setParent(parent);
			item.setOrdering(order);
			String table = null;
			if (item instanceof Article) {
				table = "articles"; //$NON-NLS-1$
			}
			if (item instanceof Folder) {
				table = "folders"; //$NON-NLS-1$
			}
			Statement s = connection.createStatement();
			s.executeUpdate("update " + table + " set parent_uuid='" //$NON-NLS-1$ //$NON-NLS-2$
					+ parent.getUUID().toString() + "', ordering=" //$NON-NLS-1$
					+ order + " where uuid='" //$NON-NLS-1$
					+ item.getUUID().toString() + "'"); //$NON-NLS-1$
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.internal.IRegistryExternalizer#open(java.lang.String
	 * )
	 */
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
				return createTables(monitor);
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
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#rename(no.resheim.aggregator
	 * .model.IAggregatorItem)
	 */
	public void rename(AggregatorUIItem item) {
		String query = null;
		if (item instanceof Folder) {
			query = "update folders set title='" + item.getTitle() //$NON-NLS-1$
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
	 * Finds all folders with the given parent item and adds the instance to the
	 * list.
	 * 
	 * @param parent
	 *            the parent item
	 * @param feeds
	 *            the list of aggregator articles to append to
	 * @throws SQLException
	 */
	private void selectFolders(ParentingAggregatorItem parent,
			ArrayList<AggregatorUIItem> feeds) throws SQLException {
		Statement s = connection.createStatement();
		String query = null;
		query = "select * from folders where parent_uuid='" //$NON-NLS-1$
				+ parent.getUUID().toString() + "' order by ordering"; //$NON-NLS-1$
		ResultSet rs = s.executeQuery(query);
		while (rs.next()) {
			feeds.add(composeFolder(parent, rs));
		}
		rs.close();
	}

	private AggregatorUIItem selectFolder(ParentingAggregatorItem parent,
			int index) throws SQLException {
		Statement s = connection.createStatement();
		String query = null;
		AggregatorUIItem folder = null;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#selectDescription(no.resheim
	 * .aggregator.model.Article)
	 */
	public String getDescription(Article item) {
		String description = null;
		try {
			Statement s = connection.createStatement();
			String query = "select description from articles where guid='" //$NON-NLS-1$
					+ item.getGuid() + "'"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			if (rs.next()) {
				description = rs.getString(1);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return description;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#selectItemCount(no.resheim
	 * .aggregator.model.Feed)
	 */
	public int getUnreadCount(ParentingAggregatorItem parent) {
		return getUnreadCount(parent.getUUID().toString());
	}

	/**
	 * Recurses through feeds and folders determining the number of unread
	 * articles contained within.
	 * 
	 * @param id
	 * @return
	 */
	private int getUnreadCount(String id) {
		int count = 0;
		try {
			if (connection.isClosed())
				return 0;
			Statement s = connection.createStatement();
			ResultSet rs = s
					.executeQuery("select count(uuid) from articles where parent_uuid='" //$NON-NLS-1$
							+ id + "' and is_read=0"); //$NON-NLS-1$
			if (rs.next())
				count += rs.getInt(1);
			rs = s.executeQuery("select uuid from folders where parent_uuid='" //$NON-NLS-1$
					+ id + "'"); //$NON-NLS-1$
			while (rs.next()) {
				count += getUnreadCount(rs.getString(1));
			}
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
	private void selectItems(ParentingAggregatorItem parent,
			ArrayList<AggregatorUIItem> feeds) {
		try {
			Statement s = connection.createStatement();
			String query = "select * from articles where parent_uuid='" //$NON-NLS-1$
					+ parent.getUUID() + "' order by ordering"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			while (rs.next()) {
				InternalArticle i = composeArticle(parent, rs);
				i.setParent(parent);
				feeds.add(i);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private AggregatorUIItem selectArticle(ParentingAggregatorItem parent,
			int index) throws SQLException {
		Statement s = connection.createStatement();
		Article article = null;
		ResultSet rs = s
				.executeQuery("select * from articles where parent_uuid='" //$NON-NLS-1$
						+ ((AggregatorUIItem) parent).getUUID().toString()
						+ "' and ordering=" + index); //$NON-NLS-1$);
		while (rs.next()) {
			article = composeArticle(parent, rs);
		}
		rs.close();
		return article;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#updateFeed(no.resheim.
	 * aggregator.model.Feed)
	 */
	public void updateFeed(Feed feed) {
		// XXX: Use SQL "update" instead of "delete" & "insert"
		delete(feed);
		add(feed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#updateReadFlag(no.resheim
	 * .aggregator.model.Article)
	 */
	public void updateReadFlag(AggregatorUIItem item) {
		try {
			Statement s = connection.createStatement();
			s.setEscapeProcessing(true);
			if (item instanceof Article) {
				s.executeUpdate("update articles set is_read=1 where uuid='" //$NON-NLS-1$
						+ item.getUUID() + "'"); //$NON-NLS-1$
			} else if (item instanceof Folder) {
				s
						.executeUpdate("update articles set is_read=1 where parent_uuid='" //$NON-NLS-1$
								+ item.getUUID() + "'"); //$NON-NLS-1$				
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.IAggregatorStorage#hasFeed(java.lang.String)
	 */
	public boolean hasFeed(String url) {
		boolean hasFeed = false;
		try {
			Statement s = connection.createStatement();
			String query = "select uuid from feeds where url='" //$NON-NLS-1$
					+ url + "'"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			hasFeed = rs.next();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hasFeed;
	}

	public AggregatorUIItem getItem(ParentingAggregatorItem parent, int index) {
		AggregatorUIItem item = null;
		try {
			if (item == null) {
				item = selectArticle(parent, index);
			}
			if (item == null) {
				item = selectFolder(parent, index);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		// Cache the retrieved item
		return item;
	}
}
