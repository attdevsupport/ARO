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
package com.att.aro.core.video;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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
public interface IVideoWriter {

	//TODO allow for multiple instances
	
	/**
	 * Create videoOutputStream with a format
	 * 
	 * @param videoOutputFile
	 * @param format
	 * @throws IOException
	 */
	void init(String videoOutputFile, VideoFormat format) throws IOException;
	
	/**
	 * Create videoOutputStream with a format 
	 * 
	 * @param videoOutputFile
	 * @param format
	 * @param compressionQuality 0.0 to 1.0f
	 * @param timeScale number of frames per second
	 * @throws IOException
	 */
	void init(String videoOutputFile, VideoFormat format, float compressionQuality, int timeScale) throws IOException;

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
	void setVideoCompressionQuality(float newValue);
	
	/**
	 * Sets the time scale for the video to the specified value. The value
	 * represents the number of time units that pass per second in the time
	 * coordinate system of the video. The default value is 600.
	 * 
	 * @param newValue
	 *            The new time scale value.
	 */
	void setTimeScale(int newValue);
	
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
	 *            The number of frames for the image to occupy.
	 * 
	 * @throws IllegalArgumentException
	 *             If the duration is less than 1, or if the dimension of the
	 *             frame does not match the dimension of the video track.
	 * @throws IOException
	 *             If writing the image failed.
	 */
	void writeFrame(BufferedImage image, int duration) throws IOException;
	
	/**
	 * Closes the output file and the output stream.
	 * 
	 * @exception IOException
	 *                if an I/O error has occurred
	 */
	void close() throws IOException;
	
	VideoFormat getFormat();

	void setFormat(VideoFormat format);

	File getVideoOutputFile();

	void setVideoOutputFile(File videoOutputFile);

	float getCompressionQuality();

	void setCompressionQuality(float compressionQuality);

	int getTimeUnits();

	/**
	 * Used to set the number of frames per second
	 * 
	 * @param timeUnits
	 */
	void setTimeUnits(int timeUnits);

}
