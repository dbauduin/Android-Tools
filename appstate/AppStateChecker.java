import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

/**
 * @author David Bauduin
 */
abstract class AppStateChecker implements Runnable {
	
	protected AppStateCheckerListener appStateCheckerListener = null;
	
	public abstract void onStartActivity(Activity activity);
	public abstract void onStopActivity(Activity activity);
	
	public static AppStateChecker newInstance(Context context) {
		AppStateChecker instance = null;
		// Instanciate appStateChecker depending on the GET_TASKS permission
		if (context.checkCallingOrSelfPermission(android.Manifest.permission.GET_TASKS) == PackageManager.PERMISSION_GRANTED) {
			// Have access to task list. Use an appStateChecker which should always provide good results.
			instance = new OptimalAppStateChecker(context);
		} else {
			// Don't have access to task list. Use an appStateChecker which provides good results in almost all cases.
			// (If you have a not full screen activity above another one and you rotate your device, if you put your application in background, this appStateChecker probably won't detect it.)
			instance = new PermissionFreeAppStateChecker(context);
		}
		return instance;
	}
	
	public AppStateCheckerListener getAppStateCheckerListener() {
		return appStateCheckerListener;
	}
	
	public void setAppStateCheckerListener(AppStateCheckerListener appStateCheckerListener) {
		this.appStateCheckerListener = appStateCheckerListener;
	}
	
}
