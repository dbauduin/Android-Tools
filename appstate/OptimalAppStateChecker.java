import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.Context;
import android.os.PowerManager;

/**
 * @author David Bauduin
 */
class OptimalAppStateChecker extends AppStateChecker {

	private ActivityManager activityManager = null;
	private PowerManager powerManager = null;
	private KeyguardManager keyguardManager = null;
	private String packageName = null;
	private boolean launched = false;
	
	public OptimalAppStateChecker(Context context) {
		this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		this.keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		this.packageName = context.getPackageName();
	}
	
	@Override
	public void onStartActivity(Activity activity) {
		// Nothing to do
	}
	
	@Override
	public void onStopActivity(Activity activity) {
		// Nothing to do
	}

	@Override
	public void run() {
		boolean background = false;
		boolean foreground = false;
		List<RunningTaskInfo> runningTasks = this.activityManager.getRunningTasks(1);
		if (runningTasks.size() > 0) {
			RunningTaskInfo lastTask = runningTasks.get(0);
			if (this.packageName.equals(lastTask.baseActivity.getPackageName())) {
				if (!this.launched) {
					foreground = true;
				} else {
					// Detect if the screen has been turned off or has been locked
					if (!this.powerManager.isScreenOn() || this.keyguardManager.inKeyguardRestrictedInputMode()) {
						background = true;
					}
				}
			} else if (this.launched) {
				background = true;
			}
		}
		if (foreground) {
			this.launched = true;
			if (this.appStateCheckerListener != null) {
				this.appStateCheckerListener.onApplicationDidEnterForeground();
			}
		} else if (background) {
			this.launched = false;
			if (this.appStateCheckerListener != null) {
				this.appStateCheckerListener.onApplicationDidEnterBackground();
			}
		}
	}
	
}
