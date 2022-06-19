package driverway.nb.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author john
 */
public class PropertyLoader {

	private static final Logger LOGGER = LogManager.getLogger();
	private String dirName;
	
	public PropertyLoader(){
		dirName = System.getenv("NBPROPERTIES") + System.getProperty("file.separator");
		//Map<String, String> env = System.getenv();
		//env.forEach((k, v) -> System.out.println(k + ":" + v));
	}
	
	public Properties load(String name) {

		Properties props = null;
		try (FileInputStream in = new FileInputStream(dirName + name)) {
			props = new Properties();
			props.load(in);
			
			LOGGER.trace("------------- Properties loaded from " + dirName + name);
			Enumeration<String> enums = (Enumeration<String>) props.propertyNames();
			while (enums.hasMoreElements()) {
				String key = enums.nextElement();
				String value = props.getProperty(key);
				if (key.contains("Secret")){
					value = "<withheld>";
				}
				LOGGER.trace(key + " : " + value);
			}
			LOGGER.trace("---------------------------------------------------------");

		} catch (IOException ex) {
			LOGGER.error("Unable to open properties file " + ex);
		}
		return props;
	}

	/*
	* Used for Google credentials file (json)
	*/
	public StringReader read(String name) {

		StringReader reader = null;
		
		try (FileInputStream in = new FileInputStream(dirName + name)) {
			byte[] data = in.readAllBytes();
			String stuff = new String(data);
			LOGGER.trace("------------- string loaded from " + name);
			reader = new StringReader(stuff);
		} catch (IOException ex) {
			LOGGER.error("Unable to open file " + ex);
		}
		LOGGER.trace("returning a StringReader");
		return reader;
	}

}
