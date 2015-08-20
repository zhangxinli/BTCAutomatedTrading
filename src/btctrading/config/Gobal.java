package org.btctrading.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Gobal {
	private static final Logger logger = Logger.getLogger(Gobal.class);
	private static final String CONFIG_FILE = "./resources/settings.properties";
	
	private static final String SECRET_KEY_ENV = "SecretKey";
	private static final String API_KEY_ENV = "APIKey";
	private static final String PASSWORD_ENV = "Password";
	private static String getProperty(String key) {
		Properties prop = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(CONFIG_FILE);
			prop.load(input);
			return prop.getProperty(key);
		} catch (Exception e) {
			logger.error("requesting [" + key + "] error " + e.getMessage());
			return null;
		}
	}
	public static String getSecretKey(){
		return getProperty(SECRET_KEY_ENV) ;
	}
	public static String getAPIKey(){
		return getProperty(API_KEY_ENV) ;
	}
	public static String getPassword(){
		return getProperty(PASSWORD_ENV) ;
	}
}
