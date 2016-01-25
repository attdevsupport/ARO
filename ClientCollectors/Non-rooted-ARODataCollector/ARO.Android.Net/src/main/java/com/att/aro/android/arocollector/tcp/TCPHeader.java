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
package com.att.aro.android.arocollector.tcp;
/**
 * data structure for TCP Header
 * @author Borey Sao
 * Date: May 8, 2014
 *
 */
public class TCPHeader {
	private int sourcePort;
	private int destinationPort;
	private int sequenceNumber;
	private int dataOffset;
	private int tcpFlags;
	private boolean isns = false;
	private boolean iscwr = false;
	private boolean isece = false;
	private boolean issyn = false;
	private boolean isack = false;
	private boolean isfin = false;
	private boolean isrst = false;
	private boolean ispsh = false;//end of letter
	private boolean isurg = false;
	private int windowSize;
	private int checksum;
	private int urgentPointer;
	private byte[] options;
	private int ackNumber;
	//vars below need to be set via setters when copy
	private int maxSegmentSize = 0;
	private int windowScale = 0;
	private boolean isSelectiveAckPermitted = false;
	private int timeStampSender = 0;
	private int timeStampReplyTo = 0;
	public TCPHeader(int sourcePort,int destinationPort,int sequenceNumber,
						int dataOffset, boolean isns,int tcpFlags,int windowSize,int checksum,
							int urgentPointer,byte[] options,int ackNumber){
		this.sourcePort = sourcePort;
		this.destinationPort = destinationPort;
		this.sequenceNumber = sequenceNumber;
		this.dataOffset = dataOffset;
		this.isns = isns;
		this.tcpFlags = tcpFlags;
		this.windowSize = windowSize;
		this.checksum = checksum;
		this.urgentPointer = urgentPointer;
		this.options = options;
		this.ackNumber = ackNumber;
		setFlagBits();
	}
	void setFlagBits(){
		  isack = (this.tcpFlags & 0x10) > 0;
		  
		  isfin = (this.tcpFlags & 0x01) > 0;
		  //End Of Letter
		  ispsh = (this.tcpFlags & 0x08) > 0;
		  
		  isrst = (this.tcpFlags & 0x04) > 0;
		  
		  issyn = (this.tcpFlags & 0x02) > 0;
		  
		  isurg = (this.tcpFlags & 0x20) > 0;
		  
		  iscwr = (this.tcpFlags & 0x80) > 0;
		  
		  isece = (this.tcpFlags & 0x40) > 0;
	  }
	public boolean isNS(){
		return isns;
	}
	public void setIsNS(boolean isns){
		this.isns = isns;
	}
	public boolean isCWR(){
		return iscwr;
	}
	public void setIsCWR(boolean iscwr){
		this.iscwr = iscwr;
		if(iscwr){
			this.tcpFlags |= 0x80;
		}else{
			this.tcpFlags &= 0x7F;
		}
	}
	public boolean isECE(){
		return isece;
	}
	public void setIsECE(boolean isece){
		this.isece = isece;
		if(isece){
			this.tcpFlags |= 0x40;
		}else{
			this.tcpFlags &= 0xBF;
		}
	}
	public boolean isSYN(){
		return issyn;
	}
	public void setIsSYN(boolean issyn){
		this.issyn = issyn;
		if(issyn){
			this.tcpFlags |= 0x02;
		}else{
			this.tcpFlags &= 0xFD;
		}
	}
	public boolean isACK(){
		return isack;
	}
	public void setIsACK(boolean isack){
		this.isack = isack;
		if(isack){
			this.tcpFlags |= 0x10;
		}else{
			this.tcpFlags &= 0xEF;
		}
	}
	public boolean isFIN(){
		return isfin;
	}
	public void setIsFIN(boolean isfin){
		this.isfin = isfin;
		if(isfin){
			this.tcpFlags |= 0x1;
		}else{
			this.tcpFlags &= 0xFE;
		}
	}
	public boolean isRST(){
		return isrst;
	}
	public void setIsRST(boolean isrst){
		this.isrst = isrst;
		if(isrst){
			this.tcpFlags |= 0x04;
		}else{
			this.tcpFlags &= 0xFB;
		}
	}
	public boolean isPSH(){
		return ispsh;
	}
	public void setIsPSH(boolean ispsh){
		this.ispsh = ispsh;
		if(ispsh){
			this.tcpFlags |= 0x08;
		}else{
			this.tcpFlags &= 0xF7;
		}
	}
	public boolean isURG(){
		return isurg;
	}
	public void setIsURG(boolean isurg){
		this.isurg = isurg;
		if(isurg){
			this.tcpFlags |= 0x20;
		}else{
			this.tcpFlags &= 0xDF;
		}
	}
	public int getSourcePort() {
		return sourcePort;
	}
	public void setSourcePort(int sourcePort) {
		this.sourcePort = sourcePort;
	}
	public int getDestinationPort() {
		return destinationPort;
	}
	public void setDestinationPort(int destinationPort) {
		this.destinationPort = destinationPort;
	}
	public int getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public int getDataOffset() {
		return dataOffset;
	}
	public void setDataOffset(int dataOffset) {
		this.dataOffset = dataOffset;
	}
	public int getTcpFlags() {
		return tcpFlags;
	}
	public void setTcpFlags(int tcpFlags) {
		this.tcpFlags = tcpFlags;
	}
	public int getWindowSize() {
		return windowSize;
	}
	public void setWindowSize(int windowSize) {
		this.windowSize = windowSize;
	}
	public int getChecksum() {
		return checksum;
	}
	public void setChecksum(int checksum) {
		this.checksum = checksum;
	}
	public int getUrgentPointer() {
		return urgentPointer;
	}
	public void setUrgentPointer(int urgentPointer) {
		this.urgentPointer = urgentPointer;
	}
	public byte[] getOptions() {
		return options;
	}
	public void setOptions(byte[] options) {
		this.options = options;
	}
	public int getAckNumber() {
		return ackNumber;
	}
	public void setAckNumber(int ackNumber) {
		this.ackNumber = ackNumber;
	}
	/**
	 * length of TCP Header including options length if available.
	 * @return
	 */
	public int getTCPHeaderLength(){
		return (dataOffset * 4);
	}
	public int getMaxSegmentSize() {
		return maxSegmentSize;
	}
	public void setMaxSegmentSize(int maxSegmentSize) {
		this.maxSegmentSize = maxSegmentSize;
	}
	public int getWindowScale() {
		return windowScale;
	}
	public void setWindowScale(int windowScale) {
		this.windowScale = windowScale;
	}
	public boolean isSelectiveAckPermitted() {
		return isSelectiveAckPermitted;
	}
	public void setSelectiveAckPermitted(boolean isSelectiveAckPermitted) {
		this.isSelectiveAckPermitted = isSelectiveAckPermitted;
	}
	public int getTimeStampSender() {
		return timeStampSender;
	}
	public void setTimeStampSender(int timeStampSender) {
		this.timeStampSender = timeStampSender;
	}
	public int getTimeStampReplyTo() {
		return timeStampReplyTo;
	}
	public void setTimeStampReplyTo(int timeStampReplyTo) {
		this.timeStampReplyTo = timeStampReplyTo;
	}
	
}
