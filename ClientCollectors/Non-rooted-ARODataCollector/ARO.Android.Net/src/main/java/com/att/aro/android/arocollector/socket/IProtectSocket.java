package com.att.aro.android.arocollector.socket;

import java.net.DatagramSocket;
import java.net.Socket;

public interface IProtectSocket {
	void protectSocket(Socket socket);
	void protectSocket(int socket);
	void protectSocket(DatagramSocket socket);
}
