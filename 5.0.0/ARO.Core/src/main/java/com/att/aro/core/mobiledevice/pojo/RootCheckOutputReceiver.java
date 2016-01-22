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
package com.att.aro.core.mobiledevice.pojo;

import java.nio.charset.Charset;

import com.android.ddmlib.IShellOutputReceiver;

public 	class RootCheckOutputReceiver implements IShellOutputReceiver {

	String data = "";
	boolean isCancelledFlag = false;

	@Override
	public void addOutput(byte[] buff, int offset, int length) {
		Charset charset = Charset.forName("ISO-8859-1");
		data = new String(buff, offset, length, charset);
		isCancelledFlag = true;
	}

	public boolean isRootId() {
		return data.contains("uid=0(root) gid=0(root)");
	}

	@Override
	public void flush() {
		isCancelledFlag = true;
	}

	@Override
	public boolean isCancelled() {
		return isCancelledFlag;
	}
}

