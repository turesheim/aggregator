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
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 */
public class RSSView extends ViewPart implements IFeedView,
		IFeedCollectionEventListener {

	private static final String MEMENTO_ORIENTATION = ".ORIENTATION"; //$NON-NLS-1$
	private static final String BLANK = ""; //$NON-NLS-1$
	private SashForm sashForm;

	private FeedCollection registry;

	/** The web browser we're using */
	private IWebBrowser browser;

	/** Tree viewer to show all the feeds and articles */
	private FeedTreeViewer treeView;

	private DrillDownAdapter drillDownAdapter;

	private Action doubleClickAction;

	private Action verticalLayoutAction;

	private Action horizontalLayoutAction;

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
		// Register for collection events
		AggregatorPlugin.getDefault().addFeedCollectionListener(this);
	}

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
		drillDownAdapter.addNavigationActions(manager);
		manager.add(new Separator("modify")); //$NON-NLS-1$
		manager.add(new Separator("selection")); //$NON-NLS-1$
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

	public FeedCollection getFeedCollection() {
		return registry;
	}

	public void setFeedCollection(FeedCollection registry) {
		this.registry = registry;
		treeView.setInput(registry);

	}

	public void collectionInitialized(FeedCollection collection) {
		if (collection.getId().equals(AggregatorPlugin.DEFAULT_COLLECTION_ID)) {
			Display d = getViewSite().getShell().getDisplay();
			d.asyncExec(new Runnable() {
				public void run() {
					registry = AggregatorPlugin.getDefault().getFeedCollection(
							AggregatorPlugin.DEFAULT_COLLECTION_ID);
					treeView.setInput(registry);
				}
			});
		}
	}

	public Viewer getFeedViewer() {
		return treeView;
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

	boolean fHorizontalLayout;

	@Override
	public void saveState(IMemento memento) {
		super.saveState(memento);
		final String name = this.getClass().getName();
		memento.putString(name + MEMENTO_ORIENTATION, Boolean
				.toString(fHorizontalLayout));
	}
}