package com.att.arocollector.packetRebuild;

public class StubbedEthernetHeader {

	public static byte[] getEthernetHeader(){
		byte[] ethHeader = new byte[14];
		
		//set destination mac to be all 0s
		ByteUtils.setBigIndianInBytesArray(ethHeader, 0, 0, 6);
		
		//set src mac, random to be 1
		ByteUtils.setBigIndianInBytesArray(ethHeader, 6, 1, 6);
		
		//set eth type: 0x0800 = 2048
		ByteUtils.setBigIndianInBytesArray(ethHeader, 12, 2048, 2);
		
		return ethHeader;
	}
}
