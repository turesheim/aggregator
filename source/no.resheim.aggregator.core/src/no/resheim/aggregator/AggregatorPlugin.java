package no.resheim.aggregator;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import no.resheim.aggregator.model.FeedRegistry;

import org.eclipse.core.net.proxy.IProxyService;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;
import org.osgi.util.tracker.ServiceTracker;

/**
 * The aggregator plug-in. This type is responsible for maintaning feed states
 * and notify listeners about any changes.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class AggregatorPlugin extends Plugin {

	/** The plug-in identifier */
	public static final String PLUGIN_ID = "no.resheim.aggregator"; //$NON-NLS-1$

	/** The shared plug-in instance */
	private static AggregatorPlugin plugin = null;

	/** The feed registry that the plug-in is using */
	private static FeedRegistry registry;

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
		// if (serviceTracker == null) {
		// this.serviceTracker = new ServiceTracker(getBundle()
		// .getBundleContext(), IProxyService.class.getName(), null);
		//
		// }
		// return (IProxyService) this.serviceTracker.getService();
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		registry = new FeedRegistry();
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
	public File getConfigDir() {
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
		return getStateLocation().toFile();
	}

	public boolean isDebugging() {
		return true;
	}

}
