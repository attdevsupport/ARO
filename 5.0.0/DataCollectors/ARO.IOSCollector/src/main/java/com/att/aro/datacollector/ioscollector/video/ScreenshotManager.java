package com.att.aro.datacollector.ioscollector.video;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;

import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.util.ImageHelper;
import com.att.aro.core.util.Util;
import com.att.aro.datacollector.ioscollector.IScreenshotPubSub;
import com.att.aro.datacollector.ioscollector.reader.ExternalScreenshotReader;
import com.att.aro.datacollector.ioscollector.utilities.Tiff2JpgUtil;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

public class ScreenshotManager extends Thread implements IScreenshotPubSub {
//	private static final Logger logger = Logger.getLogger(ScreenshotManager.class.getName());
	
	LoggerImpl log = new LoggerImpl(this.getClass().getName());
	ExternalScreenshotReader procreader;
	BufferedWriter writer;
	Process proc = null;
	String lastmessage = "";
	int counter = 0;
	String imagefolder = "";
	boolean isready = false;
	File tmpfolder;

	public ScreenshotManager(String folder) {
		imagefolder = folder + Util.FILE_SEPARATOR + "tmp";
		tmpfolder = new File(imagefolder);
		if (!tmpfolder.exists()) {
			log.debug("tmpfolder.mkdirs()"+ imagefolder);
			tmpfolder.mkdirs();
			log.debug("exists :"+tmpfolder.exists());
		}
	}

	@Override
	public void run() {
		String exepath =  Util.getAroLibrary() 
						+ Util.FILE_SEPARATOR + ".drivers" 
						+ Util.FILE_SEPARATOR + "libimobiledevice" 
						+ Util.FILE_SEPARATOR + "idevicescreenshot";
		
		log.debug("exepath :" + exepath);
		File exefile = new File(exepath);
		if (!exefile.exists()) {
			log.info("Not found exepath: " + exepath);
		}
		String[] cmds = new String[] { "bash", "-c", exepath + " 2>&1" };
		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);

		try {
			proc = builder.start();
		} catch (IOException e) {
			log.error("Error starting idevicescreenshot:", e);
			return;
		}

		log.debug("OutputStream :"+proc.getOutputStream().getClass().getName());
		writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));

		procreader = new ExternalScreenshotReader(proc.getInputStream());
		procreader.addSubscriber(this);
		procreader.start(); // look for "Connect success!"
		
		this.lastmessage = "";
		int timeoutcounter = 0;
		while (!this.lastmessage.contains("Connect success")) {
			try {

				Thread.sleep(100);
				timeoutcounter++;
			} catch (InterruptedException e) {
				log.error("InterruptedException:", e);
			}
			if (timeoutcounter > 50) {//5 seconds
				log.info("Timeout, idevicescreenshot did not start successfully");
				break;
			}
		}
		if (this.lastmessage.contains("Connect success")) {
			log.debug("Connect success");
			isready = true;
		} else {
			log.debug("false");
			isready = false;
		}
	}

	public boolean isReady() {
		return isready;
	}

	public BufferedImage getImage() {
		BufferedImage imgdata = null;
		File imgfile = null;
		String img = this.imagefolder + Util.FILE_SEPARATOR + "image" + counter + ".tiff";
		//logger.info(img);
		counter++;
		boolean success = false;

		try {
			success = this.getScreenshot(img);
		} catch (IOException e) {
			log.error("Error reading screenshot data:",e);
			success = false;
			//return null;
		}
		if (!success) {
			log.error("failed to get screenshot, last message: " + this.lastmessage);
		}
		if (success) {
			try {
				imgfile = new File(img);
				if (imgfile.exists()) {
					FileInputStream inputstream = new FileInputStream(imgfile);
					byte[] imgdataarray = new byte[(int) imgfile.length()];
					inputstream.read(imgdataarray);
					inputstream.close();

					imgdata = ImageHelper.getImageFromByte(imgdataarray);
					ByteArrayOutputStream byteArrayOutputStream = Tiff2JpgUtil.tiff2Jpg(imgfile.getAbsolutePath());
					imgdataarray = byteArrayOutputStream.toByteArray();
					imgdata = getImageFromByte(imgdataarray);
					imgfile.delete();

				} else {
					log.error("Error: image file not found => " + img);
				}
			} catch (IOException e) {
				log.error("Error reading image file:" + img , e);
			}
		}

		return imgdata;
	}

	/**
	 * convert byte array of image to BufferedImge
	 * 
	 * @param array
	 *            data of image
	 * @return new instance of BufferedImage
	 * @throws IOException
	 */
	public static BufferedImage getImageFromByte(byte[] array) throws IOException {
		InputStream instream = new ByteArrayInputStream(array);
		ImageDecoder dec = ImageCodec.createImageDecoder("jpeg", instream, null);
		RenderedImage rendering = new NullOpImage(dec.decodeAsRenderedImage(0), null, null, OpImage.OP_IO_BOUND);
		BufferedImage image = convertRenderedImage(rendering);
		return image;
	}

	public static BufferedImage convertRenderedImage(RenderedImage img) {
		if (img instanceof BufferedImage) {
			return (BufferedImage) img;
		}
		ColorModel cm = img.getColorModel();
		int width = img.getWidth();
		int height = img.getHeight();
		WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		String[] keys = img.getPropertyNames();
		if (keys != null) {
			for (int i = 0; i < keys.length; i++) {
				properties.put(keys[i], img.getProperty(keys[i]));
			}
		}
		BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
		img.copyData(raster);
		return result;
	}

	public boolean getScreenshot(String filepath) throws IOException {
		if (!filepath.endsWith("\r\n")) {
			filepath = filepath + "\r\n";
		//	log.info("Appended linefeed");
		}
		if (writer == null) {
			log.info("BufferedWriter is null");
			return false;
		}
		//logger.info("create screenshot file: "+filepath);
		this.lastmessage = "";
		writer.write(filepath);
		writer.flush();
		//wait for result
		while (this.lastmessage.length() < 1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				log.error("InterruptedException:", e);
			}
		}
		if (this.lastmessage.startsWith("OK")) {
			return true;
		}
		return false;
	}

	/**
	 * sending shutdown signal to the child process which then callback to
	 * ScreenshotManager shutDown() function, which finally self-destroy
	 * 
	 * @throws IOException
	 */
	public void signalShutdown() throws IOException {
		getScreenshot("exit\r\n");
	}

	/**
	 * stop everything and exit
	 */
	public void shutDown() {
		if (procreader != null) {
			procreader.interrupt();
			procreader = null;
		}
		if (proc != null) {
			proc.destroy();
			proc = null;
		}
		//delete left-over image file in this tmp dir
		if (tmpfolder.listFiles() != null && tmpfolder.listFiles().length > 0) {
			for (File file : tmpfolder.listFiles()) {
				file.delete();
			}
			log.info("deleted left-over image files.");
		}
		tmpfolder.delete();
		log.info("ScreenshotManager.shutDown() finished");
	}

	@Override
	public void newMessage(String message) {
		//new message from screenshot reader
		//	log.info(message);
		this.lastmessage = message.trim();
		//log.debug("RECVD:" + lastmessage);
	}

	@Override
	public void willExit() {
		log.info("willExit() called");
		//screenshot service will exit now
		this.shutDown();

	}
}//end class
