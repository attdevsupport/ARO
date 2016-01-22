/**
 *  Copyright 2016 AT&T
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

import java.util.List;

public class SpriteImageResult extends AbstractBestPracticeResult {
	private List<SpriteImageEntry> analysisResults = null;
	private String exportAllNumberOfSpriteFiles;
	
	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.SPRITEIMAGE;
	}
	public List<SpriteImageEntry> getAnalysisResults() {
		return analysisResults;
	}
	public void setAnalysisResults(List<SpriteImageEntry> analysisResults) {
		this.analysisResults = analysisResults;
	}
	public String getExportAllNumberOfSpriteFiles() {
		return exportAllNumberOfSpriteFiles;
	}
	public void setExportAllNumberOfSpriteFiles(String exportAllNumberOfSpriteFiles) {
		this.exportAllNumberOfSpriteFiles = exportAllNumberOfSpriteFiles;
	}
	
}
