package no.resheim.aggregator.google.reader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.UnknownHostException;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.google.reader.ui.CredentialsDialog;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;

public class GoogleReaderPlugin extends AbstractUIPlugin {

	private static GoogleReaderPlugin instance;

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

	public static final String NODE_ID = "no.resheim.aggregator.google";

	private static void showError(Exception e) {
		MessageDialog
				.openError(new Shell(), "Could not log-in", e.getMessage());
	}

	public static class Credentials {
		String login;
		String password;
	}

	private static Credentials getCredentials() {
		try {
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(NODE_ID);
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
			showError(e);
			return null;
		}

	}

	private static Credentials getCredentialsUI() {
		try {
			CredentialsDialog dg = new CredentialsDialog(new Shell());
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(NODE_ID);
			if (dg.open() == Dialog.OK) {
				String login = dg.getLogin();
				String password = dg.getPassword();
				feedNode.put(AggregatorPlugin.SECURE_STORAGE_USERNAME, login,
						true);
				feedNode.put(AggregatorPlugin.SECURE_STORAGE_PASSWORD,
						password, true);
				Credentials c = new Credentials();
				c.login = login;
				c.password = password;
				return c;
			}
		} catch (StorageException e) {
			showError(e);
		}
		return null;
	}

	/**
	 * Log in to the Google account.
	 */
	public static boolean login() {
		// Obtain credentials and pop up a log-in dialog if we have none.
		Credentials c = getCredentials();
		String sid = null;
		// Something bad happened or credentials were wrong if we enter here
		while ((sid = getSID(c)) == null) {
			// Use the GUI to get the credentials. Will also put these in
			// the secure storage.
			c = getCredentialsUI();
			// User cancelled or did not supply complete information.
			if (c == null || c.login.length() == 0 || c.password.length() == 0) {
				return false;
			}
		}
		// Google does not set the authentication cookie so we must do that
		// ourselves.
		CookieManager manager = new CookieManager();
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		CookieHandler.setDefault(manager);
		HttpCookie cookie = new HttpCookie("SID", sid);
		cookie.setMaxAge(1600000000);
		cookie.setPath("/");
		cookie.setDomain(".google.com");
		try {
			manager.getCookieStore().add(new URI("http://www.google.com"),
					cookie);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return true;
	}

	private static String getSID(Credentials c) {
		String sid = null;
		try {
			URL url = new URL("https://www.google.com/accounts/ClientLogin");
			URLConnection yc = AggregatorPlugin.getDefault().getConnection(url,
					true, null);
			String data = URLEncoder.encode("Email", ENCODING) + "="
					+ URLEncoder.encode(c.login, ENCODING);
			data += "&" + URLEncoder.encode("Passwd", ENCODING) + "="
					+ URLEncoder.encode(c.password, ENCODING);
			data += "&" + URLEncoder.encode("source", ENCODING) + "="
					+ URLEncoder.encode("no.resheim.aggregator", ENCODING);
			yc.setDoOutput(true);
			yc.getOutputStream().write(data.getBytes());
			yc.getOutputStream().flush();
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
			showError(e);
		} catch (UnknownHostException e) {
			showError(e);
		} catch (IOException e) {
			showError(e);
		} catch (StorageException e) {
			showError(e);
		}
		return sid;
	}
}
