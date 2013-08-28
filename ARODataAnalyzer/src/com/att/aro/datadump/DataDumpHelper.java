/*
 * Copyright 2013 AT&T
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
package com.att.aro.datadump;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.model.BurstCategory;
import com.att.aro.util.Util;

public abstract class DataDumpHelper {

	static final String ANCHOR = ">>";
	static final String appScore = Util.RB.getString("appscore.title");
	static final String basicStat = Util.RB.getString("datadump.basicstat");
	static final String bestPractices = createCSVEntry(Util.RB.getString("datadump.bpractices"));
	static final String bpAssoData = Util.RB.getString("datadump.bpassodata");
	static final String bpPassFail = Util.RB.getString("datadump.bppassfail");
	static final int BURST_INFO_SIZE_ACTUAL = 7;
	static final String burstAnalysis = Util.RB.getString("burstAnalysis.title");
	static final String burstbytes = Util.RB.getString("burstAnalysis.bytes");
	static final String burstbytpct = Util.RB.getString("burstAnalysis.bytesPct");
	static final String burstEner = Util.RB.getString("burstAnalysis.energy");
	static final String burstEngpct = Util.RB.getString("burstAnalysis.energyPct");
	static final String burstjpkb = Util.RB.getString("burstAnalysis.jpkb");
	static final String COMMA_SEP = Util.RB.getString("statics.csvCell.seperator");
	static final String conStats = Util.RB.getString("overview.sessionoverview.title");
	static final String dch = Util.RB.getString("datadump.dch");
	static final String endpntapp = Util.RB.getString("endpointsummary.appname");
	static final String endpntbyte = Util.RB.getString("endpointsummary.bytes");
	static final String endpntpkt = Util.RB.getString("endpointsummary.packets");
	static final String endpntSumm = Util.RB.getString("endpointsummary.title");
	static final String energyJ = Util.RB.getString("statics.csvUnits.j");
	static final String energySimulation = Util.RB.getString("datadump.energysimulation");
	static final String filetype = Util.RB.getString("datadump.filetype");
	static final String LINE_SEP = System.getProperty(Util.RB.getString("statics.csvLine.seperator"));
	static final String ltecr = Util.RB.getString("burstAnalysis.lteCr");
	static final String ltecrpct = Util.RB.getString("burstAnalysis.lteCrPct");
	static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0.00");
	static final String packettype = Util.RB.getString("packet.type");
	static final String pct = Util.RB.getString("simple.percent");
	static final String pctdch = Util.RB.getString("datadump.pctdch");
	static final String pctwifiActive = Util.RB.getString("burstAnalysis.wifiActivePct");
	static final String percentile = Util.RB.getString("overview.traceoverview.percentile");
	static final String QUOTE_SEP = "\"";
	static final String rrcCellDCH = Util.RB.getString("datadump.celldch");
	static final String rrcCellFACH = Util.RB.getString("datadump.cellfach");
	static final String rrcContRecep = Util.RB.getString("rrc.continuousReception");
	static final String rrcContReceptail = Util.RB.getString("rrc.continuousReceptionTail");
	static final String rrcFACHDCH = Util.RB.getString("datadump.fachdch");
	static final String rrcIdle = Util.RB.getString("datadump.idle");
	static final String rrcIdleDCH = Util.RB.getString("datadump.idledch");
	static final String rrcIdleToCont = Util.RB.getString("rrc.continuousReceptionIdle");
	static final String rrcLDRX = Util.RB.getString("rrc.longDRX");
	static final String rrcPCT = Util.RB.getString("datadump.pct");
	static final String rrcSDRX = Util.RB.getString("rrc.shortDRX");
	static final String rrcSec = Util.RB.getString("datadump.seconds");
	static final String rrcStatMachSim = Util.RB.getString("datadump.rrcstatmcsimulation");
	static final String value = Util.RB.getString("overview.traceoverview.value");
	static final String wifiAct = Util.RB.getString("rrc.wifiActive");
	static final String wifiActive = Util.RB.getString("burstAnalysis.wifiActive");
	static final String wifiIdle = Util.RB.getString("rrc.WiFiIdle");
	static final String wifiTail = Util.RB.getString("rrc.WifiTail");

	/**
	 * Adds anchor helping with troubleshooting exported data alignment issues.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	static void addAnchor(FileWriter writer) throws IOException {
		writer.append(COMMA_SEP);
		writer.append(ANCHOR);
	}

	/**
	 * Add specified number of commas.
	 * 
	 * @param writer
	 * @param repeat
	 * @throws IOException
	 */
	static void addCommas(FileWriter writer, int repeat) throws IOException {
		for (int i = 1; i <= repeat; i++) {
			writer.append(COMMA_SEP);
		}
	}

	/**
	 * Method to convert the {@link Object} values in to {@link String} values.
	 * 
	 * @param val
	 *            {@link Object} value retrieved from the table cell.
	 * @return Cell data in string format.
	 */
	static String createCSVEntry(Object val) {
		StringBuffer writer = new StringBuffer();
		String str = val != null ? val.toString() : "";
		writer.append('"');
		for (char c : str.toCharArray()) {
			switch (c) {
			case '"':
				// Add an extra
				writer.append("\"\"");
				break;
			default:
				writer.append(c);
			}
		}
		writer.append('"');
		return writer.toString();
	}

	/**
	 * Starts a new line.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	static void startNewLine(FileWriter writer) throws IOException {
		writer.append(LINE_SEP);
	}

	/**
	 * Adds Best Practice headers.
	 * 
	 * @param writer
	 * @param bpc
	 * @throws IOException
	 */
	private void addBestPractice(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {
		// Write best practice titles
		for (BestPracticeDisplay bp : bpc) {
			addContinuousHeader(writer, createCSVEntry(bp.getDetailTitle()), 1);
		}
	}

	/**
	 * Adds Best Practice headers.
	 * 
	 * @param writer
	 * @param bpc
	 * @throws IOException
	 */
	void addBestPracticeHeadersLine3(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {
		// Write best practice column headers
		for (int i = 0; i < bpc.size(); i++) {
			writer.append(COMMA_SEP);
			writer.append(createCSVEntry(bpPassFail));
			writer.append(COMMA_SEP);
			writer.append(createCSVEntry(bpAssoData));
		}
	}

	/**
	 * Adds Burst Category Headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addBurstCategoryTitles(FileWriter writer) throws IOException {
		
		for (BurstCategory bc : BurstCategory.values()) {
			// unknown bursts are excluded
			if ( !bc.equals(BurstCategory.UNKNOWN)) {
				addContinuousHeaders(writer, bc.getBurstTypeDescription(), BURST_INFO_SIZE_ACTUAL);
			}
		}
	}

	/**
	 * Adds Connection Statistic headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addConnectionStatistics(FileWriter writer) throws IOException {
		for (int j = 0; j <= 4; j++) {
			writer.append(COMMA_SEP);
			writer.append(conStats);
		}
	}

	/**
	 * Adds Continuous headers.
	 * 
	 * @param writer
	 * @param inputStr
	 * @param maxLength
	 * @throws IOException
	 */
	@Deprecated
	void addContinuousHeader(FileWriter writer, String inputStr, int maxLength) throws IOException {
		for (int i = 0; i <= maxLength; i++) {
			writer.append(COMMA_SEP);
			writer.append(inputStr);
		}
	}
	
	
	/**
	 * Adds Continuous headers.
	 * 
	 * @param writer
	 * @param inputStr
	 * @param size
	 * @throws IOException
	 */
	void addContinuousHeaders(FileWriter writer, String inputStr, int size) throws IOException {
		for (int i = 1; i <= size; i++) {
			writer.append(COMMA_SEP);
			writer.append(inputStr);
		}
	}	

	/**
	 * Adds Energy headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addEnergyHeaderLine3(FileWriter writer) throws IOException {
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("burstAnalysis.jpkb"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.gpsActive"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.gpsStandby"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.gpsTotal"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.cameraTotal"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.bluetoothActive"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.bluetoothStandby"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.bluetoothTotal"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("energy.screenTotal"));
	}

	/**
	 * Adds File Type headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addFileTypes(FileWriter writer) throws IOException {
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 3; j++) {
				writer.append(COMMA_SEP);
				writer.append(filetype + i);
			}
		}
	}

	/**
	 * Adds End Point Summary Headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addFiveEndPointSummaryHeaders(FileWriter writer) throws IOException {
		// Five End Point Summary headers
		for (int i = 1; i <= 5; i++) {
			for (int j = 1; j <= 3; j++) {
				writer.append(COMMA_SEP);
				writer.append(endpntSumm + " " + i);
			}
		}
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @param bpc
	 */
	public abstract void addHeader(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException;

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @param bpc
	 * @return
	 * @throws IOException
	 */
	void addHeadersLine2b(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {

		addContinuousHeader(writer, Util.RB.getString("datadump.energytitle"), 7);

		addAnchor(writer);

		addBurstCategoryTitles(writer);

		addAnchor(writer);

		addFileTypes(writer);
		addContinuousHeader(writer, Util.RB.getString("Export.traceoverview.throughput"), 1);
		addContinuousHeader(writer, Util.RB.getString("Export.traceoverview.jpkb"), 1);
		addContinuousHeader(writer, Util.RB.getString("Export.traceoverview.promoratio"), 1);
		addConnectionStatistics(writer);
		addTraceScoreStatistics(writer);
		addFiveEndPointSummaryHeaders(writer);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheable"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.nonCachable"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheMiss"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.notCacheable"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheHitExpiredDup304").replace(COMMA_SEP, ""), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheHitRespChanged").replace(COMMA_SEP, ""), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheHitNotExpiredDup"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheHitExpiredClientDup").replace(COMMA_SEP, ""), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.cacheHitExpiredServerDup").replace(COMMA_SEP, ""), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.partialHitNotExpiredDup"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.partialHitExpiredClientDup").replace(COMMA_SEP, ""), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.partialHitExpiredServerDup").replace(COMMA_SEP, ""), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.notExpired"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.notExpiredHeur"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.expired"), 1);
		addContinuousHeader(writer, Util.RB.getString("cache.expiredHeur"), 1);
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addHeadersLine3a(FileWriter writer) throws IOException {
		for (int i = 0; i <= 4; i++) {
			writer.append(COMMA_SEP);
			writer.append(packettype);
			writer.append(COMMA_SEP);
			writer.append(pct);
			writer.append(COMMA_SEP);
			writer.append(burstbytes);
		}
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addHeadersLine3b(FileWriter writer) throws IOException {
		for (int i = 0; i <= 2; i++) {
			writer.append(COMMA_SEP);
			writer.append(value);
			writer.append(COMMA_SEP);
			writer.append(percentile);
		}
	}

	/**
 	 * Adds headers.
 	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addHeadersLine3c(FileWriter writer) throws IOException {
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("Export.sessionoverview.sessionTerm"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("Export.sessionoverview.longBurstTerm"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("Export.sessionoverview.tightlyGroupedBurstTerm"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("Export.sessionoverview.periodicBurstTerm"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("Export.sessionoverview.nonPeriodicBurstTerm"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("appscore.subtitle.causes"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("appscore.subtitle.effects"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("appscore.subtitle.total"));
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addHeadersLine3d(FileWriter writer) throws IOException {
		for (int i = 0; i <= 4; i++) {
			writer.append(COMMA_SEP);
			writer.append(endpntapp);
			writer.append(COMMA_SEP);
			writer.append(endpntpkt);
			writer.append(COMMA_SEP);
			writer.append(endpntbyte);
		}
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addHeadersLine3e(FileWriter writer) throws IOException {
		for (int j = 0; j <= 15; j++) {
			writer.append(COMMA_SEP);
			writer.append(Util.RB.getString("statics.csvFormat.response"));
			writer.append(COMMA_SEP);
			writer.append(Util.RB.getString("statics.csvFormat.bytes"));
		}
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @param bpc
	 * @return
	 * @throws IOException
	 */
	void addHeadersLine1(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {

		addCommas(writer, 8);

		addAnchor(writer);

		// exclude unknown burst
		int len = BurstCategory.values().length - 1;
		addContinuousHeaders(writer, burstAnalysis, len * BURST_INFO_SIZE_ACTUAL);

		addAnchor(writer);

		addCommas(writer, 15);
		addContinuousHeader(writer, Util.RB.getString("overview.traceoverview.title"), 5);
		addCommas(writer, 23);
		addContinuousHeader(writer, Util.RB.getString("datadump.cachenoncache"), 3);
		addContinuousHeader(writer, Util.RB.getString("datadump.cacheCacheSim"), 19);
		addContinuousHeader(writer, Util.RB.getString("datadump.dupFile"), 7);
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @param bpc
	 * @throws IOException
	 */
	void addHeadersLine2a(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {
		addCommas(writer, 4);
		addBestPractice(writer, bpc);
		addContinuousHeader(writer, basicStat, 3);
	}

	/**
	 * Adds Teace Info headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addTraceInfoHeaderLine3a(FileWriter writer) throws IOException {
		writer.append(Util.RB.getString("datadump.tracename"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.tracedate"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.tracetime"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.appVersion"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.appnamever"));
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addTraceInfoHeadersLine3b(FileWriter writer) throws IOException {
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.sizeinbyte"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.durationinsec"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.packets"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.avgrate"));
	}

	/**
	 * Adds headers.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addTraceScoreStatistics(FileWriter writer) throws IOException {
		for (int j = 0; j <= 2; j++) {
			writer.append(COMMA_SEP);
			writer.append(appScore);
		}
	}
	
	/**
	 * Adds Burst Headers Line 3
	 * 
	 * @param writer
	 * @throws IOException
	 */
	void addBurstHeadersLine3(FileWriter writer, String header) throws IOException {
		// exclude 1 burst, unknown burst
		int len = BurstCategory.values().length -1;
		for (int i = 1; i <= len; i++) {
			writer.append(header);
		}
	}
	
}
