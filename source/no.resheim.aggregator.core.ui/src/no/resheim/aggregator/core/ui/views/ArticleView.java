package no.resheim.aggregator.core.ui.views;

import no.resheim.aggregator.core.data.Article;
import no.resheim.aggregator.core.ui.AggregatorUIPlugin;
import no.resheim.aggregator.core.ui.ArticleViewer;
import no.resheim.aggregator.core.ui.PreferenceConstants;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

public class ArticleView extends ViewPart implements ISelectionListener {

	ArticleViewer viewer;

	private void updateFromPreferences() {
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		pPreviewIsRead = store
				.getBoolean(PreferenceConstants.P_PREVIEW_IS_READ);
	}

	@Override
	public void createPartControl(Composite parent) {
		viewer = new ArticleViewer(parent, SWT.NONE);
		getSite().getWorkbenchWindow().getSelectionService()
				.addPostSelectionListener(this);
		updateFromPreferences();
		IPreferenceStore store = AggregatorUIPlugin.getDefault()
				.getPreferenceStore();
		store.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				updateFromPreferences();
				if (event.getProperty().equals(
						PreferenceConstants.P_PREVIEW_FONT)) {
					viewer.show(fLastSelectionItem);
				}
			}
		});
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	/** Preference: mark previewed items as read */
	private boolean pPreviewIsRead;

	/** The item that was last selected by the user */
	private Article fLastSelectionItem;

	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection ss = (IStructuredSelection) selection;
			Object o = ss.getFirstElement();
			if (o instanceof Article) {
				Article item = (Article) ss.getFirstElement();
				setStatusText(item.getStatusString());
				viewer.show(item);
				if (pPreviewIsRead && !item.isRead()) {
					fLastSelectionItem = item;
					Display.getCurrent().timerExec(5000, markAsRead);
				}
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

	/**
	 * Marks the last selected item as read and updates it's and the parent's
	 * labels.
	 */
	private Runnable markAsRead = new Runnable() {
		public void run() {
			if (fLastSelectionItem != null) {
				try {
					fLastSelectionItem.getCollection().setRead(
							fLastSelectionItem);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	};

}
