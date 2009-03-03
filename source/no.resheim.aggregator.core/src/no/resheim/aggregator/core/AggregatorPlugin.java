package no.resheim.aggregator.core;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import no.resheim.aggregator.core.catalog.IFeedCatalog;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.IAggregatorStorage;
import no.resheim.aggregator.core.data.internal.DerbySQLStorage;
import no.resheim.aggregator.core.data.internal.MemoryStorage;
import no.resheim.aggregator.core.synch.AbstractSynchronizer;

import org.eclipse.core.net.proxy.IProxyData;
import org.eclipse.core.net.proxy.IProxyService;
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
import org.eclipse.equinox.security.storage.EncodingUtils;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

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

	public static final String FEEDS_EXTENSION_ID = "no.resheim.aggregator.core.feeds"; //$NON-NLS-1$

	/**
	 * The default updater to use.
	 */
	public static final String DEFAULT_SYNCHRONIZER_ID = "no.resheim.aggregator.core.directSynchronizer";

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
	 * @return a new {@link AbstractSynchronizer} instance
	 */
	public static AbstractSynchronizer getSynchronizer(String id) {

		IExtensionPoint ePoint = Platform.getExtensionRegistry()
				.getExtensionPoint(FEEDS_EXTENSION_ID);
		IConfigurationElement[] synchronizers = ePoint
				.getConfigurationElements();
		for (IConfigurationElement configurationElement : synchronizers) {
			if (configurationElement.getName().equals("synchronizer")
					&& configurationElement.getAttribute("id").equals(id)) {
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

	/**
	 * @uml.property name="defaultCollection"
	 * @uml.associationEnd
	 */
	private FeedCollection defaultCollection;

	/**
	 * Name of the default feed collection.
	 */

	/**
	 * The constructor
	 */
	public AggregatorPlugin() {
		plugin = this;
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

	public IFeedCatalog[] getCatalogs() {
		ArrayList<IFeedCatalog> catalogs = new ArrayList<IFeedCatalog>();
		final IExtensionRegistry ereg = Platform.getExtensionRegistry();
		IConfigurationElement[] elements = ereg
				.getConfigurationElementsFor(AggregatorPlugin.FEEDS_EXTENSION_ID);
		for (IConfigurationElement configurationElement : elements) {
			if (configurationElement.getName().equals("catalog")) {
				try {
					Object object = configurationElement
							.createExecutableExtension("class");
					if (object instanceof IFeedCatalog) {
						IFeedCatalog catalog = (IFeedCatalog) object;
						if (catalog.isEnabled()) {
							catalogs.add(catalog);
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}

			}
		}
		return catalogs.toArray(new IFeedCatalog[catalogs.size()]);
	}

	private void initialize() {
		final IExtensionRegistry ereg = Platform.getExtensionRegistry();
		final Job job = new Job(Messages.AggregatorPlugin_Initializing) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				synchronized (collectionMap) {
					IStatus status = addCollections(ereg, monitor);
					if (status.isOK()) {
						// getFeeds();
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
				.getConfigurationElementsFor(FEEDS_EXTENSION_ID);
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
	 * 
	 * @param url
	 *            the URL to connect to
	 * @param whether
	 *            or not to connect anonymously
	 * @param the
	 *            node to use in the secure storage
	 * @return the URL connection
	 * @throws IOException
	 * @throws StorageException
	 * @throws UnknownHostException
	 */
	public URLConnection getConnection(URL url, boolean anonymous, String nodeId)
			throws IOException, StorageException, UnknownHostException {
		IProxyData proxyData = null;
		URLConnection yc = null;
		IProxyService service = getProxyService();
		// We might be unable to get a proxy service in that we'll try to
		// connect anyways.
		if (service != null && service.isProxiesEnabled()) {
			// Note that we expect the URL protocol to one of HTTP and HTTPS.
			proxyData = service.getProxyDataForHost(url.getHost(), url
					.getProtocol().toUpperCase());
		}
		// If we have no proxy data we'll use a direct connection
		if (proxyData == null) {
			yc = url.openConnection();
		} else {
			InetSocketAddress sockAddr = new InetSocketAddress(InetAddress
					.getByName(proxyData.getHost()), proxyData.getPort());
			Proxy proxy = new Proxy(Type.HTTP, sockAddr);
			yc = url.openConnection(proxy);
		}
		if (proxyData != null && proxyData.isRequiresAuthentication()) {
			String proxyLogin = proxyData.getUserId()
					+ ":" + proxyData.getPassword(); //$NON-NLS-1$
			yc.setRequestProperty("Proxy-Authorization", "Basic " //$NON-NLS-1$ //$NON-NLS-2$
					+ EncodingUtils.encodeBase64(proxyLogin.getBytes()));
		}
		if (!anonymous) {
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(nodeId);
			String credentials = feedNode.get(
					AggregatorPlugin.SECURE_STORAGE_USERNAME, "")
					+ ":" //$NON-NLS-1$
					+ feedNode
							.get(AggregatorPlugin.SECURE_STORAGE_PASSWORD, "");
			yc
					.setRequestProperty(
							"Authorization", "Basic " + EncodingUtils.encodeBase64(credentials.getBytes())); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return yc;
	}

	/**
	 * Returns the proxy service for this bundle.
	 * 
	 * @return The proxy service
	 */
	private IProxyService getProxyService() {
		Bundle bundle = Platform.getBundle(CORE_NET_BUNDLE);
		if (bundle.getState() == Bundle.UNINSTALLED) {
			return null;
		}
		try {
			bundle.start();
		} catch (BundleException e1) {
			e1.printStackTrace();
		}
		// The bundle may not be active yet and hence the service we're
		// looking
		// for is unavailable. We must wait until everything is ready.
		while (bundle.getState() != Bundle.ACTIVE) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		ServiceReference ref = bundle.getBundleContext().getServiceReference(
				IProxyService.class.getName());
		if (ref != null)
			return (IProxyService) bundle.getBundleContext().getService(ref);
		return null;
	}
}
