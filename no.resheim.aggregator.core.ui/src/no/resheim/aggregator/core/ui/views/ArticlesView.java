package no.resheim.aggregator.core.ui.views;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import no.resheim.aggregator.core.data.AggregatorItem;
import no.resheim.aggregator.core.data.AggregatorItemParent;
import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.data.AggregatorItem.ItemType;
import no.resheim.aggregator.core.ui.AggregatorItemComparer;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.CollectionViewerLabelProvider;
import no.resheim.aggregator.core.ui.FeedViewerContentProvider;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

/**
 * A view that lists articles.
 * 
 * @author Torkild Ulvøy Resheim
 * @since 1.0
 */
public class ArticlesView extends ViewPart implements ISelectionListener {

	private TreeViewer viewer;
	private Font fLabelFont;

	/**
	 * A table layout that will automatically resize all columns.
	 * 
	 * @author Torkild Ulvøy Resheim
	 * @since 1.0
	 */
	public class AutoResizeTableLayout extends TableLayout implements
			ControlListener {
		private final Tree table;
		private List<ColumnLayoutData> columns = new ArrayList<ColumnLayoutData>();
		private boolean autosizing = false;

		public AutoResizeTableLayout(Tree table) {
			this.table = table;
			table.addControlListener(this);
		}

		public void addColumnData(ColumnLayoutData data) {
			columns.add(data);
			super.addColumnData(data);
		}

		public void controlMoved(ControlEvent e) {
		}

		public void controlResized(ControlEvent e) {
			if (autosizing)
				return;
			autosizing = true;
			try {
				autoSizeColumns();
			} finally {
				autosizing = false;
			}
		}

		private void autoSizeColumns() {
			int width = table.getClientArea().width;

			// XXX: Layout is being called with an invalid value
			// the first time it is being called on Linux.
			// This method resets the layout to null,
			// so we run it only when the value is OK.
			// if (width <= 1)
			// return;

			TreeColumn[] tableColumns = table.getColumns();
			int size = Math.min(columns.size(), tableColumns.length);
			int[] widths = new int[size];
			int fixedWidth = 0;
			int numberOfWeightColumns = 0;
			int totalWeight = 0;

			// First calculate space occupied by fixed columns.
			for (int i = 0; i < size; i++) {
				ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
				if (col instanceof ColumnPixelData) {
					int pixels = ((ColumnPixelData) col).width;
					widths[i] = pixels;
					fixedWidth += pixels;
				} else if (col instanceof ColumnWeightData) {
					ColumnWeightData cw = (ColumnWeightData) col;
					numberOfWeightColumns++;
					int weight = cw.weight;
					totalWeight += weight;
				} else {
					throw new IllegalStateException(
							"Unknown column layout data");
				}
			}
			// Do we have columns that have a weight?
			if (numberOfWeightColumns > 0) {
				// Now, distribute the rest
				// to the columns with weight.
				int rest = width - fixedWidth;
				int totalDistributed = 0;
				for (int i = 0; i < size; i++) {
					ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
					if (col instanceof ColumnWeightData) {
						ColumnWeightData cw = (ColumnWeightData) col;
						int weight = cw.weight;
						int pixels = totalWeight == 0 ? 0 : weight * rest
								/ totalWeight;
						if (pixels < cw.minimumWidth)
							pixels = cw.minimumWidth;
						totalDistributed += pixels;
						widths[i] = pixels;
					}
				}

				// Distribute any remaining pixels
				// to columns with weight.
				int diff = rest - totalDistributed;
				for (int i = 0; diff > 0; i++) {
					if (i == size)
						i = 0;
					ColumnLayoutData col = (ColumnLayoutData) columns.get(i);
					if (col instanceof ColumnWeightData) {
						++widths[i];
						--diff;
					}
				}
			}

			for (int i = 0; i < size; i++) {
				if (tableColumns[i].getWidth() != widths[i])
					tableColumns[i].setWidth(widths[i]);
			}
		}
	}

	@Override
	public void createPartControl(Composite parent) {

		fLabelFont = PlatformUI.getWorkbench().getThemeManager()
				.getCurrentTheme().getFontRegistry().get(
						"no.resheim.aggregator.core.ui.articleLabelFont");
		Display display = getSite().getShell().getDisplay();
		fLabelStyle = new TextStyle(fLabelFont, display
				.getSystemColor(SWT.COLOR_WHITE), display
				.getSystemColor(SWT.COLOR_GRAY));
		final TextLayout textLayout = new TextLayout(display);

		fWaitCursor = new Cursor(display, SWT.CURSOR_WAIT);
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL
				| SWT.VIRTUAL);
		Tree tree = viewer.getTree();
		AutoResizeTableLayout layout = new AutoResizeTableLayout(tree);
		tree.setLayout(layout);

		tree.setHeaderVisible(true);
		TreeColumn c1 = new TreeColumn(tree, SWT.LEFT);
		c1.setText("Title");
		layout.addColumnData(new ColumnWeightData(75, 300, false));
		TreeColumn c2 = new TreeColumn(tree, SWT.RIGHT);
		c2.setText("Labels");
		layout.addColumnData(new ColumnWeightData(25, 140, false));
		viewer.setUseHashlookup(true);
		viewer.setComparer(new AggregatorItemComparer());
		viewer.setContentProvider(new FeedViewerContentProvider(EnumSet
				.of(ItemType.ARTICLE)));
		viewer.setLabelProvider(new CollectionViewerLabelProvider());
		final Tree table = viewer.getTree();
		table.addListener(SWT.PaintItem, new Listener() {

			public void handleEvent(Event event) {
				if (event.index == 1 && event.text != null) {
					textLayout.setStyle(fLabelStyle, event.start, event.end);
					textLayout.draw(event.gc, event.x, event.y);
				}
			}
		});
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(this);
		getSite().setSelectionProvider(viewer);
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				// TODO: Just listen to what we actually need.
				refreshView();
			}
		});
		createActions();
		hookContextMenu();
		System.out.println("ArticlesView.createPartControl()");
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

	private Cursor fWaitCursor;
	private IAction fNextUnreadAction;

	public void dispose() {
		if (fWaitCursor != null) {
			fWaitCursor.dispose();
			fWaitCursor = null;
		}
	}

	@Override
	public void setFocus() {
	}

	private void createActions() {
		fNextUnreadAction = new Action() {
			public void run() {
				AggregatorItem item = null;
				ISelection selection = viewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					item = (AggregatorItem) ((IStructuredSelection) selection)
							.getFirstElement();
				}
				TreeItem ti = ((Tree) viewer.getControl()).getSelection()[0];
				int index = ti.getParent().indexOf(ti);
				ti = ((Tree) viewer.getControl()).getItem(index + 1);
				viewer.setSelection(new StructuredSelection(ti.getData()));
				if (item instanceof Article && !((Article) item).isRead()) {
					try {
						item.getCollection().setRead(item);
					} catch (CoreException e) {
						e.printStackTrace();
					}
				}
			}
		};
		fNextUnreadAction.setText("Next Unread@CTRL+.");
	}

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object o = ss.getFirstElement();
			if (o instanceof AggregatorItemParent) {
				getSite().getShell().setCursor(fWaitCursor);
				viewer.setInput(o);
				setPartName(((AggregatorItemParent) o).getTitle());
				getSite().getShell().setCursor(null);
			}
		}
	}

	private final static Separator modify_separator = new Separator("modify"); //$NON-NLS-1$

	private final static Separator navigation_separator = new Separator(
			"navigation"); //$NON-NLS-1$

	private final static Separator selection_separator = new Separator(
			"selection"); //$NON-NLS-1$
	private TextStyle fLabelStyle;

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ArticlesView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	/**
	 * Fills the context menu with actions.
	 * 
	 * @param manager
	 *            The menu manager
	 */
	private void fillContextMenu(IMenuManager manager) {
		manager.add(fNextUnreadAction);
		manager.add(navigation_separator);
		manager.add(modify_separator);
		manager.add(selection_separator);
	}
}
