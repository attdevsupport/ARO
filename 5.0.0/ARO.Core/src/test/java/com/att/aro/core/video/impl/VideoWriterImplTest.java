/**
 * 
 */
package com.att.aro.core.video.impl;

import static org.junit.Assert.assertTrue;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.android.ddmlib.RawImage;
import com.att.aro.core.BaseTest;
import com.att.aro.core.video.IVideoWriter;
import com.att.aro.core.video.pojo.QuickTimeOutputStream;

/**
 * VideoWriterImplTest performs junit tests on VideoWriterImpl
 * 
 * @author Barry Nelson
 *
 */
public class VideoWriterImplTest extends BaseTest {

	VideoWriterImpl videoWriter;
	
	QuickTimeOutputStream qtOutputStream;
	
	String testOutputFilePath = "ouputFile.mov";

	@Before
	public void setUp() {
		videoWriter = (VideoWriterImpl)context.getBean(IVideoWriter.class);
		MockitoAnnotations.initMocks(this);
	}
	
	@After
	public void tearDown() {
		File file = new File(testOutputFilePath);
		if (file.exists()){
			file.delete();
		}
	}
	
	
	@Test
	public void mockery() {
		
		
		QuickTimeOutputStream qts = Mockito.mock(QuickTimeOutputStream.class);
		Mockito.doNothing().when(qts).setTimeScale(Mockito.anyInt());
	}
	
	@Test
	public void test() throws IOException{

		BufferedImage image = null; //new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

		videoWriter.init(testOutputFilePath, QuickTimeOutputStream.VideoFormat.JPG, 1.0f, 11);
		image = getBufferedImage();
		image.createGraphics();
		image.setRGB(05, 5, 45);
		videoWriter.writeFrame( image , 60);
		image.setRGB(15, 5, 255);
		videoWriter.writeFrame( image , 10);
		image.setRGB(25, 5, 127);
		videoWriter.writeFrame( image , 10);
		image.setRGB(35, 5, 127);
		videoWriter.writeFrame( image , 10);
		

		assertTrue(videoWriter.getVideoOutputFile().getName().equals(testOutputFilePath));
		assertTrue(videoWriter.getFormat().name().equals("JPG"));
		assertTrue(videoWriter.getCompressionQuality() == 1.0f);
		assertTrue(videoWriter.getTimeUnits() == 11);
		
		videoWriter.close();
		File file = new File(testOutputFilePath);
		assertTrue(file.exists());
		
	}
	
	@Test
	public void testPreSetValues() throws IOException{

		BufferedImage image = null; //new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

		// these parameters being set before the init(...)
		videoWriter.setTimeUnits(0);
		videoWriter.setCompressionQuality(0);
		
		videoWriter.init(testOutputFilePath, QuickTimeOutputStream.VideoFormat.JPG);
		image = getBufferedImage();
		image.createGraphics();
		image.setRGB(5, 5, 45);
		videoWriter.writeFrame( image , 1);
		image.setRGB(5, 5, 255);
		videoWriter.writeFrame( image , 1);
		image.setRGB(15, 15, 127);
		videoWriter.writeFrame( image , 20);
		image.setRGB(18, 1, 127);
		videoWriter.writeFrame( image , 5);
		
		assertTrue(videoWriter.getVideoOutputFile().getName().equals(testOutputFilePath));
		assertTrue(videoWriter.getFormat().name().equals("JPG"));
		assertTrue(videoWriter.getCompressionQuality() == 0.2f);
		assertTrue(videoWriter.getTimeUnits() == 10);
		
		videoWriter.close();
		
		File file = new File(testOutputFilePath);
		assertTrue(file.exists());
		
	}
	

	@Test
	public void testPreSetValues2() throws IOException{

		BufferedImage image = null; //new BufferedImage(20, 20, BufferedImage.TYPE_INT_RGB);

		// these parameters being set before the init(...)
		videoWriter.setTimeUnits(5);
		videoWriter.setCompressionQuality(0.9f);
		
		videoWriter.init(testOutputFilePath, QuickTimeOutputStream.VideoFormat.JPG);
		image = getBufferedImage();
		image.createGraphics();
		image.setRGB(5, 5, 45);
		videoWriter.writeFrame( image , 1);
		image.setRGB(5, 5, 255);
		videoWriter.writeFrame( image , 1);
		image.setRGB(15, 15, 127);
		videoWriter.writeFrame( image , 20);
		image.setRGB(18, 1, 127);
		videoWriter.writeFrame( image , 5);
		
		assertTrue(true);
		videoWriter.close();
		File file = new File(testOutputFilePath);
		assertTrue(file.exists());
		
	}
	

	/**
	 * provides a colorful base image
	 * @return
	 */
	public BufferedImage getBufferedImage() {
		int width = 40;
		int height = 40;
		
		int[] data = new int[width * height];
		int i = 0;
		for (int y = 0; y < height; y++) {
			int red = (y * 255) / (height - 1);
			for (int x = 0; x < width; x++) {
				int green = (x * 255) / (width - 1);
				int blue = 128;
				data[i++] = (red << 16) | (green << 8) | blue;
			}
		}
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		image.setRGB(0, 0, width, height, data, 0, width);
		
		return image;
	}
	
}
