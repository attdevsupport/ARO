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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.bp.BestPracticeDisplayFactory;
import com.att.aro.bp.fileorder.FileOrderAnalysis;
import com.att.aro.bp.fileorder.FileOrderEntry;
import com.att.aro.bp.fileorder.FileOrderResultPanel;
import com.att.aro.bp.imageSize.ImageSizeAnalysis;
import com.att.aro.bp.imageSize.ImageSizeEntry;
import com.att.aro.bp.imageSize.ImageSizeResultPanel;
import com.att.aro.bp.minification.MinificationAnalysis;
import com.att.aro.bp.minification.MinificationEntry;
import com.att.aro.bp.minification.MinificationResultPanel;
import com.att.aro.bp.spriteimage.SpriteImageAnalysis;
import com.att.aro.bp.spriteimage.SpriteImageEntry;
import com.att.aro.bp.spriteimage.SpriteImageResultPanel;
import com.att.aro.bp.asynccheck.AsyncCheckAnalysis;
import com.att.aro.bp.asynccheck.AsyncCheckEntry;
import com.att.aro.bp.asynccheck.AsyncCheckResultPanel;
import com.att.aro.main.TextFileCompressionResultPanel;
import com.att.aro.model.HttpRequestResponseInfo.Direction;

/**
 * A bean class that contains the information that appears on the Best Practices tab, 
 * such as the pass/fail status of the test, and the test results.
 */
public class BestPractices {

	private static final Logger LOGGER = Logger.getLogger(BestPractices.class.getName());

	private static final int PERIPHERAL_ACTIVE_LIMIT = 5;

	private TraceData.Analysis analysisData;
	private TraceData traceData;

	private int http1_0HeaderCount = 0;
	private TCPSession http10Session = null;
	
	private SortedMap<Integer, Integer> httpErrorCounts = new TreeMap<Integer, Integer>();
	private Map<Integer, HttpRequestResponseInfo> firstErrorRespMap = new HashMap<Integer, HttpRequestResponseInfo>();
	private SortedMap<Integer, Integer> httpRedirectCounts = new TreeMap<Integer, Integer>();
	private Map<Integer, HttpRequestResponseInfo> firstRedirectRespMap = new HashMap<Integer, HttpRequestResponseInfo>();

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
	private int inefficientCssRequests = 0;
	private int inefficientJsRequests = 0;
	private double cacheHeaderRatio = 0.0;
	private double tcpControlEnergyRatio = 0.0;
	private double tcpControlEnergy = 0.0;
	private double largestEnergyTime = 0.0;
	private double screenRotationBurstTime = 0.0;
	private PacketInfo noCacheHeaderFirstPacket;
	private PacketInfo dupContentFirstPacket;
	private PacketInfo cacheControlFirstPacket = null;
	private PacketInfo consecutiveCssJsFirstPacket = null;

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
		int tmpUserInputBurstCount = 0;
		double wastedBurstEnergy = 0.0;
		double maxEnergy = 0.0;
		double largestEnergyTime = 0.0;

		// Burst Best practices.
		for (Burst burst : analysisData.getBcAnalysis().getBurstCollection()) {
			
			LOGGER.log(Level.FINE, "Burst category: {0}", burst.getBurstCategory());
			

			// Largest burst time
			double time = burst.getEndTime() - burst.getBeginTime();
			if (time > largestBurstTime) {
				largestBurstTime = time;
				largestBurstBeginTime = burst.getBeginTime();
			}

			/*
			 * Counts number of subsequent USER bursts.
			 * Stores the highest number to be used for the best practice tab. 
			 */
			if (BurstCategory.USER_INPUT == burst.getBurstCategory()) {
				burstCategoryCount++;
			} else {
				burstCategoryCount = 0;
			}
			LOGGER.log(Level.FINE, "burstCategoryCount set to: {0}", burstCategoryCount);
			tmpUserInputBurstCount = Math.max(tmpUserInputBurstCount, burstCategoryCount);
			LOGGER.log(Level.FINE, "tmpUserInputBurstCount set to: {0}", tmpUserInputBurstCount);


			if (burst.getBurstCategory() == BurstCategory.TCP_PROTOCOL) {
				double currentEnergy = burst.getEnergy();
				wastedBurstEnergy += currentEnergy;
				if (currentEnergy > maxEnergy) {
					maxEnergy = currentEnergy;
					largestEnergyTime = burst.getBeginTime();
				}
			}
			
			//Verifying burst category to update screen rotation flag value which 
			//shows whether screen rotation triggered network activity or not.
			if (BurstCategory.SCREEN_ROTATION == burst.getBurstCategory() && this.screenRotation) {
				this.screenRotation = false;
				this.screenRotationBurstTime = burst.getBeginTime();
			}
		}
		this.largeBurstTime = largestBurstBeginTime;
		this.userInputBurstCount = tmpUserInputBurstCount;
		this.offloadingToWiFi = (bcAnalysis.getLongBurstCount() <= 3);
		if (bcAnalysis.getTotalEnergy() > 0) {
			double percentageWasted = wastedBurstEnergy / bcAnalysis.getTotalEnergy();
			this.conClosingProb = (percentageWasted < 0.05);
			this.tcpControlEnergy = wastedBurstEnergy;
			this.tcpControlEnergyRatio = percentageWasted;
			this.largestEnergyTime = largestEnergyTime;
		}

		/* Check HTTP 1.0 best practice and HTTP 301/302 and 4xx/5xx best practices
		 * and calculate number of inefficient Css and Js requests.
		*/
		for (TCPSession s : analysisData.getTcpSessions()) {
			double cssLastTimeStamp = 0.0;
			double jsLastTimeStamp = 0.0;
			for (HttpRequestResponseInfo reqRessInfo : s.getRequestResponseInfo()) {
				if (HttpRequestResponseInfo.HTTP10.equals(reqRessInfo.getVersion())) {
					++http1_0HeaderCount;
					if (null == http10Session) {
						http10Session = s;
					}
				}
				
				if (reqRessInfo.getDirection() == Direction.RESPONSE
						&& HttpRequestResponseInfo.HTTP_SCHEME
								.equals(reqRessInfo.getScheme())
						&& reqRessInfo.getStatusCode() >= 400) {
					Integer status = Integer.valueOf(reqRessInfo.getStatusCode());
					Integer count = httpErrorCounts.get(status);
					if (count != null) {
						httpErrorCounts.put(status, count + 1);
					} else {
						httpErrorCounts.put(status, 1);
					}
					if (firstErrorRespMap.get(status) == null) {
						firstErrorRespMap.put(status, reqRessInfo);
					}
				}
				
				if (reqRessInfo.getDirection() == Direction.RESPONSE
						&& HttpRequestResponseInfo.HTTP_SCHEME
								.equals(reqRessInfo.getScheme())) {
					int code = reqRessInfo.getStatusCode();
					if ((code == 301) || (code == 302)) {
						Integer status = Integer.valueOf(reqRessInfo
								.getStatusCode());
						Integer count = httpRedirectCounts.get(status);
						if (count != null) {
							httpRedirectCounts.put(status, count + 1);
						} else {
							httpRedirectCounts.put(status, 1);
						}
						if (firstRedirectRespMap.get(status) == null) {
							firstRedirectRespMap.put(status, reqRessInfo);
						}
					}
				}
				
				/* Calculate number of inefficient Css and Js requests. */
				if (reqRessInfo.getDirection() == Direction.RESPONSE) {
					if (reqRessInfo.getContentType() != null) {
						PacketInfo pktInfo = reqRessInfo.getFirstDataPacket();
						if (pktInfo != null) {
							if (reqRessInfo.getContentType().equalsIgnoreCase("text/css")) {
								if (cssLastTimeStamp == 0.0) {
									cssLastTimeStamp = pktInfo.getTimeStamp();
									continue;
								} else {
									if ((pktInfo.getTimeStamp() - cssLastTimeStamp) <= 2.0) {
										inefficientCssRequests++;
										if (consecutiveCssJsFirstPacket == null) {
											consecutiveCssJsFirstPacket = pktInfo;
										}
									}
									cssLastTimeStamp = pktInfo.getTimeStamp();
								}
							} else if (reqRessInfo.getContentType().equalsIgnoreCase("text/javascript") 
								|| reqRessInfo.getContentType().equalsIgnoreCase("application/x-javascript")
								|| reqRessInfo.getContentType().equalsIgnoreCase("application/javascript")) {
								if (jsLastTimeStamp == 0.0) {
									jsLastTimeStamp = pktInfo.getTimeStamp();
									continue;
								} else {
									if ((pktInfo.getTimeStamp() - jsLastTimeStamp) < 2.0) {
										inefficientJsRequests++;
										if (consecutiveCssJsFirstPacket == null) {
											consecutiveCssJsFirstPacket = pktInfo;
										}
									}
									jsLastTimeStamp = pktInfo.getTimeStamp();
								}
							}
						}
					}
				}//End of /* Calculate number of inefficient Css and Js requests. */
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
			
			if (this.cacheControlFirstPacket == null && hitNotExpiredDup > hitExpired304) {
				this.cacheControlFirstPacket = entry.getSessionFirstPacket();
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

		setResultsOfTextFileCompressionTest(analysisData);
		setResultsOfImageSizeTest(analysisData);
		setResultsOfMinificationTest(analysisData);
		setResultsOfSpriteImageTest(analysisData);
		setResultsOfAsyncCheckTest(analysisData);
		setResultsOfFileOrderTest(analysisData);
	}

	/**
	 * Sets text file compression test results.
	 * 
	 * @param analysisData
	 */
	private void setResultsOfTextFileCompressionTest(TraceData.Analysis analysisData) {
		// obtain the results of Text File Compression Analysis
		TextFileCompressionAnalysis tfca = analysisData.getTextFileCompressionAnalysis();
		List<TextFileCompressionEntry> tfcaResult = tfca.getResults();
		TextFileCompressionResultPanel tfc = BestPracticeDisplayFactory.getInstance().getTextFileCompression();
		tfc.setData(tfcaResult);
	}

	/**
	 * Sets image size test results.
	 * 
	 * @param analysisData
	 */
	private void setResultsOfImageSizeTest(TraceData.Analysis analysisData) {
		// obtain the results of Image Size Analysis
		ImageSizeAnalysis isa = analysisData.getImageSizeAnalysis();
		List<ImageSizeEntry> results = isa.getResults();
		ImageSizeResultPanel panel = BestPracticeDisplayFactory.getInstance().getImageSize();
		panel.setData(results);
	}

	/**
	 * Sets minification test results.
	 * 
	 * @param analysisData
	 */
	private void setResultsOfMinificationTest(TraceData.Analysis analysisData) {
		// obtain the results of Minification Analysis
		MinificationAnalysis ma = analysisData.getMinificationAnalysis();
		List<MinificationEntry> results = ma.getResults();
		MinificationResultPanel panel = BestPracticeDisplayFactory.getInstance().getMinification();
		panel.setData(results);
	}
	
	/**
	 * Sets Sprite Image test results.
	 * 
	 * @param analysisData
	 */
	private void setResultsOfSpriteImageTest(TraceData.Analysis analysisData) {
		// obtain the results of Sprite Image Analysis
		SpriteImageAnalysis siAnalysis = analysisData.getSpriteImageAnalysis();
		List<SpriteImageEntry> results = siAnalysis.getResults();
		SpriteImageResultPanel panel = BestPracticeDisplayFactory.getInstance().getSpriteImageResults();
		panel.setData(results);
	}

	/**
	 * Sets Async loading results
	 * 
	 * @param analysisData
	 */
	private void setResultsOfAsyncCheckTest(TraceData.Analysis analysisData) {
		// obtain the results of TFC Analysis
		AsyncCheckAnalysis asyncAnalysis = analysisData.getAsyncCheckAnalysis();
		List<AsyncCheckEntry> asyncAnalysisResults = asyncAnalysis.getResults();
		AsyncCheckResultPanel AsyncResultPanel = BestPracticeDisplayFactory.getInstance().getAsyncCheckResults();
		AsyncResultPanel.setData(asyncAnalysisResults);
	}

	/**
	 * Sets File Order results 
	 * 
	 * @param analysisData
	 */
	private void setResultsOfFileOrderTest(TraceData.Analysis analysisData) {
		// obtain the results of TFC Analysis
		FileOrderAnalysis fileOrderAnalysis = analysisData.getFileOrderAnalysis();
		
		List<FileOrderEntry> fileOrderResults = fileOrderAnalysis.getResults();
		FileOrderResultPanel fileOrderResultPanel = BestPracticeDisplayFactory.getInstance().getFileOrderResultPanel();
		fileOrderResultPanel.setData(fileOrderResults);
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
	 * @return A boolean value that is false if the trace data shows a connection closing 
	 * problem, and true otherwise.
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
	 * Returns the PacketInfo of session with cache control. 
	 * 
	 * @return PacketInfo.
	 */
	public PacketInfo getCacheControlStartTime() {
		return cacheControlFirstPacket;
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
	 * Returns the PacketInfo of session with consecutive Css or Js.
	 * 
	 * @return PacketInfo.
	 */
	public PacketInfo getConsecutiveCssJsFirstPacket() {
		return consecutiveCssJsFirstPacket;
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
	 * Returns the count of inefficient Css requests. 
	 * 
	 * @return An int that is the number of inefficient Css requests.
	 */
	public int getInefficientCssRequests() {
		return inefficientCssRequests;
	}
	
	/**
	 * Returns the count of inefficient Js requests. 
	 * 
	 * @return An int that is the number of inefficient Js requests.
	 */
	public int getInefficientJsRequests() {
		return inefficientJsRequests;
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
	 * Returns a boolean indicating the async check test result 
	 * 
	 * @return true/false based on the test result
	 */
	public boolean isAsyncCheckTestFailed()	{
		AsyncCheckAnalysis asyncAnalysis = analysisData.getAsyncCheckAnalysis();
		return asyncAnalysis.isTestFailed();
	}
	
	/**
	 * Returns the count for synchronously loaded files 
	 * 
	 * @return integer for the count
	 */
	public int getSyncLoadedCount(){
		AsyncCheckAnalysis asyncAnalysis = analysisData.getAsyncCheckAnalysis();
		return asyncAnalysis.getSyncPacketCount();
	}
	
	/**
	 * Returns the count for asynchronously loaded files 
	 * 
	 * @return integer for the count
	 */
	public int getSyncPacketCount(){
		AsyncCheckAnalysis asyncAnalysis = analysisData.getAsyncCheckAnalysis();
		return asyncAnalysis.getSyncPacketCount();
	}

	/**
	 * Returns the count for asynchronously loaded files 
	 * 
	 * @return integer for the count
	 */
	public int getAsyncPacketCount(){
		AsyncCheckAnalysis asyncAnalysis = analysisData.getAsyncCheckAnalysis();
		return asyncAnalysis.getAsyncPacketCount();
	}

	
	/**
	 * Indicates whether the Text File Compression test failed or not.
	 * 
	 * @return File Compression test status.
	 */
	public boolean isTextFileCompresionTestFailed() {
		return analysisData.getTextFileCompressionAnalysis().isTestFailed();
	}

	/**
	 * Get the number of uncompressed text file calculated as percentage.
	 * 
	 * @return Percentage of uncompressed text file
	 */
	public double getFileCompressionPercentage() {
		// obtain the results of TFC Analysis
		TextFileCompressionAnalysis tfca = analysisData.getTextFileCompressionAnalysis();
		return tfca.getPercentage();
	}
	
	/**
	 * Get total size of all uncompressed files in kilobytes.
	 * 
	 * @return Percentage of uncompressed text file
	 */
	public double getTotalSize() {
		// obtain the results of TFC Analysis
		TextFileCompressionAnalysis tfca = analysisData.getTextFileCompressionAnalysis();
		return tfca.getTotalSize();
	}

	/**
	 * Get the number of large image files.
	 * 
	 * @return number of large image files
	 */
	public int getNumberOfImageFiles() {
		return analysisData.getImageSizeAnalysis().getNumberOfImageFiles();
	}
	
	/**
	 * Get the number of minify files.
	 * 
	 * @return number of files to minify
	 */
	public int getNumberOfMinifyFiles() {
		return analysisData.getMinificationAnalysis().getNumberOfMinifyFiles();
	}
	
	/**
	 * Get the number of sprite image files.
	 * 
	 * @return number of sprite image files
	 */
	public int getNumberOfFilesToBeSprited() {
		return analysisData.getSpriteImageAnalysis().getNumberOfFilesToBeSprited();
	}
	
	//Returns a boolean value depends on the file order test failure
	 
	public boolean isFileOrderTestFailed(){
		FileOrderAnalysis fileOrderAnalysis = analysisData.getFileOrderAnalysis();
		return fileOrderAnalysis.isTestFailed();
	}
	
	/**
	 * Returns the number of files which fails file order test
	 * */
	public int getFileOrderCount(){
		FileOrderAnalysis fileOrderAnalysis = analysisData.getFileOrderAnalysis();
		return fileOrderAnalysis.getFileOrderCount();
	}
	

	/**
	 * Returns a sorted map of the counts of HTTP error status codes that 
	 * occurred in the trace analysis.  Note that only status codes of 400+ are
	 * included.
	 * @return the httpErrorCounts map key is HTTP status code and value is
	 * number of occurrences of the status code
	 */
	public SortedMap<Integer, Integer> getHttpErrorCounts() {
		return Collections.unmodifiableSortedMap(httpErrorCounts);
	}
	
	/**
	 * Returns map of HTTP status codes to first response instance in analysis
	 * that has the specified error code.  Note that only status codes of 400+
	 * are included.
	 * @return the firstErrorRespMap map key is HTTP status code and value is
	 * associated HTTP response
	 */
	public Map<Integer, HttpRequestResponseInfo> getFirstErrorRespMap() {
		return Collections.unmodifiableMap(firstErrorRespMap);
	}
	
	/**
	 * Returns a sorted map of the counts of HTTP redirect status codes that 
	 * occurred in the trace analysis.  Note that only status codes of 301/302
	 * are included.
	 * @return the httpRedirectCounts map key is HTTP status code and value is
	 * number of occurrences of the status code
	 */
	public SortedMap<Integer, Integer> getHttpRedirectCounts() {
		return Collections.unmodifiableSortedMap(httpRedirectCounts);
	}

	/**
	 * Returns map of HTTP status codes to first response instance in analysis
	 * that has the specified error code.  Note that only status codes of 301/302
	 * are included.
	 * @return the firstErrorRespMap map key is HTTP status code and value is
	 * associated HTTP response
	 */
	public Map<Integer, HttpRequestResponseInfo> getFirstRedirectRespMap() {
		return Collections.unmodifiableMap(firstRedirectRespMap);
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
