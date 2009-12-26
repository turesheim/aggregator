package no.resheim.aggregator.rcp.macosx;

import org.eclipse.swt.internal.cocoa.NSApplication;
import org.eclipse.swt.internal.cocoa.NSResponder;
import org.eclipse.swt.internal.cocoa.NSString;
import org.eclipse.swt.internal.cocoa.OS;

/**
 * @author Prakash G.R. (grprakash@gmail.com)
 * 
 */
@SuppressWarnings("restriction")
public class NSDockTile extends NSResponder {

	private static final long sel_setBadgeLabel_ = OS
			.sel_registerName("setBadgeLabel:");
	private static final long sel_dockTile_ = OS.sel_registerName("dockTile");
	private static final long sel_display_ = OS.sel_registerName("display");

	public NSDockTile(long id) {
		super(id);
	}

	public static NSDockTile getApplicationDockTile() {
		NSApplication sharedApplication = NSApplication.sharedApplication();
		long id = OS.objc_msgSend(sharedApplication.id, sel_dockTile_);
		NSDockTile dockTile = new NSDockTile(id);
		return dockTile;
	}

	public void setBadgeLabel(String badgeLabel) {
		NSString nsBadgeLabel = NSString.stringWith(badgeLabel);
		OS.objc_msgSend(this.id, sel_setBadgeLabel_, nsBadgeLabel.id);
		OS.objc_msgSend(this.id, sel_display_);
	}
}