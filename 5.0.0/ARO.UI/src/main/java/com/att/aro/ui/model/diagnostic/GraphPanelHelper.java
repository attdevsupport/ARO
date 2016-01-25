/**
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
package com.att.aro.ui.model.diagnostic;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.filechooser.FileFilter;

import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;

import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.util.ImageHelper;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.commonui.MessageDialogFactory;
import com.att.aro.ui.model.ExtensionFileFilter;
import com.att.aro.ui.utils.ResourceBundleHelper;
import com.att.aro.ui.view.diagnostictab.ChartPlotOptions;

public class GraphPanelHelper{
 	
 	private static final int MINOR_TICK_COUNT_10 = 10;
	private static final int MINOR_TICK_COUNT_5 = 5;
	private static final int MINOR_TICK_COUNT_4 = 4;
 
	private static final int FIVE      = 5;
	private static final int FIVE0     = 50;
	private static final int FIVE00    = 500;
	private static final int FIVE000   = 5000;
	private static final int FIVE0000  = 50000;
	private static final int FIVE00000 = 500000;
	
	private static final int TVENTYFIVE     = 25;
	private static final int TVENTYFIVE0    = 250;
	private static final int TVENTYFIVE00   = 2500;
	private static final int TVENTYFIVE000  = 25000;
	private static final int TVENTYFIVE0000 = 250000;
	
	private static final int ONE = 1;
	private static final int ONE0 = 10;
	private static final int ONE00 = 100;
	private static final int ONE000 = 1000;
	private static final int ONE0000 = 10000;
	private static final int ONE00000 = 100000;
	
	private static final int TWO = 2;
	
	private static final double POINT5  = 0.5;
	private static final double POINT25 = .25;
	private static final double POINT1  = .1;
	private static final double POINT05 = .05;
	private static final double POINT01 = .01;
	
	private  NumberFormat FORMAT = new DecimalFormat();

	private  List<ChartPlotOptions> plotOrder = new ArrayList<ChartPlotOptions>(ChartPlotOptions.values().length);
	private  TickUnits tickUnits = new TickUnits();
	
	IFileManager fileManager = ContextAware.getAROConfigContext().getBean(IFileManager.class);
	

	public GraphPanelHelper(){
		
		plotOrder.add(ChartPlotOptions.GPS);
		plotOrder.add(ChartPlotOptions.RADIO);
		plotOrder.add(ChartPlotOptions.BLUETOOTH);
		plotOrder.add(ChartPlotOptions.CAMERA);
		plotOrder.add(ChartPlotOptions.SCREEN);
		plotOrder.add(ChartPlotOptions.BATTERY);
		plotOrder.add(ChartPlotOptions.WAKELOCK);
		plotOrder.add(ChartPlotOptions.WIFI);
		plotOrder.add(ChartPlotOptions.ALARM);
		plotOrder.add(ChartPlotOptions.NETWORK_TYPE);
		plotOrder.add(ChartPlotOptions.THROUGHPUT);
		plotOrder.add(ChartPlotOptions.UL_PACKETS);
		plotOrder.add(ChartPlotOptions.DL_PACKETS);
		plotOrder.add(ChartPlotOptions.BURSTS);
		plotOrder.add(ChartPlotOptions.USER_INPUT);
		plotOrder.add(ChartPlotOptions.RRC);
		plotOrder.add(ChartPlotOptions.CPU);
		
		tickUnits.add(new NumberTickUnit(FIVE00000, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(TVENTYFIVE0000, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(ONE00000, FORMAT, MINOR_TICK_COUNT_10));

		tickUnits.add(new NumberTickUnit(FIVE0000, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(TVENTYFIVE000, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(ONE0000, FORMAT, MINOR_TICK_COUNT_10));
		
		tickUnits.add(new NumberTickUnit(FIVE000, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(TVENTYFIVE00, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(ONE000, FORMAT, MINOR_TICK_COUNT_10));
		
		tickUnits.add(new NumberTickUnit(FIVE00, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(TVENTYFIVE0, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(ONE00, FORMAT, MINOR_TICK_COUNT_10));
		
		tickUnits.add(new NumberTickUnit(FIVE0, FORMAT, MINOR_TICK_COUNT_10));
		tickUnits.add(new NumberTickUnit(TVENTYFIVE, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(ONE0, FORMAT, MINOR_TICK_COUNT_10));
		
		tickUnits.add(new NumberTickUnit(FIVE, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(TWO, FORMAT, MINOR_TICK_COUNT_4));
		tickUnits.add(new NumberTickUnit(ONE, FORMAT, MINOR_TICK_COUNT_10));
		
		tickUnits.add(new NumberTickUnit(POINT5, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(POINT25, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(POINT1, FORMAT, MINOR_TICK_COUNT_10));
		tickUnits.add(new NumberTickUnit(POINT05, FORMAT, MINOR_TICK_COUNT_5));
		tickUnits.add(new NumberTickUnit(POINT01, FORMAT, MINOR_TICK_COUNT_10));

	}
	
	public List<ChartPlotOptions> getPlotOrder() {
 		return plotOrder;
	}
	
	public TickUnits getTickUnits() {

		return tickUnits;
	}
	
	public void SaveImageAs(JViewport pane,String graphPanelSaveDirectory ){
		
 
		JFileChooser fc = new JFileChooser(graphPanelSaveDirectory);

		// Set up file types
		String[] fileTypesJPG = new String[2];
		String fileDisplayTypeJPG = ResourceBundleHelper.getMessageString("fileChooser.contentDisplayType.jpeg");
		fileTypesJPG[0] = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpeg");
		fileTypesJPG[1] = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg");
		FileFilter filterJPG = new ExtensionFileFilter(fileDisplayTypeJPG, fileTypesJPG);

		fc.addChoosableFileFilter(fc.getAcceptAllFileFilter());
		String[] fileTypesPng = new String[1];
		String fileDisplayTypePng = ResourceBundleHelper.getMessageString("fileChooser.contentDisplayType.png");
		fileTypesPng[0] = ResourceBundleHelper.getMessageString("fileChooser.contentType.png");
		FileFilter filterPng = new ExtensionFileFilter(fileDisplayTypePng, fileTypesPng);
		fc.addChoosableFileFilter(filterPng);
		fc.setFileFilter(filterJPG);
		File plotImageFile = null;

		boolean bSavedOrCancelled = false;
		while (!bSavedOrCancelled) {
			if (fc.showSaveDialog(pane) == JFileChooser.APPROVE_OPTION) {
				String strFile = fc.getSelectedFile().toString();
				String strFileLowerCase = strFile.toLowerCase();
				String fileDesc = fc.getFileFilter().getDescription();
				String fileType = ResourceBundleHelper.getMessageString("fileChooser.contentType.jpg");
				if ((fileDesc.equalsIgnoreCase(ResourceBundleHelper.getMessageString("fileChooser.contentDisplayType.png")) || strFileLowerCase
						.endsWith(ResourceBundleHelper.getMessageString("fileType.filters.dot")
								+ fileTypesPng[0].toLowerCase()))) {
					fileType = fileTypesPng[0];
				}
				if (strFile.length() > 0) {
					// Save current directory
					graphPanelSaveDirectory = fc.getCurrentDirectory().getPath();

					if ((fileType != null) && (fileType.length() > 0)) {
						String fileTypeLowerCaseWithDot = ResourceBundleHelper.getMessageString("fileType.filters.dot")
								+ fileType.toLowerCase();
						if (!strFileLowerCase.endsWith(fileTypeLowerCaseWithDot)) {
							strFile += ResourceBundleHelper.getMessageString("fileType.filters.dot") + fileType;
						}
					}
					plotImageFile = new File(strFile);
					boolean bAttemptToWriteToFile = true;
					if (plotImageFile.exists()) {
						if ( MessageDialogFactory .showConfirmDialog(pane, MessageFormat.format(
								ResourceBundleHelper.getMessageString("fileChooser.fileExists"),
								plotImageFile.getAbsolutePath()), 
								ResourceBundleHelper.getMessageString("fileChooser.confirm"), JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							bAttemptToWriteToFile = false;
						}
					}
					if (bAttemptToWriteToFile) {
						try {
							if (fileType.equalsIgnoreCase(fileTypesPng[0])) {
								BufferedImage bufImage = ImageHelper.createImage(pane.getBounds().width,pane.getBounds().height);
								Graphics2D g = bufImage.createGraphics();
								pane.paint(g);
								ImageIO.write(bufImage, "png", plotImageFile);
							} else {
								BufferedImage bufImage = ImageHelper.createImage(pane.getBounds().width,pane.getBounds().height);
								Graphics2D g = bufImage.createGraphics();
								pane.paint(g);
								ImageIO.write(bufImage, "jpg", plotImageFile);
							}
							bSavedOrCancelled = true;
						} catch (IOException e) {
							 MessageDialogFactory .showMessageDialog(
									pane,
									ResourceBundleHelper.getMessageString("fileChooser.errorWritingToFile"
											+ plotImageFile.toString()));
						}
					}
				}
			} else {
				bSavedOrCancelled = true;
			}
		}
	}
	
 }
