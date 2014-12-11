package com.att.aro.analytics;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import com.att.aro.util.Util;
/**
 * Dynamically load interface implementation at runtime if the lib is available otherwise use the 
 * default implementation
 * 
 * @author Borey Sao
 * Date: August 13, 2014
 */
public class AnalyticFactory {
	private static volatile boolean isLibloaded = false;
//	private static volatile boolean isAnalyticsloaded = false;
	private static final Logger LOGGER = Logger.getLogger(AnalyticFactory.class.getName());
	private static Object syncObj = new Object();
	//cache of all interfaces
	private static IPlatform platform = null;
	private static IGoogleAnalytics googleAnalytics = null;
	
	/**
	 * load all interface implementation from a jar file
	 */
	public static void loadLib(){
		
		if(!isLibloaded){
			String libpath = getLibpath();
			
			File file = new File(libpath.replaceAll("%20", " "));
			if(!file.exists()){
				return;
			}
			
			URLClassLoader loader = null;
			URL url = null;
			try {
				url = file.toURI().toURL();
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			
			if(url != null){
				loader = new URLClassLoader(new URL[]{url});
				
				//load implementation of IPlatform
				ServiceLoader<IPlatform> serviceloader = ServiceLoader.load(IPlatform.class, loader);
				Iterator<IPlatform> it = serviceloader.iterator();
				if(it.hasNext()){
					platform = it.next();
				}
				
				//load all other interface implementation below
				//load implementation of IPlatform
				ServiceLoader<IGoogleAnalytics> serviceloader1 = ServiceLoader.load(IGoogleAnalytics.class, loader);
				Iterator<IGoogleAnalytics> itGA = serviceloader1.iterator();
				if(itGA.hasNext()){
					googleAnalytics = itGA.next();
				}
			}
			
			isLibloaded = true;
		}
	}
	
	private static String getLibpath(){
		//LOGGER.info("aro_Analytics jar path : " +Util.getCurrentRunningDir() + Util.FILE_SEPARATOR  + "ARO_Analytics.jar");
		return Util.getCurrentRunningDir() + Util.FILE_SEPARATOR +"ARO_Analytics.jar";
	}
	public static IPlatform getPlatform(){
		//avoid reloading what already done!
		if(platform != null){
			return platform;
		}
		
		//no implementation from external lib found, then use default impl
		platform = new DefaultPlatformImpl();
		
		return platform;
	}

	public static IGoogleAnalytics getGoogleAnalytics(){
		if(googleAnalytics == null){
			synchronized(syncObj){
				//double locking strategy to prevent multiple threads access
				if(googleAnalytics == null){
					googleAnalytics = new DefaultGoogleAnalyticsImpl();
				}
			}
		}
		return googleAnalytics;
		
	}
}
