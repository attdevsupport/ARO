package com.att.aro.core.bestpractice.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.att.aro.core.bestpractice.IBestPractice;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.bestpractice.pojo.SpriteImageEntry;
import com.att.aro.core.bestpractice.pojo.SpriteImageResult;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.PacketInfo;
import com.att.aro.core.packetanalysis.pojo.Session;

public class SpriteImageImpl implements IBestPractice{
	private static final int IMAGE_SIZE_LIMIT = 6144;
	@Value("${spriteimages.title}")
	private String overviewTitle;
	
	@Value("${spriteimages.detailedTitle}")
	private String detailTitle;
	
	@Value("${spriteimages.desc}")
	private String aboutText;
	
	@Value("${spriteimages.url}")
	private String learnMoreUrl;
	
	@Value("${spriteimages.pass}")
	private String textResultPass;
	
	@Value("${spriteimages.results}")
	private String textResults;
	
	@Value("${exportall.csvNumberOfSpriteFiles}")
	private String exportAllNumberOfSpriteFiles;
	
	@Override
	public AbstractBestPracticeResult runTest(PacketAnalyzerResult tracedata) {
		SpriteImageResult result = new SpriteImageResult();
		List<SpriteImageEntry> analysisResults = new ArrayList<SpriteImageEntry>();
		for(Session session:tracedata.getSessionlist()){
			double lastTimeStamp = 0.0;
			HttpRequestResponseInfo lastReqRessInfo = null;
			HttpRequestResponseInfo secondReqRessInfo = null;
			boolean thirdOccurrenceTriggered = false;
			
			for(HttpRequestResponseInfo req:session.getRequestResponseInfo()){
				if(req.getDirection() == HttpDirection.RESPONSE && req.getContentType() != null 
						&& req.getFirstDataPacket() != null 
						&& req.getContentType().contains("image/")
						&& req.getContentLength() < IMAGE_SIZE_LIMIT){
					PacketInfo pktInfo = req.getFirstDataPacket();
					if (lastTimeStamp == 0.0) {
						lastTimeStamp = pktInfo.getTimeStamp();
						lastReqRessInfo = req;
						continue;
					} else{ 
						if ((pktInfo.getTimeStamp() - lastTimeStamp) <= 5.0) {
							if (!thirdOccurrenceTriggered) {
								secondReqRessInfo = req;
								thirdOccurrenceTriggered = true;
								continue;
							} else {
								/* -At this stage 3 images found to be downloaded in 5 secs. store them.
								 * -fix for defect DE26829*/
								
								analysisResults.add(new SpriteImageEntry(lastReqRessInfo));
								
								analysisResults.add(new SpriteImageEntry(secondReqRessInfo));
								
								analysisResults.add(new SpriteImageEntry(req));
								/* -reset the variables to search more such images in this session
								 * -fix for defect DE26829 */
								
								lastTimeStamp = 0.0;
								lastReqRessInfo = null;
								secondReqRessInfo = null;
								thirdOccurrenceTriggered = false;
							}
						}
						lastTimeStamp = pktInfo.getTimeStamp();
						lastReqRessInfo = req;
						secondReqRessInfo = null;
						thirdOccurrenceTriggered = false;
					}
				}
			}
		}
		result.setAnalysisResults(analysisResults);
		String text = "";
		if(analysisResults.isEmpty()){
			result.setResultType(BPResultType.PASS);
			text = MessageFormat.format(textResultPass, analysisResults.size());
			result.setResultText(text);
		}else{
			result.setResultType(BPResultType.FAIL);
			text = MessageFormat.format(textResults, analysisResults.size());
			result.setResultText(text);
		}
		
		result.setAboutText(aboutText);
		result.setDetailTitle(detailTitle);
		result.setLearnMoreUrl(learnMoreUrl);
		result.setOverviewTitle(overviewTitle);
		result.setExportAllNumberOfSpriteFiles(exportAllNumberOfSpriteFiles);
		return result;
	}

}
