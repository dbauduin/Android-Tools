import java.util.HashSet;

/**
 * @author David Bauduin
 */
public interface LogConfig {
	
	/**
	 * Return a log level from ERROR, WARNING, ..., VERBOSE, or ALL, DEBUG_AND_LOWER, ..., NONE
	 * @return The desired level
	 */
	byte getLogLevel();
	
	/**
	 * Return a set of classes whose logs are disabled
	 * @return The classes whose logs will be disabled
	 */
	HashSet<Class<?>> getClassesToMute();
	
	/**
	 * Return a set of package names whose logs are disabled
	 * Example: {com, com.android...}
	 * @return The package names whose logs will be disabled
	 */
	HashSet<String> getPackagesToMute();
	
}
