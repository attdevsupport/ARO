package com.att.aro.videocapture;

import java.io.FilterOutputStream;
import java.io.IOException;
import javax.imageio.stream.ImageOutputStream;

/**
 * Represents an output stream filter that supports the creation and handling of
 * an ImageOutputStream for use with the DataAtomOutputStream class.
 * 
 */
public class FilterImageOutputStream extends FilterOutputStream {
	private ImageOutputStream imgOut;

	/**
	 * Initializes a new instance of the FilterImageOutputStream class using the
	 * specified ImageOutputStream object
	 * 
	 * @param iOut
	 *            The ImageOutputStream object that is used to create the new
	 *            FilterImageOutputStream. The ImageOutputStream object that is
	 *            used to create the new FilterImageOutputStream.
	 */
	public FilterImageOutputStream(ImageOutputStream iOut) {
		super(null);
		this.imgOut = iOut;
	}

	/**
	 * Writes the specified byte to the output stream. The write method of
	 * FilterOutputStream calls the write method of its underlying OutputStream
	 * class, that is, it performs out.write(b). This method implements the
	 * abstract write method of OutputStream.
	 * 
	 * 
	 * @param b
	 *            The byte to be written.
	 * @exception IOException
	 *                If an I/O error occurs.
	 */
	@Override
	public void write(int b) throws IOException {
		imgOut.write(b);
	}

	/**
	 * Writes the specified number of bytes from the supplied byte array to the
	 * output stream, starting at the specified offset in the array. This method
	 * calls the write method of FilterOutputStream which calls the calls the
	 * FilterOutputStream.write(int b) method repeatedly to write one byte at a
	 * time from the array to the output stream. Note that this method does not
	 * call the write method of its underlying input stream with the same
	 * arguments. It is recommended that subclasses of FilterOutputStream should
	 * provide a more efficient implementation of this method.
	 * 
	 * 
	 * @param b
	 *            The array of bytes to write to the output stream.
	 * @param off
	 *            The offset in the array to begin writing from.
	 * @param len
	 *            The number of bytes to write from the array.
	 * @exception IOException
	 *                If an I/O error occurs.
	 * @see java.io.FilterOutputStream#write(int)
	 */
	@Override
	public void write(byte b[], int off, int len) throws IOException {
		imgOut.write(b, off, len);
	}

	/**
	 * Flushes the output stream by forcing any buffered output bytes to be
	 * written out to the stream. This method calls the FilterOutputStream.flush
	 * method of its underlying output stream.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FilterOutputStream#out
	 */
	@Override
	public void flush() throws IOException {
		// System.err.println(this+" discarded flush");
		// imgOut.flush();
	}

	/**
	 * Closes this output stream and releases any system resources associated
	 * with the stream. This method calls the underlying
	 * FilterOutputStream.flush method, and then calls the
	 * FilterOutputStream.close method, to flush and close its underlying output
	 * stream.
	 * 
	 * @exception IOException
	 *                if an I/O error occurs.
	 * @see java.io.FilterOutputStream#flush()
	 * @see java.io.FilterOutputStream#out
	 */
	@Override
	public void close() throws IOException {
		try {
			flush();
		} catch (IOException ignored) {
		}
		imgOut.close();
	}
}
