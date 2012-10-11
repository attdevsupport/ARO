/*
 * Copyright 2012 AT&T
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


package com.att.aro.images;

import java.awt.Image;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;

import com.att.aro.main.ResourceBundleManager;

/**
 * The Images enumeration specifies constant values that describe the types of
 * Images and ImageIcons. The constant values are used with the defined methods
 * to retrieve the Images and ImageIcons through a resource bundle lookup.
 */
public enum Images {
	/**
	 * The Icon image key.
	 */
	ICON("icon"),
	/**
	 * The Demagnify image key. The associated image is used to identify that
	 * magnification on a chart has been disabled.
	 */
	DEMAGNIFY("demagnify"),
	/**
	 * The Magnify image key. The associated image is used to identify that
	 * magnification on a chart has been enabled.
	 */
	MAGNIFY("magnify"),
	/**
	 * The Save image key.
	 */
	SAVE("save"),
	/**
	 * The ARO Logo image key.
	 */
	AROLOGO("arologo"),
	/**
	 * The Brand image key.
	 */
	BRAND("brand"),
	/**
	 * The Pass image key. The associated image is used to identify when a Best
	 * Practices test has passed.
	 */
	BP_PASS_DARK("bpPassDark"),
	/**
	 * The Fail image key. The associated image is used to identify when a Best
	 * Practices test has failed.
	 */
	BP_FAIL_DARK("bpFailDark"),
	/**
	 * The Manual image key. The associated image is used to identify when a
	 * Best Practices test has been run manually.
	 */
	BP_MANUAL("bpManual"),
	/**
	 * The Self Test image key. The associated image is used to identify when a Best
	 * Practices test has passed.
	 */
	BP_SELFTEST_TRIGGERED("bpSeftTestTriggered"),
	/**
	 * The default video player image.
	 */
	NO_VIDEO_AVAILABLE("noVideoAvailable"),
	/**
	 * The Header image key.
	 */
	HEADER_ICON("headerIcon"),
	/**
	 * The Blue header image key.
	 */
	BLUE_HEADER("blueHeader"),
	/**
	 * The Grey header image key indicating that a test has not been run.
	 */
	TEST_NOT_RUN_HEADER("greyHeader"),
	/**
	 * The Green header image key indicating that a test has passed.
	 */
	TEST_PASS_HEADER("greenHeader"),
	/**
	 * The Red header image key indicating that a test has failed.
	 */
	TEST_FAIL_HEADER("redHeader"),
	/**
	 * The Background image key.
	 */
	BACKGROUND("background"),
	/**
	 * The Divider image key.
	 */
	DIVIDER("divider"),
	/**
	 * The Export button image key.
	 */
	EXPORT_BTN("exportBtn"),
	/**
	 * The Green recording image key.
	 */
	GREEN_RECORDING_INDICATOR("greenRecordingIndicator"),
	/**
	 * The Yellow recording image key.
	 */
	YELLOW_RECORDING_INDICATOR("yellowRecordingIndicator"),
	/**
	 * The Red recording image key.
	 */
	RED_RECORDING_INDICATOR("redRecordingIndicator");

	private static final String PREFIX = "Image.";
	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final String basePath = rb.getString("ImageBasePath");
	private final String resourceKey; // resource bundle key
	private ImageIcon imageIcon = null;

	/**
	 * Private constructor for enum
	 * 
	 * @param resourceKey
	 *            key of the image.
	 */
	private Images(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	/**
	 * Gets the ImageIcon object that is associated with the current enumeration
	 * entry.
	 * 
	 * @return The ImageIcon object that is associated with the current key.
	 */
	public ImageIcon getIcon() {
		if (imageIcon == null) {
			this.imageIcon = new ImageIcon(getClass().getResource(
					basePath + rb.getString(PREFIX + this.resourceKey)));
		}
		return imageIcon;
	}

	/**
	 * Gets the Image object that is associated with the current enumeration
	 * entry.
	 * 
	 * @return The Image object that is associated with the current key.
	 */
	public Image getImage() {
		return getIcon().getImage();
	}

}
