/*
 * Copyright 2014 AT&T
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
 * Abstract best practice result which will be extended by each BestPracticeResult.
 * These various Results should contains more detail specific to each type of best
 * practice.
 * 
 * @author Borey Sao Date: November 6, 2014
 */
public abstract class AbstractBestPracticeResult {
	
	private String overviewTitle = "";
	private String detailTitle = "";
	private boolean isSelfTestBn = false;
	private String aboutText = "";
	private String learnMoreUrl = "";
	private String resultText = "";

	protected BPResultType resultType = BPResultType.PASS;

	/**
	 * Returns the title of the best practice displayed in the tests conducted
	 * list on the Best Practices page.
	 * 
	 * @return the title string
	 */
	public String getOverviewTitle() {
		return overviewTitle;
	}

	/**
	 * Sets title of the best practice displayed in the tests conducted
	 * list on the Best Practices page.
	 * 
	 * @param overviewTitle - title of the best practice
	 */
	public void setOverviewTitle(String overviewTitle) {
		this.overviewTitle = overviewTitle;
	}

	/**
	 * Returns the detailed title of the best practice that is displayed in the detail
	 * panel for the best practice group.
	 * 
	 * @return The title string.
	 */
	public String getDetailTitle() {
		return detailTitle;
	}

	/**
	 * Sets detailed title of the best practice that is displayed in the detail
	 * panel for the best practice group.
	 * 
	 * @param detailTitle - detailed title of the best practice
	 */
	public void setDetailTitle(String detailTitle) {
		this.detailTitle = detailTitle;
	}

	/**
	 * Indicates whether this best practice is a manual test.
	 * 
	 * @return true if best practice is a self test
	 */
	public boolean isSelfTest() {
		return isSelfTestBn;
	}

	/**
	 * Set whether this best practice is a manual test.
	 * 
	 * @param isSelfTest - true if best practice is a self test
	 */
	public void setSelfTest(boolean isSelfTest) {
		this.isSelfTestBn = isSelfTest;
	}

	/**
	 * Returns the detailed About description of the best practice that is
	 * displayed in the detail panel for the best practice group.
	 * 
	 * @return The text string that is the About description.
	 */
	public String getAboutText() {
		return aboutText;
	}

	/**
	 * Sets the detailed About description of the best practice that is
	 * displayed in the detail panel for the best practice group.
	 * 
	 * @param aboutText - detailed About description
	 */
	public void setAboutText(String aboutText) {
		this.aboutText = aboutText;
	}

	/**
	 * Allows a best practice to indicate a link for more detailed information
	 * about the best practice. If not empty, this URI will be displayed as a
	 * "Learn More" link in the detail panel.
	 * 
	 * @return The "Learn More" URI or null if one does not exist.
	 */
	public String getLearnMoreUrl() {
		return learnMoreUrl;
	}

	/**
	 * Set a URL link for more detailed information about a particular Best Practice. 
	 * Allows a best practice to indicate a link for more detailed information
	 * about the best practice. If not empty, this URI will be displayed as a
	 * "Learn More" link in the detail panel.
	 * 
	 * @param learnMoreUrl - URL link for more detailed information
	 */
	public void setLearnMoreUrl(String learnMoreUrl) {
		this.learnMoreUrl = learnMoreUrl;
	}

	/**
	 * Returns a string containing the results of the best practices test based
	 * on the specified analysis data.
	 * 
	 * @return A string containing the test results.
	 */
	public String getResultText() {
		return resultText;
	}

	/**
	 * Set the descriptive result of a Test
	 * 
	 * @param resultText a descriptive result of a Test
	 */
	public void setResultText(String resultText) {
		this.resultText = resultText;
	}

	/**
	 * Result of test, which could be PASS, FAIL or WARNING
	 * 
	 * @return PASS, FAIL or WARNING
	 */
	public BPResultType getResultType() {
		return this.resultType;
	}

	/**
	 * Set resultType to PASS, FAIL or WARNING from child class
	 * 
	 * @param resultType - resultType of PASS, FAIL or WARNING
	 */
	public void setResultType(BPResultType resultType) {
		this.resultType = resultType;
	}

	/**
	 * Type of best practice, which could be one of the many types defined in
	 * BestPracticeType enum
	 * 
	 * @return a BestPracticeType
	 */
	public abstract BestPracticeType getBestPracticeType();
}
