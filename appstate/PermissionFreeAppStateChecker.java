import java.util.HashSet;

import android.app.Activity;

/**
 * @author David Bauduin
 */
class PermissionFreeAppStateChecker extends AppStateChecker {

	private HashSet<Activity> activities = new HashSet<Activity>(8);
	private boolean launched = false;
	
	@Override
	public void onStartActivity(Activity activity) {
		this.activities.add(activity);
	}
	
	@Override
	public void onStopActivity(Activity activity) {
		this.activities.remove(activity);
	}

	@Override
	public void run() {
		if (this.launched) {
			if (this.activities.size() == 0) {
				this.launched = false;
				if (this.appStateCheckerListener != null) {
					this.appStateCheckerListener.onApplicationDidEnterBackground();
				}
			}
		} else {
			if (this.activities.size() > 0) {
				this.launched = true;
				if (this.appStateCheckerListener != null) {
					this.appStateCheckerListener.onApplicationDidEnterForeground();
				}
			}
		}
	}
	
}
