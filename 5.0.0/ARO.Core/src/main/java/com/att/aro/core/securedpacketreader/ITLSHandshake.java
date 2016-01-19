package com.att.aro.core.securedpacketreader;

public interface ITLSHandshake {
	int getType();

	int getNextProtocol();

	int getCompressionMethod();

	int getCipherSuite();

	byte[] getServerRandom();

	byte[] getClientRandom();

	int getSessionIDLen();

	byte[] getSessionID();

	int getTicketLen();

	byte[] getTicket();

	int read(byte[] pDataEx, int len, Integer[] hsSize);
}
