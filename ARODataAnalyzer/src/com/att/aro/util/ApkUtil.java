package com.att.aro.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.android.ddmlib.IDevice;
import com.att.aro.main.DataCollectorNoRootVpn;

public class ApkUtil {

	/**
	 * Pull the given file name from Jar and write to the local drive
	 * 
	 * @param filename
	 * @param localTraceFolder
	 * @return
	 * @throws IOException
	 */
	public static File getAroCollectorFilesFromJar(IDevice mAndroidDevice
												 , String filename
												 , String localTraceFolder) throws IOException {
		ClassLoader aroClassloader = DataCollectorNoRootVpn.class.getClassLoader();
		InputStream is = aroClassloader.getResourceAsStream(filename);
		OutputStream os = null;
		try {
			File result = new File(localTraceFolder, filename);
			if (result.createNewFile()) {
				os = new FileOutputStream(result);
				byte[] buffer = new byte[4096];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
			}
			return result;
		} finally {
			is.close();
			if (os != null) {
				os.close();
			}
		}
	}


}
