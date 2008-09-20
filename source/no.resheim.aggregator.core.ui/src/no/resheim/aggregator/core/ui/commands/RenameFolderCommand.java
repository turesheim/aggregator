package no.resheim.aggregator.core.ui.commands;

import no.resheim.aggregator.core.ui.IFeedView;
import no.resheim.aggregator.data.AggregatorItem;
import no.resheim.aggregator.data.FeedCollection;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TreeEditor;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class RenameFolderCommand extends AbstractAggregatorCommandHandler {

	public RenameFolderCommand() {
		super(false);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof IFeedView) {
			FeedCollection collection = ((IFeedView) part).getFeedCollection();
			if (collection == null) {
				return null;
			}
			Viewer viewer = ((IFeedView) part).getFeedViewer();
			if (viewer instanceof TreeViewer) {
				TreeViewer treeViewer = (TreeViewer) viewer;
				renameItem(treeViewer.getTree().getSelection()[0], treeViewer,
						getSelection(event), collection);
			}
		}
		return null;
	}

	private void renameItem(final TreeItem item, final TreeViewer treeView,
			final AggregatorItem aggregatorItem, final FeedCollection collection) {
		TreeEditor treeEditor = new TreeEditor(treeView.getTree());
		treeEditor.horizontalAlignment = SWT.LEFT;
		treeEditor.grabHorizontal = true;
		// Create a text field to do the editing
		final Text text = new Text(treeView.getTree(), SWT.NONE);
		text.setText(aggregatorItem.getTitle());
		text.selectAll();
		text.setFocus();

		// If the text field loses focus, set its text into the tree
		// and end the editing session
		text.addFocusListener(new FocusAdapter() {
			public void focusLost(FocusEvent event) {
				item.setText(text.getText());
				aggregatorItem.setTitle(text.getText());
				collection.rename(aggregatorItem);
				text.dispose();
			}
		});

		// If they hit Enter, set the text into the tree and end the editing
		// session. If they hit Escape, ignore the text and end the editing
		// session
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				switch (event.keyCode) {
				case SWT.CR:
					// Enter hit--set the text into the tree and drop through
					item.setText(text.getText());
					aggregatorItem.setTitle(text.getText());
					collection.rename(aggregatorItem);
				case SWT.ESC:
					// End editing session
					text.dispose();
					break;
				}
			}
		});
		// Set the text field into the editor
		treeEditor.setEditor(text, item);
	}

	@Override
	protected boolean handleSelection(ISelection selection) {
		boolean enabled = super.handleSelection(selection);
		if (enabled) {
			enabled = isFolderSelected(selection);
		}
		return enabled;
	}
}
