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
import no.resheim.aggregator.IFeedCollectionEventListener;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.ArticleViewer;
import no.resheim.aggregator.core.ui.FeedTreeViewer;
import no.resheim.aggregator.core.ui.FeedViewerContentProvider;
import no.resheim.aggregator.core.ui.FeedViewerLabelProvider;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.PreferenceConstants;
import no.resheim.aggregator.data.Article;
import no.resheim.aggregator.data.Feed;
import no.resheim.aggregator.data.FeedCollection;
import no.resheim.aggregator.data.Folder;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.Bundle;

/**
 * 
 * The view context menu has three groups:
 * <ul>
 * <li>navigation</li>
 * <li>modify</li>
 * <li>selection</li>
 * </ul>
 * 
 */
public class RSSView extends ViewPart implements IFeedView,
		IFeedCollectionEventListener {

	private static final String CONTEXT_ID = "no.resheim.aggregator.ui.context"; //$NON-NLS-1$
	private static final String CORE_PLUGIN_ID = "no.resheim.aggregator.core"; //$NON-NLS-1$

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
					setStatusText(item.getDetails());
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

		private void setStatusText(String text) {
			IStatusLineManager mgr = getViewSite().getActionBars()
					.getStatusLineManager();
			if (mgr != null) {
				mgr.setMessage(text);
			}

		}
	}

	private static final String BLANK = ""; //$NON-NLS-1$
	public static final String DEFAULT_COLLECTION_ID = "no.resheim.aggregator.ui.defaultFeedCollection"; //$NON-NLS-1$
	private static final String MEMENTO_ORIENTATION = ".ORIENTATION"; //$NON-NLS-1$

	private final static Separator modify_separator = new Separator("modify"); //$NON-NLS-1$

	private final static Separator navigation_separator = new Separator(
			"navigation"); //$NON-NLS-1$

	private final static Separator selection_separator = new Separator(
			"selection"); //$NON-NLS-1$

	/** The web browser we're using */
	private IWebBrowser browser;

	private Action doubleClickAction;

	private DrillDownAdapter drillDownAdapter;

	boolean fHorizontalLayout;

	/** The item that was last selected by the user */
	private Article fLastSelectionItem;

	private Action horizontalLayoutAction;

	private FeedViewerLabelProvider labelProvider;

	/**
	 * Marks the last selected item as read and updates it's and the parent's
	 * labels.
	 */
	private Runnable markAsRead = new Runnable() {
		public void run() {
			if (fLastSelectionItem != null) {
				try {
					fCollection.setRead(fLastSelectionItem);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	};

	/** Preference: mark previewed items as read */
	private boolean pPreviewIsRead;

	private ArticleViewer preview;

	private FeedCollection fCollection;

	private SashForm sashForm;

	/** Tree viewer to show all the feeds and articles */
	private FeedTreeViewer treeView;

	/** Action to change the layout of the view */
	private Action verticalLayoutAction;

	/**
	 * The constructor.
	 */
	public RSSView() {
	}

	public void collectionInitialized(FeedCollection collection) {
		if (collection.getId().equals(DEFAULT_COLLECTION_ID)) {
			setDefaultCollection();
		}
	}

	private void setDefaultCollection() {
		Display d = getViewSite().getShell().getDisplay();
		d.asyncExec(new Runnable() {
			public void run() {
				fCollection = AggregatorPlugin.getDefault().getFeedCollection(
						DEFAULT_COLLECTION_ID);
				treeView.setInput(fCollection);
				labelProvider.setCollection(fCollection);
			}
		});
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalToolBar(bars.getToolBarManager());
	}

	/**
	 * Contributes to the local view menu.
	 */
	private void contributeToMenu() {
		MenuManager mgr = new MenuManager(Messages.RSSView_LayoutMenuTitle,
				"layout"); //$NON-NLS-1$
		getViewSite().getActionBars().getMenuManager().add(mgr);
		mgr.add(verticalLayoutAction);
		mgr.add(horizontalLayoutAction);
	}

	private boolean createBrowser() {
		try {
			browser = PlatformUI.getWorkbench().getBrowserSupport()
					.createBrowser(
							IWorkbenchBrowserSupport.NAVIGATION_BAR
									| IWorkbenchBrowserSupport.LOCATION_BAR
									| IWorkbenchBrowserSupport.AS_EDITOR,
							AggregatorPlugin.PLUGIN_ID,
							Messages.RSSView_BrowserTitle, BLANK);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
		treeView
				.setLabelProvider(labelProvider = new FeedViewerLabelProvider());
		treeView.addSelectionChangedListener(new ViewSelectionListener());

		// Enable tooltips for the tree items
		ColumnViewerToolTipSupport.enableFor(treeView);

		getSite().setSelectionProvider(treeView);

		preview = new ArticleViewer(sashForm, SWT.NONE);
		sashForm.setWeights(new int[] {
				1, 1
		});

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		contributeToMenu();
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

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				((IContextService) PlatformUI.getWorkbench().getService(
						IContextService.class)).activateContext(CONTEXT_ID);
			}
		});
		// Register for collection events
		if (Platform.getBundle(CORE_PLUGIN_ID).getState() == Bundle.ACTIVE) {
			setDefaultCollection();
		} else {
			AggregatorPlugin.getDefault().addFeedCollectionListener(this);
		}
	}

	/**
	 * Fills the context menu with actions.
	 * 
	 * @param manager
	 *            The menu manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(navigation_separator);
		drillDownAdapter.addNavigationActions(manager);
		manager.add(modify_separator);
		manager.add(selection_separator);
	}

	/**
	 * Fills the local tool bar with actions.
	 * 
	 * @param manager
	 *            The menu manager
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		drillDownAdapter.addNavigationActions(manager);
	}

	public FeedCollection getFeedCollection() {
		return fCollection;
	}

	public Viewer getFeedViewer() {
		return treeView;
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

	private void hookDoubleClickAction() {
		treeView.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		// It's possible that no saved state exists yet
		if (memento == null) {
			fHorizontalLayout = true;
			return;
		}
		final String name = this.getClass().getName();
		if (memento.getString(name + MEMENTO_ORIENTATION) != null) {
			if (memento.getString(name + MEMENTO_ORIENTATION) != null) {
				fHorizontalLayout = Boolean.parseBoolean(memento.getString(name
						+ MEMENTO_ORIENTATION));
			}
		} else {
			fHorizontalLayout = true;
		}

	}

	private void makeActions() {

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
						fCollection.setRead((Article) obj);
					}
				} catch (Exception e) {
				}
			}
		};

		verticalLayoutAction = new Action(Messages.RSSView_VerticalActionTitle,
				Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.VERTICAL);
				setChecked(true);
				horizontalLayoutAction.setChecked(false);
				fHorizontalLayout = false;
			}
		};
		verticalLayoutAction.setImageDescriptor(AggregatorUIPlugin
				.getImageDescriptor("icons/etool16/vertical.gif")); //$NON-NLS-1$

		horizontalLayoutAction = new Action(
				Messages.RSSView_HorizontalActionTitle, Action.AS_RADIO_BUTTON) {
			public void run() {
				sashForm.setOrientation(SWT.HORIZONTAL);
				setChecked(true);
				verticalLayoutAction.setChecked(false);
				fHorizontalLayout = true;
			}
		};
		horizontalLayoutAction.setImageDescriptor(AggregatorUIPlugin
				.getImageDescriptor("icons/etool16/horizontal.gif")); //$NON-NLS-1$
		if (fHorizontalLayout) {
			horizontalLayoutAction.setChecked(true);
			verticalLayoutAction.setChecked(false);
			sashForm.setOrientation(SWT.HORIZONTAL);
		} else {
			horizontalLayoutAction.setChecked(false);
			verticalLayoutAction.setChecked(true);
			sashForm.setOrientation(SWT.VERTICAL);
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

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		final String name = this.getClass().getName();
		memento.putString(name + MEMENTO_ORIENTATION, Boolean
				.toString(fHorizontalLayout));
	}

	public void setFeedCollection(FeedCollection registry) {
		this.fCollection = registry;
		treeView.setInput(registry);
		labelProvider.setCollection(registry);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		treeView.getControl().setFocus();
	}

	private void updateFromPreferences() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		pPreviewIsRead = store
				.getBoolean(PreferenceConstants.P_PREVIEW_IS_READ);
	}
}