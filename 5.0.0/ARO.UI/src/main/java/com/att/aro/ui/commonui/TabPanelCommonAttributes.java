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

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import com.att.aro.ui.exception.AROUIPanelException;

/**
 * This is the input parameter object to TabPanelCommon's addLabelLine().  It encapsulates the
 * label bundle key associated with with this line of data to be rendered, label and contents.
 * A Builder is provided to help define each line of data.
 * 
 * @author Nathan F Syfrig
 *
 */
public class TabPanelCommonAttributes {
	private static final Insets DEFAULT_INSETS = new Insets(2, 2, 2, 2);

	private final String key;
	private final String contents;
	private final String contents2;
	private final boolean title;
	private final boolean header;
	private final boolean subheader;
	private final boolean labelOnly;
	private final boolean contentsOnly;
	private final int gridy;
	private final int contentsWidth;
	private final double contentsWeight;
	private final Insets insets;
	private final Insets contentsInsets;
	private final Font labelFont;
	private final Font contentsFont;
	private final Insets insetsOverride;
	private final Insets contentsInsetsOverride;
	private final Font labelFontOverride;
	private final Font contentsFontOverride;
	private final GridBagConstraints labelConstraints;
	private final GridBagConstraints contentsConstraints;
	private final GridBagConstraints contentsConstraints2;
	private final GridBagConstraints labelConstraintsOverride;
	private final GridBagConstraints contentsConstraintsOverride;
	private final Integer labelAlignmentOverride;

	public static final GridBagConstraints getDefaultLabelConstraints(int gridy, Insets insets) {
		return new GridBagConstraints(1, gridy, 1, 1, 0.0, 0.0,
				GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0);
	}
	public static final GridBagConstraints getDefaultLabelConstraints() {
		return getDefaultLabelConstraints(0, DEFAULT_INSETS);
	}
	public static final GridBagConstraints getDefaultContentsConstraints(int gridy, int width,
			double weight, Insets insets) {
		return new GridBagConstraints(2, gridy,
				width, 1, weight, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
					insets, 0, 0);
	}
	public static final GridBagConstraints getDefaultContentsConstraints() {
		return getDefaultContentsConstraints(0, 1, 0.0, DEFAULT_INSETS);
	}
	public static final GridBagConstraints getDefaultContentsConstraints2(int gridy, int width,
			double weight, Insets insets) {
		return new GridBagConstraints(3, gridy,
				width, 1, weight, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
					insets, 0, 0);
	}
	public static final GridBagConstraints getDefaultContentsConstraints2() {
		return getDefaultContentsConstraints2(0, 1, 0.0, DEFAULT_INSETS);
	}

	private TabPanelCommonAttributes(
			String key,
			String contents,
			String contents2,
			boolean title,
			boolean header,
			boolean subheader,
			boolean labelOnly,
			boolean contentsOnly,
			int gridy,
			int contentsWidth,
			double contentsWeight,
			Insets insets,
			Insets contentsInsets,
			Font labelFont,
			Font contentsFont,
			Insets insetsOverride,
			Insets contentsInsetsOverride,
			Font labelFontOverride,
			Font contentsFontOverride,
			GridBagConstraints labelConstraints,
			GridBagConstraints contentsConstraints,
			GridBagConstraints contentsConstraints2,
			GridBagConstraints labelConstraintsOverride,
			GridBagConstraints contentsConstraintsOverride,
			Integer labelAlignmentOverride) {
		this.key = key;
		this.contents = contents;
		this.contents2 = contents2;
		this.title = title;
		this.header = header;
		this.subheader = subheader;
		this.labelOnly = labelOnly;
		this.contentsOnly = contentsOnly;
		this.gridy = gridy;
		this.contentsWidth = contentsWidth;
		this.contentsWeight = contentsWeight;
		this.insets = insets;
		this.contentsInsets = contentsInsets;
		this.labelFont = labelFont;
		this.contentsFont = contentsFont;
		this.insetsOverride = insetsOverride;
		this.contentsInsetsOverride = contentsInsetsOverride;
		this.labelFontOverride = labelFontOverride;
		this.contentsFontOverride = contentsFontOverride;
		this.labelConstraints = labelConstraints;
		this.contentsConstraints = contentsConstraints;
		this.contentsConstraints2 = contentsConstraints2;
		this.labelConstraintsOverride = labelConstraintsOverride;
		this.contentsConstraintsOverride = contentsConstraintsOverride;
		this.labelAlignmentOverride = labelAlignmentOverride;
	}

	public String getKey() {
		return key;
	}
	public String getContents() {
		return contents;
	}
	public String getContents2() {
		return contents2;
	}
	public boolean isTitle() {
		return title;
	}
	public boolean isHeader() {
		return header;
	}
	public boolean isSubheader() {
		return subheader;
	}
	public boolean isLabelOnly() {
		return labelOnly;
	}
	public boolean isContentsOnly() {
		return contentsOnly;
	}
	public int getGridy() {
		return gridy;
	}
	public int getContentsWidth() {
		return contentsWidth;
	}
	public double getContentsWeight() {
		return contentsWeight;
	}
	public Insets getInsets() {
		return insets;
	}
	public Insets getContentsInsets() {
		return contentsInsets;
	}
	public Font getLabelFont() {
		return labelFont;
	}
	public Font getContentsFont() {
		return contentsFont;
	}
	public Insets getInsetsOverride() {
		return insetsOverride;
	}
	public Insets getContentsInsetsOverride() {
		return contentsInsetsOverride;
	}
	public Font getLabelFontOverride() {
		return labelFontOverride;
	}
	public Font getContentsFontOverride() {
		return contentsFontOverride;
	}
	public GridBagConstraints getLabelConstraints() {
		return labelConstraints;
	}
	public GridBagConstraints getContentsConstraints() {
		return contentsConstraints;
	}
	public GridBagConstraints getContentsConstraints2() {
		return contentsConstraints2;
	}
	public GridBagConstraints getLabelConstraintsOverride() {
		return labelConstraintsOverride;
	}
	public GridBagConstraints getContentsConstraintsOverride() {
		return contentsConstraintsOverride;
	}
	public Integer getLabelAlignmentOverride() {
		return labelAlignmentOverride;
	}

	@Override
	public String toString() {
		return "TabPanelCommonAttributes [key=" + key + ", contents=" + contents
				+ ", contents2=" + contents2 + ", title=" + title + ", header="
				+ header + ", subheader=" + subheader + ", labelOnly="
				+ labelOnly + ", gridy=" + gridy + ", contentsWidth="
				+ contentsWidth + ", contentsWeight=" + contentsWeight
				+ ", insets=" + insets + ", contentsInsets=" + contentsInsets
				+ ", labelFont=" + labelFont + ", contentsFont=" + contentsFont
				+ ", insetsOverride=" + insetsOverride
				+ ", contentsInsetsOverride=" + contentsInsetsOverride
				+ ", labelFontOverride=" + labelFontOverride
				+ ", contentsFontOverride=" + contentsFontOverride
				+ ", labelConstraints=" + labelConstraints
				+ ", contentsConstraints=" + contentsConstraints
				+ ", contentsConstraints2=" + contentsConstraints2
				+ ", labelConstraintsOverride=" + labelConstraintsOverride
				+ ", contentsConstraintsOverride="
				+ contentsConstraintsOverride + ", labelAlignmentOverride="
				+ labelAlignmentOverride + "]";
	}

	/**
	 * The builder provided to generate TabPanelCommonAttributes instances.  Note:
	 * There is a "copy" attribute for easily initializing attributes to the
	 * previous definition, making the subsequent modifications smaller.  There is
	 * also a "copyNextLine which is the same as "copy" except that attribute 'gridy'
	 * is incremented by 1, making it easy to automatically go to the next line.
	 * 
	 * @author Nathan F Syfrig
	 *
	 */
	public static class Builder {
		private String key = "";
		private String contents = "";
		private String contents2 = "";
		private boolean title = false;
		private boolean header = false;
		private boolean subheader = false;
		private boolean labelOnly = false;
		private boolean contentsOnly = false;
		private boolean contentsOnlyOverride = contentsOnly; // only used in builder
		private int gridy = 0;
		private int contentsWidth = 1;
		private double contentsWeight = 0.0;
		private Insets insets;
		private Insets contentsInsets = null;
		private Font labelFont = null;
		private Font contentsFont = null;
		private Insets insetsOverride = null;
		private Insets contentsInsetsOverride = null;
		private Font labelFontOverride = null;
		private Font contentsFontOverride = null;
		private GridBagConstraints labelConstraints = null;
		private GridBagConstraints contentsConstraints = null;
		private GridBagConstraints contentsConstraints2 = null;
		private GridBagConstraints labelConstraintsOverride = null;
		private GridBagConstraints contentsConstraintsOverride = null;
		private Integer labelAlignmentOverride = null;

		/**
		 * <p>
		 * This initializes most attributes from the argument.  If used, it must be the first
		 * builder populator method called.
		 * </p><p>
		 * Attributes excluded from this copy:
		 * </p><ul>
		 * 	<li>contents</li>
		 * 	<li>contents2</li>
		 * 	<li>title</li>
		 * 	<li>header</li>
		 * 	<li>subheader</li>
		 * 	<li>labelOnly</li>
		 * 	<li>contentsOnlyOverride</li>
		 * 	<li>insetsOverride</li>
		 * 	<li>contentsInsetsOverride</li>
		 * 	<li>labelFontOverride</li>
		 * 	<li>contentsFontOverride</li>
		 * 	<li>labelConstraintsOverride</li>
		 * 	<li>contentsConstraintsOverride</li>
		 * 	<li>labelAlignmentOverride</li>
		 * </ul>
		 * 
		 * @param attributes attributes to be initialized with (with described exceptions)
		 * @return this builder
		 */
		public Builder copy(TabPanelCommonAttributes attributes) {
			if (attributes != null) {
				key = attributes.key;
				contentsOnly = attributes.contentsOnly;
				contentsOnlyOverride = contentsOnly;
				gridy = attributes.gridy;
				contentsWidth = attributes.contentsWidth;
				contentsWeight = attributes.contentsWeight;
				insets = attributes.insets;
				contentsInsets = attributes.contentsInsets;
				labelFont = attributes.labelFont;
				contentsFont = attributes.contentsFont;
				this.labelConstraints = attributes.labelConstraints;
				this.contentsConstraints = attributes.contentsConstraints;
				this.contentsConstraints2 = attributes.contentsConstraints2;
			}
			return this;
		}
		/**
		 * Same as copy except attribute 'gridy' is automatically incremented, placing the
		 * location on the next line.
		 * 
		 * @param attributes
		 * @return
		 * @see #copy(TabPanelCommonAttributes)
		 */
		public Builder copyNextLine(TabPanelCommonAttributes attributes) {
			copy(attributes);
			++gridy;
			return this;
		}

		public Builder enumKey(Enum<?> enumParm) {
			if (enumParm != null) {
				key = enumParm.name().replaceAll("_", ".");
			}
			return this;
		}
		public Builder contents(String contents) {
			this.contents = contents;
			return this;
		}
		public Builder contents2(String contents2) {
			this.contents2 = contents2;
			return this;
		}
		public Builder title(boolean title) {
			this.title = title;
			if (title) {
				labelFont = null;
				contentsFont = null;
			}
			return this;
		}
		public Builder title() {
			return title(true);
		}
		public Builder header(boolean header) {
			this.header = header;
			if (header) {
				labelFont = null;
				contentsFont = null;
			}
			return this;
		}
		public Builder header() {
			return header(true);
		}
		public Builder subheader(boolean subheader) {
			this.subheader = subheader;
			if (subheader) {
				labelFont = null;
				contentsFont = null;
			}
			return this;
		}
		public Builder labelOnly(boolean labelOnly) {
			this.labelOnly = labelOnly;
			return this;
		}
		public Builder labelOnly() {
			return labelOnly(true);
		}
		public Builder contentsOnly(boolean contentsOnly) {
			this.contentsOnly = contentsOnly;
			return this;
		}
		public Builder contentsOnly() {
			return contentsOnly(true);
		}
		public Builder contentsOnlyAndOverride(boolean contentsOnly) {
			this.contentsOnly = contentsOnly;
			contentsOnlyOverride = contentsOnly;
			return this;
		}
		public Builder contentsOnlyAndOverride() {
			return contentsOnlyAndOverride(true);
		}
		public Builder contentsOnlyOverride(boolean contentsOnlyOverride) {
			this.contentsOnlyOverride = contentsOnlyOverride;
			return this;
		}
		public Builder contentsOnlyOverride() {
			return contentsOnlyOverride(!contentsOnly);
		}
		public Builder subheader() {
			return subheader(true);
		}
		public Builder gridy(int gridy) {
			this.gridy = gridy;
			return this;
		}
		public Builder contentsWidth(int width) {
			this.contentsWidth = width;
			return this;
		}
		public Builder contentsWeight(double weight) {
			this.contentsWeight = weight;
			return this;
		}
		public Builder insets(Insets insets) {
			this.insets = insets;
			return this;
		}
		public Builder contentsInsets(Insets contentsInsets) {
			this.contentsInsets = contentsInsets;
			return this;
		}
		public Builder labelFont(Font labelFont) {
			this.labelFont = labelFont;
			return this;
		}
		public Builder contentsFont(Font contentsFont) {
			this.contentsFont = contentsFont;
			return this;
		}
		public Builder insetsOverride(Insets insetsOverride) {
			this.insetsOverride = insetsOverride;
			return this;
		}
		public Builder contentsInsetsOverride(Insets contentsInsetsOverride) {
			this.contentsInsetsOverride = contentsInsetsOverride;
			return this;
		}
		public Builder labelFontOverride(Font labelFontOverride) {
			this.labelFontOverride = labelFontOverride;
			return this;
		}
		public Builder contentsFontOverride(Font contentsFontOverride) {
			this.contentsFontOverride = contentsFontOverride;
			return this;
		}
		public Builder labelConstraints(GridBagConstraints labelConstraints) {
			this.labelConstraints = labelConstraints;
			return this;
		}
		public Builder contentsConstraints(GridBagConstraints contentsConstraints) {
			this.contentsConstraints = contentsConstraints;
			return this;
		}
		public Builder contentsConstraints2(GridBagConstraints contentsConstraints2) {
			this.contentsConstraints2 = contentsConstraints2;
			return this;
		}
		public Builder labelConstraintsOverride(GridBagConstraints labelConstraintsOverride) {
			this.labelConstraintsOverride = labelConstraintsOverride;
			return this;
		}
		public Builder contentsConstraintsOverride(
				GridBagConstraints contentsConstraintsOverride) {
			this.contentsConstraintsOverride = contentsConstraintsOverride;
			return this;
		}
		public Builder labelAlignmentOverride(Integer labelAlignmentOverride) {
			this.labelAlignmentOverride = labelAlignmentOverride;
			return this;
		}

		private void validate() {
			if (key == null || key.length() < 1) {
				throw new AROUIPanelException("key must be specified");
			}
			if (contents == null) {
				throw new AROUIPanelException("contents cannot be null");
			}
			if (contents2 == null) {
				throw new AROUIPanelException("contents2 cannot be null");
			}
			if (insets == null) {
				insets = DEFAULT_INSETS;
			}
			if (contentsInsets == null) {
				contentsInsets = insets;
			}
			if (contentsWidth < 0) {
				throw new AROUIPanelException("contentsWidth must be >= 0");
			}
			if (contentsInsetsOverride == null) {
				contentsInsetsOverride = insetsOverride;
			}

			if (labelConstraints == null) {
				labelConstraints = getDefaultLabelConstraints(gridy, insets);
			}
			if (contentsConstraints == null) {
				contentsConstraints = getDefaultContentsConstraints(gridy, contentsWidth,
						contentsWeight, insets);
			}
			if (contentsConstraints2 == null) {
				contentsConstraints2 = getDefaultContentsConstraints2(gridy, contentsWidth,
						contentsWeight, insets);
			}
			if (contentsOnlyOverride != contentsOnly) {
				contentsOnly = contentsOnlyOverride;
			}
		}

		public TabPanelCommonAttributes build() {
			validate();
			return new TabPanelCommonAttributes(
					key,
					contents,
					contents2,
					title,
					header,
					labelOnly,
					contentsOnly,
					subheader,
					gridy,
					contentsWidth,
					contentsWeight,
					insets,
					contentsInsets,
					labelFont,
					contentsFont,
					insetsOverride,
					contentsInsetsOverride,
					labelFontOverride,
					contentsFontOverride,
					labelConstraints,
					contentsConstraints,
					contentsConstraints2,
					labelConstraintsOverride,
					contentsConstraintsOverride,
					labelAlignmentOverride);
		}


		@Override
		public String toString() {
			return "Builder [key=" + key + ", contents=" + contents
					+ ", contents2=" + contents2 + ", title=" + title
					+ ", header=" + header + ", subheader=" + subheader
					+ ", labelOnly=" + labelOnly + ", gridy=" + gridy
					+ ", contentsWidth=" + contentsWidth + ", contentsWeight="
					+ contentsWeight + ", insets=" + insets
					+ ", contentsInsets=" + contentsInsets + ", labelFont="
					+ labelFont + ", contentsFont=" + contentsFont
					+ ", insetsOverride=" + insetsOverride
					+ ", contentsInsetsOverride=" + contentsInsetsOverride
					+ ", labelFontOverride=" + labelFontOverride
					+ ", contentsFontOverride=" + contentsFontOverride
					+ ", labelConstraints=" + labelConstraints
					+ ", contentsConstraints=" + contentsConstraints
					+ ", contentsConstraints2=" + contentsConstraints2
					+ ", labelConstraintsOverride=" + labelConstraintsOverride
					+ ", contentsConstraintsOverride="
					+ contentsConstraintsOverride + ", labelAlignmentOverride="
					+ labelAlignmentOverride + "]";
		}
	}
}
