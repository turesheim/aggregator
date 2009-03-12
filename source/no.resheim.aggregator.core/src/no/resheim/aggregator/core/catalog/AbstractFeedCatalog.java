/*******************************************************************************
 * Copyright (c) 2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.catalog;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

public abstract class AbstractFeedCatalog implements IFeedCatalog,
		IExecutableExtension {

	private String icon;
	private String id;
	private String name;
	private String bundle;
	private String synchronizer;

	public URL getIcon() {
		URL url = FileLocator.find(Platform.getBundle(bundle), new Path(icon),
				null);
		return url;
	}

	public String getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getSynchronizerId() {
		return (synchronizer == null) ? DEFAULT_SYNCHRONIZER_ID : synchronizer;
	}

	public void setInitializationData(IConfigurationElement config,
			String propertyName, Object data) {
		bundle = config.getContributor().getName();
		icon = config.getAttribute("icon");
		id = config.getAttribute("id");
		name = config.getAttribute("name");
		synchronizer = config.getAttribute("synchronizer");
	}

	public String toString() {
		return name;
	}
}
