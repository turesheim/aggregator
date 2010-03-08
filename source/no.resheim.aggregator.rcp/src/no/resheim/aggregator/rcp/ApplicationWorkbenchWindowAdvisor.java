package no.resheim.aggregator.rcp;

import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

	private static final int DEFAULT_HEIGHT = 600;
	private static final int DEFAULT_WIDTH = 800;

	public ApplicationWorkbenchWindowAdvisor(
			IWorkbenchWindowConfigurer configurer) {
		super(configurer);
	}

	public void preWindowOpen() {
		// IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
		// configurer.setInitialSize(new Point(DEFAULT_WIDTH, DEFAULT_HEIGHT));
		// configurer.setShowCoolBar(true);
		// configurer.setShowStatusLine(true);
		// configurer.setShowFastViewBars(true);
		// configurer.setShowPerspectiveBar(false);
		// configurer.setShowProgressIndicator(true);
	}
}
