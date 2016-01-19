package com.att.aro.console.printstreamutils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class NullOut extends ByteArrayOutputStream {
	@Override
	public synchronized void write(int b) {
	}
	@Override
	public synchronized void write(byte[] b, int off, int len) {
	}
	@Override
	public synchronized void writeTo(OutputStream out) throws IOException {
	}
}
