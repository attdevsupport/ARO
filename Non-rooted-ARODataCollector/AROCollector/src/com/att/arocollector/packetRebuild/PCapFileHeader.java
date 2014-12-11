package com.att.arocollector.packetRebuild;

import java.io.IOException;
import java.io.InputStream;




/**
 * Class for holding libcap file header data structure.<br>
 * Each libcap start with this header.<br>
 * 
 * @author roni bar yanai
 *
 */
public class PCapFileHeader
{
	public static final int HEADER_SIZE = 24;

	long uint32MagicNum = 0;

	int ushort16VersionMajor = 0;

	int ushort16VersionMinor = 0;

	long uint32ThisTimeZone = 0;

	long uint32Sigfigs = 0;

	long uint32snaplen = 0;

	long uint32LinkType = 0;

	boolean isflip = false;
	
	byte[] mySourceByteArr = null;

	// use to determine if file was recorded on a big indian or a little
		// indian
	protected static final long MAGIC_NUMBER_FLIP = 0xd4c3b2a1L;
	protected static final long MAGIC_NUMBER_DONT_FLIP = 0xa1b2c3d4L;
		
	/**
	 * create pcap file header with defaults.
	 *
	 */
	public PCapFileHeader()
    {
		uint32MagicNum = MAGIC_NUMBER_DONT_FLIP;
		ushort16VersionMajor = 2;
		ushort16VersionMinor = 4;
		uint32ThisTimeZone = 0;
		uint32Sigfigs = 0;
		uint32snaplen = 0xffff;
		uint32LinkType = 1;
    }
	
	/**
	 * read pcap header from stream.
	 * @param in
	 * @throws IOException
	 */
	public void readHeader(InputStream in) throws IOException
	{
		byte[] tmp = new byte[24];
		in.read(tmp);
		uint32MagicNum = ByteUtils.getByteNetOrderTo_unit32(tmp, 0);
		ushort16VersionMajor = ByteUtils.getByteNetOrderTo_unit16(tmp, 4);
		ushort16VersionMinor = ByteUtils.getByteNetOrderTo_unit16(tmp, 6);
		uint32ThisTimeZone = ByteUtils.getByteNetOrderTo_unit32(tmp, 8);
		uint32Sigfigs = ByteUtils.getByteNetOrderTo_unit32(tmp, 12);
		uint32snaplen = ByteUtils.getByteNetOrderTo_unit32(tmp, 16);
		uint32LinkType = ByteUtils.getByteNetOrderTo_unit32(tmp, 20);

		if (uint32MagicNum == MAGIC_NUMBER_DONT_FLIP)
		{
			isflip = false;
        }
		else if (uint32MagicNum == MAGIC_NUMBER_FLIP)
		{
			isflip = true;
		}
		else
		{
			throw new IOException("Not a libcap file format");
		}

		if (isflip)
		{
			ushort16VersionMajor = pcapRead16(ushort16VersionMajor);
			ushort16VersionMinor = pcapRead16(ushort16VersionMinor);
			uint32ThisTimeZone = pcapRead32(uint32ThisTimeZone);
			uint32Sigfigs = pcapRead32(uint32Sigfigs);
			uint32snaplen = pcapRead32(uint32snaplen);
			uint32LinkType = pcapRead32(uint32LinkType);
		}
		
		mySourceByteArr = tmp;
	}
	
	/**
	 * @return the header in big indian order.
	 */
	public byte[] getAsByteArray()
	{
		byte[] tmp = new byte[24];
		ByteUtils.setBigIndianInBytesArray(tmp,0,uint32MagicNum,4);
		ByteUtils.setBigIndianInBytesArray(tmp,4,ushort16VersionMajor,2);
		ByteUtils.setBigIndianInBytesArray(tmp,6,ushort16VersionMinor,2);
		ByteUtils.setBigIndianInBytesArray(tmp,8,uint32ThisTimeZone,4);
		ByteUtils.setBigIndianInBytesArray(tmp,12,uint32Sigfigs,4);
		ByteUtils.setBigIndianInBytesArray(tmp,16,uint32snaplen,4);
		ByteUtils.setBigIndianInBytesArray(tmp,20,uint32LinkType,4);
		return tmp;
    }

	/**
	 *  (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "flip : " + isflip + "\n" + "major : " + ushort16VersionMajor + "\n" + "minor : " + ushort16VersionMinor + "\n" + "time zone : " + Long.toHexString(uint32ThisTimeZone) + "\n" + "sig figs : " + Long.toHexString(uint32Sigfigs) + "\n" + "snap length : " + uint32snaplen + "\n"
				+ "link type : " + uint32LinkType + "\n";
	}

	private long pcapRead32(long num)
	{
		long tmp = num;
		if (isflip)
		{
			tmp = ((tmp & 0x000000FF) << 24) + ((tmp & 0x0000FF00) << 8) + ((tmp & 0x00FF0000) >> 8) + ((tmp & 0xFF000000) >> 24);

			return tmp;
		}
		return num;
	}

	private int pcapRead16(int num)
	{
		int tmp = num;
		if (isflip)
		{
			tmp = ((tmp & 0x00FF) << 8) + ((tmp & 0xFF00) >> 8);
			return tmp;
		}
		return num;
	}

	public byte[] getSourceByteArr()
	{
		return mySourceByteArr;
	}

	public boolean isflip()
	{
		return isflip;
	}

}
