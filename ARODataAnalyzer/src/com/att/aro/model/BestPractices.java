/*
 *  Copyright 2012 AT&T
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
package com.att.aro.model;

import java.util.List;

/**
 * A bean class that contains the information that appears on the Best Practices tab, 
 * such as the pass/fail status of the test, and the test results.
 */
public class BestPractices {

	private static final int PERIPHERAL_ACTIVE_LIMIT = 5;

	private TraceData.Analysis analysisData;
	private TraceData traceData;

	private int http1_0HeaderCount = 0;
	private TCPSession http10Session = null;

	private boolean multipleTcpCon = true;
	private boolean periodicTrans = true;
	private int userInputBurstCount;
	private double largeBurstTime;

	private boolean conClosingProb = true;
	private boolean screenRotation = true;
	private boolean offloadingToWiFi = true;
	private boolean duplicateContent = true;
	private boolean usingCache = true;
	private boolean cacheControl = true;
	private boolean accessingPeripherals = true;

	private double duplicateContentBytesRatio = 0;
	private int duplicateContentsize = 0;
	private long duplicateContentBytes = 0;
	private long totalContentBytes = 0;
	private double gpsActiveStateRatio = 0;
	private double bluetoothActiveStateRatio = 0;
	private double cameraActiveStateRatio = 0;
	private int hitNotExpiredDup = 0;
	private int hitExpired304 = 0;
	private double cacheHeaderRatio = 0.0;
	private double tcpControlEnergyRatio = 0.0;
	private double tcpControlEnergy = 0.0;
	private double largestEnergyTime = 0.0;
	private double screenRotationBurstTime = 0.0;
	private PacketInfo noCacheHeaderFirstPacket;
	private PacketInfo dupContentFirstPacket;

	/**
	 * Initializes an instance of the BestPractices class, using the specified set of 
	 * trace analysis data.
	 * 
	 * @param analysisData An Analysis object containing the set of trace analysis data.
	 */
	public BestPractices(TraceData.Analysis analysisData) {
		this.analysisData = analysisData;
		this.traceData = analysisData.getTraceData();

		BurstCollectionAnalysis bcAnalysis = analysisData.getBcAnalysis();

		// Setting the best practices
		this.multipleTcpCon = (bcAnalysis.getTightlyCoupledBurstCount() == 0);
		this.periodicTrans = (bcAnalysis.getMinimumPeriodicRepeatTime() == 0.0);

		double largestBurstTime = 0.0;
		double largestBurstBeginTime = 0.0;
		int burstCategoryCount = 0;
		int userInputBurstCount = 0;
		double wastedBurstEnergy = 0.0;
		double maxEnergy = 0.0;
		double largestEnergyTime = 0.0;

		// Burst Best practices.
		for (Burst burst : analysisData.getBcAnalysis().getBurstCollection()) {

			// Largest burst time
			double time = burst.getEndTime() - burst.getBeginTime();
			if (time > largestBurstTime) {
				largestBurstTime = time;
				largestBurstBeginTime = burst.getBeginTime();
			}

			// To validate 5 user input bursts in a row
			if (BurstCategory.BURSTCAT_USER == burst.getBurstCategory()) {
				burstCategoryCount++;
			} else {
				burstCategoryCount = 0;
			}
			userInputBurstCount = Math.max(userInputBurstCount, burstCategoryCount);

			if (burst.getBurstCategory() == BurstCategory.BURSTCAT_PROTOCOL) {
				double currentEnergy = burst.getEnergy();
				wastedBurstEnergy += currentEnergy;
				if (currentEnergy > maxEnergy) {
					maxEnergy = currentEnergy;
					largestEnergyTime = burst.getBeginTime();
				}
			}
			
			//Verifying burst category to update screen rotation flag value which 
			//shows whether screen rotation triggered network activity or not.
			if (BurstCategory.BURSTCAT_SCREEN_ROTATION == burst.getBurstCategory() && this.screenRotation) {
				this.screenRotation = false;
				this.screenRotationBurstTime = burst.getBeginTime();
			}
		}
		this.largeBurstTime = largestBurstBeginTime;
		this.userInputBurstCount = userInputBurstCount;
		this.offloadingToWiFi = (bcAnalysis.getLongBurstCount() <= 3);
		if (bcAnalysis.getTotalEnergy() > 0) {
			double percentageWasted = wastedBurstEnergy / bcAnalysis.getTotalEnergy();
			this.conClosingProb = (percentageWasted < 0.05);
			this.tcpControlEnergy = wastedBurstEnergy;
			this.tcpControlEnergyRatio = percentageWasted;
			this.largestEnergyTime = largestEnergyTime;
		}

		// Check HTTP 1.0 best practice
		for (TCPSession s : analysisData.getTcpSessions()) {
			for (HttpRequestResponseInfo reqRessInfo : s.getRequestResponseInfo()) {
				if (HttpRequestResponseInfo.HTTP10.equals(reqRessInfo.getVersion())) {
					++http1_0HeaderCount;
					if (null == http10Session) {
						http10Session = s;
					}
				}
			}
		}
        TimeRange timeRange = analysisData.getFilter().getTimeRange();
        
		double traceDuration = traceData.getTraceDuration();
		double activeGPSRatio = 0.0;
		double activeBluetoothRatio = 0.0;
		double activeCameraRatio = 0.0;
		
		if(timeRange != null){
			
			double timeRangeDuration = timeRange.getEndTime() - timeRange.getBeginTime();
			
			activeGPSRatio = (analysisData.getGPSActiveDuration() * 100) / timeRangeDuration;
			activeBluetoothRatio = (analysisData.getBluetoothActiveDuration() * 100)
					/ timeRangeDuration;
			activeCameraRatio = (analysisData.getCameraActiveDuration() * 100) / timeRangeDuration;
			
		}else{
			
			activeGPSRatio = (analysisData.getGPSActiveDuration() * 100) / traceDuration;
			activeBluetoothRatio = (analysisData.getBluetoothActiveDuration() * 100)
					/ traceDuration;
			activeCameraRatio = (analysisData.getCameraActiveDuration() * 100) / traceDuration;
		}

		this.accessingPeripherals = ((activeGPSRatio > PERIPHERAL_ACTIVE_LIMIT
				|| activeBluetoothRatio > PERIPHERAL_ACTIVE_LIMIT || activeCameraRatio > PERIPHERAL_ACTIVE_LIMIT) ? false
				: true);

		this.gpsActiveStateRatio = activeGPSRatio;
		this.bluetoothActiveStateRatio = activeBluetoothRatio;
		this.cameraActiveStateRatio = activeCameraRatio;

		// Cache best practices
		CacheAnalysis cacheAnalysis = analysisData.getCacheAnalysis();
		List<CacheEntry> diagnosisResults = cacheAnalysis.getDiagnosisResults();
		int hitNotExpiredDup = 0;
		int hitExpired304 = 0;
		int validCount = 0;
		int noCacheHeadersCount = 0;
		for (CacheEntry entry : diagnosisResults) {
			switch (entry.getDiagnosis()) {
			case CACHING_DIAG_NOT_EXPIRED_DUP:
			case CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT:
				if (hitNotExpiredDup == 0) {
					this.dupContentFirstPacket = entry.getSessionFirstPacket();
				}
				hitNotExpiredDup++;
				break;

			case CACHING_DIAG_OBJ_NOT_CHANGED_304:
				hitExpired304++;
				break;
			}

			// Check for cache headers missing
			switch (entry.getDiagnosis()) {
			case CACHING_DIAG_REQUEST_NOT_FOUND:
			case CACHING_DIAG_INVALID_OBJ_NAME:
			case CACHING_DIAG_INVALID_REQUEST:
			case CACHING_DIAG_INVALID_RESPONSE:
				// Only test non-error request/response pairs
				break;
			default:
				++validCount;
				if (!entry.hasCacheHeaders()) {
					if (noCacheHeadersCount == 0) {
						this.noCacheHeaderFirstPacket = entry.getSessionFirstPacket();
					}
					++noCacheHeadersCount;
				}
			}
		}
		this.hitExpired304 = hitExpired304;
		this.hitNotExpiredDup = hitNotExpiredDup;
		this.cacheControl = (hitNotExpiredDup > hitExpired304 ? false : true);

		this.cacheHeaderRatio = validCount > 0 ? (100.0 * noCacheHeadersCount) / validCount : 0.0;
		this.usingCache = cacheHeaderRatio <= 10.0;

		this.duplicateContentBytes = cacheAnalysis.getDuplicateContentBytes();
		this.totalContentBytes = cacheAnalysis.getTotalBytesDownloaded();
		this.duplicateContentBytesRatio = cacheAnalysis.getDuplicateContentBytesRatio();
		this.duplicateContentsize = cacheAnalysis.getDuplicateContent().size();
		this.duplicateContent = duplicateContentsize <= 3;

	}

	/**
	 * Returns a value that indicates if any multiple TCP connections were found.
	 * 
	 * @return A boolean value that is true if any multiple TCP connections were found, 
	 * and is false otherwise.
	 */
	public boolean getMultipleTcpCon() {
		return multipleTcpCon;
	}

	/**
	 * Returns a value that indicates if any periodic transfers occurred. 
	 * 
	 * @return A boolean value that is true if any periodic transfers occurred, and is 
	 * false otherwise.
	 */
	public boolean getPeriodicTransfer() {
		return periodicTrans;
	}

	/**
	 * Returns a value indicating whether or not more than 5% of the total energy is being 
	 * used for TCP control; a level that indicates a connection closing problem.
	 * 
	 * @return A boolean value that is true if the trace data shows a connection closing 
	 * problem, and false otherwise.
	 */
	public boolean getConnectionClosingProblem() {
		return conClosingProb;
	}
	
	/**
	 * Returns a value that indicates whether or not, a network traffic burst was detected 
	 * after the screen was rotated.
	 * 
	 * @return A boolean value that is true if the trace data shows a screen rotation 
	 * 		   problem (a network burst after screen rotation), and false otherwise.
	 */
	public boolean getScreenRotationProblem() {
		return screenRotation;
	}

	/**
	 * Returns a value that indicates if any offloading to WiFi occurred. 
	 * 
	 * @return A boolean value that is true if offloading to WiFi occurred, and is false 
	 * otherwise.
	 */
	public boolean getOffloadingToWiFi() {
		return offloadingToWiFi;
	}

	/**
	 * Returns a value indicating whether or not more than 3 files were downloaded in a 
	 * duplicate manner; a level that indicates an issue with duplicate content. 
	 * 
	 * @return A boolean value that is true if a duplicate content issue has been 
	 * identified, and false otherwise.
	 */
	public boolean getDuplicateContent() {
		return duplicateContent;
	}

	/**
	 * Returns a value that indicates if any prefetching occurred. 
	 * 
	 * @return A boolean value that is true if it any prefetching occurred, and is false 
	 * otherwise.
	 */
	public boolean getPrefetching() {
		return (userInputBurstCount < 5);
	}

	/**
	 * Returns a count of the number of user input bursts. 
	 * 
	 * @return An int that is the number of user input bursts.
	 */
	public int getUserInputBurstCount() {
		return userInputBurstCount;
	}
	
	/**
	 * Returns the PacketInfo of session with no cache header. 
	 * 
	 * @return PacketInfo.
	 */
	public PacketInfo getNoCacheHeaderStartTime() {
		return noCacheHeaderFirstPacket;
	}
	
	/**
	 * Returns the PacketInfo of session with duplicate content.
	 * 
	 * @return PacketInfo.
	 */
	public PacketInfo getDupContentStartTime() {
		return dupContentFirstPacket;
	}

	/**
	 * Returns the count of expired but correct 304 responses from the serverhitExpired304. 
	 * 
	 * @return An int that is the number of expired but correct 304 responses.
	 */
	public int getHitExpired304Count() {
		return hitExpired304;
	}

	/**
	 * Returns the count of duplicate downloads that are not expired.
	 * 
	 * @return An int that is the number of not expired duplicate downloads.
	 */
	public int getHitNotExpiredDupCount() {
		return hitNotExpiredDup;
	}

	/**
	 * Returns the ratio of the amount of content containing cache headers compared with 
	 * the total amount of content. 
	 * 
	 * @return A double value that is the cache header ratio.
	 */
	public double getCacheHeaderRatio() {
		return cacheHeaderRatio;
	}

	/**
	 * Returns the ratio of the amount of time that the GPS is in an active state compared 
	 * to the total duration.
	 * 
	 * @return A double value that is the GPS active state ratio.
	 */
	public double getGPSActiveStateRatio() {
		return gpsActiveStateRatio;
	}

	/**
	 * Returns the ratio of the amount of time that Bluetooth is in an active state 
	 * compared to the total duration. 
	 * 
	 * @return A double value that is the Bluetooth active state ratio.
	 */
	public double getBluetoothActiveStateRatio() {
		return bluetoothActiveStateRatio;
	}

	/**
	 * Returns the ratio of the amount of time that the camera is in an active state 
	 * compared to the total duration. 
	 * 
	 * @return A double value that is the camera active state ratio.
	 */
	public double getCameraActiveStateRatio() {
		return cameraActiveStateRatio;
	}

	/**
	 * Returns the state of the Accessing Peripherals test.
	 * 
	 * @return A boolean value that is true if any peripherals were on for more than 5% of 
	 * the total duration, and is false otherwise.
	 */
	public boolean getAccessingPeripherals() {
		return accessingPeripherals;
	}

	/**
	 * Returns a value that indicates if an HTTP 1.0 header was found in the content.
	 * 
	 * @return A boolean value that is true if getHttp1_0HeaderCount() returns a value 
	 * greater than 0, and is false otherwise.
	 */
	public boolean getHttp10Usage() {
		return http1_0HeaderCount == 0;
	}

	/**
	 * Returns the ratio of the amount of duplicate content in bytes, compared to the 
	 * total amount of content in bytes. 
	 * 
	 * @return A double that is the duplicate content bytes ratio.
	 */
	public double getDuplicateContentBytesRatio() {
		return duplicateContentBytesRatio;
	}

	/**
	 * Returns the size of duplicate content files in bytes. 
	 * 
	 * @return An int that is the duplicate content size.
	 */
	public int getDuplicateContentsize() {
		return duplicateContentsize;
	}

	/**
	 * Returns the amount of duplicate content in bytes. 
	 * 
	 * @return The amount of duplicate content in bytes.
	 */
	public long getDuplicateContentBytes() {
		return duplicateContentBytes;
	}

	/**
	 * Returns the total amount of content in bytes. 
	 * 
	 * @return A long that is the total amount of content in bytes.
	 */
	public long getTotalContentBytes() {
		return totalContentBytes;
	}

	/**
	 * Returns a value that indicates whether the application is using a cache. 
	 * 
	 * @return A boolean value that is true if the application is using a cache, and is 
	 * false otherwise.
	 */
	public boolean isUsingCache() {
		return usingCache;
	}

	/**
	 * Returns a value that indicates if the Best Practices Cache Control test has passed. 
	 * The test passes when the amount of "not expired duplicate data" is NOT greater than 
	 * the amount of "not changed data" (data for which a 304 response is received, 
	 * indicating that the data has not been modified since it was last requested). 
	 * 
	 * @return A boolean value that is true if the Cache Control test has passed, and is 
	 * false otherwise.
	 */
	public boolean isCacheControl() {
		return cacheControl;
	}

	/**
	 * Returns the ratio of the amount of TCP control energy compared with the total 
	 * amount of energy. 
	 * 
	 * @return A double value that is the TCP control energy ratio. 
	 */
	public double getTcpControlEnergyRatio() {
		return tcpControlEnergyRatio;
	}

	/**
	 * Returns the amount of energy used for TCP control. 
	 * 
	 * @return A double that is the amount of TCP control energy.
	 */
	public double getTcpControlEnergy() {
		return tcpControlEnergy;
	}

	/**
	 * Returns the largest energy time. 
	 * 
	 * @return A double that is the largest energytime.
	 */
	public double getLargestEnergyTime() {
		return largestEnergyTime;
	}
	
	/**
	 * Returns the begin time of the screen rotation burst.
	 * 
	 * @return A double that is the begin time of the screen rotation burst.
	 */
	public double getScreenRotationBurstTime() {
		return screenRotationBurstTime;
	}

	/**
	 * Returns the total amount of active DCH time for bursts in the Large Burst category.
	 * 
	 * @return A double that is the Large Burst time.
	 */
	public double getLargeBurstTime() {
		return largeBurstTime;
	}

	/**
	 * Returns the count of HTTP 1.0 headers. 
	 * 
	 * @return An int that is the number of HTTP 1.0 headers.
	 */
	public int getHttp1_0HeaderCount() {
		return http1_0HeaderCount;
	}

	/**
	 * Returns an object containing the HTTP 1.0 session. 
	 * 
	 * @return A TCPSession object containing the HTTP 1.0 session.
	 */
	public TCPSession getHttp1_0Session() {
		return http10Session;
	}

	/**
	 * Returns the trace analysis data.
	 * 
	 * @return An Analysis object containing the trace analysis data.
	 */
	public TraceData.Analysis getAnalysisData() {
		return analysisData;
	}

}
