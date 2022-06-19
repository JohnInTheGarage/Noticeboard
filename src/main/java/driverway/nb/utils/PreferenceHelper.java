
package driverway.nb.utils;

import java.util.prefs.Preferences;


/**
 * Using Singleton pattern
 * @author john
 */
public final class PreferenceHelper {
	private Preferences runtimeValues;	
	private static PreferenceHelper INSTANCE;
	
	private PreferenceHelper(){
		runtimeValues = Preferences.userRoot().node("NBRuntimeValues");
	}

	public String getItem(String key){
		return runtimeValues.get(key, "");
	}
	
	public void putItem(String key, String value){
		runtimeValues.put(key, value);
	}
	
	
	public static PreferenceHelper getInstance(){
		if (INSTANCE == null)
            INSTANCE = new PreferenceHelper();
		
        return INSTANCE;
	}
}
