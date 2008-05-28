package no.resheim.aggregator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Feed.Archiving;
import no.resheim.aggregator.data.Feed.UpdatePeriod;
import no.resheim.aggregator.data.internal.DerbySQLStorage;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

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

	/** The plug-in identifier */
	public static final String PLUGIN_ID = "no.resheim.aggregator"; //$NON-NLS-1$

	/** The shared plug-in instance */
	private static AggregatorPlugin plugin = null;

	/**
	 * Registries are declared using a symbolic name, for instance
	 * "com.foo.bar.registry". This member is used to map between the symbolic
	 * name and the universally unique identifier that is required internally.
	 */
	private HashMap<String, FeedCollection> registryMap;

	private ArrayList<IAggregatorStorage> storageList;

	/** Default feeds to add */
	public static ArrayList<String[]> DEFAULT_FEEDS;

	private ServiceTracker serviceTracker;

	public static final String DEFAULT_REGISTRY_ID = "no.resheim.aggregator.core.defaultFeedCollection"; //$NON-NLS-1$

	/**
	 * The constructor
	 */
	public AggregatorPlugin() {
		plugin = this;
		DEFAULT_FEEDS = new ArrayList<String[]>();
		registryMap = new HashMap<String, FeedCollection>();
		storageList = new ArrayList<IAggregatorStorage>();
	}

	/**
	 * Returns the proxy service for this bundle.
	 * 
	 * @return The proxy service
	 */
	public IProxyService getProxyService() {
		if (serviceTracker == null) {
			this.serviceTracker = new ServiceTracker(getBundle()
					.getBundleContext(), IProxyService.class.getName(), null);

		}
		return (IProxyService) this.serviceTracker.getService();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		fDebugging = super.isDebugging();
		initialize();
		// Read in all the default feeds.
		InputStream is = FileLocator.openStream(this.getBundle(), new Path(
				"/feeds.txt"), false); //$NON-NLS-1$
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line = null;
		while ((line = br.readLine()) != null) {
			if (!line.startsWith("#")) { //$NON-NLS-1$
				String[] fields = line.split(","); //$NON-NLS-1$
				DEFAULT_FEEDS.add(new String[] {
						fields[0].trim(), fields[1].trim()
				});
			}
		}
	}

	private boolean fDebugging;

	@Override
	public boolean isDebugging() {
		return fDebugging;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		if (isDebugging()) {
			System.out.println("[DEBUG] Shutting down storage"); //$NON-NLS-1$
		}
		for (IAggregatorStorage storage : storageList) {
			System.out.println(" - " + storage);
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
		return plugin;
	}

	/**
	 * @return the feeds
	 */
	public FeedCollection getFeedCollection(String id) {
		return registryMap.get(id);
	}

	/**
	 * Returns the location of the configuration directory. If possible the
	 * shared Eclipse configuration directory is used. If not the state location
	 * of the plug-in is used.
	 * 
	 * @return A pointer to the Aggregator configuration directory
	 */
	private IPath getStorageLocation(FeedCollection registry) {
		// Location location = Platform.getConfigurationLocation();
		// if (location != null) {
		// URL configURL = location.getURL();
		// if (configURL != null && configURL.getProtocol().startsWith("file"))
		// //$NON-NLS-1$
		// {
		// return new File(configURL.getFile(), PLUGIN_ID);
		// }
		// }
		// If the configuration directory is read-only,
		// then return an alternate location
		// rather than null or throwing an Exception.
		return getStateLocation().makeAbsolute().addTrailingSeparator().append(
				registry.getId().toString());
	}

	public Collection<FeedCollection> getCollections() {
		return registryMap.values();
	}

	private void initialize() {
		IExtensionRegistry ereg = Platform.getExtensionRegistry();
		addCollections(ereg);
		addFeeds(ereg);
	}

	private void addCollections(IExtensionRegistry ereg) {
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
					FeedCollection registry = new FeedCollection(id, pub);
					registry.setTitle(name);
					registryMap.put(id, registry);
					// Create the storage for the registry
					IAggregatorStorage storage = new DerbySQLStorage(registry,
							getStorageLocation(registry));
					IStatus status = storage.startup(new NullProgressMonitor());
					if (status.isOK()) {
						registry.initialize(storage);
						storageList.add(storage);
					} else {
						System.out.println(status);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void addFeeds(IExtensionRegistry ereg) {
		IConfigurationElement[] elements = ereg
				.getConfigurationElementsFor(REGISTRY_EXTENSION_ID);
		for (IConfigurationElement element : elements) {
			// We're not going to allow an exception here to disrupt.
			try {
				if (element.getName().equals("feed")) { //$NON-NLS-1$
					String url = element.getAttribute("url"); //$NON-NLS-1$
					String id = element.getAttribute("collection"); //$NON-NLS-1$
					FeedCollection collection = getFeedCollection(id);
					if (collection != null) {
						if (!collection.hasFeed(url)) {
							collection.add(createNewFeed(collection, element));
						}
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
		feed.setUUID(UUID.randomUUID());
		feed.setParentUUID(parent.getUUID());
		// Initialize with default values from the preference store.
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
