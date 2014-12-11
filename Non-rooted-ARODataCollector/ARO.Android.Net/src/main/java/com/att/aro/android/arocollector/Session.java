/*
 *  Copyright 2014 AT&T
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

package com.att.aro.android.arocollector;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import com.att.aro.android.arocollector.ip.IPv4Header;
import com.att.aro.android.arocollector.tcp.TCPHeader;
import com.att.aro.android.arocollector.udp.UDPHeader;

/**
 * store information about a socket connection from a VPN client. Each session is used by background worker
 * to server request from client.
 * @author Borey Sao
 * Date: May 19, 2014
 */
public class Session {
	//help synchronize receivingStream
	private Object syncReceive = new Object();
	
	//for synchronizing sendingStream
	private Object syncSend = new Object();
	//for increasing and decreasing sendAmountSinceLastAck
	private Object syncSendAmount = new Object();
	
	//for setting TCP/UDP header
	private Object syncLastHeader = new Object();
	
	private SocketChannel socketchannel = null;
	
	private DatagramChannel udpchannel = null;
	
	private int destAddress = 0;
	private int destPort = 0;
	
	private int sourceIp = 0;
	private int sourcePort = 0;
	
	//sequence received from client
	private int recSequence = 0;
	
	//track ack we sent to client and waiting for ack back from client
	private int sendUnack = 0;
	private boolean isacked = false;//last packet was acked yet?
	
	//the next ack to send to client
	private int sendNext = 0;
	private int sendWindow = 0; //window = windowsize x windowscale
	private int sendWindowSize = 0;
	private int sendWindowScale = 0;
	
	//track how many byte of data has been sent since last ACK from client
	private volatile int sendAmountSinceLastAck = 0;
	
	//sent by client during SYN inside tcp options
	private int maxSegmentSize = 0;
	
	//indicate that 3-way handshake has been completed or not
	private boolean isConnected = false;
	
	//receiving buffer for storing data from remote host
	private ByteArrayOutputStream receivingStream;
	
	//sending buffer for storing data from vpn client to be send to destination host
	private ByteArrayOutputStream sendingStream;
	
	private boolean hasReceivedLastSegment = false;
	
	//last packet received from client
	private IPv4Header lastIPheader = null;
	private TCPHeader lastTCPheader = null;
	private UDPHeader lastUDPheader = null;

	//true when connection is about to be close
	private boolean closingConnection = false;
	
	//indicate data from client is ready for sending to destination
	private boolean isDataForSendingReady = false;
	
	//store data for retransmission
	private byte[] unackData = null;
	
	//in ACK packet from client, if the previous packet was corrupted, client will send flag in options field
	private boolean packetCorrupted = false;
	
	//track how many time a packet has been retransmitted => avoid loop
	private int resendPacketCounter = 0;
	
	private int timestampSender = 0;
	private int timestampReplyto = 0;
	
	//indicate that vpn client has sent FIN flag and it has been acked
	private boolean ackedToFin = false;
	//timestamp when FIN as been acked, this is used to removed session after n minute
	private long ackedToFinTime = 0;
	
	//indicate that this session is currently being worked on by some SocketDataWorker already
	private volatile boolean isbusyread = false;
	private volatile boolean isbusywrite = false;
	
	//closing session and aborting connection, will be done by background task
	private volatile boolean abortingConnection = false;
	
	private SelectionKey selectionkey = null;
	
	public long connectionStartTime = 0;
	
	public Session(){
		receivingStream = new ByteArrayOutputStream();
		sendingStream = new ByteArrayOutputStream();
	}
	/**
	 * track how many byte sent to client since last ACK to avoid overloading
	 * @param amount
	 */
	public void trackAmountSentSinceLastAck(int amount){
		synchronized(syncSendAmount){
			sendAmountSinceLastAck += amount;
		}
	}
	/**
	 * decreate value of sendAmountSinceLastAck so that client's window is not full
	 * @param amount
	 */
	public void decreaseAmountSentSinceLastAck(int amount){
		synchronized(syncSendAmount){
			sendAmountSinceLastAck -= amount;
			if(sendAmountSinceLastAck <= 0){
				sendAmountSinceLastAck = 0;
			}
		}
	}
	/**
	 * determine if client's receiving window is full or not.
	 * @return
	 */
	public boolean isClientWindowFull(){
		boolean yes = false;
		if(sendWindow > 0 && sendAmountSinceLastAck >= sendWindow){
			yes = true;
		}else if(sendWindow == 0 && sendAmountSinceLastAck > 65535){
			yes = true;
		}
		return yes;
	}
	/**
	 * append more data
	 * @param data
	 * @return
	 */
	public boolean addReceivedData(byte[] data){
		boolean success = true;
		synchronized(syncReceive){
			try {
				receivingStream.write(data);
			} catch (IOException e) {
				success = false;
			}
		}
		return success;
	}
	public void resetReceivingData(){
		synchronized(syncReceive){
			receivingStream.reset();
		}
	}
	/**
	 * get all data received in the buffer and empty it.
	 * @return
	 */
	public byte[] getReceivedData(int maxSize){
		byte[] data = null;
		synchronized(syncReceive){
			data = receivingStream.toByteArray();
			receivingStream.reset();
			if(data.length > maxSize){
				byte[] small = new byte[maxSize];
				System.arraycopy(data, 0, small, 0, maxSize);
				int len = data.length - maxSize;
				receivingStream.write(data, maxSize, len);
				data = small;
			}
		}
		return data;
	}
	/**
	 * buffer has more data for vpn client
	 * @return
	 */
	public boolean hasReceivedData(){
		return receivingStream.size() > 0;
	}
	public int getReceivedDataSize(){
		int size = 0;
		synchronized(syncReceive){
			size = receivingStream.size();
		}
		return size;
	}
	/**
	 * set data to be sent to destination server
	 * @param data
	 * @return
	 */
	public boolean setSendingData(byte[] data){
		boolean success = true;
		synchronized(syncSend){
			try {
				sendingStream.write(data);
			} catch (IOException e) {
				success = false;
			}
		}
		return success;
	}
	public int getSendingDataSize(){
		return sendingStream.size();
	}
	/**
	 * dequeue data for sending to server
	 * @return
	 */
	public byte[] getSendingData(){
		byte[] data = null;
		synchronized(syncSend){
			data = sendingStream.toByteArray();
			sendingStream.reset();
		}
		return data;
	}
	/**
	 * buffer contains data for sending to destination server
	 * @return
	 */
	public boolean hasDataToSend(){
		return sendingStream.size() > 0;
	}

	public int getDestAddress() {
		return destAddress;
	}

	public void setDestAddress(int destAddress) {
		this.destAddress = destAddress;
	}

	public int getDestPort() {
		return destPort;
	}

	public void setDestPort(int destPort) {
		this.destPort = destPort;
	}

	public int getSendUnack() {
		return sendUnack;
	}

	public void setSendUnack(int sendUnack) {
		this.sendUnack = sendUnack;
	}

	public int getSendNext() {
		return sendNext;
	}

	public void setSendNext(int sendNext) {
		this.sendNext = sendNext;
	}

	public int getSendWindow() {
		return sendWindow;
	}

	public int getMaxSegmentSize() {
		return maxSegmentSize;
	}

	public void setMaxSegmentSize(int maxSegmentSize) {
		this.maxSegmentSize = maxSegmentSize;
	}

	public boolean isConnected() {
		return isConnected;
	}

	public void setConnected(boolean isConnected) {
		this.isConnected = isConnected;
	}

	public ByteArrayOutputStream getReceivingStream() {
		return receivingStream;
	}

	public ByteArrayOutputStream getSendingStream() {
		return sendingStream;
	}

	public int getSourceIp() {
		return sourceIp;
	}

	public void setSourceIp(int sourceIp) {
		this.sourceIp = sourceIp;
	}

	public int getSourcePort() {
		return sourcePort;
	}

	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}

	public int getSendWindowSize() {
		return sendWindowSize;
	}

	public void setSendWindowSizeAndScale(int sendWindowSize, int sendWindowScale) {
		this.sendWindowSize = sendWindowSize;
		this.sendWindowScale = sendWindowScale;
		this.sendWindow = sendWindowSize * sendWindowScale;
	}

	public int getSendWindowScale() {
		return sendWindowScale;
	}

	public boolean isAcked() {
		return isacked;
	}

	public void setAcked(boolean isacked) {
		this.isacked = isacked;
	}

	public int getRecSequence() {
		return recSequence;
	}

	public void setRecSequence(int recSequence) {
		this.recSequence = recSequence;
	}

	public SocketChannel getSocketchannel() {
		return socketchannel;
	}

	public void setSocketchannel(SocketChannel socketchannel) {
		this.socketchannel = socketchannel;
	}
	
	public DatagramChannel getUdpchannel() {
		return udpchannel;
	}
	public void setUdpchannel(DatagramChannel udpchannel) {
		this.udpchannel = udpchannel;
	}
	public boolean hasReceivedLastSegment() {
		return hasReceivedLastSegment;
	}
	public void setHasReceivedLastSegment(boolean hasReceivedLastSegment) {
		this.hasReceivedLastSegment = hasReceivedLastSegment;
	}
	public IPv4Header getLastIPheader() {
		IPv4Header header = null;
		synchronized(syncLastHeader){
			header = lastIPheader;
		}
		return header;
	}
	public void setLastIPheader(IPv4Header lastIPheader) {
		synchronized(syncLastHeader){
			this.lastIPheader = lastIPheader;
		}
	}
	public TCPHeader getLastTCPheader() {
		TCPHeader header = null;
		synchronized(syncLastHeader){
			header = lastTCPheader;
		}
		return header;
	}
	public void setLastTCPheader(TCPHeader lastTCPheader) {
		synchronized(syncLastHeader){
			this.lastTCPheader = lastTCPheader;
		}
	}
	
	public UDPHeader getLastUDPheader() {
		UDPHeader header = null;
		synchronized(syncLastHeader){
			header = lastUDPheader;
		}
		return header;
	}
	public void setLastUDPheader(UDPHeader lastUDPheader) {
		synchronized(syncLastHeader){
			this.lastUDPheader = lastUDPheader;
		}
	}
	public boolean isClosingConnection() {
		return closingConnection;
	}
	public void setClosingConnection(boolean closingConnection) {
		this.closingConnection = closingConnection;
	}
	public boolean isDataForSendingReady() {
		return isDataForSendingReady;
	}
	public void setDataForSendingReady(boolean isDataForSendingReady) {
		this.isDataForSendingReady = isDataForSendingReady;
	}
	public byte[] getUnackData() {
		return unackData;
	}
	public void setUnackData(byte[] unackData) {
		this.unackData = unackData;
	}
	
	public boolean isPacketCorrupted() {
		return packetCorrupted;
	}
	public void setPacketCorrupted(boolean packetCorrupted) {
		this.packetCorrupted = packetCorrupted;
	}
	public int getResendPacketCounter() {
		return resendPacketCounter;
	}
	public void setResendPacketCounter(int resendPacketCounter) {
		this.resendPacketCounter = resendPacketCounter;
	}
	public int getTimestampSender() {
		return timestampSender;
	}
	public void setTimestampSender(int timestampSender) {
		this.timestampSender = timestampSender;
	}
	public int getTimestampReplyto() {
		return timestampReplyto;
	}
	public void setTimestampReplyto(int timestampReplyto) {
		this.timestampReplyto = timestampReplyto;
	}
	public boolean isAckedToFin() {
		return ackedToFin;
	}
	public void setAckedToFin(boolean ackedToFin) {
		this.ackedToFin = ackedToFin;
	}
	public long getAckedToFinTime() {
		return ackedToFinTime;
	}
	public void setAckedToFinTime(long ackedToFinTime) {
		this.ackedToFinTime = ackedToFinTime;
	}
	
	public boolean isBusyread() {
		return isbusyread;
	}
	public void setBusyread(boolean isbusyread) {
		this.isbusyread = isbusyread;
	}
	public boolean isBusywrite() {
		return isbusywrite;
	}
	public void setBusywrite(boolean isbusywrite) {
		this.isbusywrite = isbusywrite;
	}
	public boolean isAbortingConnection() {
		return abortingConnection;
	}
	public void setAbortingConnection(boolean abortingConnection) {
		this.abortingConnection = abortingConnection;
	}
	public SelectionKey getSelectionkey() {
		return selectionkey;
	}
	public void setSelectionkey(SelectionKey selectionkey) {
		this.selectionkey = selectionkey;
	}
	
	
}
