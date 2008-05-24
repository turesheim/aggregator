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
package no.resheim.aggregator.ui.views;

import java.net.URL;

import no.resheim.aggregator.AggregatorPlugin;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.ArticleViewer;
import no.resheim.aggregator.core.ui.FeedTreeViewer;
import no.resheim.aggregator.core.ui.FeedViewerContentProvider;
import no.resheim.aggregator.core.ui.FeedViewerLabelProvider;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.PreferenceConstants;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.Folder;
import no.resheim.aggregator.data.FeedRegistry;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 */
public class RSSView extends ViewPart implements IFeedView {

	private static final String BLANK = ""; //$NON-NLS-1$
	private SashForm sashForm;

	private FeedRegistry registry;

	/** The web browser we're using */
	private IWebBrowser browser;

	/** Tree viewer to show all the feeds and articles */
	private FeedTreeViewer treeView;

	private DrillDownAdapter drillDownAdapter;

	private Action doubleClickAction;

	private Action renameAction;

	/** Preference: mark previewed items as read */
	private boolean pPreviewIsRead;

	/** The item that was last selected by the user */
	private Article fLastSelectionItem;

	/**
	 * Marks the last selected item as read and updates it's and the parent's
	 * labels.
	 */
	private Runnable markAsRead = new Runnable() {
		public void run() {
			if (fLastSelectionItem != null) {
				registry.setRead(fLastSelectionItem);
			}
		}
	};

	private ArticleViewer preview;

	/**
	 * Listens to selection events in the
	 */
	class ViewSelectionListener implements ISelectionChangedListener {

		public void selectionChanged(SelectionChangedEvent event) {
			// Reset in case it's not an Item that is selected.
			fLastSelectionItem = null;
			if (event.getSelection() instanceof IStructuredSelection) {
				IStructuredSelection selection = (IStructuredSelection) event
						.getSelection();
				if (selection.getFirstElement() instanceof Article) {
					Article item = (Article) selection.getFirstElement();
					preview.show(item);
					if (pPreviewIsRead && !item.isRead()) {
						fLastSelectionItem = item;
						Display.getCurrent().timerExec(5000, markAsRead);
					}
				}
				if (selection.getFirstElement() instanceof Feed) {
					preview.show((Feed) selection.getFirstElement());
				}
			}
		}
	}

	class NameSorter extends ViewerComparator {

		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			if (e1 instanceof Article && e2 instanceof Article) {
				Article i1 = (Article) e1;
				Article i2 = (Article) e2;
				long t1 = i1.getPublicationDate();
				long t2 = i2.getPublicationDate();
				if (t1 == 0)
					t1 = i1.getAdded();
				if (t2 == 0)
					t2 = i2.getAdded();
				// Note that we're putting the oldest items last!
				if (t1 == t2)
					return 0;
				else if (t1 < t2)
					return 1;
				else
					return -1;
			}
			return super.compare(viewer, e1, e2);
		}
	}

	private void refreshView() {
		Runnable update = new Runnable() {
			public void run() {
				if (treeView != null) {
					treeView.refresh();

				}
			};
		};
		Display.getDefault().asyncExec(update);
	}

	private void updateFromPreferences() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		pPreviewIsRead = store
				.getBoolean(PreferenceConstants.P_PREVIEW_IS_READ);
	}

	/**
	 * The constructor.
	 */
	public RSSView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		updateFromPreferences();

		sashForm = new SashForm(parent, SWT.SMOOTH);
		treeView = new FeedTreeViewer(sashForm, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(treeView);
		treeView.setContentProvider(new FeedViewerContentProvider());
		treeView.setLabelProvider(new FeedViewerLabelProvider());
		treeView.setComparator(new NameSorter());
		registry = AggregatorPlugin.getRegistry();
		treeView.setInput(registry);
		treeView.addSelectionChangedListener(new ViewSelectionListener());
		getSite().setSelectionProvider(treeView);

		preview = new ArticleViewer(sashForm, SWT.NONE);
		sashForm.setWeights(new int[] {
				1, 1
		});

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		createBrowser();
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateFromPreferences();
				if (event.getProperty().equals(
						PreferenceConstants.P_PREVIEW_FONT)) {
					preview.show(fLastSelectionItem);
				} else {
					refreshView();
				}
			}

		});

	}

	private boolean createBrowser() {
		try {
			browser = PlatformUI.getWorkbench().getBrowserSupport()
					.createBrowser(
							IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.AS_EDITOR,
							AggregatorPlugin.PLUGIN_ID, "Aggregator Feed",
							BLANK);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RSSView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(treeView.getControl());
		treeView.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, treeView);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Fills the context menu with actions.
	 * 
	 * @param manager
	 *            The menu manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(new Separator("selection")); //$NON-NLS-1$
		manager.add(renameAction);
		manager.add(new Separator("navigation")); //$NON-NLS-1$
		drillDownAdapter.addNavigationActions(manager);
		ISelection selection = treeView.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		renameAction.setEnabled(obj instanceof Folder);
	}

	/**
	 * Fills the local tool bar with actions.
	 * 
	 * @param manager
	 *            The menu manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {

		renameAction = new RenameAction(treeView);
		renameAction.setText("Rename");
		renameAction.setToolTipText("Renames the selected item");

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = treeView.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				String url = BLANK;
				if (obj instanceof Folder)
					return;
				if (obj instanceof Feed)
					url = ((Feed) obj).getURL();
				if (obj instanceof Article) {
					url = ((Article) obj).getLink();
				}
				try {
					browser.openURL(new URL(url));
					// Make the item as read if we were able to open the
					// browser on it's URL.
					if (obj instanceof Article) {
						registry.setRead((Article) obj);
					}
				} catch (Exception e) {
				}
			}
		};
	}

	private void hookDoubleClickAction() {
		treeView.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeView.getControl().setFocus();
	}

	public FeedRegistry getFeedRegistry() {
		return registry;
	}
}