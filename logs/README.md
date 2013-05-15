The class `android.util.Log` has a lot of drawbacks:
 * For each log, you have to pass a `tag` parameter which is generally defined this way: `private static final String TAG = "MyActivity";` (as [recommended by Google](http://developer.android.com/reference/android/util/Log.html)).
 * To disable logs or at least some levels, you have to specify it in ProGuard file (or add `if` tests around your logs...).
 * The second parameter of a log method must be a `String`.
 * ...

**With this Log extension class:**
 * You don't have to give a `tag` parameter. This parameter will automatically be set using reflection (you can cutomize the tag).
 * You can log any object, the `toString()` method of this object will be called.
 * You can disable all logs or just the ones coming from some classes or some packages
 * You can only display logs of certain levels (DEBUG and WARNING for example, or all logs with a level lower than or equal to INFO level for instance).
 * You can configure the logs thanks to a configuration file (so it is simple to deactivate logs in release with Jenkins for example)


How to use it
-------------

 * Add the file _"log.properties"_ in _"assets"_ folder.
 * In the `Application` class, inside `onCreate()` method, initialize the `Log` class:

        Log.init(this, new LogConfig() {
            @Override
            public HashSet<String> getPackagesToMute() {
                return null;
            }
            
            @Override
            public byte getLogLevel() {
                return Log.ALL;
            }
            
            @Override
            public HashSet<Class<?>> getClassesToMute() {
                return null;
            }
        });


Customization
-------------

It is possible to configure the logger thanks to the configuration file _"log.properties"_:

    showLogs = [true|false]
    logPrefix = <your_prefix>
    logClassSimpleName = [true|false]

By default,
* the logs are disabled
* the `tag` parameter has no prefix
* the tag is the full name of the class that wants to log something (package + class name)


A little word about performances
--------------------------------

Since reflection is used, a log with this class takes more time. However, it should not be a problem since in release all logs should be disabled.
