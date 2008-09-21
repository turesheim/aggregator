package no.resheim.aggregator.core.ui;

import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IStartup;
import org.osgi.framework.BundleException;

public class KickStarter implements IStartup {

	public void earlyStartup() {
		try {
			Platform.getBundle("no.resheim.aggregator.core").start(); //$NON-NLS-1$
		} catch (BundleException e) {
			e.printStackTrace();
		}
	}

}
