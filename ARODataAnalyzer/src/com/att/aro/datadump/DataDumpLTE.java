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

public class DataDumpLTE extends DataDumpHelper {

	private static final int BEST_PRACTICES_HEADERS = 4;
	private static final int ENERGY_SIMULATION_HEADERS = 13;
	private static final int RRC_COMMAS = 6;
	private static final int RRC_ENERGY_HEADERS_LINE_2 = 5;
	private static final int RRC_HEADERS_LINE_2 = 5;
	private static final int RRC_STATE_MCH_HEADERS = 15;

	/**
	 * Adds LTE headers.
	 * 
	 * @param writer
	 * @param bpc
	 *            best practice display collection
	 */
	@Override
	public void addHeader(FileWriter writer, Collection<BestPracticeDisplay> bpc) throws IOException {

		// LINE 1:
		addContinuousHeader(writer, bestPractices, (bpc.size() * 2) - 1);
		addCommas(writer, BEST_PRACTICES_HEADERS);
		addContinuousHeader(writer, rrcStatMachSim, RRC_STATE_MCH_HEADERS);
		addContinuousHeader(writer, energySimulation, ENERGY_SIMULATION_HEADERS);
		addHeadersLine1(writer, bpc);

		// LINE 2:
		startNewLine(writer);

		addHeadersLine2a(writer, bpc);
		addRrcHeadersLine2a(writer);
		addCommas(writer, RRC_COMMAS);
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
	 * Adds burst headers line 3.
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
		strBuff.append(ltecr);
		strBuff.append(COMMA_SEP);
		strBuff.append(ltecrpct);
		strBuff.append(COMMA_SEP);
		strBuff.append(burstjpkb);
		addBurstHeadersLine3(writer, strBuff.toString());
	}
	
	/**
	 * Adds RRC Energy Headers line 3.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcEnergyHeadersLine3(FileWriter writer) throws IOException {
		for (int i = 0; i <= RRC_ENERGY_HEADERS_LINE_2; i++) {
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
		writer.append(rrcContRecep);
		writer.append(COMMA_SEP);
		writer.append(rrcContRecep);
		writer.append(COMMA_SEP);
		writer.append(rrcContReceptail);
		writer.append(COMMA_SEP);
		writer.append(rrcContReceptail);
		writer.append(COMMA_SEP);
		writer.append(rrcSDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcSDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcLDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcLDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleToCont);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleToCont);
	}

	/**
	 * Adds RRC Headers line 2.
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcHeadersLine2b(FileWriter writer) throws IOException {
		writer.append(rrcIdle);
		writer.append(COMMA_SEP);
		writer.append(rrcIdle);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleToCont);
		writer.append(COMMA_SEP);
		writer.append(rrcIdleToCont);
		writer.append(COMMA_SEP);
		writer.append(rrcContRecep);
		writer.append(COMMA_SEP);
		writer.append(rrcContRecep);
		writer.append(COMMA_SEP);
		writer.append(rrcContReceptail);
		writer.append(COMMA_SEP);
		writer.append(rrcContReceptail);
		writer.append(COMMA_SEP);
		writer.append(rrcSDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcSDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcLDRX);
		writer.append(COMMA_SEP);
		writer.append(rrcLDRX);
		writer.append(COMMA_SEP);
	}

	/**
	 * Adds RRC Headers line 3.
	 * 
	 * @param writer
	 * @throws IOException
	 */
	private void addRrcHeadersLine3a(FileWriter writer) throws IOException {
		for (int i = 0; i <= RRC_HEADERS_LINE_2; i++) {
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
		writer.append(Util.RB.getString("rrc.crTailRatio"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("rrc.longDRXRatio"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("rrc.shortDRXRatio"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.promoratio"));
		writer.append(COMMA_SEP);
		writer.append(Util.RB.getString("datadump.totalE"));
	}
}