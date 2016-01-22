/**
 * Copyright 2016 AT&T
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
package com.att.aro.core.packetreader.impl;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetreader.INativePacketSubscriber;
import com.att.aro.core.packetreader.IPacketListener;
import com.att.aro.core.packetreader.IPacketReader;
import com.att.aro.core.packetreader.IPacketService;
import com.att.aro.core.packetreader.pojo.Packet;
import com.att.aro.core.util.Util;
import com.att.aro.pcap.PCapAdapter;
import com.att.aro.pcap.packetrebuild.PCapFileWriter;

public class PacketReaderImpl implements IPacketReader, INativePacketSubscriber {

	@InjectLogger
	private static ILogger logger;

	@Autowired
	private IPacketService packetservice;

	@Autowired
	private IFileManager filemanager;

	private IPacketListener packetlistener;
	
	String aroJpcapLibName = null;
	String aroJpcapLibFileName = null;

	private String currentPacketfile = null;

	PCapAdapter adapter = null;

	/*
	 * converting pcap file support
	 */
	String convertedCapFile = "converted.cap";
	String backupCapFileName = "backup.cap";
	private File currentPcapfile = null;
	private File convertedPcapFile;
	private PCapFileWriter pcapOutput;

	public PacketReaderImpl() {

	}

	public void setAdapter(PCapAdapter adapter) {
		this.adapter = adapter;
	}

	@Override
	public void readPacket(String packetfile, IPacketListener listener) throws IOException {

		if (aroJpcapLibName == null) {
			setAroJpcapLibName();
		}
		
		currentPacketfile = packetfile;
		provisionalStartPcapConversion(packetfile);
		
		if (listener == null) {
			logger.error("PacketListener cannot be null");
			throw new IllegalArgumentException("PacketListener cannot be null");
		}

		this.packetlistener = listener;

		if (adapter == null) {
			adapter = new PCapAdapter();
			adapter.loadAroJpacapLib(aroJpcapLibFileName, aroJpcapLibName);
		}

		adapter.setSubscriber(this);
		
		// jni - loopPacket(...) reads trace file sends data packets to PacketListener:packetArrived
		String result = adapter.readData(packetfile);

		// finish 
		if (pcapOutput != null) {
			logger.info("close converted.cap and rename stuff");
			pcapOutput.close();
			pcapOutput = null;

			if (filemanager.renameFile(currentPcapfile, backupCapFileName)) {
				filemanager.renameFile(convertedPcapFile, currentPcapfile.getName());
			}
		}

		if (result != null) {
			logger.debug("Result from executing all pcap packets: " + result);
			throw new IOException(result);
		}
		logger.debug("Created PCapAdapter");
	}

	public void setAroJpcapLibName() {
		setAroJpcapLibName(Util.OS_NAME, Util.OS_ARCHYTECTURE);
	}

	/**
	 * Sets ARO Jpcap DLL library name.
	 */
	public void setAroJpcapLibName(String osname, String osarch) {

		logger.info("OS: " + osname);

		logger.info("OS Arch: " + osarch);

		if (osname != null && osarch != null) {

			if (osname.contains("Windows") && osarch.contains("64")) { // _______ 64 bit Windows jpcap64.DLL
				aroJpcapLibName = "jpcap64";
				aroJpcapLibFileName = aroJpcapLibName + ".dll";

			} else if (osname.contains("Windows")) { // _________________________ 32 bit Windows jpcap.DLL
				aroJpcapLibName = "jpcap";
				aroJpcapLibFileName = aroJpcapLibName + ".dll";

			} else if (osname.contains("Linux") && osarch.contains("amd64")) { // 64 bit Linux libjpcap64.so
				aroJpcapLibName = "jpcap64";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".so";

			} else if (osname.contains("Linux") && osarch.contains("i386")) { //  32 bit Linux libjpcap.so
				aroJpcapLibName = "jpcap32";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".so";

			} else { // _________________________________________________________ Mac OS X libjpcap.jnilib
				aroJpcapLibName = "jpcap";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".jnilib";
			}
		}
		logger.info("ARO Jpcap DLL lib file name: " + aroJpcapLibFileName);
	}

	/**
	 * Get name of ARO Jpcap DLL library file.
	 */
	public String getAroJpcapLibFileName() {
		return aroJpcapLibFileName;
	}

	@Override
	public void receive(int datalink, long seconds, long microSeconds, int len, byte[] data) {
		try {
			if (packetservice == null) {
				packetservice = new PacketServiceImpl();
			}
			Packet tempPacket = packetservice.createPacketFromPcap(datalink, seconds, microSeconds, len, data, currentPacketfile);
			packetlistener.packetArrived(null, tempPacket);
			if (pcapOutput != null) {
				int offset = tempPacket.getDatalinkHeaderSize();
				if (offset == 4) {
					int length = tempPacket.getData().length;
					pcapOutput.addPacketConvertedPcapng(tempPacket.getData(), offset, length, seconds * 1000000 + microSeconds);
				} else {
					pcapOutput.addPacket(tempPacket.getData(), seconds * 1000000 + microSeconds);
				}
			}
		} catch (Throwable t) {
			logger.error("Unexpected exception parsing packet", t);
		}
	}

	/**
	 * Potentially start the pcapng conversion process. Two conditions are
	 * tested, has conversion already been done and is the pcap file a pcapng.
	 * 
	 * @param file
	 */
	private void provisionalStartPcapConversion(String traceFile) {
		File file = new File(traceFile);
		String tracePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length());
		File backupCapFile = new File(tracePath, backupCapFileName);
		if (!backupCapFile.exists()) {
			try {
				PcapngHelperImpl pcapngHelper = new PcapngHelperImpl();
				if (pcapngHelper.isApplePcapng(file)) {
					currentPcapfile = new File(traceFile);
					convertedPcapFile = new File(tracePath, convertedCapFile);
					pcapOutput = new PCapFileWriter(convertedPcapFile);
				}
			} catch (Exception e) {
				logger.error("failed to create :" + convertedPcapFile);
				pcapOutput = null;
			}
		}
	}


	
}
