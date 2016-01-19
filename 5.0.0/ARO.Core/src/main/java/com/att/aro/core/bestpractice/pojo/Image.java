/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.bestpractice.pojo;


/**
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
	public Boolean isLargerThan(Image img) {

		// this image fits into the other image horizontally or vertically
		if (((this.height <= img.height) && (this.width <= img.width)) || ((this.width <= img.height) && (this.height <= img.width))) {
			return false;
		}

		// width of this image does not fit vertically or horizontally into
		// the other image
		if ((this.width > img.width) && (this.width > img.height)) {
			return true;
		}

		// height of this image does not fit vertically or horizontally into
		// the other image
		if ((this.height > img.height) && (this.height > img.width)) {
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
		StringBuilder strBr = new StringBuilder(Integer.toString(this.width));
		strBr.append(" x ");
		strBr.append(this.height);
		return strBr.toString();
	}

}
