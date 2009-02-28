package no.resheim.aggregator.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.IAggregatorStorage;
import no.resheim.aggregator.core.data.Feed.Archiving;
import no.resheim.aggregator.core.data.Feed.UpdatePeriod;
import no.resheim.aggregator.core.data.internal.DerbySQLStorage;
import no.resheim.aggregator.core.data.internal.MemoryStorage;
import no.resheim.aggregator.core.synch.AbstractSynchronizer;

import org.eclipse.core.internal.resources.Synchronizer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 * This type is responsible for handling the feed registries that contains the
 * actual feeds while managing the life cycle of the storage backend for these
 * registries.
 * <p>
 * Once a feed registry has been declared, it can be retrieved from this plug-in
 * using the registry's unique identifier.
 * </p>
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AggregatorPlugin extends Plugin {

	public static final String REGISTRY_EXTENSION_ID = "no.resheim.aggregator.core.feeds"; //$NON-NLS-1$

	/**
	 * The default synchronizer to use.
	 */
	public static final String DEFAULT_SYNCHRONIZER_ID = "no.resheim.aggregator.core.defaultSynchronizer";
	private static final String SYNCHRONIZERS_EXTENSION_POINT = "no.resheim.aggregator.core.synchronizers";

	/** The plug-in identifier */
	public static final String PLUGIN_ID = "no.resheim.aggregator"; //$NON-NLS-1$

	/**
	 * The shared plug-in instance
	 * 
	 * @uml.property name="plugin"
	 * @uml.associationEnd
	 */
	private static AggregatorPlugin plugin = null;

	/**
	 * Returns the feed synchroniser with the given <i>id</i>. If it could not
	 * be found <code>null</code> is returned.
	 * 
	 * @param id
	 *            the identifier of the synchroniser
	 * @return a new {@link Synchronizer} instance
	 */
	public static AbstractSynchronizer getSynchronizer(String id) {

		IExtensionPoint ePoint = Platform.getExtensionRegistry()
				.getExtensionPoint(SYNCHRONIZERS_EXTENSION_POINT);
		IConfigurationElement[] synchronizers = ePoint
				.getConfigurationElements();
		for (IConfigurationElement configurationElement : synchronizers) {
			if (configurationElement.getAttribute("id").equals(id)) {
				try {
					Object object = configurationElement
							.createExecutableExtension("class");
					if (object instanceof AbstractSynchronizer) {
						return ((AbstractSynchronizer) object);
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Collections are declared using a symbolic name, for instance
	 * "com.foo.bar.registry". This member is used to map between the symbolic
	 * name and the universally unique identifier that is required internally.
	 * It's contents may be manipulated by different threads so it has been made
	 * thread safe using synchronized blocks.
	 */
	private final HashMap<String, FeedCollection> collectionMap = new HashMap<String, FeedCollection>();

	private final ArrayList<IAggregatorStorage> storageList = new ArrayList<IAggregatorStorage>();

	private final ArrayList<IFeedCollectionEventListener> fCollectionListeners = new ArrayList<IFeedCollectionEventListener>();

	/** Default feeds to add */
	private ArrayList<Feed> fDefaultFeeds;

	/**
	 * @uml.property name="defaultCollection"
	 * @uml.associationEnd
	 */
	private FeedCollection defaultCollection;

	public ArrayList<Feed> getDefaultFeeds() {
		return fDefaultFeeds;
	}

	/**
	 * Name of the default feed collection.
	 */

	/**
	 * The constructor
	 */
	public AggregatorPlugin() {
		plugin = this;
		// DEFAULT_FEEDS = new ArrayList<String[]>();
		fDefaultFeeds = new ArrayList<Feed>();
	}

	public void addFeedCollectionListener(IFeedCollectionEventListener listener) {
		synchronized (fCollectionListeners) {
			fCollectionListeners.add(listener);
		}
	}

	public void removeFeedCollectionListener(
			IFeedCollectionEventListener listener) {
		synchronized (fCollectionListeners) {
			fCollectionListeners.remove(listener);
		}
	}

	private static final String CORE_NET_BUNDLE = "org.eclipse.core.net"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fDebugging = super.isDebugging();
		System.out.println("Starting aggregator core"); //$NON-NLS-1$
		Bundle bundle = Platform.getBundle(CORE_NET_BUNDLE);
		if (bundle.getState() != Bundle.UNINSTALLED) {
			try {
				bundle.start();
			} catch (BundleException e1) {
				e1.printStackTrace();
			}
		}
		initialize();
	}

	private boolean fDebugging;

	/** Preference key for the secure storage password */
	public static final String SECURE_STORAGE_PASSWORD = "password"; //$NON-NLS-1$

	/** Preference key for the secure storage user name */
	public static final String SECURE_STORAGE_USERNAME = "username"; //$NON-NLS-1$

	/** Preference key for the secure storage root */
	public static final String SECURE_STORAGE_ROOT = PLUGIN_ID;

	@Override
	public boolean isDebugging() {
		return fDebugging;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		for (IAggregatorStorage storage : storageList) {
			storage.shutdown();
		}
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AggregatorPlugin getDefault() {
		if (plugin == null) {
			new AggregatorPlugin();
		}
		return plugin;
	}

	/**
	 * By passing an identifier the corresponding feed collection is returned.
	 * If <b>null</b> is passed, the default collection is returned. If the
	 * collection is not found <b>null</b> is returned.
	 * 
	 * @return the feed collection
	 */
	public FeedCollection getFeedCollection(String id) {
		synchronized (collectionMap) {
			if (id == null)
				return defaultCollection;
			return collectionMap.get(id);
		}
	}

	/**
	 * Returns the location of the configuration directory. If possible the
	 * shared Eclipse configuration directory is used. If not the state location
	 * of the plug-in is used.
	 * 
	 * @return A pointer to the Aggregator configuration directory
	 */
	private IPath getStorageLocation(FeedCollection registry) {
		return getStateLocation().makeAbsolute().addTrailingSeparator().append(
				registry.getId());
	}

	/**
	 * Returns a collection of all declared feed collections. If the
	 * initialisation procedure has not completed the caller will have to wait.
	 * 
	 * @return
	 */
	public Collection<FeedCollection> getCollections() {
		synchronized (collectionMap) {
			return collectionMap.values();
		}
	}

	private boolean fDoneInitializing = false;

	private void initialize() {
		final IExtensionRegistry ereg = Platform.getExtensionRegistry();
		final Job job = new Job(Messages.AggregatorPlugin_Initializing) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (collectionMap) {
					IStatus status = addCollections(ereg, monitor);
					if (status.isOK()) {
						addFeeds(ereg);
						Collections.sort(fDefaultFeeds);
					}
					fDoneInitializing = true;
					System.out.println("Aggregator core initialized"); //$NON-NLS-1$
					return status;
				}
			}
		};
		job.schedule();

	}

	public boolean isCollectionsInitialized() {
		return fDoneInitializing;
	}

	private IStatus addCollections(IExtensionRegistry ereg,
			IProgressMonitor monitor) {
		IConfigurationElement[] elements = ereg
				.getConfigurationElementsFor(REGISTRY_EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			// We're not going to allow an exception here to disrupt.
			try {
				if (element.getName().equals("collection")) { //$NON-NLS-1$
					String id = element.getAttribute("id"); //$NON-NLS-1$
					String name = element.getAttribute("name"); //$NON-NLS-1$
					boolean pub = Boolean.parseBoolean(element
							.getAttribute("public")); //$NON-NLS-1$
					boolean def = Boolean.parseBoolean(element
							.getAttribute("default")); //$NON-NLS-1$
					boolean persistent = Boolean.parseBoolean(element
							.getAttribute("persistent")); //$NON-NLS-1$

					final FeedCollection collection = new FeedCollection(id,
							pub, def);
					collection.setTitle(name);
					collectionMap.put(id, collection);
					IAggregatorStorage storage = null;
					if (persistent) {
						storage = new DerbySQLStorage(collection,
								getStorageLocation(collection));
					} else {
						storage = new MemoryStorage(collection,
								getStorageLocation(collection));
					}

					IStatus status = storage.startup(monitor);
					if (status.isOK()) {
						collection.initialize(storage);
						storageList.add(storage);
						if (def) {
							defaultCollection = collection;
						}
						synchronized (fCollectionListeners) {
							for (final IFeedCollectionEventListener listener : fCollectionListeners) {
								SafeRunner.run(new ISafeRunnable() {
									public void handleException(
											Throwable exception) {
										exception.printStackTrace();
									}

									public void run() throws Exception {
										listener
												.collectionInitialized(collection);
									}

								});
							}
						}
						ResourcesPlugin.getWorkspace().addSaveParticipant(this,
								storage);
					} else {
						return status;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return Status.OK_STATUS;
	}

	/**
	 * Reads all feed declarations from the extension registry and adds these to
	 * the list of default feeds and optionally adds the feed to the
	 * collection(s).
	 * 
	 * @param ereg
	 */
	private void addFeeds(IExtensionRegistry ereg) {
		IConfigurationElement[] elements = ereg
				.getConfigurationElementsFor(REGISTRY_EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			// We're not going to allow an exception here to disrupt.
			try {
				if (element.getName().equals("feed")) { //$NON-NLS-1$
					String url = element.getAttribute("url"); //$NON-NLS-1$
					String collectionId = element.getAttribute("collection"); //$NON-NLS-1$
					boolean add = Boolean.parseBoolean(element
							.getAttribute("create")); //$NON-NLS-1$
					// Will use the default collection if the collectionId is
					// null.
					FeedCollection collection = getFeedCollection(collectionId);
					if (collection != null) {
						Feed feed = createNewFeed(collection, element);
						if (add && !collection.hasFeed(url)) {
							collection.addNew(feed);
						}
						fDefaultFeeds.add(feed);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private Feed createNewFeed(FeedCollection parent,
			IConfigurationElement element) {
		Feed feed = new Feed();
		// Initialise with default values from the preference store.
		// This is done here as the preference system is a UI component.
		feed.setTitle(element.getAttribute("title")); //$NON-NLS-1$
		feed.setURL(element.getAttribute("url")); //$NON-NLS-1$
		feed.setArchiving(Archiving.valueOf(element
				.getAttribute("archivingMethod"))); //$NON-NLS-1$
		feed.setArchivingDays(Integer.parseInt(element
				.getAttribute("archivingDays"))); //$NON-NLS-1$
		feed.setArchivingItems(Integer.parseInt(element
				.getAttribute("archivingItems"))); //$NON-NLS-1$
		feed.setUpdateInterval(Integer.parseInt(element
				.getAttribute("updateInterval"))); //$NON-NLS-1$
		feed.setUpdatePeriod(UpdatePeriod.valueOf(element
				.getAttribute("updatePeriod"))); //$NON-NLS-1$
		return feed;
	}
}
