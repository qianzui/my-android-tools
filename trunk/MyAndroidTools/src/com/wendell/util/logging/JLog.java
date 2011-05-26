package com.wendell.util.logging;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

/**
 * <p>日志管理器类
 * @author Wendell
 * @version 1.1
 */
public final class JLog {
	
	static{
		try{
			loadNativeConfig();
		}catch(Exception e){
			System.err.println("Loading native configuration failed");
			e.printStackTrace(System.err);
		}
	}
	
	private JLog(){};
	
	/**
	 * <p>该方法在原则上应该最多只被调用一次
	 * @param configInput
	 * @throws IOException
	 */
	public static void loadConfig(InputStream configInput) throws IOException {
		java.util.logging.LogManager logManager = java.util.logging.LogManager.getLogManager();
		logManager.readConfiguration(configInput);
	}
	
	/**
	 * <p>该方法在原则上应该最多只被调用一次
	 * @throws IOException
	 */
	private static void loadNativeConfig() throws IOException {
		InputStream input = null;
		try{
			input = JLog.class.getResourceAsStream("/com/wendell/util/logging/templat/logging.properties");
			loadConfig(input);
		}finally{
			if(input != null) input.close();
		}
	}
	
	public static Logger getLogger(String name){
		return Logger.getLogger(name);
	}
	
	public static Logger getLogger(String name,String resourceBundleName){
		return Logger.getLogger(name,resourceBundleName);
	}
	
}
