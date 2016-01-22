/*
 * Copyright 2016 AT&T
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

package com.att.aro.datacollector.ioscollector.utilities;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.att.aro.core.ILogger;
import com.att.aro.core.impl.LoggerImpl;
import com.att.aro.core.util.ImageHelper;

public class Tiff2JpgUtil {

	private static ILogger log = new LoggerImpl("Tiff2JpgUtil");
	
	public static ByteArrayOutputStream tiff2Jpg(String inputFile) {
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			BufferedImage image = tiff2bufferedImage(inputFile);
			image = convert(image, BufferedImage.TYPE_INT_RGB);
			ImageIO.write(image, "jpg", byteArrayOutputStream);
		} catch (Exception e) {
			log.debug("Exception:", e);
		}
		return byteArrayOutputStream;
	}

	public static BufferedImage tiff2bufferedImage(String tiffFile)
			throws IOException {
		FileInputStream inputstream = new FileInputStream(tiffFile);
		byte[] imgdataarray = new byte[(int) new File(tiffFile).length()];
		inputstream.read(imgdataarray);
		inputstream.close();

		BufferedImage imgdata = ImageHelper.getImageFromByte(imgdataarray);
		return imgdata;
	}

	public static BufferedImage convert(BufferedImage src, int bufImgType) {
		BufferedImage img = new BufferedImage(src.getWidth(), src.getHeight(),
				bufImgType);
		Graphics2D g2d = img.createGraphics();
		g2d.drawImage(src, 0, 0, null);
		g2d.dispose();
		return img;
	}
}