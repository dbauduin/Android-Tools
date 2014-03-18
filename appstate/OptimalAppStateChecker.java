import java.util.List;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.PowerManager;

/**
 * @author David Bauduin
 */
class OptimalAppStateChecker extends AppStateChecker {

	///////////////////////
	//     Variables     //
	///////////////////////
	
	private ActivityManager activityManager = null;
	private PowerManager powerManager = null;
	private KeyguardManager keyguardManager = null;
	private String packageName = null;
	private boolean launched = false;
	private boolean backgroundDetected = false;
	
	
	///////////////////////
	//    Constructors   //
	///////////////////////
	
	public OptimalAppStateChecker(Context context) {
		this.activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		this.keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
		this.packageName = context.getPackageName();
		// Register USER_PRESENT receiver to detect when the screen is on and the lock screen is not displayed
		IntentFilter filter = new IntentFilter(Intent.ACTION_USER_PRESENT);
		if (VERSION.SDK_INT < VERSION_CODES.HONEYCOMB) {
			filter.addAction(Intent.ACTION_SCREEN_OFF);
		}
		context.getApplicationContext().registerReceiver(new ScreenActionsReceiver(), filter);
	}
	
	
	///////////////////////
	//      Methods      //
	///////////////////////
	
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
					// Detect if the screen is on
					if (this.powerManager.isScreenOn()) {
						if (VERSION.SDK_INT < VERSION_CODES.ICE_CREAM_SANDWICH || !this.keyguardManager.inKeyguardRestrictedInputMode()) {
							foreground = true;
						}
					}
				} else {
					// Detect if the screen has been turned off or has been locked
					if (!this.powerManager.isScreenOn() || (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH && this.keyguardManager.inKeyguardRestrictedInputMode())) {
						background = true;
						this.backgroundDetected = true;
					}
				}
			} else if (this.launched) {
				background = true;
			}
		}
		if (foreground) {
			foreground();
		} else if (background) {
			background();
		}
	}
	
	private void background() {
		this.launched = false;
		if (this.appStateCheckerListener != null) {
			this.appStateCheckerListener.onApplicationDidEnterBackground();
		}
	}
	
	private void foreground() {
		this.launched = true;
		if (this.appStateCheckerListener != null) {
			this.appStateCheckerListener.onApplicationDidEnterForeground();
		}
	}
	
	
	///////////////////////
	//   Inner classes   //
	///////////////////////
	
	public class ScreenActionsReceiver extends BroadcastReceiver {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_USER_PRESENT.equals(intent.getAction()) && backgroundDetected) {
				backgroundDetected = false;
				if (!launched) {
					foreground();
				}
			} else if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				if (launched) {
					backgroundDetected = true;
					background();
				}
			}
		}
		
	}
	
}
