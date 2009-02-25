/*******************************************************************************
 * Copyright (c) 2007-2009 Torkild Ulvøy Resheim.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Torkild Ulvøy Resheim - initial API and implementation
 *******************************************************************************/
package no.resheim.aggregator.core.ui.views;

import java.net.URL;
import java.util.ArrayList;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.IFeedCollectionEventListener;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.IAggregatorEventListener;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.ArticleViewer;
import no.resheim.aggregator.core.ui.FeedTreeViewer;
import no.resheim.aggregator.core.ui.FeedViewerContentProvider;
import no.resheim.aggregator.core.ui.FeedViewerLabelProvider;
import no.resheim.aggregator.core.ui.IArticleViewerListener;
import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.core.ui.PreferenceConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
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
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

/**
 * 
 * The view context menu has three groups:
 * <ul>
 * <li>navigation</li>
 * <li>modify</li>
 * <li>selection</li>
 * </ul>
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RSSView extends ViewPart implements IFeedView,
		IFeedCollectionEventListener {

	private static final int NOTIFICATION_TIMER_INTERVAL = 10000;
	private static final String CONTEXT_ID = "no.resheim.aggregator.ui.context"; //$NON-NLS-1$

	class BrowserTitleListener implements TitleListener {

		public void changed(TitleEvent event) {
		}
	}

	private static ArrayList<AggregatorItem> notificationItems;

	class NotificationTimer implements Runnable {

		public void run() {
			synchronized (notificationItems) {
				if (notificationItems.size() > 0) {
					new NotificationPopup(RSSView.this, notificationItems
							.toArray(new AggregatorItem[notificationItems
									.size()]));
					notificationItems.clear();
				}
				Display display = getViewSite().getShell().getDisplay();
				display.timerExec(NOTIFICATION_TIMER_INTERVAL, this);
			}
		}
	}

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
					fLastArticleInfo = item.getDetails();
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
	}

	class ArticleViewerListener implements IArticleViewerListener {

		public void statusTextChanged(String text) {
			if (text.length() == 0 && fLastArticleInfo != null) {
				setStatusText(fLastArticleInfo);
			} else {
				setStatusText(text);
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

	private static final String BLANK = ""; //$NON-NLS-1$
	public static final String DEFAULT_COLLECTION_ID = "no.resheim.aggregator.ui.defaultFeedCollection"; //$NON-NLS-1$
	private static final String MEMENTO_ORIENTATION = ".ORIENTATION"; //$NON-NLS-1$

	private final static Separator modify_separator = new Separator("modify"); //$NON-NLS-1$

	private final static Separator navigation_separator = new Separator(
			"navigation"); //$NON-NLS-1$

	private final static Separator selection_separator = new Separator(
			"selection"); //$NON-NLS-1$

	private Action doubleClickAction;

	boolean fHorizontalLayout;

	/** The item that was last selected by the user */
	private Article fLastSelectionItem;

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

	/**
	 * The constructor.
	 */
	public RSSView() {
		if (notificationItems == null) {
			notificationItems = new ArrayList<AggregatorItem>();
		}
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
		registerDesktopNotifications();
	}

	@Override
	public void dispose() {
		treeView.removeSelectionChangedListener(fViewSelectionListener);
		AggregatorPlugin.getDefault().removeFeedCollectionListener(this);
	}

	private String fLastArticleInfo;
	private ViewSelectionListener fViewSelectionListener;

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {

		updateFromPreferences();

		sashForm = new SashForm(parent, SWT.SMOOTH);
		treeView = new FeedTreeViewer(sashForm, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
		treeView.setContentProvider(new FeedViewerContentProvider());
		treeView
				.setLabelProvider(labelProvider = new FeedViewerLabelProvider());
		fViewSelectionListener = new ViewSelectionListener();
		treeView.addSelectionChangedListener(fViewSelectionListener);

		// Enable tooltips for the tree items
		ColumnViewerToolTipSupport.enableFor(treeView);

		getSite().setSelectionProvider(treeView);

		preview = new ArticleViewer(sashForm, SWT.NONE);
		preview.addListener(new ArticleViewerListener());
		sashForm.setWeights(new int[] {
				1, 1
		});

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
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
		if (AggregatorPlugin.getDefault().isCollectionsInitialized()) {
			setDefaultCollection();
		} else {
			AggregatorPlugin.getDefault().addFeedCollectionListener(this);
		}
		// Make sure we update the layout visibly
		setLayout(fHorizontalLayout ? Layout.HORIZONTAL : Layout.VERTICAL);
	}

	/**
	 * Fills the context menu with actions.
	 * 
	 * @param manager
	 *            The menu manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(navigation_separator);
		manager.add(modify_separator);
		manager.add(selection_separator);
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
			fHorizontalLayout = false;
			return;
		}
		final String name = this.getClass().getName();
		if (memento.getString(name + MEMENTO_ORIENTATION) != null) {
			if (memento.getString(name + MEMENTO_ORIENTATION) != null) {
				fHorizontalLayout = Boolean.parseBoolean(memento.getString(name
						+ MEMENTO_ORIENTATION));
			}
		} else {
			fHorizontalLayout = false;
		}
	}

	private void registerDesktopNotifications() {
		FeedCollection collection = AggregatorPlugin.getDefault()
				.getFeedCollection(null);
		collection.addFeedListener(new IAggregatorEventListener() {

			public void aggregatorItemChanged(
					final AggregatorItemChangedEvent event) {
				if (event.getType().equals(EventType.CREATED)) {
					Object[] items = event.getItems();
					for (Object object : items) {
						if (object instanceof Article) {
							notificationItems.add((AggregatorItem) object);
						}
					}
				}
			}

		});
		// Create the timer that will pop up a notification if we have any items
		// in the notificationItems list.
		NotificationTimer timer = new NotificationTimer();
		Display display = getViewSite().getShell().getDisplay();
		display.asyncExec(timer);
	}

	private void makeActions() {

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = treeView.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				String url = BLANK;
				if (obj instanceof Folder) {
					if (((Folder) obj).getFeed() != null) {
						url = ((Folder) obj).getFeed().getLink();
					}
				} else if (obj instanceof Article) {
					url = ((Article) obj).getLink();
				} else
					return;
				try {
					AggregatorUIPlugin.getSharedBrowser().openURL(new URL(url));
					// Make the item as read if we were able to open the
					// browser on it's URL.
					if (obj instanceof Article) {
						fCollection.setRead((Article) obj);
					}
				} catch (Exception e) {
				}
			}
		};

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
		final String name = this.getClass().getName();
		memento.putString(name + MEMENTO_ORIENTATION, Boolean
				.toString(fHorizontalLayout));
		System.out.println(fHorizontalLayout);
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

	public Layout getLayout() {
		return null;
	}

	public void setLayout(Layout layout) {
		switch (layout) {
		case HORIZONTAL:
			sashForm.setOrientation(SWT.HORIZONTAL);
			fHorizontalLayout = true;
			break;
		case VERTICAL:
			sashForm.setOrientation(SWT.VERTICAL);
			fHorizontalLayout = false;
			break;
		default:
			break;
		}
	}
}