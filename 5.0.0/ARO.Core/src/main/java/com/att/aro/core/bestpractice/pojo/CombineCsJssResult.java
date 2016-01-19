/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.bestpractice.pojo;

import java.util.List;

import com.att.aro.core.packetanalysis.pojo.PacketInfo;

public class CombineCsJssResult extends AbstractBestPracticeResult {
	private int inefficientCssRequests = 0;
	private PacketInfo consecutiveCssJsFirstPacket = null;
	private int inefficientJsRequests = 0;
	private String exportAllInefficientCssRequest;
	private String exportAllInefficientJsRequest;
	private List<CsJssFilesDetails> filesDetails;
	
	public int getInefficientCssRequests() {
		return inefficientCssRequests;
	}

	public void setInefficientCssRequests(int inefficientCssRequests) {
		this.inefficientCssRequests = inefficientCssRequests;
	}

	public PacketInfo getConsecutiveCssJsFirstPacket() {
		return consecutiveCssJsFirstPacket;
	}

	public void setConsecutiveCssJsFirstPacket(
			PacketInfo consecutiveCssJsFirstPacket) {
		this.consecutiveCssJsFirstPacket = consecutiveCssJsFirstPacket;
	}

	public int getInefficientJsRequests() {
		return inefficientJsRequests;
	}

	public void setInefficientJsRequests(int inefficientJsRequests) {
		this.inefficientJsRequests = inefficientJsRequests;
	}
	
	public String getExportAllInefficientCssRequest() {
		return exportAllInefficientCssRequest;
	}

	public void setExportAllInefficientCssRequest(
			String exportAllInefficientCssRequest) {
		this.exportAllInefficientCssRequest = exportAllInefficientCssRequest;
	}

	public String getExportAllInefficientJsRequest() {
		return exportAllInefficientJsRequest;
	}

	public void setExportAllInefficientJsRequest(
			String exportAllInefficientJsRequest) {
		this.exportAllInefficientJsRequest = exportAllInefficientJsRequest;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.COMBINE_CS_JSS;
	}

	/**
	 * @return the filesDetails
	 */
	public List<CsJssFilesDetails> getFilesDetails() {
		return filesDetails;
	}

	/**
	 * @param filesDetails the filesDetails to set
	 */
	public void setFilesDetails(List<CsJssFilesDetails> filesDetails) {
		this.filesDetails = filesDetails;
	}

}
