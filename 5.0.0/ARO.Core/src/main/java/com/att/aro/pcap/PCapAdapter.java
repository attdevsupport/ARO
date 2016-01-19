package com.att.aro.pcap;//.core.packetreader.impl;

import com.att.aro.core.packetreader.INativePacketSubscriber;
import com.att.aro.core.util.Util;

public class PCapAdapter {
	boolean hasloaded = false;
	INativePacketSubscriber subscriber = null;

	/**
	 * Checks that all necessary Pcap libraries are installed on the system
	 * 
	 * @throws UnsatisfiedLinkError
	 *             When a failure occurs.
	 */
	public native void ping();

	/**
	 * Reads all packets from the pcap files. packet data is delivered to pcapHandler
	 * 
	 * @param filename
	 *            pcap file name.
	 * @return result null if any error occurs while looping, else success
	 *         message.
	 */
	private native String loopPacket(String filename);

	public void setSubscriber(INativePacketSubscriber subscriber) {
		this.subscriber = subscriber;
	}

	/**
	 * jni - loopPacket(...) reads trace file,
	 * sends data packets to PacketListener:packetArrived
	 * 
	 * @param filename
	 * @return
	 */
	public String readData(String filename) {
		return this.loopPacket(filename);
	}

	/**
	 * Callback listener used by the native code that accesses pcap
	 * Packet data is forwarded to subscriber
	 * 
	 * @param datalink
	 * @param seconds
	 * @param microSeconds
	 * @param len
	 * @param data
	 */
	protected void pcapHandler(int datalink, long seconds, long microSeconds, int len, byte[] data) {
		if (this.subscriber != null) {
			this.subscriber.receive(datalink, seconds, microSeconds, len, data);
		}
	}

	/**
	 * Load ARO Jpcap DLL lib file.
	 */
	public void loadAroJpacapLib(String filename, String libname) throws UnsatisfiedLinkError {
		try {
			String libFolder = Util.makeLibFilesFromJar(filename);
			Util.loadLibrary(filename, libFolder);
		} catch (Exception e) {
			System.load(libname);
		}
	}

}
