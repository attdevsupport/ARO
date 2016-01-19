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


package com.att.aro.ui.commonui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

import javax.swing.JPanel;

/**
 * Represents a printable panel in ARO. This class implements a printable
 * JPanel.
 */
public class AROPrintablePanel implements Printable {

	private JPanel printablePanel;

	/**
	 * Initializes a new instance of the AROPrintablePanel class using the
	 * specified JComponent printable panel object.
	 * 
	 * @param printablePanel
	 *            The JComponent printable panel object.
	 */
	public AROPrintablePanel(JPanel printablePanel) {
		this.printablePanel = printablePanel;
	}

	/**
	 * Prints the specified page of the contents of the printable panel using
	 * the specified graphics object, page format, and page index. This method
	 * implements the print command of the java.awt.print.Printable interface.
	 * 
	 * @see Printable#print(Graphics, PageFormat, int)
	 */
	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int page)
			throws PrinterException {

		Dimension size = printablePanel.getSize();

		/*
		 * User (0,0) is typically outside the imageable area, so we must
		 * translate by the X and Y values in the PageFormat to avoid clipping
		 */
		Graphics2D graphics2d = (Graphics2D) graphics;
		graphics2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
		double scale = getScaleToFit(pageFormat);
		if (scale != 1.0) {
			graphics2d.scale(scale, scale);
		}
		double imageHeight = pageFormat.getImageableHeight() / scale;
		double clip = page * imageHeight;
		if (size.height > clip) {
			graphics2d.translate(0, -clip);
			graphics2d.setClip(0, (int) clip, (int) (pageFormat.getImageableWidth() / scale),
					(int) imageHeight);
		} else {
			return NO_SUCH_PAGE;
		}

		/* Now print the window and its visible contents */
		printablePanel.printAll(graphics2d);

		/* tell the caller that this page is part of the printed document */
		return PAGE_EXISTS;
	}

	/**
	 * Returns the number of pages that the printable panel contains.
	 * 
	 * @param pageFormat - The page format to use for printing.
	 * 
	 * @return The number of pages that the printable panel contains.
	 */
	public int getPageCount(PageFormat pageFormat) {
		return (int) Math.ceil((double) printablePanel.getSize().height
				/ (pageFormat.getImageableHeight() / getScaleToFit(pageFormat)));
	}

	/**
	 * Returns the scale factor for printing the panel in the specified Page
	 * format.
	 * 
	 * @param pageFormat
	 *            The page format used for printing.
	 * @return The scaling factor.
	 */
	private double getScaleToFit(PageFormat pageFormat) {
		Dimension size = printablePanel.getSize();

		return size.width > pageFormat.getImageableWidth() ? pageFormat.getImageableWidth()
				/ size.width : 1.0;
	}
}
