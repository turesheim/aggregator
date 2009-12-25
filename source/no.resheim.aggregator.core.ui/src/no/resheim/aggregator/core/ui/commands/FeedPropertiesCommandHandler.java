/*******************************************************************************
 * Copyright (c) 2007-2008 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui.commands;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.Subscription;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.SubscriptionWorkingCopy;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.FeedPropertiesDialog;
import no.resheim.aggregator.core.ui.IFeedView;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class FeedPropertiesCommandHandler extends
		AbstractAggregatorCommandHandler implements IHandler {

	public FeedPropertiesCommandHandler() {
		super(true);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			AggregatorCollection registry = ((IFeedView) part).getFeedCollection();
			if (registry == null) {
				return null;
			}
			AggregatorItem o = getSelection(event);
			if (o instanceof Folder) {
				Subscription feed = ((Folder) o).getFeed();
				if (feed != null) {
					SubscriptionWorkingCopy wc = new SubscriptionWorkingCopy(feed);
					FeedPropertiesDialog dialog = new FeedPropertiesDialog(
							HandlerUtil.getActiveShell(event),
							Messages.FeedPropertiesCommand_Title,
							AggregatorUIPlugin.getDefault().getImageRegistry()
									.get(AggregatorUIPlugin.IMG_FEED_OBJ),
							Messages.FeedPropertiesCommand_Description, 0,
							new String[] {
									Messages.FeedPropertiesCommand_OK,
									Messages.FeedPropertiesCommand_CANCEL
							}, 0, wc);
					if (dialog.open() == Window.OK) {
						// Remove the node
						if (!feed.isAnonymousAccess() && wc.isAnonymousAccess()) {
							removeCredentials(feed);

						}
						if (!wc.isAnonymousAccess()) {
							setCredentials(wc);
						}
						feed.updateFromWorkingCopy(wc);
						registry.updateFeedData(feed);
						// Reflect the feed title in the folder title.
						o.setTitle(feed.getTitle());
						registry.notifyListerners(new Object[] {
							o
						}, EventType.CHANGED);
					}
				}
			}
		}
		return null;
	}

	private void setCredentials(SubscriptionWorkingCopy wc) {
		ISecurePreferences root = SecurePreferencesFactory.getDefault().node(
				AggregatorPlugin.SECURE_STORAGE_ROOT);
		ISecurePreferences feedNode = root.node(wc.getUUID().toString());
		try {
			feedNode.put(AggregatorPlugin.SECURE_STORAGE_USERNAME, wc
					.getUsername(), true);
			feedNode.put(AggregatorPlugin.SECURE_STORAGE_PASSWORD, wc
					.getPassword(), true);
		} catch (StorageException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected boolean handleSelection(ISelection selection) {
		return isFeedSelected(selection);
	}

	private void removeCredentials(Subscription wc) {
		try {
			ISecurePreferences root = SecurePreferencesFactory.getDefault()
					.node(AggregatorPlugin.SECURE_STORAGE_ROOT);
			ISecurePreferences feedNode = root.node(wc.getUUID().toString());
			feedNode.removeNode();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
