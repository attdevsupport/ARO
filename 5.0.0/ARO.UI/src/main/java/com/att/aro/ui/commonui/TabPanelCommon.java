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
package com.att.aro.ui.commonui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import com.att.aro.ui.exception.AROUIPanelException;
import com.att.aro.ui.utils.ResourceBundleHelper;

/**
 * This is encapsulating an easy to specify and populate JLabels within a JPanel.
 * The JPanel is a category of an ARO Analyzer tab.
 * 
 * @author Nathan F Syfrig
 *
 */
public class TabPanelCommon {
	private final List<JPanel> dataPanels = new ArrayList<JPanel>();
	private static final Font LABEL_FONT = new Font("TEXT_FONT", Font.BOLD, 12);
	private static final Font TEXT_FONT = new Font("TEXT_FONT", Font.PLAIN, 12);
	private static final Font TITLE_FONT = new Font("HeaderFont", Font.BOLD, 18);
	private static final Font HEADER_FONT = new Font("HeaderFont", Font.BOLD, 16);
	private static final Font SUBHEADER_FONT = new Font("HeaderFont", Font.BOLD, 14);

	private final Map<String, JLabel> labelContents = new HashMap<String, JLabel>();
	private final Map<String, JLabel> labelContents2;
	private final Map<String, JLabel> labels = new HashMap<String, JLabel>();
	private final ResourceBundle resource;
	private int currentDataPanel = 0;

	public TabPanelCommon(boolean use2Contents) {
		resource = ResourceBundleHelper.getDefaultBundle();
		labelContents2 = use2Contents ? new HashMap<String, JLabel>() : null;
	}
	public TabPanelCommon() {this(false);}
	public TabPanelCommon(ResourceBundle resource, boolean use2Contents) {
		this.resource = resource;
		labelContents2 = use2Contents ? new HashMap<String, JLabel>() : null;
	}
	public TabPanelCommon(ResourceBundle resource) {this(resource, false);}

	public void initTabPanel(JPanel containerPanel, Color backgroundColorParm) {
		final int borderGap = 10;
		Color backgroundColor = backgroundColorParm == null ?
				UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY) : backgroundColorParm;

		containerPanel.setLayout(new BorderLayout(borderGap, borderGap));
		containerPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
		containerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, borderGap, 0));
		initDataPanel(backgroundColor);
	}
	public void initTabPanel(JPanel containerPanel) {
		initTabPanel(containerPanel, null);
	}

	/**
	 * Returns a singleton JPanel for this instance.
	 * 
	 * @return The head JPanel used for this instance
	 */
	public JPanel getTabPanel() {
		if (dataPanels.size() < 1) {
			addDataPanel();
		}
		return dataPanels.get(0);
	}

	private void ensureFirstDataPanel() {
		if (dataPanels.size() < 1) {
			addDataPanel();
		}
	}

	private GridBagConstraints getLabelConstraints(TabPanelCommonAttributes attributes) {
		GridBagConstraints currentConstraints;
		Insets currentInsets = attributes.getInsetsOverride() == null ?
				attributes.getInsets() : attributes.getInsetsOverride();
		if (attributes.getLabelConstraintsOverride() == null) {
			currentConstraints = attributes.getLabelConstraints();
			currentConstraints.gridy = attributes.getGridy();
			currentConstraints.insets = currentInsets;
		}
		else {
			currentConstraints = attributes.getLabelConstraintsOverride();
		}
		return currentConstraints;
	}
	private GridBagConstraints getContentsConstraints(TabPanelCommonAttributes attributes) {
		GridBagConstraints currentContentsConstraints;
		Insets currentContentsInsets = attributes.getContentsInsetsOverride() == null ?
				attributes.getContentsInsets() : attributes.getContentsInsetsOverride();
		if (attributes.getContentsConstraintsOverride() == null) {
			currentContentsConstraints = attributes.getContentsConstraints();
			currentContentsConstraints.gridy = attributes.getGridy();
			currentContentsConstraints.gridwidth = attributes.getContentsWidth();
			currentContentsConstraints.weightx = attributes.getContentsWeight();
			currentContentsConstraints.insets = currentContentsInsets;
		}
		else {
			currentContentsConstraints = attributes.getContentsConstraintsOverride();
		}
		return currentContentsConstraints;
	}
	private GridBagConstraints getContentsConstraints2(TabPanelCommonAttributes attributes) {
		GridBagConstraints currentContentsConstraints;
		Insets currentContentsInsets = attributes.getContentsInsetsOverride() == null ?
				attributes.getContentsInsets() : attributes.getContentsInsetsOverride();
		if (attributes.getContentsConstraintsOverride() == null) {
			currentContentsConstraints = attributes.getContentsConstraints2();
			currentContentsConstraints.gridy = attributes.getGridy();
			currentContentsConstraints.gridwidth = attributes.getContentsWidth();
			currentContentsConstraints.weightx = attributes.getContentsWeight();
			currentContentsConstraints.insets = currentContentsInsets;
		}
		else {
			currentContentsConstraints = attributes.getContentsConstraintsOverride();
		}
		return currentContentsConstraints;
	}

	public TabPanelCommonAttributes changeToNextDataPanel(TabPanelCommonAttributes attributes) {
		ensureFirstDataPanel();
		++currentDataPanel;
		while (currentDataPanel >= dataPanels.size()) {
			addDataPanel();
			dataPanels.get(dataPanels.size() - 2).add(dataPanels.get(dataPanels.size() - 1),
					getLabelConstraints(attributes));
		}
		return attributes;
	}
	public boolean changeToNextDataPanel() {
		boolean atLast = isLastDataPanel();
		if (!atLast) {
			++currentDataPanel;
		}
		return atLast;
	}
	public boolean isLastDataPanel() {
		return currentDataPanel >= dataPanels.size() - 1;
	}
	public boolean isFirstDataPanel() {
		return currentDataPanel <= 0;
	}
	public boolean changeToPreviousDataPanel() {
		boolean changed = false;
		if (currentDataPanel > 0) {
			--currentDataPanel;
			changed = true;
		}
		return changed;
	}
	public void changeToFirstDataPanel() {
		currentDataPanel = 0;
	}
	public void changeToLastDataPanel() {
		ensureFirstDataPanel();
		currentDataPanel = dataPanels.size() - 1;
	}
	private void addDataPanel() {
		dataPanels.add(new JPanel(new GridBagLayout()));
	}
	private JPanel getCurrentDataPanel() {
		ensureFirstDataPanel();
		return dataPanels.get(currentDataPanel);
	}

	public JPanel initDataPanel(Color backgroundColor) {
		JPanel dataPanel = getTabPanel();
		dataPanel.setBackground(backgroundColor);
		return dataPanel;
	}

	/**
	 * Registers two JLabel entities - one for the label text and one for the contents
	 * (contents populated later via setText).
	 * 
	 * @param attributes Specification for the new JLabel label and label value entities
	 * @return
	 */
	public TabPanelCommonAttributes addLabelLine(TabPanelCommonAttributes attributes) {
		if (attributes == null) {
			throw new AROUIPanelException("attributes must be specified");
		}

		String labelText = attributes.getKey();

		JLabel currentLabelContents = labelContents.get(labelText);
		if (currentLabelContents == null) {
			currentLabelContents = new JLabel();
			currentLabelContents.setName(labelText + "_contents");
			labelContents.put(labelText, currentLabelContents);
		}

		if (attributes.getContentsFontOverride() != null) {
			currentLabelContents.setFont(attributes.getContentsFontOverride());
		}
		else if (attributes.getContentsFont() != null) {
			currentLabelContents.setFont(attributes.getContentsFont());
		}
		else {
			currentLabelContents.setFont(TEXT_FONT);
		}

		JLabel currentLabelContents2 = null;
		if (labelContents2 != null) {
			currentLabelContents2 = labelContents2.get(labelText);
			if (currentLabelContents2 == null) {
				currentLabelContents2 = new JLabel();
				currentLabelContents2.setName(labelText + "_contents2");
				labelContents2.put(labelText, currentLabelContents2);
			}
			if (attributes.getContentsFontOverride() != null) {
				currentLabelContents2.setFont(attributes.getContentsFontOverride());
			}
			else if (attributes.getContentsFont() != null) {
				currentLabelContents2.setFont(attributes.getContentsFont());
			}
			else {
				currentLabelContents2.setFont(TEXT_FONT);
			}
		}

		JLabel label = labels.get(labelText);
		if (label == null) {
			label = attributes.getLabelAlignmentOverride() != null ?
				new JLabel(resource.getString(labelText),
					attributes.getLabelAlignmentOverride()) :
					new JLabel(resource.getString(labelText));
			label.setName(labelText + "_label");
			if (attributes.getLabelFontOverride() != null) {
				label.setFont(attributes.getLabelFontOverride());
			}
			else if (attributes.getLabelFont() != null) {
				label.setFont(attributes.getLabelFont());
			}
			else if (attributes.isTitle()) {
				label.setFont(TITLE_FONT);
			}
			else if (attributes.isHeader()) {
				label.setFont(HEADER_FONT);
			}
			else if (attributes.isSubheader()) {
				label.setFont(SUBHEADER_FONT);
			}
			else {
				label.setFont(LABEL_FONT);
			}
			labels.put(labelText, label);
		}

		JPanel dataPanel = getCurrentDataPanel();

		dataPanel.add(label, getLabelConstraints(attributes));

		dataPanel.add(currentLabelContents, getContentsConstraints(attributes));
		if (attributes.getContents().length() > 0) {
			currentLabelContents.setText(attributes.getContents());
		}

		if (currentLabelContents2 != null) {
			if (attributes.getContents2().length() > 0) {
				currentLabelContents2.setText(attributes.getContents2());
			}

			dataPanel.add(currentLabelContents2, getContentsConstraints2(attributes));
		}

		return attributes;
	}

	/**
	 * Sets the contents of label.  Note:  This is going away.
	 * 
	 * @param key key of the bundle message (associated with the registered JLabel)
	 * @param value contents of this bucket
	 * @see #setText(String, int, String)
	 */
	private void setText(String key, String value) {
		if (key == null || value == null || key.length() < 1) {
			throw new AROUIPanelException("key and value must be specified");
		}

		JLabel infoLabel = labelContents.get(key);
		if (infoLabel != null) {
			((GridBagLayout) infoLabel.getParent().getLayout()).getConstraints(infoLabel);
			infoLabel.setText(value);
		}
	}

	/**
	 * Sets the second contents column of label, if it exists (exception if it doesn't exist).
	 * Note:  This is going away.
	 * 
	 * @param key key of the bundle message (associated with the registered JLabel)
	 * @param value contents of this bucket
	 * @see #setText(String, int, String)
	 */
	private void setText2(String key, String value) {
		if (key == null || value == null || key.length() < 1) {
			throw new AROUIPanelException("key and value must be specified");
		}

		if (labelContents2 != null) {
			JLabel infoLabel = labelContents2.get(key);
			if (infoLabel != null) {
				infoLabel.setText(value);
			}
		}
		else {
			throw new AROUIPanelException("2nd column does not exist");
		}
	}
	/**
	 * Temporary dumb implementation that currently handles setting the contents of
	 * buckets.  Index 0 is for the "title", while indexes %gt; 0 are for the corresponding
	 * column number.  Note:  No support currently exists for more than 2 data columns.
	 * Also, you can't change the label contents at this time.
	 * 
	 * @param key key of the bundle message (associated with the registered JLabel)
	 * @param columnIndex
	 * @param value contents of this bucket
	 */
	private void setText(String key, int columnIndex, String value) {
		if (key == null || value == null || key.length() < 1) {
			throw new AROUIPanelException("key and value must be specified");
		}
		if (columnIndex < 0) {
			throw new AROUIPanelException("Invalid colum index");
		}
		if (columnIndex == 0) {
			throw new AROUIPanelException("Currently cannot change label contents");
		}
		if (columnIndex > 2) {
			throw new AROUIPanelException(
					"Support for > 2 content buckets currently unimplemented");
		}

		/*
		 * TODO:  This is such a hack (from when no thought was given to more than 1 or 2
		 * columns)!  FIX!!
		 */
		if (columnIndex == 1) {
			setText(key, value);
		}
		else if (columnIndex == 2) {
			setText2(key, value);
		}
	}

	private String getKeyFromEnum(Enum<?> enumParm) {
		return enumParm.name().replaceAll("_", ".");
	}

	/**
	 * Gets the contents of the label using the key name specified by an enum.  Dots (.) are
	 * delineated by an underscore (_) since enum names cannot contain dots.  So
	 * an enum name of bestpractices_date becomes the key bestpractices.date.
	 * 
	 * @param enumParm
	 * @return bundle key contents
	 * @see #getText(Enum, String[])
	 */
	public String getText(Enum<?> enumParm) {
		return resource.getString(getKeyFromEnum(enumParm));
	}
	/**
	 * Gets contents from a resource bundle that also uses arguments in the key contents.
	 * 
	 * @param enumParm
	 * @param args
	 * @return key contents (message arguments filled in)
	 * @see #getText(Enum)
	 */
	public String getText(Enum<?> enumParm, String[] args) {
		return MessageFormat.format(resource.getString(getKeyFromEnum(enumParm)),
				(Object[]) args);
	}
	/**
	 * Gets the contents in an array from a resource bundle where the passed in
	 * enum array represents the bundle keys.
	 * 
	 * @param enumParms The array of enums that specify the bundle keys
	 * @return The key values from the bundle where enumParms specifies the keys
	 */
	public String[] getText(Enum<?>[] enumParms) {
		String[] bundleValues = null;
		if (enumParms != null) {
			bundleValues = new String[enumParms.length];
			for (int index = 0; index < enumParms.length; ++index) {
				bundleValues[index] = resource.getString(getKeyFromEnum(enumParms[index]));
			}
		}
		else {
			throw new AROUIPanelException("enum array must be specified");
		}
		return bundleValues;
	}
	/**
	 * Gets the contents in an array from a resource bundle that also has argument
	 * substitutions.
	 * 
	 * @param enumParms The array of enums that specify the bundle keys
	 * @param args The bundle value arguments to be substituted in the respective places
	 * @return The key values from the bundle with argument substitutions
	 */
	public String[] getText(Enum<?>[] enumParms, String[] args) {
		String[] bundleValues = null;
		if (enumParms != null) {
			bundleValues = new String[enumParms.length];
			for (int index = 0; index < enumParms.length; ++index) {
				bundleValues[index] = MessageFormat.format(resource.getString(
						getKeyFromEnum(enumParms[index])), (Object[]) args);
			}
		}
		else {
			throw new AROUIPanelException("enum array must be specified");
		}
		return bundleValues;
	}
	/**
	 * Sets the contents of the label using the key name specified by an enum.  Dots (.) are
	 * delineated by an underscore (_) since enum names cannot contain dots.  So
	 * an enum name of bestpractices_date becomes the key bestpractices.date.
	 * 
	 * @param enumParm
	 * @param value
	 */
	public void setText(Enum<?> enumParm, String value) {
		if (enumParm == null) {
			throw new AROUIPanelException("enumParm must be specified");
		}

		setText(getKeyFromEnum(enumParm), 1, value);
	}
	/**
	 * Sets the second column contents of the label using the key name specified by an enum,
	 * if the second column exists (exception thrown if it doesn't exist).  This is done in
	 * the same manner as <em>setText(...)</em> - please see that method for further details.
	 * 
	 * @param enumParm
	 * @param value
	 * @see #setText(Enum, String)
	 */
	public void setText2(Enum<?> enumParm, String value) {
		if (enumParm == null) {
			throw new AROUIPanelException("enumParm must be specified");
		}

		setText(getKeyFromEnum(enumParm), 2, value);
	}
	public void setText(Enum<?> enumParm, int columnIndex, String value) {
		
	}
	public void setText(Enum<?> enumParm, Enum<?> columnEnum, String value) {		
	}
}
