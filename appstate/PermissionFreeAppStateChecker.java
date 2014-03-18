import java.util.HashSet;

import android.app.Activity;
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
class PermissionFreeAppStateChecker extends AppStateChecker {
	
	///////////////////////
	//     Variables     //
	///////////////////////
	
	private PowerManager powerManager = null;
	private KeyguardManager keyguardManager = null;
	
	private HashSet<Activity> activities = new HashSet<Activity>(8);
	private boolean launched = false;
	private boolean backgroundDetected = false;
	
	
	///////////////////////
	//    Constructors   //
	///////////////////////
	
	public PermissionFreeAppStateChecker(Context context) {
		this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
		this.keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
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
				background();
			} else if (!this.powerManager.isScreenOn() || (VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH && this.keyguardManager.inKeyguardRestrictedInputMode())) {
				// The screen is off
				background();
				this.backgroundDetected = true;
			}
		} else {
			if (this.activities.size() > 0) {
				foreground();
			}
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
