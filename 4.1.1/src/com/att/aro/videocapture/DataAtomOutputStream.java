package com.att.aro.videocapture;

import java.io.*;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Represents an output stream filter that supports the common data types used
 * inside of QuickTime Data Atoms.
 */
public class DataAtomOutputStream extends FilterOutputStream {

	/**
	 * The Mac Epoch format timestamp value.
	 */
	protected static final long MAC_TIMESTAMP_EPOCH = new GregorianCalendar(
			1904, GregorianCalendar.JANUARY, 1).getTimeInMillis();
	/**
	 * The current number of bytes written to the data output stream. If this
	 * counter overflows, it will be wrapped to Integer.MAX_VALUE.
	 */
	protected long written;

	/**
	 * Initializes a new instance of the DataAtomOutputStream class using the
	 * specified OutputStream object.
	 * 
	 * @param out
	 *            The OutputStream object that is used to create the new
	 *            DataAtomOutputStream.
	 */
	public DataAtomOutputStream(OutputStream out) {
		super(out);
	}

	/**
	 * Writes a 4 byte Atom Type identifier to the output stream.
	 * 
	 * @param s
	 *            The Atom Type identifier to be written. A string with a length
	 *            of 4 characters.
	 */
	public void writeType(String s) throws IOException {
		if (s.length() != 4) {
			throw new IllegalArgumentException(
					"type string must have 4 characters");
		}

		try {
			out.write(s.getBytes("ASCII"), 0, 4);
			incCount(4);
		} catch (UnsupportedEncodingException e) {
			throw new InternalError(e.toString());
		}
	}

	/**
	 * Writes a byte to the underlying output stream as a 1 byte value. If no
	 * exception is thrown, the byte counter is incremented by 1.
	 * 
	 * @param v
	 *            The byte value to be written.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public final void writeByte(int v) throws IOException {
		out.write(v);
		incCount(1);
	}

	/**
	 * Writes the specified number of bytes from a byte array to the underlying
	 * output stream, starting at the specified offset. If no exception is
	 * thrown, the number of bytes written (the byte counter) is incremented by
	 * the value of the len parameter.
	 * 
	 * @param b
	 *            The array of bytes to write to the output stream.
	 * @param off
	 *            The offset in the array to begin writing from.
	 * @param len
	 *            The number of bytes to write from the array.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	@Override
	public synchronized void write(byte b[], int off, int len)
			throws IOException {
		out.write(b, off, len);
		incCount(len);
	}

	/**
	 * Writes the specified byte (the low eight bits of the int parameter b) to
	 * the underlying output stream. If no exception is thrown, the byte counter
	 * is incremented by 1.This method implements the write method of the
	 * underlying OutputStream class.
	 * 
	 * @param b
	 *            The byte to be written.
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	@Override
	public synchronized void write(int b) throws IOException {
		out.write(b);
		incCount(1);
	}

	/**
	 * Writes an int value to the underlying output stream as four bytes. The
	 * high byte is written first. If no exception is thrown, the byte counter
	 * is incremented by 4.
	 * 
	 * @param v
	 *            The int value to be written.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public void writeInt(int v) throws IOException {
		out.write((v >>> 24) & 0xff);
		out.write((v >>> 16) & 0xff);
		out.write((v >>> 8) & 0xff);
		out.write((v >>> 0) & 0xff);
		incCount(4);
	}

	/**
	 * Writes an unsigned 32 bit integer value to the output stream.
	 * 
	 * @param v
	 *            The unsigned 32 bit value to be written.
	 * @throws java.io.IOException
	 */
	public void writeUInt(long v) throws IOException {
		out.write((int) ((v >>> 24) & 0xff));
		out.write((int) ((v >>> 16) & 0xff));
		out.write((int) ((v >>> 8) & 0xff));
		out.write((int) ((v >>> 0) & 0xff));
		incCount(4);
	}

	/**
	 * Writes a signed 16-bit integer value to the output stream.
	 * 
	 * @param v
	 *            The int value to be written.
	 * @throws java.io.IOException
	 */
	public void writeShort(int v) throws IOException {
		out.write((int) ((v >> 8) & 0xff));
		out.write((int) ((v >>> 0) & 0xff));
		incCount(2);
	}

	/**
	 * Writes a BCD2 to the underlying output stream.
	 * 
	 * @param v
	 *            An int value that is the BCD2 to be written.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public void writeBCD2(int v) throws IOException {
		out.write(((v % 100 / 10) << 4) | (v % 10));
		incCount(1);
	}

	/**
	 * Writes a BCD4 to the underlying output stream.
	 * 
	 * @param v
	 *            An int value that is the BCD4 to be written.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public void writeBCD4(int v) throws IOException {
		out.write(((v % 10000 / 1000) << 4) | (v % 1000 / 100));
		out.write(((v % 100 / 10) << 4) | (v % 10));
		incCount(2);
	}

	/**
	 * Writes a 32-bit Mac format timestamp (the number of seconds since 1902),
	 * to the output stream.
	 * 
	 * @param date
	 *            The date value to be written as a Mac format timestamp.
	 * @throws java.io.IOException
	 */
	public void writeMacTimestamp(Date date) throws IOException {
		long millis = date.getTime();
		long qtMillis = millis - MAC_TIMESTAMP_EPOCH;
		long qtSeconds = qtMillis / 1000;
		writeUInt(qtSeconds);
	}

	/**
	 * Writes a 32-bit fixed-point number divided as a 16 bit number with a 16
	 * bit decimal value (16.16).
	 * 
	 * @param f
	 *            The double value to be written in the (16.16) format.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public void writeFixed16D16(double f) throws IOException {
		double v = (f >= 0) ? f : -f;

		int wholePart = (int) v;
		int fractionPart = (int) ((v - wholePart) * 65536);
		int t = (wholePart << 16) + fractionPart;

		if (f < 0) {
			t = t - 1;
		}
		writeInt(t);
	}

	/**
	 * Writes a 32-bit fixed-point number divided as a 2 bit number with a 30
	 * bit decimal value (2.30).
	 * 
	 * @param f
	 *            The double value to be written in the (2.30) format.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public void writeFixed2D30(double f) throws IOException {
		double v = (f >= 0) ? f : -f;

		int wholePart = (int) v;
		int fractionPart = (int) ((v - wholePart) * 1073741824);
		int t = (wholePart << 30) + fractionPart;

		if (f < 0) {
			t = t - 1;
		}
		writeInt(t);
	}

	/**
	 * Writes a 16-bit fixed-point number divided as an 8 bit number with an 8
	 * bit decimal value (8.8).
	 * 
	 * @param f
	 *            The int value to be written in the (8.8) format.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	public void writeFixed8D8(float f) throws IOException {
		float v = (f >= 0) ? f : -f;

		int wholePart = (int) v;
		int fractionPart = (int) ((v - wholePart) * 256);
		int t = (wholePart << 8) + fractionPart;

		if (f < 0) {
			t = t - 1;
		}
		writeUShort(t);
	}

	/**
	 * Writes a Pascal String to the underlying output stream.
	 * 
	 * @param s
	 *            The Pascal string to be written.
	 * @throws java.io.IOException
	 */
	public void writePString(String s) throws IOException {
		if (s.length() > 0xffff) {
			throw new IllegalArgumentException("String too long for PString");
		}
		if (s.length() < 256) {
			out.write(s.length());
		} else {
			out.write(0);
			writeShort(s.length()); // increments +2
		}
		for (int i = 0; i < s.length(); i++) {
			out.write(s.charAt(i));
		}
		incCount(1 + s.length());
	}

	/**
	 * Writes a Pascal String, padded to the specified fixed size in bytes, to
	 * the output stream.
	 * 
	 * @param s
	 *            The Pascal string to be written.
	 * @param length
	 *            The fixed size in bytes that the string should be padded to
	 *            fill.
	 * @throws java.io.IOException
	 */
	public void writePString(String s, int length) throws IOException {
		if (s.length() > length) {
			throw new IllegalArgumentException(
					"String too long for PString of length " + length);
		}
		if (s.length() < 256) {
			out.write(s.length());
		} else {
			out.write(0);
			writeShort(s.length()); // increments +2
		}
		for (int i = 0; i < s.length(); i++) {
			out.write(s.charAt(i));
		}

		// write pad bytes
		for (int i = 1 + s.length(); i < length; i++) {
			out.write(0);
		}

		incCount(length);
	}

	/**
	 * Writes a long value to the output stream.
	 * 
	 * @param v
	 *            The long value to be written.
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	public void writeLong(long v) throws IOException {
		out.write((int) (v >>> 56) & 0xff);
		out.write((int) (v >>> 48) & 0xff);
		out.write((int) (v >>> 40) & 0xff);
		out.write((int) (v >>> 32) & 0xff);
		out.write((int) (v >>> 24) & 0xff);
		out.write((int) (v >>> 16) & 0xff);
		out.write((int) (v >>> 8) & 0xff);
		out.write((int) (v >>> 0) & 0xff);
		incCount(8);
	}

	/**
	 * Writes an unsigned short value to the output stream.
	 * 
	 * @param v
	 *            The unsigned short value to be written.
	 */
	public void writeUShort(int v) throws IOException {
		out.write((int) ((v >> 8) & 0xff));
		out.write((int) ((v >>> 0) & 0xff));
		incCount(2);
	}

	/**
	 * Increases the written counter by the specified value until it reaches
	 * Long.MAX_VALUE.
	 * @param value - The value to increase the written counter.
	 */
	protected void incCount(int value) {
		long temp = written + value;
		if (temp < 0) {
			temp = Long.MAX_VALUE;
		}
		written = temp;
	}

	/**
	 * Returns the current value of the counter <code>written</code>, the number
	 * of bytes written to this data output stream so far. If the counter
	 * overflows, it will be wrapped to Integer.MAX_VALUE.
	 * 
	 * @return the value of the <code>written</code> field.
	 * @see java.io.DataOutputStream#written
	 */
	public final long size() {
		return written;
	}

}
