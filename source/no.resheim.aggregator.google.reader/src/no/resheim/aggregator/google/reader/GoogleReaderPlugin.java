package no.resheim.aggregator.google.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.text.MessageFormat;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.google.reader.ui.CredentialsDialog;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.statushandlers.StatusManager;

public class GoogleReaderPlugin extends AbstractUIPlugin {

	private static GoogleReaderPlugin instance;

	public static final String PLUGIN_ID = "no.resheim.aggregator.google.reader";

	public GoogleReaderPlugin() {
		instance = this;
	}

	public static final GoogleReaderPlugin getDefault() {
		if (instance == null) {
			new GoogleReaderPlugin();
		}
		return instance;
	}

	private static final String ENCODING = "UTF-8";

	public static final String SECURE_STORAGE_NODE_ID = "no.resheim.aggregator.google";

	public static class Credentials {
		String login;
		String password;
	}

	/**
	 * Returns the credentials obtained from the secure storage.
	 * 
	 * @return the Google credentials
	 * @throws CoreException
	 */
	private static Credentials getCredentials() throws CoreException {
		try {
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(SECURE_STORAGE_NODE_ID);
			String login = feedNode.get(
					AggregatorPlugin.SECURE_STORAGE_USERNAME, null);
			String password = feedNode.get(
					AggregatorPlugin.SECURE_STORAGE_PASSWORD, null);
			if (login == null || password == null) {
				// Log in for the first time
				return getCredentialsUI();
			}
			Credentials c = new Credentials();
			c.login = login;
			c.password = password;
			return c;
		} catch (StorageException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not get credentials", e));
		}

	}

	/**
	 * Obtains credentials using the GUI, asking the user to type in user name
	 * and password.
	 * 
	 * @return the credentials.
	 */
	private static Credentials getCredentialsUI() {
		final Credentials c = new Credentials();
		final Display display = getDefault().getWorkbench().getDisplay();
		display.syncExec(new Runnable() {
			public void run() {
				try {
					CredentialsDialog dg = new CredentialsDialog(display
							.getActiveShell());
					ISecurePreferences root = SecurePreferencesFactory
							.getDefault().node(
									AggregatorPlugin.SECURE_STORAGE_ROOT);
					ISecurePreferences feedNode = root
							.node(SECURE_STORAGE_NODE_ID);
					if (dg.open() == Dialog.OK) {
						String login = dg.getLogin();
						String password = dg.getPassword();
						feedNode.put(AggregatorPlugin.SECURE_STORAGE_USERNAME,
								login, true);
						feedNode.put(AggregatorPlugin.SECURE_STORAGE_PASSWORD,
								password, true);
						c.login = login;
						c.password = password;
					}
				} catch (StorageException e) {
					StatusManager.getManager().handle(
							new Status(IStatus.ERROR, PLUGIN_ID,
									"Could not get credentials", e));
				}
			}
		});
		return c;
	}

	/**
	 * Log in to the Google account. If the process of obtaining credentials
	 * succeeded an OK status is returned. Otherwise a more detailed error
	 * description is returned. Any exception will return an error status.
	 * 
	 * @return status of the login procedure.
	 */
	public static IStatus login() {
		try {
			// Obtain credentials and pop up a log-in dialog if we have none.
			Credentials c = getCredentials();
			String sid = null;
			// Something bad happened or credentials were wrong if we enter
			// this loop here
			while ((sid = getSID(c)) == null) {
				// Use the GUI to get the credentials. Will also put these
				// in the secure storage.
				c = getCredentialsUI();
				// User cancelled or did not supply complete information.
				if (c.login == null || c.login.length() == 0
						|| c.password.length() == 0) {
					return Status.CANCEL_STATUS;
				}
			}
			// Google does not set the authentication cookie so we must do that
			// ourselves.
			CookieManager manager = new CookieManager();
			manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
			CookieHandler.setDefault(manager);
			HttpCookie cookie = new HttpCookie("SID", sid);
			cookie.setMaxAge(160000000);
			cookie.setPath("/");
			cookie.setDomain(".google.com");
			manager.getCookieStore().add(new URI("http://www.google.com"),
					cookie);
			return Status.OK_STATUS;
		} catch (Exception e) {
			if (e instanceof CoreException) {
				// Use the existing status code if we have one.
				return ((CoreException) e).getStatus();
			}
			return new Status(IStatus.ERROR, GoogleReaderPlugin.PLUGIN_ID,
					"Could not log into Google Reader.", e);
		}
	}

	public static String getToken() throws CoreException {
		String token = null;
		try {
			URL url = new URL("http://www.google.com/reader/api/0/token");
			URLConnection yc = AggregatorPlugin.getDefault().getConnection(url,
					true, null);
			BufferedReader rd = new BufferedReader(new InputStreamReader(yc
					.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				token = line;
			}
			rd.close();
		} catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not get authentication token from Google Reader", e));
		} catch (UnknownHostException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not get authentication token from Google Reader", e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not get authentication token from Google Reader", e));
		} catch (StorageException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not get authentication token from Google Reader", e));
		}
		return token;

	}

	/**
	 * Connects to the Google server and attempts to obtain a session identifier
	 * (SID) using the given credentials. If for some reason the SID could not
	 * be obtained an {@link CoreException} is normally thrown. Details of the
	 * failure should be obtained by examining the exception. If there was a
	 * HTTP error the response code is likely to be found in the status' code.
	 * 
	 * @param c
	 *            the credentials to use
	 * @return a session identifier or <code>null</code>
	 */
	private static String getSID(Credentials c) throws CoreException {
		String sid = null;
		int responseCode = 0;
		try {
			URL url = new URL("https://www.google.com/accounts/ClientLogin");
			HttpURLConnection yc = AggregatorPlugin.getDefault().getConnection(
					url, true, null);
			String data = URLEncoder.encode("Email", ENCODING) + "="
					+ URLEncoder.encode(c.login, ENCODING);
			data += "&" + URLEncoder.encode("Passwd", ENCODING) + "="
					+ URLEncoder.encode(c.password, ENCODING);
			data += "&" + URLEncoder.encode("source", ENCODING) + "="
					+ URLEncoder.encode("no.resheim.aggregator", ENCODING);
			yc.setDoOutput(true);
			yc.getOutputStream().write(data.getBytes());
			yc.getOutputStream().flush();
			responseCode = yc.getResponseCode();
			// This is a special case. If the credentials we were using were not
			// accepted we're indicating this by returning a <code>null</code>
			// SID rather than throwing an exception.
			if (responseCode == HttpURLConnection.HTTP_FORBIDDEN)
				return null;
			// Get the response
			BufferedReader rd = new BufferedReader(new InputStreamReader(yc
					.getInputStream()));
			String line;
			while ((line = rd.readLine()) != null) {
				if (line.startsWith("SID")) {
					sid = line.split("=")[1];
				}
			}
			// wr.close();
			rd.close();
		} catch (MalformedURLException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not obtain Google session identifier.", e));
		} catch (UnknownHostException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not obtain Google session identifier.", e));
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					responseCode, MessageFormat.format(
							"Could not obtain Google session identifier",
							new Object[] {}), e));
		} catch (StorageException e) {
			throw new CoreException(new Status(IStatus.ERROR, PLUGIN_ID,
					"Could not obtain Google session identifier.", e));
		}
		return sid;
	}
}
