package com.att.aro.core.mobiledevice;

import java.io.IOException;

import com.android.ddmlib.IDevice;

public interface IAndroidDevice {

	/**
	 * Check if a connected Android device is rooted or not.
	 * <p>performs 'su -c id' on Android<br>
	 * a response containing "uid=0(root) gid=0(root)" is considered rooted</p>
	 * 
	 * @throws IOException
	 */
	boolean isAndroidRooted(IDevice device) throws Exception;

	/**
	 * Check Android device for SELinux enforcement
	 * 
	 * @param device - a real device or emulator
	 * @return true if SELinux-Enforced, false if permissive
	 * @throws Exception
	 */
	boolean isSeLinuxEnforced(IDevice device) throws Exception;

}