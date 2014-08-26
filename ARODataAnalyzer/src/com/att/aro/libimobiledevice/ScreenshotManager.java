package com.att.aro.libimobiledevice;

import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import com.att.aro.util.ImageHelper;
import com.att.aro.util.Util;

public class ScreenshotManager extends Thread implements ScreenshotPubSub {
	private static final Logger logger = Logger.getLogger(ScreenshotManager.class.getName());
	ExternalScreenshotReader procreader;
	BufferedWriter writer;
	Process proc = null;
	String lastmessage = "";
	int counter = 0;
	String imagefolder = "";
	boolean isready = false;
	File tmpfolder;
	public ScreenshotManager(String folder){
		imagefolder = folder + Util.FILE_SEPARATOR + "tmp";
		tmpfolder = new File(imagefolder);
		if(!tmpfolder.exists()){
			tmpfolder.mkdirs();
		}
	}
	
	@Override
    public void run() {
		String exepath = "";// "/usr/local/bin/idevicescreenshot";
		String dir = Util.getCurrentRunningDir();
		File dirfile = new File(dir);
		dir = dirfile.getParent();
		exepath = dir + Util.FILE_SEPARATOR +"bin" + Util.FILE_SEPARATOR + "libimobiledevice" + Util.FILE_SEPARATOR + "idevicescreenshot";
		
		File exefile = new File(exepath);
		if(!exefile.exists()){
			logger.info("Not found exepath: "+exepath);
		}
		String[] cmds = new String[]{"bash","-c",exepath};
		ProcessBuilder builder = new ProcessBuilder(cmds);
		builder.redirectErrorStream(true);
		
		
		try {
			proc = builder.start();
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("Error starting idevicescreenshot:"+ e.getMessage());
			return;
		}
		
		writer = new BufferedWriter(new OutputStreamWriter(proc.getOutputStream()));
		
		procreader = new ExternalScreenshotReader(proc.getInputStream());
		procreader.addSubscriber(this);
		procreader.start();
		this.lastmessage = "";
		int timeoutcounter = 0;
		while(!this.lastmessage.contains("Connect success")){
			try {
				
				Thread.sleep(100);
				timeoutcounter++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(timeoutcounter>50){//5 seconds
				logger.info("Timeout, idevicescreenshot did not start successfully");
				break;
			}
		}
		if(this.lastmessage.contains("Connect success")){
			isready = true;
		}else{
			isready = false;
		}
	}
	public boolean isReady(){
		return isready;
	}
	public BufferedImage getImage(){
		BufferedImage imgdata = null;
		File imgfile = null;
		String img = this.imagefolder + Util.FILE_SEPARATOR + "image"+counter+".tiff";
		//logger.info(img);
		counter++;
		boolean success = false;
		
		try {
			success = this.getScreenshot(img);
		} catch (IOException e) {
			e.printStackTrace();
			logger.severe("Error reading screenshot data:"+ e.getMessage());
			return null;
		}
		if(!success){
			logger.severe("failed to get screenshot, last message: "+this.lastmessage);
		}
		if(success){
			try {
				imgfile = new File(img);
				if(imgfile.exists()){
					FileInputStream inputstream = new FileInputStream(imgfile);
					byte[] imgdataarray = new byte[(int)imgfile.length()];
					inputstream.read(imgdataarray);
					inputstream.close();
					
					imgdata = ImageHelper.getImageFromByte(imgdataarray);
					
					imgfile.delete();
					
				}else{
					logger.severe("Error: image file not found => "+img);
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.severe("Error reading image file:"+img+"\n"+ e.getMessage());
			}
		}
		
		return imgdata;
	}
	public boolean getScreenshot(String filepath) throws IOException{
		if(!filepath.endsWith("\r\n")){
			filepath = filepath + "\r\n";
			Out("Appended linefeed");
		}
		if(writer == null){
			logger.info("BufferedWriter is null");
			return false;
		}
		//logger.info("create screenshot file: "+filepath);
		this.lastmessage = "";
		writer.write(filepath);
		writer.flush();
		//wait for result
		while(this.lastmessage.length() < 1){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(this.lastmessage.startsWith("OK")){
			return true;
		}
		return false;
	}
	/**
	 * sending shutdown signal to the child process which then callback to ScreenshotManager 
	 * shutDown() function, which finally self-destroy
	 * @throws IOException
	 */
	public void signalShutdown() throws IOException{
		getScreenshot("exit\r\n");
	}
	/**
	 * stop everything and exit
	 */
	public void shutDown(){
		if(procreader != null){
			procreader.interrupt();
			procreader = null;
		}
		if(proc != null){
			proc.destroy();
			proc = null;
		}
		//delete left-over image file in this tmp dir
		if(tmpfolder.listFiles() != null && tmpfolder.listFiles().length > 0){
			 for(File f : tmpfolder.listFiles()){
				 f.delete();
			 }
			 Out("deleted left-over image files.");
		}
		tmpfolder.delete();
		Out("ScreenshotManager.shutDown() finished");
	}

	public void Out(String str){
		//logger.info(str);
	}
	@Override
	public void newMessage(String message) {
		//new message from screenshot reader
		Out(message);
		this.lastmessage = message.trim();
	}

	@Override
	public void willExit() {
		Out("willExit() called");
		//screenshot service will exit now
		this.shutDown();
		
	}
}//end class
