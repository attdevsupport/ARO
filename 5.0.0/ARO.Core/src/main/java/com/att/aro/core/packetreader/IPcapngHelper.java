package com.att.aro.core.packetreader;

import java.io.File;
import java.io.FileNotFoundException;

public interface IPcapngHelper {
	/**
	 * check if a packet file is Pcap-ng
	 * @param file
	 * @return
	 * @throws FileNotFoundException 
	 */
	boolean isApplePcapng(File file) throws FileNotFoundException;
	boolean isApplePcapng(String filepath) throws FileNotFoundException;
	String getHardware();
	String getOs();
}
