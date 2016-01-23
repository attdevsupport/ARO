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
package com.att.aro.ui.view.bestpracticestab;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseWheelListener;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.AsyncCheckEntry;
import com.att.aro.core.bestpractice.pojo.AsyncCheckInScriptResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.bestpractice.pojo.DisplayNoneInCSSEntry;
import com.att.aro.core.bestpractice.pojo.DisplayNoneInCSSResult;
import com.att.aro.core.bestpractice.pojo.DuplicateContentResult;
import com.att.aro.core.bestpractice.pojo.FileCompressionResult;
import com.att.aro.core.bestpractice.pojo.FileOrderEntry;
import com.att.aro.core.bestpractice.pojo.FileOrderResult;
import com.att.aro.core.bestpractice.pojo.Http3xxCodeResult;
import com.att.aro.core.bestpractice.pojo.Http4xx5xxResult;
import com.att.aro.core.bestpractice.pojo.Http4xx5xxStatusResponseCodesEntry;
import com.att.aro.core.bestpractice.pojo.HttpCode3xxEntry;
import com.att.aro.core.bestpractice.pojo.ImageSizeEntry;
import com.att.aro.core.bestpractice.pojo.ImageSizeResult;
import com.att.aro.core.bestpractice.pojo.MinificationEntry;
import com.att.aro.core.bestpractice.pojo.MinificationResult;
import com.att.aro.core.bestpractice.pojo.SpriteImageEntry;
import com.att.aro.core.bestpractice.pojo.SpriteImageResult;
import com.att.aro.core.bestpractice.pojo.TextFileCompressionEntry;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionEntry;
import com.att.aro.core.bestpractice.pojo.UnnecessaryConnectionResult;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.AROUIManager;
import com.att.aro.ui.commonui.BrowserGenerator;
import com.att.aro.ui.commonui.IARODiagnosticsOverviewRoute;
import com.att.aro.ui.commonui.IAROExpandable;
import com.att.aro.ui.commonui.UIComponent;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class BpDetailItem extends AbstractBpPanel implements IAROExpandable{

	private static final long serialVersionUID = 1L;
	
	JLabel imageLabel = null;
	JLabel nameLabel = null;
//	JTextPane nameTextLabel = null;
	JLabel nameTextLabel = null;
	JLabel aboutLabel = null;
	JTextPane aboutTextLabel = null;
	JLabel resultsLabel = null;
	JTextPane resultsTextLabel = null;
	private AbstractBpDetailTablePanel resultsTablePanel;
	private IARODiagnosticsOverviewRoute diagnosticsOverviewRoute;
	
	Insets imageInsets = new Insets(25, 10, 10, 10);
	Insets startInsets = new Insets(25, 5, 2, 5);
	Insets insets = new Insets(2, 5, 2, 5);
	
	JPanel dataPanel;

	private BestPracticeType bpType;

	static final Font TEXT_FONT = new Font("TextFont", Font.PLAIN, 12);
	private static final int TEXT_WIDTH = 600;
	
	public BpDetailItem(String name, BestPracticeType bpType, AbstractBpDetailTablePanel resultsTablePanel) {
		super();
		
		this.bpType = bpType;
		imageLabel = new JLabel(loadImageIcon(null));
		nameLabel = new JLabel();
		aboutLabel = new JLabel();
		resultsLabel = new JLabel();
		nameTextLabel = new JLabel();
		
		this.resultsTablePanel = resultsTablePanel;
		
		add(layoutPanel(name), BorderLayout.CENTER);
	}

	public void addTablePanelRoute(IARODiagnosticsOverviewRoute DiagnosticsOverviewRoute) {
		this.diagnosticsOverviewRoute = DiagnosticsOverviewRoute;
		if (resultsTablePanel != null) {
			resultsTablePanel.addTablePanelRoute(DiagnosticsOverviewRoute);;
		}
	}
	
	public void setwidth(JTextPane textPanel, int width){
		
		textPanel.setPreferredSize(null);
		textPanel.setSize(width, 9999);
		Dimension dimTextPanel = textPanel.getPreferredSize();
		dimTextPanel.width = width;
		textPanel.setPreferredSize(dimTextPanel);
		textPanel.setMinimumSize(dimTextPanel);
	}
	
	public JPanel layoutPanel(String name) {

		JScrollPane scroll;
		
		if (dataPanel == null) {
			dataPanel = new JPanel(new GridBagLayout());
			dataPanel.setBackground(UIManager.getColor(AROUIManager.PAGE_BACKGROUND_KEY));
			
			Insets insets = new Insets(2, 2, 2, 2);
			int idx = 0;

			//
			nameLabel 		  .setText(        ResourceBundleHelper.getMessageString("bestPractices.test"));
			//nameTextLabel 		 = createJTextPane(ResourceBundleHelper.getMessageString(name + ".detailedTitle"));
			nameTextLabel.setText(ResourceBundleHelper.getMessageString(name + ".detailedTitle"));
			aboutLabel		.setText(ResourceBundleHelper.getMessageString("bestPractices.About"));
			aboutTextLabel   = createJTextArea(ResourceBundleHelper.getMessageString(name + ".desc") , getLearnMoreURI());
			resultsLabel 	  .setText(  ResourceBundleHelper.getMessageString("bestPractices.results" ));
			resultsTextLabel = createJTextPane("");
			
			//setwidth(nameTextLabel 	    , 180); 
			setwidth(aboutTextLabel     , TEXT_WIDTH);

			resultsTextLabel.setPreferredSize(null);
			resultsTextLabel.setSize(TEXT_WIDTH, 9999);
			Dimension labelDim = resultsTextLabel.getPreferredSize();
			labelDim.width = TEXT_WIDTH;
			labelDim.height = 60;
			resultsTextLabel.setPreferredSize(labelDim);
			resultsTextLabel.setMinimumSize(labelDim);
			
			resultsTextLabel.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {
					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						performAction(e);
					}
				}

			});
			
			// Icon
			dataPanel.add(imageLabel, new GridBagConstraints(0, idx, 1, 4, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, imageInsets, 0, 0));
	
			// Text:
			dataPanel.add(nameLabel, new GridBagConstraints(1, idx, 1, 1, 0.0, 0.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, startInsets, 0, 0));

			// about: detailedTitle - desc
		//	dataPanel.add(aboutLabel, new GridBagConstraints(1, ++idx, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
			nameLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));
			nameTextLabel.setFont(new Font("TimesRoman", Font.PLAIN, 16));
			scroll = new JScrollPane(nameTextLabel);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			removeMouseWheelListeners(scroll);
			dataPanel.add(scroll, new GridBagConstraints(2, idx, 1, 1, 1.0, 1.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, insets, 0, 0));
			
			dataPanel.add(aboutLabel, new GridBagConstraints(1, ++idx, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
			
			scroll = new JScrollPane(aboutTextLabel);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			removeMouseWheelListeners(scroll);
			dataPanel.add(scroll, new GridBagConstraints(2, idx, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.HORIZONTAL, insets, 0, 0));

			// Results
			dataPanel.add(resultsLabel, new GridBagConstraints(1, ++idx, 1, 1, 0.0, 0.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, insets, 0, 0));
			scroll = new JScrollPane(resultsTextLabel);
			scroll.setBorder(BorderFactory.createEmptyBorder());
			removeMouseWheelListeners(scroll);
			dataPanel.add(scroll, new GridBagConstraints(2, idx, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
			
			// Table
			if (resultsTablePanel != null) {
				dataPanel.add(resultsTablePanel, new GridBagConstraints(2, ++idx, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, insets, 0, 0));
			}
		}
		return dataPanel;
	}
	
	void performAction(HyperlinkEvent e){
		diagnosticsOverviewRoute.routeHyperlink(this.bpType);	}
	
	/**
	 * This method creates the about text area.
	 */
	private JTextPane createJTextArea(String textToDisplay, final URI url) {
		HTMLDocument doc = new HTMLDocument();
		StyleSheet style = doc.getStyleSheet();
		style.addRule("body { font-family: " + TEXT_FONT.getFamily() + "; "
				+ "font-size: " + TEXT_FONT.getSize() + "pt; }");
		style.addRule("a { text-decoration: underline; font-weight:bold; }");
		JTextPane jTextArea = new JTextPane(doc);
		jTextArea.setEditable(false);
		jTextArea.setEditorKit(new HTMLEditorKit());
		jTextArea.setStyledDocument(doc);
		jTextArea.setMargin(new Insets(0, 0, 0, 0));
		if (url != null) {
			jTextArea.setText(textToDisplay + "&nbsp;" + " <a href=\"#\">"
					+ ResourceBundleHelper.getMessageString("bestPractices.learnMore") + "</a>");
			jTextArea.addHyperlinkListener(new HyperlinkListener() {

				@Override
				public void hyperlinkUpdate(HyperlinkEvent e) {

					if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
						try {
							BrowserGenerator.openBrowser(url);
						} catch (IOException e1) {
//							MessageDialogFactory.showUnexpectedExceptionDialog(parent, e1);
						}
					}
				}

			});
		} else {
			jTextArea.setText(textToDisplay);
		}
		
		// Calculate preferred size
		jTextArea.setSize(TEXT_WIDTH, 9999);
		Dimension d = jTextArea.getPreferredSize();
		d.width = TEXT_WIDTH;
		jTextArea.setPreferredSize(d);
		jTextArea.setMinimumSize(d);

		return jTextArea;
	}
	
	private URI getLearnMoreURI(){

		String learnMoreURI = "";
		switch (bpType) {
			case FILE_COMPRESSION: learnMoreURI = ResourceBundleHelper.getMessageString("textFileCompression.url");
			break;
			case DUPLICATE_CONTENT: learnMoreURI = ResourceBundleHelper.getMessageString("caching.duplicateContent.url") ;
			break;
			case USING_CACHE: learnMoreURI = ResourceBundleHelper.getMessageString("caching.cacheControl.url");
			break;
			case CACHE_CONTROL: learnMoreURI = ResourceBundleHelper.getMessageString("caching.usingCache.url");
			break;
			case COMBINE_CS_JSS: learnMoreURI = ResourceBundleHelper.getMessageString("combinejscss.url") ;
			break;
			case IMAGE_SIZE: learnMoreURI = ResourceBundleHelper.getMessageString("imageSize.url");
			break;
			case MINIFICATION: learnMoreURI = ResourceBundleHelper.getMessageString("minification.url") ;
			break;
			case SPRITEIMAGE: learnMoreURI = ResourceBundleHelper.getMessageString("spriteimages.url");
			break;
			case UNNECESSARY_CONNECTIONS: learnMoreURI = ResourceBundleHelper.getMessageString("connections.unnecssaryConn.url") ;
			break;
			case SCRIPTS_URL: learnMoreURI = ResourceBundleHelper.getMessageString("3rd.party.scripts.url");
			break;
			case SCREEN_ROTATION: learnMoreURI = ResourceBundleHelper.getMessageString("connections.screenRotation.url");
			break;
			case PERIODIC_TRANSFER: learnMoreURI = ResourceBundleHelper.getMessageString("connections.periodic.url");
			break; 
			case HTTP_4XX_5XX: learnMoreURI = ResourceBundleHelper.getMessageString("connections.http4xx5xx.url");
			break; 
			case HTTP_3XX_CODE: learnMoreURI = ResourceBundleHelper.getMessageString("connections.http3xx.url");
			break; 
			case HTTP_1_0_USAGE: learnMoreURI = ResourceBundleHelper.getMessageString("html.httpUsage.url");
			break; 
			case FLASH: learnMoreURI = ResourceBundleHelper.getMessageString("flash.url");
			break; 
			case FILE_ORDER: learnMoreURI = ResourceBundleHelper.getMessageString("html.fileorder.url");
			break; 
			case EMPTY_URL: learnMoreURI = ResourceBundleHelper.getMessageString("empty.url.url");
			break;
			case DISPLAY_NONE_IN_CSS: learnMoreURI = ResourceBundleHelper.getMessageString("html.displaynoneincss.url");
			break;
			case CONNECTION_OPENING: learnMoreURI = ResourceBundleHelper.getMessageString("connections.connectionOpening.url");
			break;
			case CONNECTION_CLOSING: learnMoreURI = ResourceBundleHelper.getMessageString("connections.connClosing.url");
			break;
			case ASYNC_CHECK: learnMoreURI = ResourceBundleHelper.getMessageString("html.asyncload.url");
			break;
			case ACCESSING_PERIPHERALS: learnMoreURI = ResourceBundleHelper.getMessageString("other.accessingPeripherals.url");
			break;
		}			
		return URI.create(learnMoreURI);
	}
	
	/**
	 * Wrap supplied HTML decorated text in a JTextPane
	 * 
	 * @param text
	 * @return
	 */
	private JTextPane createJTextPane(String text) {
		HTMLDocument doc = new HTMLDocument();
		StyleSheet style = doc.getStyleSheet();
		style.addRule("body { font-family: " + TEXT_FONT.getFamily() + "; " + "font-size: " + TEXT_FONT.getSize() + "pt; }");
		style.addRule("a { text-decoration: underline }");
		JTextPane jTextArea = new JTextPane(doc);
		jTextArea.setEditable(false);
		jTextArea.setEditorKit(new HTMLEditorKit());
		jTextArea.setStyledDocument(doc);
		jTextArea.setMargin(new Insets(0, 0, 0, 0));
		if (text != null) {
			jTextArea.setText(text);
			jTextArea.setSize(TEXT_WIDTH, 9999);
			Dimension dim = jTextArea.getPreferredSize();
			dim.width = TEXT_WIDTH;
			jTextArea.setPreferredSize(dim);
			jTextArea.setMinimumSize(dim);
		} else {
			jTextArea.setPreferredSize(new Dimension(500, 50));
		}
		return jTextArea;
	}
	
	/**
	 * locate and load an ImageIcon based on AbstractBestPracticeResult
	 * 
	 * @param bestPracticeResult
	 * @return ImageIcon corresponding to PASS, FAIL, WARNING, SELF_TEST
	 */
	private ImageIcon loadImageIcon(AbstractBestPracticeResult bestPracticeResult) {
		String imageName = "Image.naGray";
		if (bestPracticeResult != null) {
			BPResultType resType = bestPracticeResult.getResultType(); // PASS, FAIL or WARNING
			if (resType.equals(BPResultType.PASS)) {
				imageName = "Image.bpPassDark";
			} else if (resType.equals(BPResultType.FAIL)) {
				imageName = "Image.bpFailDark";
			} else if (resType.equals(BPResultType.WARNING)) {
				imageName = "Image.bpWarningDark";
			} else if (resType.equals(BPResultType.SELF_TEST)) {
				imageName = "Image.bpManual";
			}
		}
		return UIComponent.getInstance().getIconByKey(imageName);
	}

	private static void removeMouseWheelListeners(JScrollPane scrollPane) {
		for (MouseWheelListener mwl : scrollPane.getMouseWheelListeners()) {
			scrollPane.removeMouseWheelListener(mwl);
		}
	}

	@Override
	public void refresh(AROTraceData model) {
		List<AbstractBestPracticeResult> bpResults = model.getBestPracticeResults();
		for (AbstractBestPracticeResult bpr : bpResults) {

			if (bpr.getBestPracticeType().equals(this.bpType)) {
				resultsTextLabel.setText(bpr.getResultText());
				imageLabel.setIcon(loadImageIcon(bpr));

				BestPracticeType resultType = bpr.getBestPracticeType();
				
				switch (resultType) {

				case FILE_COMPRESSION:
					((BpFileCompressionTablePanel)resultsTablePanel).setData((Collection<TextFileCompressionEntry>) ((FileCompressionResult) bpr).getResults());
					return;
				case DUPLICATE_CONTENT:
					((BpFileDuplicateContentTablePanel)resultsTablePanel).setData((Collection<CacheEntry>) ((DuplicateContentResult) bpr).getDuplicateContentList());
					return;
				case IMAGE_SIZE:
					((BpFileImageSizeTablePanel)resultsTablePanel).setData((Collection<ImageSizeEntry>) ((ImageSizeResult) bpr).getResults());
					return;
				case MINIFICATION:
					((BpFileMinificationTablePanel)resultsTablePanel).setData((Collection<MinificationEntry>) ((MinificationResult) bpr).getMinificationEntryList());
					return;
				case SPRITEIMAGE:
					((BpFileSpriteImagesTablePanel)resultsTablePanel).setData((Collection<SpriteImageEntry>) ((SpriteImageResult) bpr).getAnalysisResults());
					return;
				case HTTP_4XX_5XX:
					((BpConnectionsHttp4xx5xxTablePanel)resultsTablePanel).setData((Collection<Http4xx5xxStatusResponseCodesEntry>) ((Http4xx5xxResult) bpr).getHttpResCodelist());
					return;
				case HTTP_3XX_CODE:
					((BpConnectionsHttp3xxTablePanel)resultsTablePanel).setData((Collection<HttpCode3xxEntry>) ((Http3xxCodeResult) bpr).getHttp3xxResCode());
					return;
				case ASYNC_CHECK:
					List<AsyncCheckEntry> res = ((AsyncCheckInScriptResult) bpr).getResults();
					((BpHtmlAsyncLoadTablePanel)resultsTablePanel).setData((Collection<AsyncCheckEntry>) res);
					/*
					 * ((BpHtmlAsyncLoadTablePanel)resultsTablePanel).setData((
					 * Collection<AsyncCheckEntry>) ((AsyncCheckInScriptResult)
					 * bpr).getResults());
					 */
					return;
				case FILE_ORDER:
					((BpHtmlFileOrderTablePanel)resultsTablePanel).setData((Collection<FileOrderEntry>) ((FileOrderResult) bpr).getResults());
					return;
				case DISPLAY_NONE_IN_CSS:
					((BpHtmlDisplayNoneInCSSTablePanel)resultsTablePanel).setData((Collection<DisplayNoneInCSSEntry>) ((DisplayNoneInCSSResult) bpr).getResults());
					return;
				case UNNECESSARY_CONNECTIONS:
					((BpConnectionsUnnecessaryTablePanel)resultsTablePanel).setData((Collection<UnnecessaryConnectionEntry>) ((UnnecessaryConnectionResult) bpr).getTightlyCoupledBurstsDetails());
					return;
				default:
					return;
				}
			}
		}
	}
	
	@Override
	public JPanel layoutDataPanel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

	/**
	 * forward the expand() to the resultsTablePanel
	 */
	@Override
	public void expand() {
		if (resultsTablePanel != null) {
			resultsTablePanel.expand();
		}
	}
}
