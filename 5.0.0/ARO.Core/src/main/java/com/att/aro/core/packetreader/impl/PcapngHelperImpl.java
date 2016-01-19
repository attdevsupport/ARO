package com.att.aro.core.packetreader.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetreader.IPcapngHelper;

public class PcapngHelperImpl implements IPcapngHelper {

	private String prevfilepath;
	private long prevlastmodifytime = 0;
	private String hardware = "";
	String osname = "";
	String appname = "";
	int osVersion = 0;
	int osMajor = 0;
	int appVersion = 0;
	private boolean applePcapNG;

	@InjectLogger
	private static ILogger logger;

	/**
	 * check pcapng file header to see if it is created by Apple Tool
	 * 
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	@Override
	public boolean isApplePcapng(File file) throws FileNotFoundException {
		//reuse previous result if the same file is passed in for calculation
		if (file.lastModified() == this.prevlastmodifytime && file.getAbsolutePath().equals(this.prevfilepath)) {
			return this.applePcapNG;
		}

		this.prevfilepath = file.getAbsolutePath();
		this.prevlastmodifytime = file.lastModified();

		FileInputStream stream = new FileInputStream(file);
		applePcapNG = isApplePcapng(stream, (int) file.length());
		return applePcapNG;
	}

	public boolean isApplePcapng(FileInputStream stream, int filesize) {
		boolean result = false;
		int size = 2048;//header size should never be bigger than this.
		if (filesize < size) {
			size = filesize;
		}
		byte[] data = new byte[size];
		try {
			stream.read(data);
		} catch (IOException e) {
			logger.error("failed to read packet file");
			return result;
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				logger.error("failed to close packet file");
			}
		}
		result = this.isApplePcapng(data);
		return result;
	}

	public boolean isApplePcapng(byte[] data) {
		boolean result = false;
		ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
		int blocktype = 0x0A0D0D0A;
		int type = buffer.getInt();
		if (type != blocktype) {
			return result;
		}
		int blocklen = buffer.getInt();
		int startpos = 24;
		short optioncode = 0;
		short optionlen = 0;
		byte[] dst = null;
		int mod;
		int stop = blocklen - 4;
		buffer.position(startpos);
		do {
			optioncode = buffer.getShort();
			optionlen = buffer.getShort();
			startpos = buffer.position();

			if (optionlen > 0) {
				dst = new byte[optionlen];
				buffer.get(dst, 0, optionlen);
				startpos = buffer.position();
			}
			switch (optioncode) {
			case 2://hardware like x86_64 etc.
				hardware = new String(dst);
				break;
			case 3://Operating System like Mac OS 10.8.5
				osname = new String(dst);
				break;
			case 4://name of application that created this packet file like tcpdump( libpcap version 1.3)
				appname = new String(dst);
				break;
			default:
				break;
			}
			//16 bit align and 32 bit align
			mod = startpos % 2;
			startpos += mod;
			mod = startpos % 4;
			startpos += mod;
			buffer.position(startpos);
		} while (optioncode > 0 && optionlen > 0 && startpos < stop);
		if (osname.length() > 1 && appname.length() > 1) {
			/*
			 * look for OS >= Darwin 13.0.0 App: tcpdump (libpcap version 1.3.0
			 * - Apple version 41)
			 */
			extractOSVersion();
			extractAppVersion();
			if (osVersion >= 13 && osMajor >= 0 && appVersion >= 41) {
				result = true;
			}
		}
		return result;
	}

	void extractOSVersion() {
		Pattern pattern = Pattern.compile("Darwin (\\d+)\\.(\\d+)");
		Matcher match = pattern.matcher(osname);
		boolean success = match.find();
		if (success) {
			osVersion = Integer.parseInt(match.group(1));
			osMajor = Integer.parseInt(match.group(2));
		}
	}

	void extractAppVersion() {
		Pattern pattern = Pattern.compile("tcpdump.+Apple version (\\d+)");
		Matcher match = pattern.matcher(appname);
		boolean success = match.find();
		if (success) {
			appVersion = Integer.parseInt(match.group(1));
		}
	}

	@Override
	public String getHardware() {
		return hardware;
	}

	@Override
	public String getOs() {
		return osname;
	}

	@Override
	public boolean isApplePcapng(String filepath) throws FileNotFoundException {
		File file = new File(filepath);
		return this.isApplePcapng(file);
	}

}
