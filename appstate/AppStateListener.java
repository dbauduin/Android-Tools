/**
 * @author David Bauduin
 */
public interface AppStateListener {
	
	void onApplicationDidEnterForeground(boolean awakeFromBackground, int launchNumber);
	void onApplicationDidEnterBackground();
	
}
