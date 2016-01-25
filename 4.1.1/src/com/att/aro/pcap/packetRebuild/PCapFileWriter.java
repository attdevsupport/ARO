package com.att.aro.pcap.packetRebuild;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;






/**
 * Class for creating capture files in libcap format.<br>
 * 
 * if using java version less then 1.5 then the packet time resolution will
 * be in msec and no nanosec.<br>
 * 
 * @since java 1.5
 * @author roni bar yanai
 *
 */
public class PCapFileWriter implements CaptureFileWriter
{
	private static final int MAX_PACKET_SIZE = 65356;

	public static final long DEFAULT_LIMIT = 100000000000l;

	// limit the file size
	private long myLimit = DEFAULT_LIMIT;

	// the out stream
	private OutputStream myOutStrm = null;

	private boolean _isopened = false;

	// used to calculate the packets time.
	private long myStartTime = 0;

	// total ~bytes written so far.
	private long myTotalBytes = 0;

	private boolean isAboveJave1_4 = true;

	/**
	 * open new file
	 * @param file
	 * @throws IOException - on file creation failure.
	 */
	public PCapFileWriter(File file) throws IOException
	{
		this(file, false);
	}
	
	/**
	 * open new file
	 * @param file - the file name
	 * @throws IOException - on file creation failure.
	 */
	public PCapFileWriter(String file) throws IOException
	{
		this(new File(file), false);
	}

	/**
	 * open new file
	 * @param file
	 * @param append 
	 * @throws IOException - on file creation failure.
	 */
	public PCapFileWriter(File file, boolean append) throws IOException
	{
		if (file == null) throw new IllegalArgumentException("Got null file object");

		init(file, append);
		myStartTime = getNanoTime();
	}

	/**
	 * open new file
	 * @param thefile
	 * @param thelimit - max bytes
	 * @throws IOException - on file creation failure.
	 */
	public PCapFileWriter(File thefile, long thelimit) throws IOException
	{
		this(thefile);
		myLimit = thelimit;
	}
	
	/**
	 * write to provided output stream.
	 * @param outStream
	 * @throws IOException
	 */
	public PCapFileWriter(OutputStream outStream) throws IOException
	{
		init(outStream);
	}
	
	/**
	 * set java version > 1.4
	 * @param isAboveJave1_4
	 */
	public void setAboveJave1_4(boolean isAboveJave1_4)
	{
		this.isAboveJave1_4 = isAboveJave1_4;
	}

	/**
	 * 
	 * @return time stamp in nano seconds
	 */
	private long getNanoTime()
	{
		if (isAboveJave1_4)
		{
			return System.nanoTime();
		}
		else
		{
			return System.currentTimeMillis();// * 1000000;
		}
	}

	/**
	 * open the out stream and write the cap header.
	 * @param file
	 * @throws IOException
	 */
	private void init(File file, boolean append) throws IOException
	{
		boolean putHdr = !file.exists() || !append;

		myOutStrm = new FileOutputStream(file, append);

		// put hdr only if not appending or file not exits (new file).
		if (putHdr)
		{
			PCapFileHeader hdr = new PCapFileHeader();
			myOutStrm.write(hdr.getAsByteArray());
		}
		_isopened = true;

		myTotalBytes += PCapFileHeader.HEADER_SIZE;
	}
	
	private void init(OutputStream out) throws IOException
	{
		myOutStrm = out;

		// put hdr only if not appending or file not exits (new file).
		PCapFileHeader hdr = new PCapFileHeader();
		myOutStrm.write(hdr.getAsByteArray());
		
		_isopened = true;

		myTotalBytes += PCapFileHeader.HEADER_SIZE;
	}
	
	/**
	 * 0 Byte Header (null/loopback) add packet to already opened cap. if close
	 * method was called earlier then will not add it.
	 * 
	 * @param thepkt
	 * @param time
	 *            - time offset in micro sec
	 * @return true if packet added and false otherwise
	 * @throws IOException
	 * @throws IOException
	 */
	public boolean addPacketConvertedPcapng(byte[] thepkt, int offset, int length, long time) throws IOException {

		if (thepkt == null || !_isopened || myTotalBytes > myLimit)
			return false;

		PCapPacketHeader hder = new PCapPacketHeader();

		length -= offset;
		
		if (time == 0) {
			time = getNanoTime() - myStartTime; // the gap since start in nano sec
		}

		hder.setTimeValMsec32Uint((time ) % 1000000);
		hder.setTimeValSec32Uint(time / 1000000l);

		// updated to use the real packet length
		hder.setPktlenUint32(length + ETHERNET_HDR_LEN);
		hder.setCaplen32Uint(length + ETHERNET_HDR_LEN);

		if (length > MAX_PACKET_SIZE)
			throw new IOException("Got illeagl packet size : " + thepkt.length);

		byte[] x = hder.getAsByteArray();
		myOutStrm.write(hder.getAsByteArray());

		// added to write fake ethernet header

		byte[] y = StubbedEthernetHeader.getEthernetHeader();
		myOutStrm.write(StubbedEthernetHeader.getEthernetHeader());

		myOutStrm.write(thepkt, offset, length);

		// update to use real packet length and add in len of ethernet header
		myTotalBytes += length + ETHERNET_HDR_LEN + PCapPacketHeader.HEADER_SIZE;

		return true;
	}


	/**
	 * add packet to already opened cap.
	 * if close method was called earlier then will not add it.
	 * @param thepkt
	 * @param time - time offset in micro sec 
	 * @return true if packet added and false otherwise
	 * @throws IOException 
	 * @throws IOException
	 */
	public boolean addPacket(byte[] thepkt,long time) throws IOException
	{

		if (thepkt == null || !_isopened || myTotalBytes > myLimit) return false;

		PCapPacketHeader hder = new PCapPacketHeader();

		hder.setTimeValMsec32Uint((time ) % 1000000);
		hder.setTimeValSec32Uint(time / 1000000l);
		hder.setPktlenUint32(thepkt.length);
		hder.setCaplen32Uint(thepkt.length);

		if (thepkt.length > MAX_PACKET_SIZE){
			throw new IOException("Got illeagl packet size : "+thepkt.length);
		}
		myOutStrm.write(hder.getAsByteArray());
		myOutStrm.write(thepkt);

		myTotalBytes += thepkt.length + PCapPacketHeader.HEADER_SIZE;

		return true;
	
		
	}

	int ETHERNET_HDR_LEN = 14;
	/**
	 * add packet to alreay opened cap.
	 * if close method was called earlier then will not add it.
	 * @param thepkt
	 * @return true if packet added and false otherwise
	 * @throws IOException
	 */
	public boolean addPacket(byte[] thepkt, int offset, int length) throws IOException
	{
		if (thepkt == null || !_isopened || myTotalBytes > myLimit) return false;

		PCapPacketHeader hder = new PCapPacketHeader();

		long gap = getNanoTime() - myStartTime; // the gap since start in nano sec

		hder.setTimeValMsec32Uint((gap / 1000) % 1000000);
		hder.setTimeValSec32Uint(gap / 1000000000l);
		
		//updated to use the real packet length
		hder.setPktlenUint32(length + ETHERNET_HDR_LEN);
		hder.setCaplen32Uint(length + ETHERNET_HDR_LEN);

		if (length > MAX_PACKET_SIZE)
			throw new IOException("Got illeagl packet size : "+thepkt.length);
		
		myOutStrm.write(hder.getAsByteArray());
		
		//added to write fake ethernet header
		myOutStrm.write(StubbedEthernetHeader.getEthernetHeader());
		
		myOutStrm.write(thepkt, offset, length);

		//update to use real packet length and add in len of ethernet header
		myTotalBytes += length + ETHERNET_HDR_LEN + PCapPacketHeader.HEADER_SIZE;

		return true;
	}

	/**
	 * add packet to alreay opened cap.
	 * if close method was called earlier then will not add it.
	 * 
	 * @param thepkt packet to store
	 * @param offset
	 * @param length length of packet
	 * @param time timestamp
	 * @return
	 * @throws IOException
	 */
	public boolean addPacket(byte[] thepkt, int offset, int length, long time) throws IOException {
		
		if (thepkt == null || !_isopened || myTotalBytes > myLimit)
			return false;

		PCapPacketHeader hder = new PCapPacketHeader();

		if (time == 0) {
			time = getNanoTime() - myStartTime; // the gap since start in nano sec
		}

		hder.setTimeValMsec32Uint((time / 1000) % 1000000);
		hder.setTimeValSec32Uint(time / 1000000000l);

		// updated to use the real packet length
		hder.setPktlenUint32(length + ETHERNET_HDR_LEN);
		hder.setCaplen32Uint(length + ETHERNET_HDR_LEN);

		if (length > MAX_PACKET_SIZE)
			throw new IOException("Got illeagl packet size : " + thepkt.length);

		myOutStrm.write(hder.getAsByteArray());

		// added to write fake ethernet header
		myOutStrm.write(StubbedEthernetHeader.getEthernetHeader());

		myOutStrm.write(thepkt, offset, length);

		// update to use real packet length and add in len of ethernet header
		myTotalBytes += length + ETHERNET_HDR_LEN + PCapPacketHeader.HEADER_SIZE;

		return true;
	}


	/**
	 * close file.
	 * not reversible
	 * @throws IOException
	 */
	public void close() 
	{
		if (_isopened && myOutStrm != null)
		{
			try
			{
				myOutStrm.close();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
			_isopened = false;
			myOutStrm = null;
		}
	}

	/**
	 * @return number of bytes written so far.
	 */
	public long getTotalBytes()
	{
		return myTotalBytes;
	}

	/**
	 * @return true if cap limit reached.
	 */
	public boolean isLimitReached()
	{
		return myTotalBytes >= myLimit;
	}

	/**
	 * set the cap max number of bytes.
	 * @param theLimit
	 */
	public void setLimit(long theLimit)
	{
		myLimit = theLimit;
	}

}
