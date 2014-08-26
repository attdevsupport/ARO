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
import java.util.Collection;

import com.att.aro.bp.BestPracticeDisplay;
import com.att.aro.model.BurstCategory;
import com.att.aro.util.Util;

public class DataDump3G extends DataDumpHelper {

	private static final int COMMAS_2 = 2;
	private static final int COMMAS_4 = 4;
	private static final int COMMAS_5 = 5;
	private static final int ENERGY_SIM_15 = 15;
	private static final int RRC_ENERGY_HEADERS_7 = 7;
	private static final int RRC_HEADERS_4 = 4;
	private static final int RRC_STATE_MACH_14 = 14;

	/**
	 * Adds 3G headers.
	 * 
	 * @param writer
	 * @param bpc
	 *            best practice display collection
	 */
	public void addHeader(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {

		// LINE 1:
		addContinuousHeader(writer, bestPractices, (bpc.size() * 2) - 1);
		addCommas(writer, COMMAS_4);
		addContinuousHeader(writer, rrcStatMachSim, RRC_STATE_MACH_14);
		addContinuousHeader(writer, energySimulation, ENERGY_SIM_15);
		addHeadersLine1(writer, bpc);

		// LINE 2:
		startNewLine(writer);

		addHeadersLine2a(writer, bpc);
		addRrcHeadersLine2a(writer);
		addCommas(writer, COMMAS_5);
		addRrcHeadersLine2b(writer);
		addHeadersLine2b(writer, bpc);

		// LINE 3:
		startNewLine(writer);

		addTraceInfoHeaderLine3a(writer);
		addBestPracticeHeadersLine3(writer, bpc);
		addTraceInfoHeadersLine3b(writer);
		addRrcHeadersLine3a(writer);
		addRrcHeadersLine3b(writer);
		addRrcEnergyHeadersLine3(writer);
		addEnergyHeaderLine3(writer);

		addAnchor(writer);

		addBurstHeadersLine3(writer);

		addAnchor(writer);

		addHeadersLine3a(writer);
		addHeadersLine3b(writer);
		addHeadersLine3c(writer);
		addHeadersLine3d(writer);
		addHeadersLine3e(writer);
	}

	/**
	 * Adds Burst Headers Line 3.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addBurstHeadersLine3(FileWriter writer) throws IOException {
		StringBuilder strBuff = new StringBuilder();
		strBuff.append(COMMA_SEP);
		strBuff.append(burstbytes);
		strBuff.append(COMMA_SEP);
		strBuff.append(burstbytpct);
		strBuff.append(COMMA_SEP);
		strBuff.append(burstEner);
		strBuff.append(COMMA_SEP);
		strBuff.append(burstEngpct);
		strBuff.append(COMMA_SEP);
		strBuff.append(dch);
		strBuff.append(COMMA_SEP);
		strBuff.append(pctdch);
		strBuff.append(COMMA_SEP);
		strBuff.append(burstjpkb);
		addBurstHeadersLine3(writer, strBuff.toString());
	}

	/**
	 * Adds RRC Energy headers line 3.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcEnergyHeadersLine3(FileWriter writer) throws IOException {
		for (int i = 1; i <= RRC_ENERGY_HEADERS_7; i++) {
			writer.append(COMMA_SEP);
			writer.append(energyJ);
			writer.append(COMMA_SEP);
			writer.append(rrcPCT);
		}
	}

	/**
	 * Adds RRC headers line 2.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcHeadersLine2a(FileWriter writer) throws IOException {
		writer.append(COMMA_SEP);
		writer.append(rrcIdle);
		writer.append(COMMA_SEP);
		writer.append(rrcIdle);
		writer.append(COMMA_SEP);
		writer.append(rrcCellDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcCellDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcCellFACH);
		writer.append(COMMA_SEP);
		writer.append(rrcCellFACH);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcFACHDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcFACHDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcFACHDCH);
	}

	/**
	 * Adds RRC Headers line 2.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcHeadersLine2b(FileWriter writer) throws IOException {
		writer.append(rrcIdle);
		writer.append(COMMA_SEP);
		writer.append(rrcIdle);
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.dch"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.dch"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.fach"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.fach"));
		writer.append(COMMA_SEP);
		writer.append(rrcIdleDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcFACHDCH);
		writer.append(COMMA_SEP);
		writer.append(rrcFACHDCH);
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("RRCState.TAIL_DCH"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("RRCState.TAIL_DCH"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("RRCState.TAIL_FACH"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("RRCState.TAIL_FACH"));
		writer.append(COMMA_SEP);
	}

	/**
	 * Adds RRC Headers line 3.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcHeadersLine3a(FileWriter writer) throws IOException {
		for (int i = 1; i <= RRC_HEADERS_4; i++) {
			writer.append(COMMA_SEP);
			writer.append(rrcSec);
			writer.append(COMMA_SEP);
			writer.append(rrcPCT);
		}
	}

	/**
	 * Adds RRC Headers line 3.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcHeadersLine3b(FileWriter writer) throws IOException {
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.hash"));
		writer.append(COMMA_SEP);
		writer.append(rrcSec);
		writer.append(COMMA_SEP);
		writer.append(rrcPCT);
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.hash"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.dchtail"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.fachtail"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.promoratio"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.totalE"));
	}
}