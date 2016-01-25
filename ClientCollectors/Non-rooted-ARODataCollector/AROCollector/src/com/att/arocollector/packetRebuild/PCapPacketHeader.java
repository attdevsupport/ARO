package com.att.arocollector.packetRebuild;

import java.io.IOException;
import java.io.InputStream;




/**
 * Pkt header in the libcap file.
 * 
 * 
 * struct sf_pkthdr {
 *     struct timeval  ts;         
 *     UINT            caplen;     
 *     UINT            len;        
 *                   };
 *                   
 *  The caplen is the portion of the packet found in the cap (it's possible that only part of the packet will be recorded).
 *  The len is the packet len as recorded.
 *  
 * @author roni bar-yanai 
 */

public class PCapPacketHeader
{
	public static final int HEADER_SIZE = 16;
	
  	protected long timeValSec32Uint = 0;

	protected long timeValMsec32Uint = 0;

	protected long caplen32Uint = 0;

	protected long pktlenUint32 = 0;
	
	protected byte[] myOriginalCopy = null;
	 
	
	//Commenting it out as we won;t need this  for writing PCAP 
	
//	/**
//	 * read header from in stream.
//	 * @param in - the stream
//	 * @param flip - flip bytes or not (recorder in little or big indian system)
//	 * @return the header
//	 * @throws IOException
//	 */
//	public PCapPacketHeader readNextPcktHeader(InputStream in, boolean flip) throws IOException
//	{
//		byte[] tmp = new byte[16];
//		if (in.read(tmp) != tmp.length) return null;
//		
//		myOriginalCopy = tmp;
//		
//		timeValSec32Uint = ByteUtils.getByteNetOrderTo_unit32(tmp, 0);
//		timeValMsec32Uint = ByteUtils.getByteNetOrderTo_unit32(tmp, 4);
//		caplen32Uint = ByteUtils.getByteNetOrderTo_unit32(tmp, 8);
//		pktlenUint32 = ByteUtils.getByteNetOrderTo_unit32(tmp, 12);
//
//		if (flip)
//		{
//			timeValSec32Uint = PCapFileReader.pcapflip32(timeValSec32Uint);
//			timeValMsec32Uint = PCapFileReader.pcapflip32(timeValMsec32Uint);
//			caplen32Uint = PCapFileReader.pcapflip32(caplen32Uint);
//			pktlenUint32 = PCapFileReader.pcapflip32(pktlenUint32);
//		}
//		return this;
//	}
//	
	/**
	 * @return the header as little indian.
	 */
	public byte[] getAsByteArray()
	{
		byte[] tmp = new byte[16];
		 
		ByteUtils.setLittleIndianInBytesArray(tmp,0,pcapRead32(timeValSec32Uint),4);
		ByteUtils.setLittleIndianInBytesArray(tmp,4,pcapRead32(timeValMsec32Uint),4);
		ByteUtils.setLittleIndianInBytesArray(tmp,8,pcapRead32(caplen32Uint),4);
		ByteUtils.setLittleIndianInBytesArray(tmp,12,pcapRead32(pktlenUint32),4);

		return tmp;
	}
	
	/**
	 * @return the header as read from the stream.
	 */
	protected byte[] getTheHeaderByteArray()
	{
		return myOriginalCopy;
	}

	/**
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "Time Sec: " + timeValSec32Uint + "\n" + "Time MSec : " + timeValMsec32Uint + "\n" + "Cap Len : " + caplen32Uint + "\n" + "PKT Len : " + pktlenUint32 + "\n";
	}

	/**
	 * The recorded packet portion.
	 * @param theCaplen32Uint
	 */
	public void setCaplen32Uint(long theCaplen32Uint)
	{
		caplen32Uint = theCaplen32Uint;
	}

	/**
	 * The packet wire length.
	 * @param thePktlenUint32
	 */
	public void setPktlenUint32(long thePktlenUint32)
	{
		pktlenUint32 = thePktlenUint32;
	}

	/**
	 * The time in microsec.
	 * @param theTimeValMsec32Uint
	 */
	public void setTimeValMsec32Uint(long theTimeValMsec32Uint)
	{
		timeValMsec32Uint = theTimeValMsec32Uint;
	}

	/**
	 * the time in sec.
	 * @param theTimeValSec32Uint
	 */
	public void setTimeValSec32Uint(long theTimeValSec32Uint)
	{
		timeValSec32Uint = theTimeValSec32Uint;
	}
	
	private long pcapRead32(long num)
	{
		long tmp = num;
		tmp = ((tmp & 0x000000FF) << 24) + ((tmp & 0x0000FF00) << 8) + ((tmp & 0x00FF0000) >> 8) + ((tmp & 0xFF000000) >> 24);
		return tmp;
	}

	/**
	 * 
	 * @return
	 */
	public long getTimeValMsec32Uint()
	{
		return timeValMsec32Uint;
	}

	/**
	 * 
	 * @return
	 */
	public long getTimeValSec32Uint()
	{
		return timeValSec32Uint;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTime()
	{
		return timeValSec32Uint*1000000 + timeValMsec32Uint;
	}
}
