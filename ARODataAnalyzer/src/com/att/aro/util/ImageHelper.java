package com.att.aro.util;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;

public class ImageHelper {
	private static Logger logger = Logger.getLogger(ImageHelper.class.getName());
	/**
	 * resize image to fit target size
	 * @param image
	 * @param targetWidth
	 * @param targetHeight
	 * @return
	 */
	public static BufferedImage resize(BufferedImage image, int targetWidth, int targetHeight){
		int imageType = BufferedImage.TYPE_INT_ARGB;
		
		BufferedImage result = image;
		
		BufferedImage tmp = new BufferedImage(targetWidth, targetHeight, imageType);
		Object hint = RenderingHints.VALUE_INTERPOLATION_BILINEAR;
		Graphics2D g2 = tmp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, hint);
        g2.drawImage(image, 0, 0, targetWidth, targetHeight, 0, 0, image.getWidth(), image.getHeight(), null);
        g2.dispose();
        
        result = tmp;
		return result;
	}
	public static BufferedImage getImageFromByte(byte[] array) throws IOException{
		InputStream instream = new ByteArrayInputStream(array);
		ImageDecoder dec = ImageCodec.createImageDecoder("tiff", instream, null);
		RenderedImage rendering = new NullOpImage(dec.decodeAsRenderedImage(0),null,null,OpImage.OP_IO_BOUND);
		BufferedImage image = convertRenderedImage(rendering);
		return image;
	}
	public static BufferedImage convertRenderedImage(RenderedImage img) {
	    if (img instanceof BufferedImage) {
	        return (BufferedImage)img;  
	    }   
	    ColorModel cm = img.getColorModel();
	    int width = img.getWidth();
	    int height = img.getHeight();
	    WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
	    boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
	    Hashtable properties = new Hashtable();
	    String[] keys = img.getPropertyNames();
	    if (keys!=null) {
	        for (int i = 0; i < keys.length; i++) {
	            properties.put(keys[i], img.getProperty(keys[i]));
	        }
	    }
	    BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
	    img.copyData(raster);
	    return result;
	}
	public static BufferedImage rorateImage(BufferedImage image, int angle){
		
	    AffineTransform tx = new AffineTransform();
	    int x = image.getWidth() / 2;
	    int y = image.getHeight() / 2;
	    tx.translate(y, x);
	    tx.rotate(Math.toRadians(angle));
	    tx.translate(-x, -y);
	    AffineTransformOp op = new AffineTransformOp(tx,
	        AffineTransformOp.TYPE_BILINEAR);
	    image = op.filter(image, null);
	    return image;
	}
	public static BufferedImage rotate90DegreeRight(BufferedImage img) {
	    int w = img.getWidth();
	    int h = img.getHeight();

	    BufferedImage rot = new BufferedImage(h, w, BufferedImage.TYPE_INT_RGB);

	    double theta = Math.PI / 2;

	    AffineTransform xform = new AffineTransform();
	    xform.translate(0.5*h, 0.5*w);
	    xform.rotate(theta);
	    xform.translate(-0.5*w, -0.5*h);

	    Graphics2D g = (Graphics2D) rot.createGraphics();
	    g.drawImage(img, xform, null);
	    g.dispose();

	    return rot;
	}
	
}//end class
