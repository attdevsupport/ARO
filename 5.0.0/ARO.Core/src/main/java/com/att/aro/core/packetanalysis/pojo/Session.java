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
package com.att.aro.core.packetanalysis.pojo;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

/**
 * Session contains all of the packets from the session. 
 * The purpose is for analysis and modeling of session data.
 */

public class Session implements Serializable, Comparable<Session> {

	private static final long serialVersionUID = 1L;

	public static final int COMPRESS_DEFLATE = 1;
	public static final int COMPRESS_NONE = 0;
	/** plain text, no https */
	public static final int HTTPS_MODE_NONE = 0;
	/** https, no compression */
	public static final int HTTPS_MODE_NORMAL = 1;
	/** https, with deflate compression */
	public static final int HTTPS_MODE_DEFLATE = 2;
	public static final int ALERT_LEVEL_WARNING = 1;
	public static final int ALERT_LEVEL_FATAL = 2;
	public static final int ALERT_CLOSE_NOTIFY = 0;

	/**
	 * The remote IP address.
	 */
	private InetAddress remoteIP;

	/**
	 * The remote host domain name.
	 */
	private String remoteHostName;

	/**
	 * A dns Request Packet
	 */
	private PacketInfo dnsRequestPacket;

	/**
	 * A dns Response Packet
	 */
	private PacketInfo dnsResponsePacket;

	/**
	 * The last SSL handshake packet
	 */
	private PacketInfo lastSslHandshakePacket;

	/**
	 * Domain name is the initial host name requested that initiated a TCP
	 * session. This value is either the host name specified by the first HTTP
	 * request in the session or the referrer domain that caused this session to
	 * be opened
	 */
	private String domainName;

	/**
	 * A count of the number of files downloaded during the TCP session.
	 */
	private int fileDownloadCount;

	/**
	 * The number of bytes transferred during the session.
	 */
	private long bytesTransferred;

	/**
	 * The remote port.
	 */
	private int remotePort;

	/**
	 * The local port.
	 */
	private int localPort;

	/**
	 * Indicates whether SSL packets were detected in this session
	 */
	private boolean ssl;

	/**
	 * Indicates whether the session is UDP only or not.<br>
	 * true if session is UDP otherwise false
	 */
	private boolean udpOnly = false;

	/**
	 * A List of PacketInfo objects containing all packets in the session
	 */
	private List<PacketInfo> packets = new ArrayList<PacketInfo>();

	/**
	 * A List of PacketInfo objects containing the packet data.
	 */
	private List<PacketInfo> udpPackets = new ArrayList<PacketInfo>();

	/**
	 * A Set of strings containing the application names.
	 */
	private Set<String> appNames = new HashSet<String>(1);

	/**
	 * A TCPSession.Termination object containing the information<br>
	 * Object is null for no session termination in the trace.
	 */
	private Termination sessionTermination;

	/**
	 * A List of HTTPRequestResponseInfo objects containing the information.
	 */
	private List<HttpRequestResponseInfo> requestResponseInfo = new ArrayList<HttpRequestResponseInfo>();

	/**
	 * An array of bytes containing the uplink storage.
	 */
	private byte[] storageUl;

	/**
	 * A Map of offsets and corresponding PacketInfo objects that contain the
	 * uplink packet data.
	 */
	private SortedMap<Integer, PacketInfo> packetOffsetsUl;

	/**
	 * An array of bytes containing the downlink storage.
	 */
	private byte[] storageDl;

	/**
	 * A Map of offsets and corresponding PacketInfo objects that contain the
	 * downlink packet data.
	 */
	private SortedMap<Integer, PacketInfo> packetOffsetsDl;

	/**
	 * An ArrayList of ints, the packet index Note: stored in conjunction with
	 * session.getPackets().add(packet), but not used.
	 */
	private List<Integer> pktIndex = new ArrayList<Integer>();

	/**
	 * A Reassembler object, used to reassemble a session
	 */
	private Reassembler pStorageBothRAW = new Reassembler();

	/**
	 * A List of packet upload ranges
	 */
	private List<PacketRangeInStorage> pktRangesUl;

	/**
	 * A List of packet download ranges
	 */
	private List<PacketRangeInStorage> pktRangesDl;

	/**
	 * app-layer-protocol<br>
	 * has been refactored out of this class
	 */
	private int protocol; //app-layer-protocol

	/**
	 * HttpsMode<br>
	 * has been refactored out of this class
	 */
	private int httpsMode = HTTPS_MODE_NONE;

	/**
	 * An array of bytes containing the extended uplink storage.
	 */
	private byte[] storageUlext = null;

	/**
	 * An array of bytes containing the extended downlink storage.
	 */
	private byte[] storageDlext = null;

	/**
	 * A ByteArrayOutputStream<br>
	 * May be replaced by storageUl (Already defined above) after testing.<br>
	 * unused, has been refactored out of this class
	 */
	private ByteArrayOutputStream pStorageULDCPT = new ByteArrayOutputStream(); //May be replaced by storageUl (Already defined above) after testing.

	/**
	 * A ByteArrayOutputStream<br>
	 * May be replaced by storageDl (Already defined above) after testing.<br>
	 * unused, has been refactored out of this class
	 */
	private ByteArrayOutputStream pStorageDLDCPT = new ByteArrayOutputStream(); //May be replaced by storageDl (Already defined above) after testing.

	/**
	 * A ByteArrayOutputStream<br>
	 * unused, has been refactored out of this class
	 */
	private ByteArrayOutputStream pStorageBothDCPT = new ByteArrayOutputStream();

	/**
	 * Initializes an instance of the TCPSession class, using the specified
	 * remote IP, remote port, and local port.
	 * 
	 * @param remoteIP
	 *            The remote IP address.
	 * 
	 * @param remotePort
	 *            The remote port.
	 * 
	 * @param localPort
	 *            The local port.
	 */
	public Session(InetAddress remoteIP, int remotePort, int localPort) {
		this.remoteIP = remoteIP;
		this.remotePort = remotePort;
		this.localPort = localPort;
	}

	/**
	 * Compares Session start times
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Session session) {
		return Double.valueOf(getSessionStartTime()).compareTo(Double.valueOf(session.getSessionStartTime()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Session) {
			Session oTcp = (Session) obj;
			return Double.valueOf(getSessionStartTime()) == oTcp.getSessionStartTime();
		} else {
			return false;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return (int) Double.doubleToLongBits(getSessionStartTime());
	}

	/**
	 * Returns the set of application names contained in the TCP session.
	 * 
	 * @return A Set of strings containing the application names.
	 */
	public Set<String> getAppNames() {
		return appNames;
	}

	/**
	 * Returns the remote IP address.
	 * 
	 * @return The remote IP.
	 */
	public InetAddress getRemoteIP() {
		return remoteIP;
	}

	/**
	 * Returns the remote port.
	 * 
	 * @return The remote port.
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * Returns the local port.
	 * 
	 * @return The local port.
	 */
	public int getLocalPort() {
		return localPort;
	}

	/**
	 * Indicates whether SSL packets were detected in this session
	 * 
	 * @return the ssl
	 */
	public boolean isSsl() {
		return ssl;
	}

	/**
	 * Returns the name of the remote host.
	 * 
	 * @return The remote host name.
	 */
	public String getRemoteHostName() {
		return remoteHostName;
	}

	/**
	 * Returns The dns Request Packet
	 * 
	 * @return the dnsRequestPacket
	 */
	public PacketInfo getDnsRequestPacket() {
		return dnsRequestPacket;
	}

	/**
	 * Returns The dns Response Packet
	 * 
	 * @return the dnsResponsePacket
	 */
	public PacketInfo getDnsResponsePacket() {
		return dnsResponsePacket;
	}

	/**
	 * Returns the last SSL handshake packet
	 * 
	 * @return the lastSslHandshakePacket
	 */
	public PacketInfo getLastSslHandshakePacket() {
		return lastSslHandshakePacket;
	}

	/**
	 * Returns the name of the TCP domain.
	 * 
	 * @return The TCP domain name.
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * Returns a count of the number of files downloaded during the TCP session.
	 * 
	 * @return The file download count.
	 */
	public int getFileDownloadCount() {
		return fileDownloadCount;
	}

	/**
	 * Returns the number of bytes transferred during the session.
	 * 
	 * @return The total number of bytes transferred.
	 */
	public long getBytesTransferred() {
		return bytesTransferred;
	}

	/**
	 * Returns all of the packets in the TCP session.
	 * 
	 * @return A List of PacketInfo objects containing the packet data.
	 */
	public List<PacketInfo> getPackets() {
		return packets;
	}

	/**
	 * Returns all the UDP packets
	 * 
	 * @return A List of PacketInfo objects containing the packet data.
	 */
	public List<PacketInfo> getUDPPackets() {
		return udpPackets;
	}

	/**
	 * Returns true if session is UDP otherwise false
	 * 
	 * @return true if session is UDP otherwise false
	 */
	public boolean isUDP() {
		return udpOnly;
	}

	/**
	 * Gets the start time of the session, in seconds, relative to the start of
	 * the trace.
	 * 
	 * @return The start time of the session.
	 */
	public double getSessionStartTime() {
		if (packets != null && !packets.isEmpty()) {
			return packets.get(0).getTimeStamp();
		}

		if (udpPackets != null && !udpPackets.isEmpty()) {
			return udpPackets.get(0).getTimeStamp();
		}

		return 0.0;
	}

	/**
	 * Gets the end time of the session, in seconds, relative to the start of
	 * the trace.
	 * 
	 * @return The end time of the session.
	 */
	public double getSessionEndTime() {
		return packets.get(packets.size() - 1).getTimeStamp();
	}

	/**
	 * Gets the start time of the UDP session, in seconds, relative to the start
	 * of the trace.
	 * 
	 * @return The start time of the UDP session.
	 */
	public double getUDPSessionStartTime() {
		return udpPackets.get(0).getTimeStamp();
	}

	/**
	 * Gets the end time of the UDP session, in seconds, relative to the start
	 * of the trace.
	 * 
	 * @return The end time of the UDP session.
	 */
	public double getUDPSessionEndTime() {
		return udpPackets.get(udpPackets.size() - 1).getTimeStamp();
	}

	/**
	 * Return the request/response information for all of the packets.
	 * 
	 * @return A List of HTTPRequestResponseInfo objects containing the
	 *         information.
	 */
	public List<HttpRequestResponseInfo> getRequestResponseInfo() {
		return requestResponseInfo;
	}

	/**
	 * Returns the consolidated string for uplink and downlink storage.
	 * 
	 * @return The result string.
	 */
	public String getDataText() {
		//trim the buffer size, most of the contents are not available for use string presented
		//1000 according to the average packet size 1500
		StringBuffer buf = new StringBuffer(storageUl.length + storageDl.length);
		byte[] temp = Arrays.copyOf(storageUl, 1000);
		buf.append(new String(temp));
		temp = Arrays.copyOf(storageDl, 1000);
		buf.append(new String(temp)+"...");
		
//		buf.append(new String(storageUl));
//		buf.append(new String(storageDl));
//		String result = buf.toString();
		return buf.toString();
	}

	/**
	 * Return the uplink storage.
	 * 
	 * @return An array of bytes containing the uplink storage.
	 */
	public byte[] getStorageUl() {
		return storageUl;
	}

	/**
	 * Return the extended uplink storage.
	 * 
	 * @return An array of bytes containing the extended uplink storage.
	 */
	public byte[] getStorageUlEx() {
		return storageUlext;
	}

	/**
	 * Return the extended downlink storage.
	 * 
	 * @return An array of bytes containing the extended downlink storage.
	 */
	public byte[] getStorageDlEx() {
		return storageDlext;
	}

	/**
	 * Return the downlink storage.
	 * 
	 * @return An array of bytes containing the downlink storage.
	 */
	public byte[] getStorageDl() {
		return storageDl;
	}

	/**
	 * Returns a sorted Map of offsets and packet data for each uplink packet in
	 * the storage array.
	 * 
	 * @return A Map of offsets and corresponding PacketInfo objects that
	 *         contain the uplink packet data.
	 */
	public SortedMap<Integer, PacketInfo> getPacketOffsetsUl() {
		return packetOffsetsUl;
	}

	/**
	 * Returns a sorted Map of offsets and packet data for each downlink packet
	 * in the storage array.
	 * 
	 * @return A Map of offsets and corresponding PacketInfo objects that
	 *         contain the downlink packet data.
	 */
	public SortedMap<Integer, PacketInfo> getPacketOffsetsDl() {
		return packetOffsetsDl;
	}

	/**
	 * Returns information about the session termination if one exists in the
	 * trace.
	 * 
	 * @return A TCPSession.Termination object containing the information, or
	 *         null, if there was no session termination in the trace.
	 */
	public Termination getSessionTermination() {
		return sessionTermination;
	}

	/**
	 * Set A dns Request Packet
	 * 
	 * @param dnsRequestPacket - A dns Request Packet
	 */
	public void setDnsRequestPacket(PacketInfo dnsRequestPacket) {
		this.dnsRequestPacket = dnsRequestPacket;
	}

	/**
	 * A dns Response Packet
	 * 
	 * @param dnsResponsePacket - A dns Response Packet
	 */
	public void setDnsResponsePacket(PacketInfo dnsResponsePacket) {
		this.dnsResponsePacket = dnsResponsePacket;
	}

	/**
	 * Returns true if session is UDP otherwise false
	 * 
	 * @return true if session is UDP otherwise false
	 */
	public boolean isUdpOnly() {
		return udpOnly;
	}

	/**
	 * Set true to indicate session is UDP only
	 * 
	 * @param udpOnly
	 *            - true to indicate session is UDP only
	 */
	public void setUdpOnly(boolean udpOnly) {
		this.udpOnly = udpOnly;
	}

	/**
	 * Set a List of PacketInfo objects containing the packet data.
	 * 
	 * @param udpPackets
	 *            - List of PacketInfo objects containing the packet data.
	 */
	public void setUdpPackets(List<PacketInfo> udpPackets) {
		this.udpPackets = udpPackets;
	}

	/**
	 * Returns the packet index
	 * 
	 * @return the packet index
	 */
	public List<Integer> getPktIndex() {
		return pktIndex;
	}

	/**
	 * Sets the packet index
	 * 
	 * @param pktIndex - the packet index
	 */
	public void setPktIndex(List<Integer> pktIndex) {
		this.pktIndex = pktIndex;
	}

	/**
	 * Returns a Reassembler object
	 * 
	 * @return - A Reassembler object, used to reassemble a session
	 */
	public Reassembler getpStorageBothRAW() {
		return pStorageBothRAW;
	}

	/**
	 * 
	 * Used in Reassembler<br>
	 * has been refactored out of this class
	 * 
	 * @param pStorageBothRAW - A Reassembler object, used to reassemble a session
	 */
	public void setpStorageBothRAW(Reassembler pStorageBothRAW) {
		this.pStorageBothRAW = pStorageBothRAW;
	}

	/**
	 * 
	 * @return A List of packet upload ranges
	 */
	public List<PacketRangeInStorage> getPktRangesUl() {
		return pktRangesUl;
	}

	public void setPktRangesUl(List<PacketRangeInStorage> pktRangesUl) {
		this.pktRangesUl = pktRangesUl;
	}

	/**
	 * @return A List of packet download ranges
	 */
	public List<PacketRangeInStorage> getPktRangesDl() {
		return pktRangesDl;
	}

	/**
	 * Sets A List of packet download ranges
	 * 
	 * @param pktRangesDl - A List of packet download ranges
	 */
	public void setPktRangesDl(List<PacketRangeInStorage> pktRangesDl) {
		this.pktRangesDl = pktRangesDl;
	}

	/**
	 * app-layer-protocol<br>
	 * has been refactored out of this class
	 * 
	 * @return protocol - app-layer-protocol
	 */
	public int getProtocol() {
		return protocol;
	}

	/**
	 * app-layer-protocol<br>
	 * has been refactored out of this class
	 * 
	 * @param protocol - app-layer-protocol
	 */
	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	/**
	 * httpsMode<br>
	 * has been refactored out of this class
	 * 
	 * @return httpsMode - HttpsMode
	 */
	public int getHttpsMode() {
		return httpsMode;
	}

	/**
	 * httpsMode<br>
	 * has been refactored out of this class
	 * 
	 * @param httpsMode - HttpsMode
	 */
	public void setHttpsMode(int httpsMode) {
		this.httpsMode = httpsMode;
	}

	/**
	 * Return the extended uplink storage.
	 * 
	 * @return An array of bytes containing the extended uplink storage.
	 */
	public byte[] getStorageUlext() {
		return storageUlext;
	}

	/**
	 * Set An array of bytes containing the extended uplink storage.
	 * 
	 * @param storageUlext
	 *            - An array of bytes containing the extended uplink storage.
	 */
	public void setStorageUlext(byte[] storageUlext) {
		this.storageUlext = storageUlext;
	}

	/**
	 * Return the extended downlink storage.
	 * 
	 * @return An array of bytes containing the extended downlink storage.
	 */
	public byte[] getStorageDlext() {
		return storageDlext;
	}

	/**
	 * Set An array of bytes containing the extended downlink storage.
	 * 
	 * @param storageDlext
	 *            - An array of bytes containing the extended downlink storage.
	 */
	public void setStorageDlext(byte[] storageDlext) {
		this.storageDlext = storageDlext;
	}

	/**
	 * A ByteArrayOutputStream<br>
	 * May be replaced by storageUl (Already defined above) after testing.<br>
	 * unused, has been refactored out of this class
	 * 
	 * @return A ByteArrayOutputStream
	 */
	public ByteArrayOutputStream getpStorageULDCPT() {
		return pStorageULDCPT;
	}

	/**
	 * A ByteArrayOutputStream<br>
	 * May be replaced by storageUl (Already defined above) after testing.<br>
	 * unused, has been refactored out of this class
	 * 
	 * @param pStorageULDCPT
	 *            - A ByteArrayOutputStream
	 */
	public void setpStorageULDCPT(ByteArrayOutputStream pStorageULDCPT) {
		this.pStorageULDCPT = pStorageULDCPT;
	}

	/**
	 * A ByteArrayOutputStream<br>
	 * May be replaced by storageDl (Already defined above) after testing.<br>
	 * unused, has been refactored out of this class
	 * 
	 * @return A ByteArrayOutputStream
	 */
	public ByteArrayOutputStream getpStorageDLDCPT() {
		return pStorageDLDCPT;
	}

	/**
	 * A ByteArrayOutputStream<br>
	 * May be replaced by storageDl (Already defined above) after testing.<br>
	 * unused, has been refactored out of this class
	 * 
	 * @param pStorageDLDCPT - A ByteArrayOutputStream
	 */
	public void setpStorageDLDCPT(ByteArrayOutputStream pStorageDLDCPT) {
		this.pStorageDLDCPT = pStorageDLDCPT;
	}

	/**
	 * A ByteArrayOutputStream<br>
	 * unused, has been refactored out of this class
	 * 
	 * @return A ByteArrayOutputStream
	 */
	public ByteArrayOutputStream getpStorageBothDCPT() {
		return pStorageBothDCPT;
	}

	/**
	 * A ByteArrayOutputStream<br>
	 * unused, has been refactored out of this class
	 * 
	 * @param pStorageBothDCPT
	 *            - A ByteArrayOutputStream
	 */
	public void setpStorageBothDCPT(ByteArrayOutputStream pStorageBothDCPT) {
		this.pStorageBothDCPT = pStorageBothDCPT;
	}

	/**
	 * Sets The remote IP address.
	 * 
	 * @param remoteIP
	 *            - The remote IP address.
	 */
	public void setRemoteIP(InetAddress remoteIP) {
		this.remoteIP = remoteIP;
	}

	/**
	 * The remote host domain name.
	 * 
	 * @param remoteHostName
	 *            - The remote host domain name.
	 */
	public void setRemoteHostName(String remoteHostName) {
		this.remoteHostName = remoteHostName;
	}

	/**
	 * The last SSL handshake packet
	 * 
	 * @param lastSslHandshakePacket
	 *            - The last SSL handshake packet
	 */
	public void setLastSslHandshakePacket(PacketInfo lastSslHandshakePacket) {
		this.lastSslHandshakePacket = lastSslHandshakePacket;
	}

	/**
	 * The domain name
	 * 
	 * @param domainName
	 *            - The domain name
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * Sets A count of the number of files downloaded during the session.
	 * 
	 * @param fileDownloadCount
	 *            - number of files downloaded during the session.
	 */
	public void setFileDownloadCount(int fileDownloadCount) {
		this.fileDownloadCount = fileDownloadCount;
	}

	/**
	 * The number of bytes transferred during the session.
	 * 
	 * @param bytesTransferred
	 *            - number of bytes transferred during the session.
	 */
	public void setBytesTransferred(long bytesTransferred) {
		this.bytesTransferred = bytesTransferred;
	}

	/**
	 * The remote port.
	 * 
	 * @param remotePort
	 *            - The remote port.
	 */
	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	/**
	 * The local port.
	 * 
	 * @param localPort
	 *            - The local port.
	 */
	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	/**
	 * Sets boolean to indicates whether SSL packets were detected in this
	 * session
	 * 
	 * @param ssl
	 *            true if ssl packets detected, false if not
	 */
	public void setSsl(boolean ssl) {
		this.ssl = ssl;
	}

	/**
	 * Sets the List of PacketInfo objects containing all packets in the session
	 * 
	 * @param packets
	 *            - List of PacketInfo objects containing all packets in the
	 *            session
	 */
	public void setPackets(List<PacketInfo> packets) {
		this.packets = packets;
	}

	/**
	 * Sets a Set of strings containing the application names.
	 * 
	 * @param appNames
	 *            - A Set of strings containing the application names.
	 */
	public void setAppNames(Set<String> appNames) {
		this.appNames = appNames;
	}

	/**
	 * Returns information about the session termination if one exists in the
	 * trace.
	 * 
	 * @param sessionTermination
	 *            A TCPSession.Termination object containing the information, or
	 *            null, if there was no session termination in the trace.
	 */
	public void setSessionTermination(Termination sessionTermination) {
		this.sessionTermination = sessionTermination;
	}

	/**
	 * A List of HTTPRequestResponseInfo objects containing the information.
	 * 
	 * @param requestResponseInfo
	 *            - List of HTTPRequestResponseInfo objects containing the
	 *            information.
	 */
	public void setRequestResponseInfo(List<HttpRequestResponseInfo> requestResponseInfo) {
		this.requestResponseInfo = requestResponseInfo;
	}

	/**
	 * An array of bytes containing the uplink storage.
	 * 
	 * @param storageUl - An array of bytes containing the uplink storage.
	 */
	public void setStorageUl(byte[] storageUl) {
		this.storageUl = storageUl;
	}

	/**
	 * A Map of offsets and corresponding PacketInfo objects that contain the
	 * uplink packet data.
	 * 
	 * @param packetOffsetsUl - A Map of offsets and corresponding PacketInfo objects that contain the uplink packet data.
	 */
	public void setPacketOffsetsUl(SortedMap<Integer, PacketInfo> packetOffsetsUl) {
		this.packetOffsetsUl = packetOffsetsUl;
	}

	/**
	 * An array of bytes containing the downlink storage.
	 * 
	 * @param storageDl - An array of bytes containing the downlink storage.
	 */
	public void setStorageDl(byte[] storageDl) {
		this.storageDl = storageDl;
	}

	/**
	 * A Map of offsets and corresponding PacketInfo objects that contain the
	 * downlink packet data.
	 * 
	 * @param packetOffsetsDl
	 *            - Map of offsets and corresponding PacketInfo objects
	 */
	public void setPacketOffsetsDl(SortedMap<Integer, PacketInfo> packetOffsetsDl) {
		this.packetOffsetsDl = packetOffsetsDl;
	}

}//end class
