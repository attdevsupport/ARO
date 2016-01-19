/*
 *  Copyright 2015 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.core.video.impl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.video.IVideoWriter;
import com.att.aro.core.video.pojo.QuickTimeOutputStream;
import com.att.aro.core.video.pojo.QuickTimeOutputStream.VideoFormat;

/**
 * Provides access to a VideoOutputStream to create a movie file, with each image written as a video
 * frame(s). The video frames (images) can be encoded with the RAW, JPG, or PNG
 * image data format, but all frames must use the same format. When the JPG
 * format is used, each frame can have an individual encoding quality.
 * 
 * 
 * @author Barry Nelson
 */
public class VideoWriterImpl implements IVideoWriter{

	@InjectLogger
	protected static ILogger logger;
	
	private IFileManager fileManager;

	private QuickTimeOutputStream qtOutputStream = null;

	private VideoFormat format = null;
	private float compressionQuality = 0;
	private int timeUnits = 0;

	private File videoOutputFile = null;
	
	@Autowired
	public void setFileManager(IFileManager fileManager){
		this.fileManager = fileManager;
	}

	/**
	 * Guarantees 0.0 will not be used for timeUnits and compressionQuality
	 */
	private void checkSetDefaults() {
		if (timeUnits == 0){
			timeUnits = 10;
		}
		if (compressionQuality == 0){
			compressionQuality = 0.2f;
		}
	}

	/**
	 * Create videoOutputStream with a format
	 * 
	 * @param videoOutputFile
	 * @param format
	 * @throws IOException
	 */
	@Override
	public void init(String videoOutputFile, VideoFormat format) throws IOException {
		// check and set defaults
		checkSetDefaults();
		init(videoOutputFile, format, compressionQuality, timeUnits);
	}

	/**
	 * Create videoOutputStream with a format 
	 * 
	 * @param videoOutputFile
	 * @param format
	 * @param compressionQuality 0.0 to 1.0f
	 * @param timeScale number of frames per second
	 * @throws IOException
	 */
	@Override
	public void init(String videoOutputFile, VideoFormat format, float compressionQuality, int timeUnits) throws IOException {

		checkSetDefaults();
		
		setTimeUnits(timeUnits);
		setCompressionQuality(compressionQuality);
		setFormat(format);
		setVideoOutputFile(fileManager.createFile(videoOutputFile));

		qtOutputStream = new QuickTimeOutputStream(this.videoOutputFile, format);
		setVideoCompressionQuality(compressionQuality);
		setTimeScale(timeUnits);
	}

	/**
	 * Sets the compression quality of the video to a value between 0 and 1. A
	 * compression quality value of 0 stands for
	 * "high compression is important", while a value of 1 stands for
	 * "high image quality is important". Changing this value affects frames
	 * which are subsequently written to the QuickTimeOutputStreamImpl. Frames which
	 * have already been written are not changed. This value has no effect on
	 * videos encoded with the PNG format. The default video compression quality
	 * value is 0.9.
	 * 
	 * 
	 * @param newValue
	 *            The new video compression quality value.
	 */
	@Override
	public void setVideoCompressionQuality(float value) {
		qtOutputStream.setVideoCompressionQuality(value);
	}

	/**
	 * Sets the time scale for the video to the specified value. The value
	 * represents the number of time units that pass per second in the time
	 * coordinate system of the video. The default value is 600.
	 * 
	 * @param newValue
	 *            The new time scale value.
	 */
	@Override
	public void setTimeScale(int value) {
		qtOutputStream.setTimeScale(value);
	}

	/**
	 * Writes a frame consisting of the specified image, to the video output
	 * stream, for the specified duration. If the dimension of the video has not
	 * been explicitly set using the setVideoDimension method, it is derived
	 * from the first buffered image added to the QuickTimeOutputStreamImpl.
	 * 
	 * 
	 * @param image
	 *            The frame image.
	 * @param duration
	 *             The number of frames for the image to occupy.
	 * 
	 * @throws IllegalArgumentException
	 *             If the duration is less than 1, or if the dimension of the
	 *             frame does not match the dimension of the video track.
	 * @throws IOException
	 *             If writing the image failed.
	 */
	@Override
	public void writeFrame(BufferedImage bufferedImage, int duration) throws IOException {
		qtOutputStream.writeFrame(bufferedImage, duration);
	}

	/**
	 * Closes the output file and the output stream.
	 * 
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	@Override
	public void close() throws IOException {
		try {
			qtOutputStream.close();
		} catch (IOException e) {
			logger.warn("Exception closing video output stream"+ e.getMessage());
		}
	}

	/**
	 * returns VideoFormat, currently RAW, JPG, PNG
	 */
	public VideoFormat getFormat() {
		return format;
	}

	public void setFormat(VideoFormat format) {
		this.format = format;
	}

	public File getVideoOutputFile() {
		return videoOutputFile;
	}

	public void setVideoOutputFile(File videoOutputFile) {
		this.videoOutputFile = videoOutputFile;
	}

	public float getCompressionQuality() {
		return compressionQuality;
	}

	public void setCompressionQuality(float compressionQuality) {
		this.compressionQuality = compressionQuality;
	}

	public int getTimeUnits() {
		return timeUnits;
	}

	/**
	 * Used to set the number of frames per second
	 * 
	 * @param timeUnits
	 */
	public void setTimeUnits(int timeUnits) {
		this.timeUnits = timeUnits;
	}
}
