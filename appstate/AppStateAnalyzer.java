import java.util.HashSet;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Handler;

/**
 * @author David Bauduin
 */
public class AppStateAnalyzer implements AppStateCheckerListener {

	///////////////////////
	//     Variables     //
	///////////////////////
	
	private final static long CHECK_APP_STATE_DELAY = 300; // In ms
	
	private final static String SHARED_PREFERENCES_NAME = "appStateData";
	private final static String SHARED_PREFERENCES_CONTEXT_ID_KEY = "contextId";
	private final static String SHARED_PREFERENCES_LAUNCH_NUMBER_KEY = "launchNumber";

	private boolean initialized = false;
	private AppStateChecker appStateChecker = null;
	private Handler handler = new Handler();
	private HashSet<AppStateListener> listeners = new HashSet<AppStateListener>();
	private SharedPreferences sharedPreferences = null;
	private int contextIdentifier = 0;
	private boolean gingerbreadOrAbove = false;
	
	
	///////////////////////
	//    Constructors   //
	///////////////////////
	
	private AppStateAnalyzer() {
		this.initialized = false;
	}
	
	
	///////////////////////
	//      Methods      //
	///////////////////////
	
	/* Public */
	
	public static void onStartActivity(Activity activity) {
		getInstance(activity).internalOnStartActivity(activity);
	}
	
	public static void onStopActivity(Activity activity) {
		getInstance(activity).internalOnStopActivity(activity);
	}
	
	public static void addAppStateListener(AppStateListener listener) {
		getInstance(null).listeners.add(listener);
	}
	
	public static void removeAppStateListener(AppStateListener listener) {
		getInstance(null).listeners.remove(listener);
	}
	
	
	@Override
	public void onApplicationDidEnterForeground() {
		int lastContextIdentifier = this.sharedPreferences.getInt(SHARED_PREFERENCES_CONTEXT_ID_KEY, 0);
		boolean awakeFromBackground = (lastContextIdentifier == this.contextIdentifier);
		
		int launchNumber = this.sharedPreferences.getInt(SHARED_PREFERENCES_LAUNCH_NUMBER_KEY, 0) + 1;
		saveIntForKey(launchNumber, SHARED_PREFERENCES_LAUNCH_NUMBER_KEY);
		
		// Call listeners
		HashSet<AppStateListener> deadListeners = null;
		for (AppStateListener listener : this.listeners) {
			try {
				listener.onApplicationDidEnterForeground(awakeFromBackground, launchNumber);
			} catch (Exception e) {
				if (deadListeners == null) {
					deadListeners = new HashSet<AppStateListener>();
				}
				deadListeners.add(listener);
			}
		}
		if (deadListeners != null) {
			this.listeners.removeAll(deadListeners);
		}
	}

	@Override
	public void onApplicationDidEnterBackground() {
		// Save current context identifier
		saveIntForKey(this.contextIdentifier, SHARED_PREFERENCES_CONTEXT_ID_KEY);

		// Call listeners
		HashSet<AppStateListener> deadListeners = null;
		for (AppStateListener listener : this.listeners) {
			try {
				listener.onApplicationDidEnterBackground();
			} catch (Exception e) {
				if (deadListeners == null) {
					deadListeners = new HashSet<AppStateListener>();
				}
				deadListeners.add(listener);
			}
		}
		if (deadListeners != null) {
			this.listeners.removeAll(deadListeners);
		}
	}
	
	
	/* Private */
	
	private void initialize(Context context) {
		this.initialized = true;
		
		this.sharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
		
		this.gingerbreadOrAbove = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD);
		
		this.appStateChecker = AppStateChecker.newInstance(context);
		this.appStateChecker.setAppStateCheckerListener(this);
	}
	
	private void internalOnStartActivity(Activity activity) {
		this.handler.removeCallbacks(this.appStateChecker);
		this.handler.postDelayed(this.appStateChecker, CHECK_APP_STATE_DELAY);
		
		this.contextIdentifier = activity.getApplicationContext().hashCode();
		
		this.appStateChecker.onStartActivity(activity);
	}
	
	private void internalOnStopActivity(Activity activity) {
		this.appStateChecker.onStopActivity(activity);
		
		this.handler.removeCallbacks(this.appStateChecker);
		this.handler.postDelayed(this.appStateChecker, CHECK_APP_STATE_DELAY);
	}
	

	@SuppressLint("NewApi")
	private void saveIntForKey(int valueToSave, String key) {
		Editor editor = this.sharedPreferences.edit();
		editor.putInt(key, valueToSave);
		if (this.gingerbreadOrAbove) {
			editor.apply();
		} else {
			editor.commit();
		}
	}
	
	
	///////////////////////
	// Getters / Setters //
	///////////////////////
	
	private static AppStateAnalyzer getInstance(Context context) {
		AppStateAnalyzer instance = AppStateAnalyzerSingleton.INSTANCE;
		if (!instance.initialized && context != null) {
			instance.initialize(context);
		}
		return instance;
	}
	
	
	///////////////////////
	//   Inner classes   //
	///////////////////////
	
	private static class AppStateAnalyzerSingleton {
		private final static AppStateAnalyzer INSTANCE = new AppStateAnalyzer();
	}
	
}
