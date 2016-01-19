package com.att.aro.core.packetreader.impl;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetreader.INetmonPacketSubscriber;
import com.att.aro.core.packetreader.IPacketListener;
import com.att.aro.core.packetreader.IPacketReader;
import com.att.aro.core.packetreader.IPacketService;
@Deprecated
public class NetmonPacketReaderImpl implements IPacketReader, INetmonPacketSubscriber {

	@InjectLogger
	private ILogger logger;
	
	@Autowired
	private IPacketService packetservice;
	
	/**
	 * A code that identifies a Microsoft Network Monitor related error.
	 */
	public static final int NETMON_ERROR = -100;

	/**
	 * An error code that iIndicates that the Microsoft Network Monitor trace file could not be loaded. 
	 */
	public static final int NETMON_TRACE_FILE_LOAD_ERROR = -101;

	/**
	 * Indicates that parsing of the Microsoft Network Monitor trace file was successful.
	 */
	public static final int NETMON_PARSING_SUCCESS = 0;
	
	private IPacketListener packetlistener;
	
	private NetmonAdapter netmon = null;
	
	public void setNetmon(NetmonAdapter netmon){
		this.netmon = netmon;
	}
	
	@Override
	public void readPacket(String packetfile, IPacketListener listener)
			throws IOException {
		if(netmon == null){
			netmon = new NetmonAdapter();
			netmon.loadNativeLibs();
		}
		
		logger.info("Creating Netmon Adapter...");
		if (listener == null) {
			logger.error("PacketListener cannot be null");
			throw new IllegalArgumentException("PacketListener cannot be null");
		}

		this.packetlistener = listener;
		int retval = netmon.parseTraceFile(packetfile);
		switch (retval) {
		case NETMON_PARSING_SUCCESS:
			return;
		case NETMON_TRACE_FILE_LOAD_ERROR:
		case NETMON_ERROR:
		default:
			logger.error("NetMon error code: " + retval);
			throw new IOException("NetMon error code: " + retval);
		}
	}
	

	@Override
	public void receiveNetmonPacket(int datalink, long seconds,
			long microSeconds, int len, byte[] data, String appName) {
		// Ignore netmon datalink type frames
		if (datalink >= 0xf000) {
			return;
		}

		packetlistener.packetArrived(appName, packetservice.createPacketFromNetmon(datalink, seconds, microSeconds,
				len, data));
		
	}

}
