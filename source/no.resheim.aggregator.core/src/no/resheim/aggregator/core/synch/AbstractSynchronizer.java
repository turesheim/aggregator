package no.resheim.aggregator.core.synch;

import java.text.MessageFormat;

import no.resheim.aggregator.core.data.Feed;
import no.resheim.aggregator.core.data.FeedCollection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractSynchronizer extends Job implements
		IFeedSynchronizer {
	private static final String EMPTY_STRING = "";
	protected Feed feed;
	protected FeedCollection collection;

	public AbstractSynchronizer() {
		super(EMPTY_STRING);
		setPriority(Job.DECORATE);
		setUser(false);
	}

	@Override
	protected abstract IStatus run(IProgressMonitor monitor);

	public void setCollection(FeedCollection collection) {
		this.collection = collection;
	}

	public void setFeed(Feed feed) {
		this.feed = feed;
		setName(MessageFormat.format(Messages.FeedUpdateJob_Title,
				new Object[] { feed.getTitle() }));
	}

}
