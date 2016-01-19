package com.att.aro.core.util;

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;

import org.junit.Test;
import static org.junit.Assert.*;

import com.att.aro.core.util.ImageHelper;

public class ImageHelperTest {
	//small tiff image that was converted to byte array
	//Size: 600 bytes
	//Dimension: 10x10 pixel
	byte[] imagedata = new byte[]{
    		73,73,42,0,120,0,0,0,-128,57,31,-128,16,3,-4,6,22,0,66,92,1,7,-16,0,6,-17,10,-127,-33,-32,7,80,76,0,-3,2,59,64,-79,112,35,-92,36,121,62,-125,93,111,-16,64,37,-28,-10,86,-99,-46,-60,-9,64,16,16,-77,35,59,-116,-83,18,-70,17,-60,22,117,18,-108,15,18,-53,52,-122,-103,1,-71,20,12,50,10,-107,8,103,9,45,-47,-114,-107,-128,68,30,69,102,-119,
    		-33,-51,50,50,-66,18,0,104,60,31,96,16,40,40,92,-4,0,0,96,32,21,0,0,1,3,0,1,0,0,0,10,0,0,0,1,1,3,0,1,0,0,0,10,0,0,0,2,1,3,0,1,0,0,0,8,0,0,0,3,1,3,0,1,0,0,0,5,0,0,0,6,1,3,0,1,0,0,0,1,0,0,0,10,1,3,0,1,0,0,0,1,0,0,0,13,1,2,0,124,0,
    		0,0,-54,1,0,0,14,1,2,0,18,0,0,0,70,2,0,0,17,1,4,0,1,0,0,0,8,0,0,0,18,1,3,0,1,0,0,0,1,0,0,0,21,1,3,0,1,0,0,0,1,0,0,0,22,1,3,0,1,0,0,0,51,3,0,0,23,1,4,0,1,0,0,0,112,0,0,0,26,1,5,0,1,0,0,0,122,1,0,0,27,1,5,0,1,0,0,0,-126,1,
    		0,0,28,1,3,0,1,0,0,0,1,0,0,0,40,1,3,0,1,0,0,0,3,0,0,0,41,1,3,0,2,0,0,0,0,0,1,0,61,1,3,0,1,0,0,0,2,0,0,0,62,1,5,0,2,0,0,0,-70,1,0,0,63,1,5,0,6,0,0,0,-118,1,0,0,0,0,0,0,-1,-1,-1,-1,96,-33,42,2,-1,-1,-1,-1,96,-33,42,2,0,10,-41,-93,-1,-1,
    		-1,-1,-128,-31,122,84,-1,-1,-1,-1,0,-51,-52,76,-1,-1,-1,-1,0,-102,-103,-103,-1,-1,-1,-1,-128,102,102,38,-1,-1,-1,-1,-16,40,92,15,-1,-1,-1,-1,-128,27,13,80,-1,-1,-1,-1,0,88,57,84,-1,-1,-1,-1,47,115,114,118,47,119,119,119,47,118,104,111,115,116,115,47,111,110,108,105,110,101,45,99,111,110,118,101,114,116,46,99,111,109,47,115,97,118,101,47,113,117,
    		101,117,101,100,47,48,47,56,47,97,47,48,56,97,100,53,56,99,57,100,55,99,56,50,97,52,102,53,102,101,49,97,54,98,97,51,100,100,102,98,101,54,99,47,105,110,116,101,114,109,101,100,105,97,116,101,49,47,111,95,98,51,98,50,48,97,100,97,48,99,48,101,49,51,57,52,46,116,105,102,102,0,67,114,101,97,116,101,100,32,119,105,116,104,32,71,73,77,80,0	
    };
	@Test
	public void testGetImageFromByte() throws IOException{
		BufferedImage image = ImageHelper.getImageFromByte(imagedata);
		assertEquals(10, image.getWidth());
		assertEquals(10,  image.getWidth());
	}
	@Test
	public void resizeTest(){
		BufferedImage img = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
		BufferedImage newimg = ImageHelper.resize(img, 50, 40);
		assertEquals(50, newimg.getWidth());
		assertEquals(40, newimg.getHeight());
	}
	@Test
	public void rorateImageTest(){
		BufferedImage img = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
		BufferedImage newimg = ImageHelper.rorateImage(img, 90);
		assertEquals(128, newimg.getWidth());
		assertEquals(256, newimg.getHeight());
	}
	@Test
	public void rorate90ImageTest(){
		BufferedImage img = new BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB);
		BufferedImage newimg = ImageHelper.rotate90DegreeRight(img);
		assertEquals(128, newimg.getWidth());
		assertEquals(256, newimg.getHeight());
	}
	@Test
	public void convertRenderedImageTest(){
		RenderedImage renderimage = new BufferedImage(100,100,BufferedImage.TYPE_INT_ARGB);
		BufferedImage newimage = ImageHelper.convertRenderedImage(renderimage);
		assertTrue(newimage instanceof BufferedImage);
	}
	
	@Test
	public void createImageTest(){
		BufferedImage newimage = ImageHelper.createImage(10, 10);
		assertEquals(10, newimage.getWidth());
		assertEquals(10,  newimage.getWidth());

	}
}
