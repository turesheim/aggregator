package no.resheim.aggregator.core.data.internal;

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

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.AggregatorItem.Mark;
import no.resheim.aggregator.core.data.Feed.Archiving;
import no.resheim.aggregator.core.data.Feed.UpdatePeriod;
import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.EncodingUtils;

/**
 * Aggregator storage that uses the Derby embedded database to store it's data.
 * The only instantiated items are those created on request in order to keep the
 * memory use low.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class DerbySQLStorage extends AbstractAggregatorStorage {

	private static final String SQL_SEPARATOR = ";"; //$NON-NLS-1$

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
		feed.setTitle(rs.getString(2).trim());
		feed.setLocation(UUID.fromString(rs.getString(3)));
		feed.setURL(rs.getString(4).trim());
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
		if (rs.getString(19) != null) {
			feed.setImageData(EncodingUtils.decodeBase64(rs.getString(19)));
		}
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
	private InternalArticle composeArticle(AggregatorItemParent parent,
			ResultSet rs) throws SQLException {
		InternalArticle item = new InternalArticle(parent, UUID.fromString(rs
				.getString(1)), UUID.fromString(rs.getString(4)));
		item.setOrdering(rs.getInt(3));
		item.setGuid(rs.getString(5));
		item.setTitle(rs.getString(6).trim());
		item.setLink(rs.getString(7));
		item.setMark(Mark.valueOf(rs.getString(8)));
		item.setFlags(decodeFlags(rs.getString(9)));
		item.setRead(rs.getInt(10) != 0);
		item.setPublicationDate(rs.getLong(11));
		item.setReadDate(rs.getLong(12));
		item.setAddedDate(rs.getLong(13));
		item.setCreator(rs.getString(15));
		item.setMediaPlayerURL(rs.getString(16));
		item.setMediaEnclosureURL(rs.getString(17));
		item.setMediaEnclosureDuration(rs.getInt(18));
		item.setMediaEnclosureType(rs.getString(19));
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
	private AggregatorItem composeFolder(AggregatorItemParent parent,
			ResultSet rs) throws SQLException {
		InternalFolder item = new InternalFolder(parent, UUID.fromString(rs
				.getString(1)));
		item.setOrdering(rs.getInt(3));
		if (rs.getString(4) != null) {
			item.setFeed(UUID.fromString(rs.getString(4)));
		}
		item.setSystem(rs.getInt(5) != 0);
		item.setTitle(rs.getString(6).trim());
		item.setMark(Mark.valueOf(rs.getString(7)));
		item.setFlags(decodeFlags(rs.getString(8)));
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
				if (in.contains(SQL_SEPARATOR)) {
					create.append(in.substring(0, in.length() - 1));
					try {
						s.executeUpdate(create.toString());
					} catch (SQLException sqle) {
						System.err.println(sqle.getMessage());
						System.err.println(create.toString());
						// In case the table already exists
					}
					create.setLength(0);
				} else {
					create.append(in);
					create.append('\n');
				}
			}
			br.close();
			// Create a folder to represent the collection root. This is
			// required for maintaining relation integrity.
			Folder root = new InternalFolder(null, collection.getUUID());
			root.setTitle("ROOT"); //$NON-NLS-1$
			root.setSystem(true);
			insert(root);

			return Status.OK_STATUS;
		} catch (IOException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create tables", e); //$NON-NLS-1$
		} finally {
			try {
				br.close();
			} catch (IOException e) {
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#delete(no.resheim.aggregator
	 * .model.AggregatorItem)
	 */
	public void delete(AggregatorItem item) {
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

	public void delete(Feed feed) {
		try {
			Statement s = connection.createStatement();
			s.executeUpdate("delete from feeds where uuid='" //$NON-NLS-1$
					+ feed.getUUID().toString() + "'"); //$NON-NLS-1$
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see no.resheim.aggregator.model.AggregatorStorage#getChildren(no.resheim
	 * .aggregator.model.AggregatorItem)
	 */
	public AggregatorItem[] getChildren(AggregatorItemParent item) {
		Assert.isNotNull(item);
		ArrayList<AggregatorItem> items = new ArrayList<AggregatorItem>();
		try {
			selectFolders(item, items);
			selectItems(item, items);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return items.toArray(new AggregatorItem[items.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.IAggregatorStorage#getChildCount(no.resheim.aggregator
	 * .data.AggregatorItem)
	 */
	public synchronized int getChildCount(AggregatorItem parent) {
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
		} finally {

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
	 * .model.AggregatorItem)
	 */
	public IStatus add(AggregatorItem item) {
		try {
			if (item instanceof Article) {
				// Set the order of the item
				((InternalArticle) item)
						.setOrdering(getChildCount(((InternalArticle) item)
								.getLocation()));
				insert((InternalArticle) item);
			}
			if (item instanceof Folder) {
				((AggregatorItem) item)
						.setOrdering(getChildCount(((AggregatorItem) item)
								.getParent()));
				insert((Folder) item);
			}
			return Status.OK_STATUS;
		} catch (SQLException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.DerbySQLStorage_StoreError,
							item), e);
		}
	}

	public void add(Feed feed) {
		try {
			insert(feed);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Inserts a feed item into the database.
	 * 
	 * @param item
	 *            The item to insert.
	 * @throws SQLException
	 */
	private void insert(InternalArticle item) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into articles values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, item.getUUID().toString());
		ps.setString(2, item.getLocation().toString());
		ps.setLong(3, item.getOrdering());
		ps.setString(4, item.getFeedUUID().toString());
		ps.setString(5, item.getGuid());
		ps.setString(6, item.getTitle());
		ps.setString(7, item.getLink());
		ps.setString(8, item.getMark().toString());
		ps.setString(9, encodeFlags(item.getFlags()));
		ps.setInt(10, item.isRead() ? 1 : 0);
		ps.setLong(11, item.getPublicationDate());
		ps.setLong(12, item.getReadDate());
		ps.setLong(13, item.getAdded());
		ps.setString(14, item.internalGetText());
		ps.setString(15, item.getCreator());
		ps.setString(16, item.getMediaPlayerURL());
		ps.setString(17, item.getMediaEnclosureURL());
		ps.setInt(18, item.getMediaEnclosureDuration());
		ps.setString(19, item.getMediaEnclosureType());
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
				.prepareStatement("insert into feeds values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
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
		// Make sure we don't attempt to insert null image data or data that we
		// do not enough space for.
		if (feed.getImageData() == null) {
			ps.setNull(19, Types.VARCHAR);
		} else {
			String data = EncodingUtils.encodeBase64(feed.getImageData());
			if (data.length() <= 10240) {
				ps.setString(19, data);
			} else {
				ps.setNull(19, Types.VARCHAR);
			}
		}
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
				.prepareStatement("insert into folders values(?,?,?,?,?,?,?,?) "); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, folder.getUUID().toString());
		// Folders are used to represent the root aggregator item
		if (folder.getParent() != null) {
			ps.setString(2, ((AggregatorItem) folder.getParent()).getUUID()
					.toString());
		} else {
			ps.setNull(2, Types.CHAR);
		}
		ps.setLong(3, folder.getOrdering());
		// Folders are used to represent the root aggregator item
		if (folder.getFeedUUID() != null) {
			ps.setString(4, folder.getFeedUUID().toString());
		} else {
			ps.setNull(4, Types.CHAR);
		}
		ps.setInt(5, folder.isSystem() ? 1 : 0);
		ps.setString(6, folder.getTitle());
		ps.setString(7, folder.getMark().toString());
		ps.setString(8, encodeFlags(folder.getFlags()));
		ps.executeUpdate();
		ps.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#move(no.resheim.aggregator
	 * .model.AggregatorItem, no.resheim.aggregator.model.AggregatorItem)
	 */
	public void move(AggregatorItem item) {
		try {
			String table = null;
			if (item instanceof Article) {
				table = "articles"; //$NON-NLS-1$
			}
			if (item instanceof Folder) {
				table = "folders"; //$NON-NLS-1$
			}
			Statement s = connection.createStatement();
			s.executeUpdate("update " + table + " set parent_uuid='" //$NON-NLS-1$ //$NON-NLS-2$
					+ item.getParent().getUUID().toString() + "', ordering=" //$NON-NLS-1$
					+ item.getOrdering() + " where uuid='" //$NON-NLS-1$
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
	 * .model.AggregatorItem)
	 */
	public void rename(AggregatorItem item) {
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
	private void selectFolders(AggregatorItemParent parent,
			ArrayList<AggregatorItem> feeds) throws SQLException {
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

	private AggregatorItem selectFolder(AggregatorItemParent parent, int index)
			throws SQLException {
		Statement s = connection.createStatement();
		String query = null;
		AggregatorItem folder = null;
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
	public int getUnreadCount(AggregatorItem parent) {
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
	private void selectItems(AggregatorItemParent parent,
			ArrayList<AggregatorItem> feeds) {
		try {
			Statement s = connection.createStatement();
			String query = "select * from articles where parent_uuid='" //$NON-NLS-1$
					+ parent.getUUID() + "' order by ordering"; //$NON-NLS-1$
			ResultSet rs = s.executeQuery(query);
			while (rs.next()) {
				InternalArticle i = composeArticle(parent, rs);
				feeds.add(i);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private AggregatorItem selectArticle(AggregatorItemParent parent, int index)
			throws SQLException {
		Statement s = connection.createStatement();
		Article article = null;
		ResultSet rs = s
				.executeQuery("select * from articles where parent_uuid='" //$NON-NLS-1$
						+ ((AggregatorItem) parent).getUUID().toString()
						+ "' and ordering=" + index); //$NON-NLS-1$
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
	public void updateReadFlag(AggregatorItem item) {
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
			ResultSet rs = s.executeQuery("select uuid from feeds where url='" //$NON-NLS-1$
					+ url + "'"); //$NON-NLS-1$
			hasFeed = rs.next();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hasFeed;
	}

	public AggregatorItem getItem(AggregatorItemParent parent, int index) {
		Assert.isNotNull(parent);
		AggregatorItem item = null;
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
		return item;
	}

	public void update(AggregatorItem item) {
		try {
			Statement s = connection.createStatement();
			s.setEscapeProcessing(true);
			if (item instanceof Article) {
				s.executeUpdate("update articles set marking='" //$NON-NLS-1$
						+ item.getMark().toString() + "' where uuid='" //$NON-NLS-1$
						+ item.getUUID() + "'"); //$NON-NLS-1$
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Filter[] getFilters() {
		ArrayList<Filter> filters = new ArrayList<Filter>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery("select * from filters"); //$NON-NLS-1$
			while (rs.next()) {
				Filter filter = new Filter(rs.getString("title"), UUID //$NON-NLS-1$
						.fromString(rs.getString("uuid"))); //$NON-NLS-1$
				// Add filter actions
				ResultSet rs2 = s
						.executeQuery("select * from filter_actions where filter_uuid='" //$NON-NLS-1$
								+ filter.getUuid().toString() + "'"); //$NON-NLS-1$
				while (rs2.next()) {

				}
				filters.add(filter);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return filters.toArray(new Filter[filters.size()]);
	}

	public void setFilters(Filter[] filters) {
		// TODO Auto-generated method stub

	}
}
