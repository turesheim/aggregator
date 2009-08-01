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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.BrokenItem;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.MediaContent;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.data.MediaContent.Medium;
import no.resheim.aggregator.core.data.Subscription.Archiving;
import no.resheim.aggregator.core.data.Subscription.UpdatePeriod;
import no.resheim.aggregator.core.filter.Filter;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
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

	/** Connection options */
	private static final String CONNECT_OPTIONS = ";create=true"; //$NON-NLS-1$

	/** Database driver */
	private static final String DB_DRIVER = "org.apache.derby.jdbc.EmbeddedDriver"; //$NON-NLS-1$

	/** Disconnection options */
	private static final String DISCONNECT_OPTIONS = ";shutdown=true"; //$NON-NLS-1$

	private static final String JDBC_DERBY = "jdbc:derby:"; //$NON-NLS-1$

	private static final String SQL_SEPARATOR = ";"; //$NON-NLS-1$

	/** Name of the SQL file used to create the tables */
	private static final String TABLES_SQL = "tables.sql"; //$NON-NLS-1$

	/** The database connection */
	private Connection connection;

	/**
	 * 
	 */
	public DerbySQLStorage(AggregatorCollection registry, IPath path) {
		super(registry, path);
		fArticleResults = new HashMap<UUID, ResultSet>();
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
				insert((Article) item);
			}
			if (item instanceof Folder) {
				insert((Folder) item);
			}
			return Status.OK_STATUS;
		} catch (SQLException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.DerbySQLStorage_StoreError,
							item), e);
		}
	}

	public void add(Subscription feed) {
		try {
			insert(feed);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Adds a new {@link Filter} to the database. All contained criteria and
	 * actions are also stored.
	 * 
	 * @param filter
	 *            the {@link Filter} instance to store
	 * @return the operation status
	 */
	public IStatus add(Filter filter) {
		try {
			insert(filter);
			return Status.OK_STATUS;
		} catch (SQLException e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					MessageFormat.format(Messages.DerbySQLStorage_StoreError,
							filter), e);
		}
	}

	/**
	 * Composes an Item instance from the result set but leaves out the
	 * description field.
	 * 
	 * @param rs
	 * @return
	 * @throws SQLException
	 */
	private Article composeArticle(AggregatorItemParent parent, ResultSet rs)
			throws SQLException {
		Article item = new Article(parent, UUID.fromString(rs.getString(1)),
				UUID.fromString(rs.getString(3)));
		updateFromResultSet(rs, item);
		return item;
	}

	/**
	 * Updates the given article instance with values from the result set.
	 * 
	 * @param rs
	 *            the result set
	 * @param item
	 *            the article to update
	 * @throws SQLException
	 */
	private void updateFromResultSet(ResultSet rs, Article item)
			throws SQLException {
		item.setGuid(rs.getString(4));
		item.setTitle(rs.getString(5).trim());
		item.setLink(rs.getString(6));
		item.setFlags(decodeFlags(rs.getString(7)));
		String labels = rs.getString(8);
		if (labels.length() > 0) {
			item.setLabels(rs.getString(8).split(","));
		}
		item.setRead(rs.getInt(9) != 0);
		item.setPublicationDate(rs.getLong(10));
		item.setReadDate(rs.getLong(11));
		item.setAddedDate(rs.getLong(12));
		// Field #13 is the article text which we will only retrieve when
		// required.
		item.setCreator(rs.getString(14));
		item.setMediaPlayerURL(rs.getString(15));
		item.setLastChanged(rs.getLong(16));
		item.setStarred(rs.getInt(17) != 0);
	}

	/**
	 * Uses data from the result set to create a new subscription instance.
	 * 
	 * @param rs
	 *            the result set to read from
	 * @return a subscription instance
	 * @throws SQLException
	 */
	private Subscription composeSubcription(ResultSet rs) throws SQLException {
		Subscription feed = new Subscription();
		feed.setUUID(UUID.fromString(rs.getString(1)));
		feed.setLocation(UUID.fromString(rs.getString(2)));
		feed.setTitle(rs.getString(3).trim());
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
		feed.setKeepUread(rs.getInt(19) != 0);
		if (rs.getString(20) != null) {
			feed.setImageData(EncodingUtils.decodeBase64(rs.getString(20)));
		}
		feed.setSynchronizer(rs.getString(21));
		return feed;
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
		Folder item = new Folder(parent, UUID.fromString(rs.getString(1)));
		if (rs.getString(3) != null) {
			item.setFeed(UUID.fromString(rs.getString(3)));
		}
		item.setSystem(rs.getInt(4) != 0);
		item.setTitle(rs.getString(5).trim());
		item.setFlags(decodeFlags(rs.getString(6)));
		item.setLabels(rs.getString(7).split(","));
		return item;
	}

	private MediaContent composeMediaContent(ResultSet rs) throws SQLException {
		MediaContent content = new MediaContent();
		content.setContentURL(rs.getString(3));
		content.setThumbnailURL(rs.getString(4));
		content.setContentType(rs.getString(5));
		content.setMedium(Medium.valueOf(rs.getString(7)));
		// TODO:FIX THE REST
		return content;
	}

	private IStatus createTables(IProgressMonitor monitor) throws SQLException {
		monitor.subTask(Messages.DerbySQLStorage_Initializing_Database);
		InputStream is = DerbySQLStorage.class.getResourceAsStream(TABLES_SQL);
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		Statement s = connection.createStatement();
		StringBuffer create = new StringBuffer();
		String in = null;
		IStatus status = Status.OK_STATUS;
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
						status = new Status(IStatus.ERROR,
								AggregatorPlugin.PLUGIN_ID,
								MessageFormat.format(
										"Problem creating tables:\n{0}\n{1}", //$NON-NLS-1$
										new Object[] { sqle.getMessage(),
												create.toString() }));
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
			Folder root = new Folder(null, collection.getUUID());
			root.setTitle("ROOT"); //$NON-NLS-1$
			root.setSystem(true);
			insert(root);
			return status;
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
		// Note that BrokenItem instances are never deleted
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

	public void delete(Subscription feed) {
		try {
			Statement s = connection.createStatement();
			s.executeUpdate("delete from subscriptions where uuid='" //$NON-NLS-1$
					+ feed.getUUID().toString() + "'"); //$NON-NLS-1$
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.IAggregatorStorage#getChildCount(no.resheim.aggregator
	 * .data.AggregatorItem)
	 */
	public synchronized int getChildCount(AggregatorItem parent,
			EnumSet<ItemType> types) {
		UUID parentID = parent.getUUID();
		return getChildCount(parentID, types);
	}

	private int getChildCount(UUID parentID, EnumSet<ItemType> types) {
		int count = 0;
		try {
			if (connection.isClosed())
				return 0;
			Statement s = connection.createStatement();
			if (types.contains(ItemType.ARTICLE)) {
				ResultSet rs = s
						.executeQuery("select count(uuid) from articles where parent_uuid='" //$NON-NLS-1$
								+ parentID.toString() + "'"); //$NON-NLS-1$
				if (rs.next())
					count += rs.getInt(1);
				rs.close();
			}
			if (types.contains(ItemType.FOLDER)) {
				ResultSet rs2 = s
						.executeQuery("select count(uuid) from folders where parent_uuid='" //$NON-NLS-1$
								+ parentID.toString() + "'"); //$NON-NLS-1$
				if (rs2.next())
					count += rs2.getInt(1);
				rs2.close();
			}
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
	 * @see no.resheim.aggregator.model.IAggregatorStorage#initializeFeeds()
	 */
	public HashMap<UUID, Subscription> getSubscriptions() {
		HashMap<UUID, Subscription> feeds = new HashMap<UUID, Subscription>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s.executeQuery("select * from subscriptions"); //$NON-NLS-1$
			while (rs.next()) {
				Subscription f = composeSubcription(rs);
				feeds.put(f.getUUID(), f);
			}
			rs.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return feeds;
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
				// TODO: Go through all filters and add the details
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

	public AggregatorItem getChildAt(AggregatorItemParent parent,
			EnumSet<ItemType> types, int index) throws CoreException {
		Assert.isNotNull(parent);
		AggregatorItem item = null;
		try {
			if (types.contains(ItemType.ARTICLE)) {
				item = selectArticle(parent, index);
			}
			if (item == null && types.contains(ItemType.FOLDER)) {
				item = selectFolder(parent, index);
			}
			// We should have something here so we're going to return a
			// dummy item. See bug 636. In this we're assuming that we're never
			// requesting an item that should not exist.
			if (item == null) {
				item = new BrokenItem(parent);
			}
		} catch (SQLException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					AggregatorPlugin.PLUGIN_ID, "Could not obtain child item",
					e));
		}
		return item;
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
	 * @see no.resheim.aggregator.IAggregatorStorage#hasFeed(java.lang.String)
	 */
	public boolean hasSubscription(String url) {
		boolean hasFeed = false;
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s
					.executeQuery("select uuid from subscriptions where url='" //$NON-NLS-1$
							+ url + "'"); //$NON-NLS-1$
			hasFeed = rs.next();
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return hasFeed;
	}

	/**
	 * Inserts a new feed into the database.
	 * 
	 * @param feed
	 *            The feed to insert
	 * @throws SQLException
	 */
	private void insert(Subscription feed) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into subscriptions values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, feed.getUUID().toString());
		ps.setString(2, feed.getLocation().toString());
		ps.setString(3, feed.getTitle());
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
		ps.setInt(19, feed.keepUnread() ? 1 : 0);
		// Make sure we don't attempt to insert null image data or data that we
		// do not enough space for.
		if (feed.getImageData() == null) {
			ps.setNull(20, Types.VARCHAR);
		} else {
			String data = EncodingUtils.encodeBase64(feed.getImageData());
			if (data.length() <= 10240) {
				ps.setString(20, data);
			} else {
				ps.setNull(20, Types.VARCHAR);
			}
		}
		ps.setString(21, feed.getSynchronizer());
		ps.executeUpdate();
		ps.close();
	}

	/**
	 * Creates and executes the SQL code required to insert the filter instance
	 * into the database.
	 * 
	 * @param filter
	 *            the {@link Filter} to store
	 * @throws SQLException
	 */
	private void insert(Filter filter) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into filters values (?,?,?,?)"); //$NON-NLS-1$
		ps.setString(1, filter.getUuid().toString());
		ps.setString(2, filter.getTitle());
		ps.setInt(3, filter.isMatchAllCriteria() ? 1 : 0);
		ps.setInt(4, filter.isManualOnly() ? 1 : 0);
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
				.prepareStatement("insert into folders values(?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, folder.getUUID().toString());
		// Folders are used to represent the root aggregator item
		if (folder.getParent() != null) {
			ps.setString(2, ((AggregatorItem) folder.getParent()).getUUID()
					.toString());
		} else {
			ps.setNull(2, Types.CHAR);
		}
		// Folders are used to represent the root aggregator item
		if (folder.getFeedUUID() != null) {
			ps.setString(3, folder.getFeedUUID().toString());
		} else {
			ps.setNull(3, Types.CHAR);
		}
		ps.setInt(4, folder.isSystem() ? 1 : 0);
		ps.setString(5, folder.getTitle());
		ps.setString(6, encodeFlags(folder.getFlags()));
		ps.setString(7, folder.getLabelString());
		ps.executeUpdate();
		ps.close();
	}

	/**
	 * Inserts an article into the database.
	 * 
	 * @param item
	 *            The item to insert.
	 * @throws SQLException
	 */
	private void insert(Article item) throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into articles values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setString(1, item.getUUID().toString());
		ps.setString(2, item.getLocation().toString());
		ps.setString(3, item.getSubscriptionUUID().toString());
		ps.setString(4, item.getGuid());
		ps.setString(5, item.getTitle());
		ps.setString(6, item.getLink());
		ps.setString(7, encodeFlags(item.getFlags()));
		ps.setString(8, item.getLabelString());
		ps.setInt(9, item.isRead() ? 1 : 0);
		ps.setLong(10, item.getPublicationDate());
		ps.setLong(11, item.getReadDate());
		ps.setLong(12, item.getAdded());
		ps.setString(13, item.internalGetText());
		ps.setString(14, item.getCreator());
		ps.setString(15, item.getMediaPlayerURL());
		ps.setLong(16, item.getLastChanged());
		ps.setInt(17, item.isStarred() ? 1 : 0);
		ps.executeUpdate();
		ps.close();
		int count = 0;
		for (MediaContent content : item.getMediaContent()) {
			count += 1;
			insert(content, item, count);
		}
	}

	private void insert(MediaContent content, Article article, int order)
			throws SQLException {
		PreparedStatement ps = connection
				.prepareStatement("insert into media_content values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"); //$NON-NLS-1$
		ps.setEscapeProcessing(true);
		ps.setInt(1, order);
		ps.setString(2, article.getUUID().toString());
		ps.setString(3, content.getContentURL());
		ps.setString(4, content.getThumbnailURL());
		ps.setString(5, content.getContentType());
		ps.setLong(6, content.getFilesSize());
		ps.setString(7, content.getMedium().toString());
		ps.setInt(8, content.isDefault() ? 1 : 0);
		ps.setString(9, content.getExpression().toString());
		ps.setInt(10, content.getBitrate());
		ps.setInt(11, content.getFramerate());
		ps.setInt(12, content.getSamplingrate());
		ps.setInt(13, content.getChannels());
		ps.setInt(14, content.getDuration());
		ps.setString(15, content.getHeight());
		ps.setString(16, content.getWidth());
		ps.setString(17, content.getLang());
		// Optional elements
		ps.setString(18, content.getMediaPlayer());
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
	public void moved(AggregatorItem item) {
		// These instances are never moved. They don't exist in the database
		if (item instanceof BrokenItem) {
			return;
		}
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
					+ item.getParent().getUUID().toString() + "' where uuid='" //$NON-NLS-1$
					+ item.getUUID().toString() + "'"); //$NON-NLS-1$
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Article selection result sets */
	private HashMap<UUID, ResultSet> fArticleResults;

	private static final String ARTICLE_QUERY = "SELECT * FROM articles WHERE parent_uuid=? ORDER BY publication_date DESC";

	/**
	 * Statement used with <b>ARTICLE_QUERY</b> to produce article view. Note
	 * that these statements cannot be updated directly with Derby as we have
	 * WHERE and ORDER BY clauses in the query. This must be handled manually.
	 */
	private PreparedStatement viewArticlesStatement;

	/**
	 * Returns the article with the given parent at the given position.
	 * 
	 * @param parent
	 *            the parent item
	 * @param index
	 *            the index of the article
	 * @return the article if found
	 * @throws SQLException
	 */
	private AggregatorItem selectArticle(AggregatorItemParent parent, int index)
			throws SQLException {
		Article article = null;
		ResultSet rs = fArticleResults.get(parent);
		if (viewArticlesStatement == null) {
			viewArticlesStatement = connection.prepareStatement(ARTICLE_QUERY,
					ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			rs = null;
		}
		if (rs == null) {
			viewArticlesStatement.setString(1, parent.getUUID().toString());
			rs = viewArticlesStatement.executeQuery();
			fArticleResults.put(parent.getUUID(), rs);
		}
		rs.absolute(index);
		if (rs.next()) {
			article = composeArticle(parent, rs);
		}
		if (article != null) {
			Statement s = connection.createStatement();
			ResultSet rs2 = s
					.executeQuery("select * from media_content where article_uuid='" //$NON-NLS-1$
							+ article.getUUID().toString()
							+ "' order by ordering"); //$NON-NLS-1$
			while (rs2.next()) {
				article.addMediaContent(composeMediaContent(rs2));
			}
			rs2.close();
		}
		return article;
	}

	private AggregatorItem selectFolder(AggregatorItemParent parent, int index)
			throws SQLException {
		Statement s = connection.createStatement(
				ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
		String query = null;
		AggregatorItem folder = null;
		query = "select * from folders where parent_uuid='" //$NON-NLS-1$
				+ parent.getUUID().toString() + "'";

		ResultSet rs = s.executeQuery(query);
		rs.absolute(index);
		if (rs.next()) {
			folder = composeFolder(parent, rs);
		}
		rs.close();
		return folder;
	}

	public void setFilters(Filter[] filters) {
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
			// connection.setHoldability(ResultSet.CLOSE_CURSORS_AT_COMMIT);
			// See if the database already exists. If not we must create it.
			DatabaseMetaData metadata = connection.getMetaData();
			/*
			 * System.out.println(metadata.supportsResultSetConcurrency(
			 * ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY))
			 */
			ResultSet rs = metadata.getTables(null,
					"APP", "SUBSCRIPTIONS", null); //$NON-NLS-1$ //$NON-NLS-2$
			if (!rs.next()) {
				return createTables(monitor);
			}
		} catch (Exception e) {
			return new Status(IStatus.ERROR, AggregatorPlugin.PLUGIN_ID,
					"Could not create feeds", e); //$NON-NLS-1$
		}
		return Status.OK_STATUS;
	}

	public void writeBack(AggregatorItem item) {
		try {
			// We cannot use the prepared statement for this, so we must create
			// a new one.
			Statement s = connection.createStatement();
			s.setEscapeProcessing(true);
			if (item instanceof Article) {
				s
						.executeUpdate("update articles set labels='" //$NON-NLS-1$
								+ item.getLabelString()
								+ "', is_read="
								+ (((Article) item).isRead() ? "1" : "0")
								+ ", last_changed=" + ((Article) item).getLastChanged() + ", starred=" + (((Article) item).isStarred() ? 1 : 0) + " where uuid='" //$NON-NLS-1$
								+ item.getUUID() + "'"); //$NON-NLS-1$
			}
			if (item instanceof Folder) {
				s.executeUpdate("update folders set labels='" //$NON-NLS-1$
						+ item.getLabelString() + "' title='"
						+ item.getTitle()
						+ "' where uuid='" //$NON-NLS-1$
						+ item.getUUID() + "'"); //$NON-NLS-1$
			}
			// Re-execute this one so that articles view is updated.
			if (item instanceof Article) {
				AggregatorItem parent = item.getParent();
				ResultSet rs = fArticleResults.get(parent);
				if (viewArticlesStatement == null) {
					viewArticlesStatement = connection.prepareStatement(
							ARTICLE_QUERY, ResultSet.TYPE_SCROLL_INSENSITIVE,
							ResultSet.CONCUR_READ_ONLY);
				}
				if (rs == null) {
					viewArticlesStatement.setString(1, parent.getUUID()
							.toString());
					rs = viewArticlesStatement.executeQuery();
					fArticleResults.put(parent.getUUID(), rs);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.model.IAggregatorStorage#updateFeed(no.resheim.
	 * aggregator.model.Feed)
	 */
	public void updateSubscription(Subscription feed) {
		// XXX: Use SQL "update" instead of "delete" & "insert"
		delete(feed);
		add(feed);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * no.resheim.aggregator.core.data.IAggregatorStorage#getChangedArticles
	 * (no.resheim.aggregator.core.data.Subscription, long)
	 */
	public List<Article> getChangedArticles(Subscription subscription, long time) {
		ArrayList<Article> articles = new ArrayList<Article>();
		try {
			Statement s = connection.createStatement();
			ResultSet rs = s
					.executeQuery("select * from articles where subscription_uuid='" //$NON-NLS-1$
							+ subscription.getUUID().toString()
							+ "' and last_changed>" + time); //$NON-NLS-1$
			while (rs.next()) {
				Article item = new Article(subscription, UUID.fromString(rs
						.getString(1)));
				updateFromResultSet(rs, item);
				System.out.println("Changed " + item);
				articles.add(item);
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return articles;
	}
}
