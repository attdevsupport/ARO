/*
 Copyright [2012] [AT&T]
 
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
 
       http://www.apache.org/licenses/LICENSE-2.0
 
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package com.att.aro.pcap;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An adapter class that is used to access the Pcap libraries. This class should
 * be used to access Pcap data instead of accessing JPCap libraries directly.
 * The PcapAdapter class forms an abstraction so that different lower level Pcap
 * libraries can be accessed through one entry point.
 */
public class PCapAdapter {

	private static final Logger logger = Logger.getLogger(PCapAdapter.class
			.getName());

	private PacketListener pl;

	/**
	 * Checks that all necessary Pcap libraries are installed on the system
	 * 
	 * @throws UnsatisfiedLinkError
	 *             When a failure occurs.
	 */
	public static native void ping();

	/**
	 * Creates a new instance of the PCapAdapter class using the specified file,
	 * and a PacketLIstener object to create a callback for capturing packets.
	 * This constructor initiates the WinPcap native API to loop the packets.
	 * 
	 * @param file
	 *            The file object to access.
	 * @param pl
	 *            A Packetlistener object that enables a callback to capture the
	 *            packets.
	 * @throws java.io.IOException
	 */
	public PCapAdapter(File file, final PacketListener pl) throws IOException {
		logger.fine("Creating a new instance of the PCapAdapter");
		if (pl == null) {
			logger.severe("PacketListener cannot be null");
			throw new IllegalArgumentException("PacketListener cannot be null");
		}

		this.pl = pl;
		String result = loopPacket(file.getAbsolutePath());

		if (result != null) {
			logger.info("Result from executing all pcap packets: " +  result);
			throw new IOException(result);
		}
		logger.fine("Created PCapAdapter");
	}

	/**
	 * Callback listener used by the native code that accesses pcap
	 * 
	 * @param datalink
	 * @param seconds
	 * @param microSeconds
	 * @param len
	 * @param data
	 */
	private void pcapHandler(int datalink, long seconds, long microSeconds,
			int len, byte[] data) {
		try {
			pl.packetArrived(null, Packet.createPacketFromPcap(datalink, seconds, microSeconds,
					len, data));
		} catch (Throwable t) {

			// Log exceptions before they are returned to native code
			logger.log(Level.SEVERE, "Unexpected exception parsing packet", t);
		}
	}

	/**
	 * Executes all packets from the pcap files.
	 * 
	 * @param filename
	 *            pcap file name.
	 * @param handler
	 * @return result null if any error occurs while looping, else success
	 *         message.
	 */
	private native String loopPacket(String filename);

	static {
		String osname = System.getProperty("os.name");
		String os = System.getProperty("os.arch");
		logger.info("OS: " + osname);
		logger.info("OS Arch: " + os);
		// Loads the jpcap library as per the file system types.
		if (osname != null && osname.contains("Windows") && os != null
				&& os.contains("64")) {
			System.loadLibrary("jpcap64");
		} else {
			System.loadLibrary("jpcap");
		}
	}
}
