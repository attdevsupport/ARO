/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.model.waterfall;

import java.text.MessageFormat;

import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.ui.utils.ResourceBundleHelper;


/**
 * @author Harikrishna Yaramachu
 *
 */
public class WaterfallCategory implements Comparable<WaterfallCategory> {
	private HttpRequestResponseInfo reqResp;
	private Session session;
	private int index;

	public WaterfallCategory(HttpRequestResponseInfo reqResp,Session session) {
		this.reqResp = reqResp;
		this.session = session;
	}
	
	

	@Override
	public int compareTo(WaterfallCategory arg0) {
		return Double.valueOf(reqResp.getWaterfallInfos().getStartTime())
				.compareTo(arg0.reqResp.getWaterfallInfos().getStartTime());
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return MessageFormat.format(
				ResourceBundleHelper.getMessageString("waterfall.categoryText"),
				index,
				reqResp.getHostName() != null ? reqResp.getHostName() : ResourceBundleHelper.getMessageString("waterfall.unknownHost"),
				(reqResp != null && reqResp.getObjName() != null ? reqResp
						.getObjName() : ""));
	}

	/**
	 * Tooltip to be displayed for this item
	 * @return
	 */
	public String getTooltip() {
		if (reqResp.isSsl()) {
			return ResourceBundleHelper.getMessageString("waterfall.https");
		} else {
			return reqResp != null && reqResp.getObjUri() != null ? reqResp.getObjUri().toString() : ResourceBundleHelper.getMessageString("waterfall.unknownHost");
		}
	}

	/**
	 * @return the reqResp
	 */
	public HttpRequestResponseInfo getReqResp() {
		return reqResp;
	}
	
	public Session getSession(){
		return session;
	}

	/**
	 * @return the index
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * @param index the index to set
	 */
	public void setIndex(int index) {
		this.index = index;
	}
}
