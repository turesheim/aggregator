package no.resheim.aggregator;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import no.resheim.aggregator.data.DerbySQLStorage;
import no.resheim.aggregator.data.FeedRegistry;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
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

	/** The plug-in identifier */
	public static final String PLUGIN_ID = "no.resheim.aggregator"; //$NON-NLS-1$

	/** The shared plug-in instance */
	private static AggregatorPlugin plugin = null;

	private static final UUID DEFAULT_ID = UUID
			.fromString("067e6162-3b6f-4ae2-a171-2470b63dff00"); //$NON-NLS-1$

	/** The feed registry that the plug-in is using */
	private static FeedRegistry registry;
	private static IAggregatorStorage storage;
	/**
	 * Registries are declared using a symbolic name, for instance
	 * "com.foo.bar.registry". This member is used to map between the symbolic
	 * name and the universally unique identifier that is required internally.
	 */
	private HashMap<String, UUID> registryMap;

	/** Default feeds to add */
	public static ArrayList<String[]> DEFAULT_FEEDS;

	private ServiceTracker serviceTracker;

	/**
	 * The constructor
	 */
	public AggregatorPlugin() {
		plugin = this;
		DEFAULT_FEEDS = new ArrayList<String[]>();
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
		// Create a default feed registry
		registry = new FeedRegistry(DEFAULT_ID);
		storage = new DerbySQLStorage(registry, getStorageLocation(registry));
		storage.startup(new NullProgressMonitor());
		registry.load(storage);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		storage.shutdown();
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
	public static FeedRegistry getRegistry() {
		return registry;
	}

	/**
	 * Returns the location of the configuration directory. If possible the
	 * shared Eclipse configuration directory is used. If not the state location
	 * of the plug-in is used.
	 * 
	 * @return A pointer to the Aggregator configuration directory
	 */
	private IPath getStorageLocation(FeedRegistry registry) {
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
				registry.getUUID().toString());
	}

	public boolean isDebugging() {
		return true;
	}

}
