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
package com.att.aro.ui.model;


/**
 * @author Harikrishna Yaramachu
 *
 * Represents image used by Image Size Best Practice.
 */
public class Image {

	private int height;
	private int width;

	/**
	 * Represents image used by Image Size Best Practice.
	 * 
	 * @param width
	 *            width of the image
	 * @param height
	 *            height of the image
	 */
	public Image(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Returns height of the image.
	 * 
	 * @return height of the image
	 */
	public int getHeight() {
		return this.height;
	}

	/**
	 * Returns with of the image.
	 * 
	 * @return width of the image
	 */
	public int getWidth() {
		return this.width;
	}

	/**
	 * Compares image sizes considering that the images can have different
	 * orientations.
	 * 
	 * @return true if this image is larger, otherwise returns false
	 * 
	 */
	public Boolean isLargerThan(Image overSizeImg) {

		// this image fits into the other image horizontally or vertically
		boolean smallheight = this.height <= overSizeImg.height && this.width <= overSizeImg.width;
		boolean smallwidth = this.width <= overSizeImg.height && this.height <= overSizeImg.width;
		if (smallheight || smallwidth) {
			return false;
		}

		// width of this image does not fit vertically or horizontally into
		// the other image
		if (this.width > overSizeImg.width && this.width > overSizeImg.height) {
			return true;
		}

		// height of this image does not fit vertically or horizontally into
		// the other image
		if (this.height > overSizeImg.height && this.height > overSizeImg.width) {
			return true;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sBuilder = new StringBuilder(Integer.toString(this.width));
		sBuilder.append(" x ");
		sBuilder.append(this.height);
		return sBuilder.toString();
	}

}
