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

import com.att.aro.pcap.packetRebuild.PCapFileWriter;
import com.att.aro.util.Util;

/**
 * An adapter class that is used to access the Pcap libraries. This class should
 * be used to access Pcap data instead of accessing JPCap libraries directly.
 * The PcapAdapter class forms an abstraction so that different lower level Pcap
 * libraries can be accessed through one entry point.
 */
public class PCapAdapter {

	private static final Logger logger = Logger.getLogger(PCapAdapter.class.getName());
	
	static String aroJpcapLibName;
	static String aroJpcapLibFileName;

	private PacketListener pl;
	
	private File currentPcapfile = null;

	/*
	 * converting pcap file support
	 */
	String convertedCapFile = "converted.cap";
	String backupCapFileName = "backup.cap";
	private File convertedPcapFile;
	private PCapFileWriter pcapOutput;

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
		currentPcapfile = file;
		logger.fine("Creating a new instance of the PCapAdapter");

		provisionalStartPcapConversion(file);
		
		if (pl == null) {
			logger.severe("PacketListener cannot be null");
			throw new IllegalArgumentException("PacketListener cannot be null");
		}

		this.pl = pl;
		String result = loopPacket(file.getAbsolutePath());

		if (pcapOutput != null) {
			logger.info("close converted.cap and rename stuff");
			pcapOutput.close();
			pcapOutput = null;

			if (renameFile(currentPcapfile, backupCapFileName)) {
				renameFile(convertedPcapFile, currentPcapfile.getName());
			}
		}

		if (result != null) {
			logger.info("Result from executing all pcap packets: " +  result);
			throw new IOException(result);
		}
		logger.fine("Created PCapAdapter");
	}
	/**
	 * Potentially start the pcapng conversion process. Two conditions are tested, has conversion already been done and is the pcap file a pcapng.
	 * 
	 * @param file
	 */
	private void provisionalStartPcapConversion(File file) {
		String tracePath = file.getAbsolutePath().substring(0, file.getAbsolutePath().length() - file.getName().length());
		File backupCapFile = new File(tracePath, backupCapFileName);
		if (!backupCapFile.exists()) {
			try {
				if (Packet.getPcapng().isApplePcapng(file)) {
					convertedPcapFile = new File(tracePath, convertedCapFile);
					pcapOutput = new PCapFileWriter(convertedPcapFile);
				}
			} catch (Exception e) {
				logger.severe("failed to create :" + convertedPcapFile);
				pcapOutput = null;
			}
		}
	}
	
	/**
	 * rename a File with a newName
	 * 
	 * @param origFileName a File
	 * @param newName a String containing a new name
	 * @return
	 */
	private boolean renameFile(File origFileName, String newName){
		String path = origFileName.getAbsolutePath().substring(0, origFileName.getAbsolutePath().length() - origFileName.getName().length());
		File renameFile = new File(path, newName);
		if (!renameFile.exists()){
			if (origFileName.renameTo(renameFile)){
				return true;
			} 
		}
		return false;
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
	private void pcapHandler(int datalink, long seconds, long microSeconds, int len, byte[] data) {
		try {
			Packet packet = Packet.createPacketFromPcap(datalink, seconds, microSeconds, len, data, currentPcapfile);
			pl.packetArrived(null, packet);
			if (pcapOutput != null) {
				int offset = packet.getDatalinkHeaderSize();
				if (offset == 4) {
					int length = packet.getData().length;
					pcapOutput.addPacketConvertedPcapng(packet.getData(), offset, length, seconds * 1000000 + microSeconds);
				} else {
					pcapOutput.addPacket(packet.getData(), seconds * 1000000 + microSeconds);
				}
			}
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

	/**
	 * Load ARO Jpcap DLL lib file.
	 */
	public static void loadAroJpacapLib() throws UnsatisfiedLinkError {
		setAroJpcapLibName();
		System.loadLibrary(aroJpcapLibName);
	}
	
	/**
	 * Load ARO Jpcap DLL lib file from the specified path.
	 * 
	 * @param path
	 * 		Library path.
	 */
	public static void loadAroJpacapLib(String path) throws UnsatisfiedLinkError {
		setAroJpcapLibName();
		File file = new File(path, aroJpcapLibFileName);
		path = file.getAbsolutePath();
		System.load(path);
	}
	
	/**
	 * Sets ARO Jpcap DLL library name.
	 */
	static void setAroJpcapLibName() {
		String osname = Util.OS_NAME;
		String os = Util.OS_ARCHYTECTURE;
		logger.fine("OS: " + osname);
		logger.fine("OS Arch: " + os);
		
		if (osname != null && os != null) {
			
			if (osname.contains("Windows") && os.contains("64")) {
				aroJpcapLibName = "jpcap64";
				aroJpcapLibFileName = aroJpcapLibName + ".DLL";  
			} else if (osname.contains("Windows")){  // 32 bit Windows
				aroJpcapLibName = "jpcap";
				aroJpcapLibFileName = aroJpcapLibName + ".DLL";  
			} else { // Mac OS X
				aroJpcapLibName = "jpcap";
				aroJpcapLibFileName = "lib" + aroJpcapLibName + ".jnilib";  
			}
		}
		
		logger.fine("ARO Jpcap DLL lib file name: " + aroJpcapLibFileName);
	}
	
	/**
	 * Get name of ARO Jpcap DLL library file.
	 */
	public static String getAroJpcapLibFileName() {
		return aroJpcapLibFileName;
	}
}
