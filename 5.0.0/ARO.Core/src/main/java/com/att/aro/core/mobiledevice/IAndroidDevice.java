/**
 *  Copyright 2016 AT&T
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