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
import java.util.EnumSet;

import no.resheim.aggregator.core.AggregatorPlugin;
import no.resheim.aggregator.core.IFeedCollectionEventListener;
import no.resheim.aggregator.core.data.AggregatorCollection;
import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.Folder;
import no.resheim.aggregator.core.data.IAggregatorEventListener;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.data.AggregatorItemChangedEvent.EventType;
import no.resheim.aggregator.core.ui.AggregatorItemComparer;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.CollectionViewerLabelProvider;
import no.resheim.aggregator.core.ui.FeedViewerContentProvider;
import no.resheim.aggregator.core.ui.IArticleViewerListener;
import no.resheim.aggregator.core.ui.IFeedView;

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
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.part.ViewPart;

/**
 * The main view. This shows all the feeds, folders and labels available.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class RSSView extends ViewPart implements IFeedView,
		IFeedCollectionEventListener {

	class ArticleViewerListener implements IArticleViewerListener {

		public void statusTextChanged(String text) {
			if (text.length() == 0 && fLastArticleInfo != null) {
				setStatusText(fLastArticleInfo);
			} else {
				setStatusText(text);
			}
		}
	}

	class BrowserTitleListener implements TitleListener {

		public void changed(TitleEvent event) {
		}
	}

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

	private static final String BLANK = ""; //$NON-NLS-1$

	private static final String CONTEXT_ID = "no.resheim.aggregator.ui.context"; //$NON-NLS-1$

	public static final String DEFAULT_COLLECTION_ID = "no.resheim.aggregator.ui.defaultFeedCollection"; //$NON-NLS-1$

	private static final String MEMENTO_ORIENTATION = ".ORIENTATION"; //$NON-NLS-1$

	private final static Separator modify_separator = new Separator("modify"); //$NON-NLS-1$

	private final static Separator navigation_separator = new Separator(
			"navigation"); //$NON-NLS-1$
	private static final int NOTIFICATION_TIMER_INTERVAL = 10000;
	private static ArrayList<AggregatorItem> notificationItems;

	private final static Separator selection_separator = new Separator(
			"selection"); //$NON-NLS-1$

	private Action doubleClickAction;

	private AggregatorCollection fCollection;

	boolean fHorizontalLayout = false;

	private String fLastArticleInfo;

	/** The item that was last selected by the user */
	private Article fLastSelectionItem;

	/** Tree viewer to show all the feeds and articles */
	private TreeViewer viewer;

	/**
	 * Whether or not to split the navigation pane into two parts. Either we
	 * have a combined folders and articles view or these are shown separately.
	 */
	private boolean fSplitBrowsing = true;

	private Cursor fWaitCursor;

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

	private SashForm sashForm;

	/**
	 * The constructor.
	 */
	public RSSView() {
		if (notificationItems == null) {
			notificationItems = new ArrayList<AggregatorItem>();
		}
	}

	public void collectionInitialized(AggregatorCollection collection) {
		System.out.println("Collection initialized");
		if (collection.getId().equals(DEFAULT_COLLECTION_ID)) {
			setDefaultCollection();
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialise
	 * it.
	 */
	public void createPartControl(Composite parent) {

		fWaitCursor = new Cursor(getSite().getShell().getDisplay(),
				SWT.CURSOR_WAIT);
		sashForm = new SashForm(parent, SWT.SMOOTH);
		viewer = new TreeViewer(sashForm, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL | SWT.VIRTUAL);
		viewer.setUseHashlookup(true);
		viewer.setComparer(new AggregatorItemComparer());
		initDND();
		// If split browsing is enabled we'll only show folders in this tree
		// view
		if (fSplitBrowsing) {
			viewer.setContentProvider(new FeedViewerContentProvider(EnumSet
					.of(ItemType.FOLDER)));
		} else {
			viewer.setContentProvider(new FeedViewerContentProvider(EnumSet
					.allOf(ItemType.class)));
		}
		viewer.setLabelProvider(new CollectionViewerLabelProvider());

		// Enable tooltips for the tree items
		ColumnViewerToolTipSupport.enableFor(viewer);
		getSite().setSelectionProvider(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();

		PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
			public void run() {
				((IContextService) PlatformUI.getWorkbench().getService(
						IContextService.class)).activateContext(CONTEXT_ID);
			}
		});
		if (AggregatorPlugin.isInitialized()) {
			setDefaultCollection();
		}
		AggregatorPlugin.getDefault().addFeedCollectionListener(this);
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				// TODO: Just listen to what we need.
				refreshView();
			}
		});
	}

	@Override
	public void dispose() {
		AggregatorPlugin.getDefault().removeFeedCollectionListener(this);
		if (fWaitCursor != null) {
			fWaitCursor.dispose();
			fWaitCursor = null;
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
		manager.add(modify_separator);
		manager.add(selection_separator);
	}

	public AggregatorCollection getFeedCollection() {
		return fCollection;
	}

	public Viewer getFeedViewer() {
		return viewer;
	}

	public Layout getLayout() {
		return null;
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RSSView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
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

	/**
	 * Initialise drag and drop.
	 */
	private void initDND() {
		Transfer[] types = new Transfer[] { TextTransfer.getInstance() };
		int operations = DND.DROP_MOVE;
		final Tree tree = viewer.getTree();
		final DragSource source = new DragSource(tree, operations);
		source.setTransfer(types);
		final TreeItem[] dragSourceItem = new TreeItem[1];
		source.addDragListener(new DragSourceListener() {
			public void dragFinished(DragSourceEvent event) {
				if (event.detail == DND.DROP_MOVE) {
					dragSourceItem[0].dispose();
					dragSourceItem[0] = null;
				}
			};

			public void dragSetData(DragSourceEvent event) {
				AggregatorItem item = (AggregatorItem) dragSourceItem[0]
						.getData();
				event.data = item.getUUID().toString();
			}

			public void dragStart(DragSourceEvent event) {
				TreeItem[] selection = tree.getSelection();
				event.doit = false;
				if (selection.length > 0) {
					event.doit = true;
					dragSourceItem[0] = selection[0];
				}
			}
		});
		DropTarget target = new DropTarget(tree, operations);
		target.setTransfer(types);
		target.addDropListener(new ViewerDropAdapter(viewer) {

			private AggregatorItemParent getParent(TreeItem item) {
				if (item.getParentItem() == null) {
					return getFeedCollection();
				} else {
					return (AggregatorItemParent) item.getParentItem()
							.getData();
				}
			}

			@Override
			public boolean performDrop(Object data) {
				// The item being dragged...
				AggregatorItem source = (AggregatorItem) ((IStructuredSelection) viewer
						.getSelection()).getFirstElement();

				Object input = viewer.getInput();

				if (!(input instanceof AggregatorCollection)) {
					return false;
				}
				AggregatorItemParent newParent = (AggregatorItemParent) getCurrentTarget();
				AggregatorItemParent oldParent = getParent(dragSourceItem[0]);

				// Don't allow to drop into itself!
				if (newParent == source) {
					return false;
				}

				try {
					if (!newParent.equals(oldParent)) {
						getFeedCollection().move(source, oldParent, newParent);
					}
					// Tell our listeners that the deed is done
					getFeedCollection().notifyListerners(
							new Object[] { source }, EventType.MOVED);

				} catch (Exception e) {
					e.printStackTrace();
				}
				return true;
			}

			@Override
			public boolean validateDrop(Object target, int operation,
					TransferData transferType) {

				return target instanceof Folder;
			}
		});
	}

	private void makeActions() {

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
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
					AggregatorUIPlugin.getBrowser().openURL(new URL(url));
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
				if (viewer != null) {
					viewer.refresh();

				}
			};
		};
		Display.getDefault().asyncExec(update);
	}

	/**
	 * Listens to collection events and pops up a notification of new feed items
	 * has been added.
	 * 
	 * TODO: Use selected collection
	 */
	private void registerDesktopNotifications() {
		AggregatorCollection collection = AggregatorPlugin.getDefault()
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

	@Override
	public void saveState(IMemento memento) {
		final String name = this.getClass().getName();
		memento.putString(name + MEMENTO_ORIENTATION, Boolean
				.toString(fHorizontalLayout));
	}

	private void setDefaultCollection() {
		System.out.println("Using default collection");
		Display d = getViewSite().getShell().getDisplay();
		d.asyncExec(new Runnable() {
			public void run() {
				fCollection = AggregatorPlugin.getDefault().getFeedCollection(
						DEFAULT_COLLECTION_ID);
				viewer.setInput(fCollection);
				viewer.refresh();
			}
		});
		registerDesktopNotifications();
	}

	public void setFeedCollection(AggregatorCollection registry) {
		this.fCollection = registry;
		viewer.setInput(registry);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
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

	private void setStatusText(String text) {
		IStatusLineManager mgr = getViewSite().getActionBars()
				.getStatusLineManager();
		if (mgr != null) {
			mgr.setMessage(text);
		}
	}

}