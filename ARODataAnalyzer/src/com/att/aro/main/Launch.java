/*
 * Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.att.aro.main;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.ref.SoftReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.att.aro.commonui.AROUIManager;
import com.att.aro.commonui.MessageDialogFactory;

/**
 * Provides the main entry point for the ARO Data Analyzer application.
 */
public class Launch {

	private static final Logger logger = Logger.getLogger(Launch.class
			.getName());
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();

	/**
	 * The starting point for the ARO Data Analyzer. This method launches the
	 * application with the specified arguments.
	 * 
	 * @param args
	 *            Argument strings that are passed to the application at
	 *            startup.
	 */
	public static void main(String[] args) {
		JarSignersHardLinker.go();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {

				// Initialize application window
				AROUIManager.init();
				final ApplicationResourceOptimizer mainClass = new ApplicationResourceOptimizer();
				mainClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				mainClass.setVisible(true);
				Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {

					@Override
					public void uncaughtException(Thread t, Throwable e) {
						e.printStackTrace();
						if (e instanceof OutOfMemoryError) {
							logger.log(Level.SEVERE, "Out of memory error", e);
							MessageDialogFactory.showErrorDialog(mainClass,
									rb.getString("Error.outOfMemory"));
						} else {
							logger.log(Level.SEVERE, "Uncaught exception", e);
							MessageDialogFactory.showUnexpectedExceptionDialog(
									mainClass, e);
						}
					}

				});

				// Code to display About window from Mac menu
				if (System.getProperty("os.name").contains("Mac")) {
				    try {
				        Object app = Class.forName("com.apple.eawt.Application").getMethod("getApplication",
				         (Class[]) null).invoke(null, (Object[]) null);

				        Object al = Proxy.newProxyInstance(Class.forName("com.apple.eawt.AboutHandler")
				                .getClassLoader(), new Class[] { Class.forName("com.apple.eawt.AboutHandler") },
				                    new AboutListener());
				        app.getClass().getMethod("setAboutHandler", new Class[] {
				            Class.forName("com.apple.eawt.AboutHandler") }).invoke(app, new Object[] { al });
				    } catch (Exception e) {
				        //fail quietly
				    }
				}
				
				// Display splash
				final SplashScreen splash = new SplashScreen(mainClass);
				splash.setVisible(true);
				splash.setAlwaysOnTop(true);
				new SwingWorker<Object, Object>() {

					@Override
					protected Object doInBackground() {
						try {
							Thread.sleep(2000);
						} catch (InterruptedException e) {
						}
						return null;
					}

					/**
					 * @see javax.swing.SwingWorker#done()
					 */
					@Override
					protected void done() {
						splash.dispose();
					}

				}.execute();

			}
		});
	}

	private static class AboutListener implements InvocationHandler {

	    public Object invoke(Object proxy, Method method, Object[] args) {
	    	new AboutDialog(null).setVisible(true);
	        return null;
	    }
	}
	
	/**
	 * A utility class for working around the java webstart jar signing/security bug.  The source
	 * for this class is based upon a JVM defect workaround posted at
	 * https://forums.oracle.com/forums/thread.jspa?threadID=2148354&tstart=1
	 * 
	 * see http://bugs.sun.com/view_bug.do?bug_id=6967414 and http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6805618
	  */
	private static class JarSignersHardLinker {
		
	    private static final String JRE_1_6_0 = "1.6.0_";
	    private static final String JRE_1_7_0 = "1.7.0_";
	    
	    /**
	     * the 1.6.0 update where this problem first occurred
	     */
	    private static final int PROBLEM_JRE_UPDATE = 19;

	    private static final List<Object> sm_hardRefs = new ArrayList<Object>();
	    
	    protected static void makeHardSignersRef(JarFile jar) throws java.io.IOException { 
	        
	        logger.info("Making hard refs for: " + jar );
	        if(jar != null && jar.getClass().getName().equals("com.sun.deploy.cache.CachedJarFile")) {
	 
	        	//lets attempt to get at the each of the soft links.
	        	//first neet to call the relevant no-arg method to ensure that the soft ref is populated
	        	//then we access the private member, resolve the softlink and throw it in a static list.
	            
	            callNoArgMethod("getSigners", jar);
	            makeHardLink("signersRef", jar);
	            
	            callNoArgMethod("getSignerMap", jar);
	            makeHardLink("signerMapRef", jar);
	            
//	            callNoArgMethod("getCodeSources", jar);
//	            makeHardLink("codeSourcesRef", jar);
	            
	            callNoArgMethod("getCodeSourceCache", jar);
	            makeHardLink("codeSourceCacheRef", jar);

	            // Java 1.7
	            callNoArgMethod("getSigningData", jar);
	            makeHardLink("signingDataRef", jar); 	        
	        }            
	    }
	    
	    
	    /**
	     * if the specified field for the given instance is a Softreference
	     * That soft reference is resolved and the returned ref is stored in a static list,
	     * making it a hard link that should never be garbage collected
	     * @param fieldName
	     * @param instance
	     */
	    private static void makeHardLink(String fieldName, Object instance) {
	        
	    	if (logger.isLoggable(Level.FINE)) {
		        logger.fine("attempting hard ref to " + instance.getClass().getName() + "." + fieldName);
	    	}
	        
	        try {
	            Field signersRef = instance.getClass().getDeclaredField(fieldName);
	            
	            signersRef.setAccessible(true);
	            
	            Object o = signersRef.get(instance);
	            
	            if(o instanceof SoftReference) {
	                SoftReference<?> r = (SoftReference<?>) o;
	                Object o2 = r.get();
	                sm_hardRefs.add(o2);
	            } else {
	                //System.out.println("noooo!");
	            }
	        } catch (NoSuchFieldException e) {
	            logger.log(Level.FINE, "NoSuchFieldException", e);
	            return;
	        } catch (IllegalAccessException e) {
	            logger.log(Level.FINE, "IllegalAccessException", e);
	        }
	    }
	    
	    /**
	     * Call the given no-arg method on the given instance
	     * @param methodName
	     * @param instance
	     */
	    private static void callNoArgMethod(String methodName, Object instance) {
	    	if (logger.isLoggable(Level.FINE)) {
	    		logger.fine("calling noarg method hard ref to " + instance.getClass().getName() + "." + methodName + "()");
	    	}
	        try {
	            Method m = instance.getClass().getDeclaredMethod(methodName);
	            m.setAccessible(true);
	            
	            m.invoke(instance);
	 
	        } catch (SecurityException e1) {
	            logger.log(Level.FINE, "SecurityException", e1);
	        } catch (NoSuchMethodException e1) {
	            logger.log(Level.FINE, "NoSuchMethodException", e1);
	        } catch (IllegalArgumentException e) {
	            logger.log(Level.FINE, "IllegalArgumentException", e);
	        } catch (IllegalAccessException e) {
	            logger.log(Level.FINE, "IllegalAccessException", e);
	        } catch (InvocationTargetException e) {
	            logger.log(Level.FINE, "InvocationTargetException", e);
	        }
	    }
	 
	    
	    /**
	     * is the preloader enabled. ie: will the preloader run in the current environment
	     * @return
	     */
	    public static boolean isHardLinkerEnabled() {
	    	
	    	boolean isHardLinkerDisabled = false;  //change this to use whatever mechanism you use to enable or disable the preloader
	        
	        return !isHardLinkerDisabled && isRunningOnJre1_6_0_19OrHigher() && isRunningOnWebstart();
	    }
	    
	    /**
	     * is the application currently running on webstart
	     * 
	     * detect the presence of a JNLPclassloader
	     * 
	     * @return
	     */
	    public static boolean isRunningOnWebstart() {
	        ClassLoader cl = Thread.currentThread().getContextClassLoader();
	        
	        while(cl != null) {
	            if(cl.getClass().getName().equals("com.sun.jnlp.JNLPClassLoader")) {
	                return true;
	            }
	            cl = cl.getParent();
	        }
	        
	        return false;
	 
	    }
	    
	    /**
	     * Is the JRE 1.6.0_19 or higher?
	     * @return
	     */
	    public static boolean isRunningOnJre1_6_0_19OrHigher() {
	        String javaVersion = System.getProperty("java.version");
	        
	        if(javaVersion.startsWith(JRE_1_6_0)) {
	            //then lets figure out what update we are on
	            String updateStr = javaVersion.substring(JRE_1_6_0.length());
	            
	            try {
	                return Integer.parseInt(updateStr) >= PROBLEM_JRE_UPDATE;
	            } catch (NumberFormatException e) {
	                //then unable to determine updatedate level
	                return false;
	            }
	        } else if (javaVersion.startsWith(JRE_1_7_0)) {
	        	return true;
	        }
	        
	        //all other cases
	        return false;
	        
	    }
	    
	    
		/**
		 * get all the JarFile objects for all of the jars in the classpath
		 * @return
		 */
		public static Set<JarFile> getAllJarsFilesInClassPath() {
		
			Set<JarFile> jars = new LinkedHashSet<JarFile> (); 
		    
		    for (URL url : getAllJarUrls()) {
		        try {
		            jars.add(getJarFile(url));
		        } catch(IOException e) {
		        	logger.log(Level.WARNING, "unable to retrieve jar at URL: " + url, e);
		        }
		    }
		    
		    return jars;
		}
		
	    /**
	     * Returns set of URLS for the jars in the classpath.
	     * URLS will have the protocol of jar eg: jar:http://HOST/PATH/JARNAME.jar!/META-INF/MANIFEST.MF
	     */
	    static Set<URL> getAllJarUrls() {
	        try {
	            Set<URL> urls = new LinkedHashSet<URL>();
	            Enumeration<URL> mfUrls = Thread.currentThread().getContextClassLoader().getResources("META-INF/MANIFEST.MF");
	            while(mfUrls.hasMoreElements()) {
	                URL jarUrl = mfUrls.nextElement();
//	                System.out.println(jarUrl);
	                if(!jarUrl.getProtocol().equals("jar")) continue;
	                urls.add(jarUrl);
	            }
	            return urls;
	        } catch(IOException e) {
	            throw new RuntimeException(e);
	        }
	    }
	    
	    /**
	     * get the jarFile object for the given url
	     * @param jarUrl
	     * @return
	     * @throws IOException
	     */
	    public static JarFile getJarFile(URL jarUrl) throws IOException {
	        URLConnection urlConnnection = jarUrl.openConnection();
	        if(urlConnnection instanceof JarURLConnection) {
	            // Using a JarURLConnection will load the JAR from the cache when using Webstart 1.6
	            // In Webstart 1.5, the URL will point to the cached JAR on the local filesystem
	            JarURLConnection jcon = (JarURLConnection) urlConnnection;
	            return jcon.getJarFile();
	        } else {
	            throw new AssertionError("Expected JarURLConnection");
	        }
	    }
	    
	    
	    /**
	     * Spawn a new thread to run through each jar in the classpath and create a hardlink
	     * to the jars softly referenced signers infomation.
	     */
	    public static void go() {
	        if(!isHardLinkerEnabled()) {
	            return;
	        }
	        
	        logger.info("Starting Resource Preloader Hardlinker");
	        
	        Thread t = new Thread(new Runnable() {
	 
	            public void run() {
	                
	                try {
	                    Set<JarFile> jars = getAllJarsFilesInClassPath();
	                    
	                    for (JarFile jar : jars) {
	                        makeHardSignersRef(jar);
	                    }
	 
	                } catch (Exception e) {
	                    logger.log(Level.WARNING, "Problem preloading resources", e);
	                } catch (Error e) {
	                	logger.log(Level.WARNING, "Error preloading resources", e);
	                }
	            }
	            
	        });
	        
	        t.start();
	        
	    }
	}
}
