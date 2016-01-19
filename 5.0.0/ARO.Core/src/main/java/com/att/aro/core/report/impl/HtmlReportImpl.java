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
package com.att.aro.core.report.impl;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.att.aro.core.ILogger;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.bestpractice.pojo.BPResultType;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.model.InjectLogger;
import com.att.aro.core.packetanalysis.pojo.AbstractTraceResult;
import com.att.aro.core.packetanalysis.pojo.EnergyModel;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.report.IReport;

public class HtmlReportImpl implements IReport {

	@Autowired
	private IFileManager filereader;	

	@InjectLogger
	private static ILogger logger;
  
	@Override
	public boolean reportGenerator(String resultFilePath,
			AROTraceData results) {
		if (results == null) {
			return false;
		} 
		 
		PacketAnalyzerResult analyzerResults = results.getAnalyzerResult();
		
		List<AbstractBestPracticeResult> bpResults = results.getBestPracticeResults();
 
		StringBuffer htmlString = new StringBuffer(200);
		htmlString.append(getHtmlHead());
		htmlString.append("	<body>");
		htmlString.append(System.getProperty(lineSeperator()));
		htmlString.append("		<table class='table'>");
		htmlString.append(System.getProperty(lineSeperator()));
		htmlString.append(getTableHeader(analyzerResults));
		htmlString.append(getTraceRows(analyzerResults));
		htmlString.append(getBPSummaryRows(bpResults));
		htmlString.append("<tr><th></th><td></td></tr><tr><th>Best Practices Results</th><td></td></tr>\n");
		htmlString.append(getBpRows(bpResults));
		htmlString.append("		</table>");
		htmlString.append(System.getProperty(lineSeperator()));
		htmlString.append(System.getProperty(lineSeperator()));
		htmlString.append("	</body>");
		htmlString.append(System.getProperty(lineSeperator()));
		htmlString.append("</html>");
		try { 
			File file = filereader.createFile(resultFilePath);
			PrintWriter writer = new PrintWriter(file);
			writer.println(htmlString.toString());
			writer.close();
			return true;
		} catch (IOException e) {
			logger.info("IOException: "+e);
		}

		return false;
	}

	// writes the HTML head tag and its components
	private String getHtmlHead() {
		StringBuffer htmlHead = new StringBuffer(300);

		htmlHead.append("<!DOCTYPE html>");
		htmlHead.append(System.getProperty(lineSeperator()));
		htmlHead.append("<html>");
		htmlHead.append(System.getProperty(lineSeperator()));
		htmlHead.append("	<head>");
		htmlHead.append(System.getProperty(lineSeperator()));
		htmlHead.append("		<title>ARO Best Practices</title>");
		htmlHead.append(System.getProperty(lineSeperator()));
		htmlHead.append(getCss());
		htmlHead.append(System.getProperty(lineSeperator()));
		htmlHead.append("	</head>");
		htmlHead.append(System.getProperty(lineSeperator()));

		return htmlHead.toString();
	}


	private String getTableHeader(PacketAnalyzerResult analyzerResults) {
		AbstractTraceResult traceResults = analyzerResults.getTraceresult();
		StringBuffer sbuffer = new StringBuffer(150);

		sbuffer.append(tableLIne()+"<th>File</th><th>"+traceResults.getTraceDirectory()+ "</th></tr>");	

		sbuffer.append(System.getProperty(lineSeperator())); 
		return sbuffer.toString();
	}
	
	private String getTraceRows(PacketAnalyzerResult analyzerResults) {
		StringBuffer sbuffer = new StringBuffer(180);
		
		AbstractTraceResult traceResults = analyzerResults.getTraceresult();
		if(traceResults!=null){		

		sbuffer.append(tableLIne()+"<th>Date</th><td>" + traceResults.getTraceDateTime() + tableChange()
				+System.getProperty(lineSeperator())+tableLIne()+"<th>Duration (Second)</th><td>" + getRoundDouble(traceResults.getTraceDuration())
				+tableChange()+System.getProperty(lineSeperator()));

		//if it is from rooted collector and load from trace trace folder
		TraceResultType traceType = traceResults.getTraceResultType();

			if(TraceResultType.TRACE_DIRECTORY.equals(traceType)){
				TraceDirectoryResult traceDirResult = (TraceDirectoryResult)traceResults;
				sbuffer.append(tableLIne());
				sbuffer.append("<th>Application Names</th><td>");					
				Map<String, String> appVersionMap = traceDirResult.getAppVersionMap();
				
				//getAppInfos() and getAppVersionMap() are never null
				if(traceDirResult.getAppInfos().size() > 0){
					for(String appname : traceDirResult.getAppInfos()){
						sbuffer.append("<p>").append(appname);
						if(appVersionMap.containsKey(appname)){
							sbuffer.append(':').append(appVersionMap.get(appname));
						}
						sbuffer.append("</p>");
					}
				}else{
					sbuffer.append("<p>Not Available</p>");
				}
				sbuffer.append(tableChange());			
				
				sbuffer.append(System.getProperty(lineSeperator()));
				
				sbuffer.append(tableLIne());
				sbuffer.append("<th>Device Make/Model</th>");
				if(traceDirResult.getDeviceMake()!=null&&traceDirResult.getDeviceModel()!=null){
					sbuffer.append("<td>" + traceDirResult.getDeviceMake() + " / " + traceDirResult.getDeviceModel()+ "</td>");
				}else{
					sbuffer.append("<p>Not Available</p>");
				}
				sbuffer.append("</tr>");
				sbuffer.append(System.getProperty(lineSeperator()));
				
				sbuffer.append(tableLIne());
				sbuffer.append("<th>Platform Version</th>");
				if(traceDirResult.getOsType()!=null&&traceDirResult.getOsVersion()!=null){
					sbuffer.append("<td>" + traceDirResult.getOsType()+ " / " +traceDirResult.getOsVersion() + "</td>");
				}else{
					sbuffer.append("<p>Not Available</p>");
				}
				sbuffer.append("</tr>");
				sbuffer.append(System.getProperty(lineSeperator()));
			}
		
		}

		Statistic statistic = analyzerResults.getStatistic();
		if(statistic!=null){
			EnergyModel energyModel = analyzerResults.getEnergyModel();
			sbuffer.append(tableLIne()+"<th>Total Data (Byte)</th><td>" + statistic.getTotalByte() 
					+ tableChange()+System.getProperty(lineSeperator())+tableLIne()+"<th>HTTPS Data Not Analyzed (Byte)</th><td>" 
					+ statistic.getTotalHTTPSByte() + tableChange()+System.getProperty(lineSeperator())
					+tableLIne()+"<th>Energy Consumed (J)</th><td>" + getRoundDouble(energyModel.getTotalEnergyConsumed()) 
					+ tableChange()+System.getProperty(lineSeperator()));

		}
		return sbuffer.toString();
	}
 
	private double getRoundDouble(double number){

		BigDecimal bdecimal = new BigDecimal(number);
	    bdecimal = bdecimal.setScale(2, RoundingMode.HALF_UP);
	    return bdecimal.doubleValue();
			
	}
	
	private String getBPSummaryRows(List<AbstractBestPracticeResult> bpResults) {

		int pass = 0;
		int fail = 0;
		int warning = 0;
		int selftest =0;
		for (AbstractBestPracticeResult result : bpResults) {

			if (result.getResultType() == BPResultType.PASS) {
				pass++;
			} else if (result.getResultType() == BPResultType.FAIL) {
				fail++;
			} else if (result.getResultType()== BPResultType.WARNING){
				warning++;
			} else{
				selftest++;
			}
		}

		StringBuffer sbuffer = new StringBuffer(160);
		sbuffer.append(tableLIne()+"<th>Best Practices Passed</th><td>" + pass 
					+ tableChange()+System.getProperty(lineSeperator())+tableLIne()
					+"<th>Best Practices Failed</th><td>" + fail 
					+ tableChange()+System.getProperty(lineSeperator())
					+tableLIne()+"<th>Best Practices with Warnings</th><td>" + warning
					+tableLIne()+"<th>Best Practices with Self Test</th><td>" +selftest
					+ tableChange()+System.getProperty(lineSeperator()));
		
		return sbuffer.toString();
	}

	// gets all bp rows - name, result (pass/fail/warning/selftest)
	// end of first table - difference in columns in 2nd table
	private String getBpRows(List<AbstractBestPracticeResult> bpResults) {
		ArrayList<StringBuffer> sbTemps = new ArrayList<StringBuffer>();
		// makes the row start tag and BP name cell
		for (int row = 0; row < bpResults.size(); row++) {
			StringBuffer temp = new StringBuffer(65);
			temp.append(tableLIne()+"<tr><th rowspan=\"2\">" + 
			"<a href =\""+ bpResults.get(row).getLearnMoreUrl()+"\" target=\"_blank\" >"+
					bpResults.get(row).getBestPracticeType()
					+ "<a></th>");
			sbTemps.add(temp);
		}

		// makes the cells containing test results
		// cells are colored according to their results
 
		for (int row = 0; row < bpResults.size(); row++) {
			StringBuffer temp = sbTemps.get(row);
			BPResultType result = bpResults.get(row).getResultType();
			if (result.equals(BPResultType.PASS)) {				
				temp.append("<td class='success'>"+result + tableChange()+"<tr><td class='success'>"+bpResults.get(row).getResultText()+tableSeperate());
			} else if (result.equals(BPResultType.FAIL)) {
				temp.append("<td class='danger'>"+ result + tableChange()+"<tr><td class='danger'>"+bpResults.get(row).getResultText()+tableSeperate());
			} else if (result.equals(BPResultType.WARNING)) {
				temp.append("<td class='warning'>"+result + tableChange()+"<tr><td class='warning'>"+bpResults.get(row).getResultText()+tableSeperate());
			} else {
				temp.append("<td class='info'>"+result + tableChange()+"<tr><td class='info'>"+bpResults.get(row).getResultText()+tableSeperate());
			}			
			
		}
		
		// makes the row end tag and starts new line
		for (int row = 0; row < sbTemps.size(); row++) {
			StringBuffer temp = sbTemps.get(row);
			temp.append("</tr>");
			temp.append(System.getProperty(lineSeperator()));
		}

		StringBuffer finalBpRows = new StringBuffer();
		for (int i = 0; i < sbTemps.size(); i++) {
			finalBpRows.append(sbTemps.get(i).toString());
		}

		return finalBpRows.toString();
	}
	
	private String lineSeperator(){
		return "line.separator";
	}
	
	private String tableLIne(){
		return "\t\t\t<tr>";
	}
	
	private String tableChange(){
		return "</td></tr>";
	}

	private String tableSeperate(){
		return "</td></tr><tr><th></th><td></td></tr>";
	}
	private String getCss(){
		StringBuilder str = new StringBuilder(416).append("<style type=\"text/css\">").append("html {").append("  font-family: sans-serif;")
		.append("  -webkit-text-size-adjust: 100%;").append("      -ms-text-size-adjust: 100%;")
		.append("}").append("body {").append("  margin: 0;").append("}").append("table {").append("  border-spacing: 0;")
		.append("  border-collapse: collapse;  }").append("td,").append("th {").append("  padding: 0; }")
		.append("@media (min-width: 1200px) {")
		.append("  .col-lg-1, .col-lg-2, .col-lg-3, .col-lg-4, .col-lg-5, .col-lg-6, .col-lg-7, .col-lg-8, .col-lg-9, .col-lg-10, .col-lg-11, .col-lg-12 {")
		.append("    float: left;  }").append("  .col-lg-12 {").append("    width: 100%;  }")
		.append("  .col-lg-11 {").append("    width: 91.66666667%;  }").append("  .col-lg-10 {")
		.append("    width: 83.33333333%;  }").append("  .col-lg-9 {").append("    width: 75%;  }")
		.append("  .col-lg-8 {").append("    width: 66.66666667%;  }").append("  .col-lg-7 {")
		.append("    width: 58.33333333%;  }").append("  .col-lg-6 {").append("    width: 50%;  }")
		.append("  .col-lg-5 {").append("    width: 41.66666667%; }").append("  .col-lg-4 {")
		.append("    width: 33.33333333%;  }").append("  .col-lg-3 {").append("    width: 25%;  }")
		.append("  .col-lg-2 {").append("    width: 16.66666667%;  }").append("  .col-lg-1 {")
		.append("    width: 8.33333333%;  }").append("  .col-lg-pull-12 {").append("    right: 100%;  }")
		.append("  .col-lg-pull-11 {").append("    right: 91.66666667%;  }").append("  .col-lg-pull-10 {")
		.append("    right: 83.33333333%;  }").append("  .col-lg-pull-9 {").append("    right: 75%;  }")
		.append("  .col-lg-pull-8 {").append("    right: 66.66666667%;  }").append("  .col-lg-pull-7 {")
		.append("    right: 58.33333333%;  }").append("  .col-lg-pull-6 {").append("    right: 50%;  }")
		.append("  .col-lg-pull-5 {").append("    right: 41.66666667%;  }").append("  .col-lg-pull-4 {")
		.append("    right: 33.33333333%;  }").append("  .col-lg-pull-3 {").append("    right: 25%;  }")
		.append("  .col-lg-pull-2 {").append("    right: 16.66666667%;  }").append("  .col-lg-pull-1 {")
		.append("    right: 8.33333333%;  }").append("  .col-lg-pull-0 {").append("    right: auto;  }")
		.append("  .col-lg-push-12 {").append("    left: 100%;  }").append("  .col-lg-push-11 {")
		.append("    left: 91.66666667%;  }").append("  .col-lg-push-10 {").append("    left: 83.33333333%;  }")
		.append("  .col-lg-push-9 {").append("    left: 75%;  }").append("  .col-lg-push-8 {")
		.append("    left: 66.66666667%;  }").append("  .col-lg-push-7 {").append("    left: 58.33333333% }")
		.append("  .col-lg-push-6 {").append("    left: 50%;  }").append("  .col-lg-push-5 {")
		.append("    left: 41.66666667%;  }").append("  .col-lg-push-4 {").append("    left: 33.33333333%; }")
		.append("  .col-lg-push-3 {").append("    left: 25%;  }").append("  .col-lg-push-2 {")
		.append("    left: 16.66666667%;  }").append("  .col-lg-push-1 {").append("    left: 8.33333333%;  }")
		.append("  .col-lg-push-0 {").append("    left: auto;  }").append("  .col-lg-offset-12 {")
		.append("    margin-left: 100%; }").append("  .col-lg-offset-11 {").append("    margin-left: 91.66666667%;  }")
		.append("  .col-lg-offset-10 {").append("    margin-left: 83.33333333%;  }").append("  .col-lg-offset-9 {")
		.append("    margin-left: 75%;  }").append("  .col-lg-offset-8 {").append("    margin-left: 66.66666667%;  }")
		.append("  .col-lg-offset-7 {").append("    margin-left: 58.33333333%;  }").append("  .col-lg-offset-6 {")
		.append("    margin-left: 50%;  }").append("  .col-lg-offset-5 {").append("    margin-left: 41.66666667%;  }")
		.append("  .col-lg-offset-4 {").append("    margin-left: 33.33333333%;  }").append("  .col-lg-offset-3 {")
		.append("    margin-left: 25%; }").append("  .col-lg-offset-2 {").append("    margin-left: 16.66666667%;  }")
		.append("  .col-lg-offset-1 {").append("    margin-left: 8.33333333%;  }")
		.append("  .col-lg-offset-0 {").append("    margin-left: 0;  } }").append("table {  background-color: transparent; }")
		.append("caption {  padding-top: 8px;").append("  padding-bottom: 8px;")
		.append("  color: #777;").append("  text-align: left; }").append("th {").append("  text-align: left; }")
		.append(".table {  width: 100%;").append("  max-width: 100%;").append("  margin-bottom: 20px;}")
		.append(".table > thead > tr > th,").append(".table > tbody > tr > th,").append(".table > tfoot > tr > th,")
		.append(".table > thead > tr > td,").append(".table > tbody > tr > td,").append(".table > tfoot > tr > td {")
		.append("  padding: 8px;").append("  line-height: 1.42857143;").append("  vertical-align: top;")
		.append("  border-top: 1px solid #ddd; }").append(".table > thead > tr > th {").append("  vertical-align: bottom; }")
		.append(".table > caption + thead > tr:first-child > th,").append(".table > colgroup + thead > tr:first-child > th,")
		.append(".table > caption + thead > tr:first-child > td,").append(".table > colgroup + thead > tr:first-child > td,")
		.append(".table > thead:first-child > tr:first-child > td {").append("  border-top: 0;").append("}").append(".table > tbody + tbody {")
		.append("  border-top: 2px solid #ddd;}").append(".table .table {").append("  background-color: #fff; }")
		.append(".table-condensed > thead > tr > th,").append(".table-condensed > tbody > tr > th,").append(".table-condensed > tfoot > tr > th,")
		.append(".table-condensed > thead > tr > td,").append(".table-condensed > tbody > tr > td,")
		.append(".table-condensed > tfoot > tr > td {   padding: 5px; }").append(".table-bordered {")
		.append("  border: 1px solid #ddd; }").append(".table-bordered > thead > tr > th,").append(".table-bordered > tbody > tr > th,")
		.append(".table-bordered > tfoot > tr > th,").append(".table-bordered > thead > tr > td,")
		.append(".table-bordered > tbody > tr > td,").append(".table-bordered > tfoot > tr > td {")
		.append("  border: 1px solid #ddd; }").append(".table-bordered > thead > tr > th,")
		.append(".table-bordered > thead > tr > td {").append("  border-bottom-width: 2px; }")
		.append(".table-striped > tbody > tr:nth-of-type(odd) {").append("  background-color: #f9f9f9; }")
		.append(".table-hover > tbody > tr:hover {").append("  background-color: #f5f5f5; }")
		.append("table col[class*=\"col-\"] {").append("  position: static;").append("  display: table-column;")
		.append("  float: none; }").append("table td[class*=\"col-\"],").append("table th[class*=\"col-\"] {")
		.append("  position: static;").append("  display: table-cell;").append("  float: none; }")
		.append(".table > thead > tr > td.active,").append(".table > tbody > tr > td.active,")
		.append(".table > tfoot > tr > td.active,").append(".table > thead > tr > th.active,").append(".table > tbody > tr > th.active,")
		.append(".table > tfoot > tr > th.active,").append(".table > thead > tr.active > td,").append(".table > tbody > tr.active > td,")
		.append(".table > tfoot > tr.active > td,").append(".table > thead > tr.active > th,").append(".table > tbody > tr.active > th,")
		.append(".table > tfoot > tr.active > th {").append("  background-color: #f5f5f5; }").append(".table-hover > tbody > tr > td.active:hover,")
		.append(".table-hover > tbody > tr > th.active:hover,").append(".table-hover > tbody > tr.active:hover > td,")
		.append(".table-hover > tbody > tr:hover > .active,").append(".table-hover > tbody > tr.active:hover > th {")
		.append("  background-color: #e8e8e8; }").append(".table > thead > tr > td.success,")
		.append(".table > tbody > tr > td.success,").append(".table > tfoot > tr > td.success,").append(".table > thead > tr > th.success,")
		.append(".table > tbody > tr > th.success,").append(".table > tfoot > tr > th.success,").append(".table > thead > tr.success > td,")
		.append(".table > tbody > tr.success > td,").append(".table > tfoot > tr.success > td,").append(".table > thead > tr.success > th,")
		.append(".table > tfoot > tr.success > th {  background-color: #dff0d8; }").append(".table-hover > tbody > tr > td.success:hover,")
		.append(".table-hover > tbody > tr > th.success:hover,").append(".table-hover > tbody > tr:hover > .success,")
		.append(".table-hover > tbody > tr.success:hover > th {")
		.append("  background-color: #d0e9c6; }").append(".table > thead > tr > td.info,").append(".table > tbody > tr > td.info,")
		.append(".table > tfoot > tr > td.info,").append(".table > thead > tr > th.info,").append(".table > tbody > tr > th.info,")
		.append(".table > tfoot > tr > th.info,").append(".table > thead > tr.info > td,").append(".table > tbody > tr.info > td,")
		.append(".table > tfoot > tr.info > td,").append(".table > thead > tr.info > th,").append(".table > tbody > tr.info > th,")
		.append(".table > tfoot > tr.info > th {").append("  background-color: #d9edf7; }")
		.append(".table-hover > tbody > tr > td.info:hover,").append(".table-hover > tbody > tr > th.info:hover,")
		.append(".table-hover > tbody > tr.info:hover > td,").append(".table-hover > tbody > tr:hover > .info,")
		.append(".table-hover > tbody > tr.info:hover > th {").append("  background-color: #c4e3f3;").append("}")
		.append(".table > thead > tr > td.warning,").append(".table > tbody > tr > td.warning,").append(".table > tfoot > tr > td.warning,")
		.append(".table > thead > tr > th.warning,").append(".table > tbody > tr > th.warning,").append(".table > tfoot > tr > th.warning,")
		.append(".table > thead > tr.warning > td,").append(".table > tbody > tr.warning > td,").append(".table > tfoot > tr.warning > td,")
		.append(".table > thead > tr.warning > th,").append(".table > tbody > tr.warning > th,").append(".table > tfoot > tr.warning > th {")
		.append("  background-color: #fcf8e3; }").append(".table-hover > tbody > tr > td.warning:hover,").append(".table-hover > tbody > tr > th.warning:hover,")
		.append(".table-hover > tbody > tr.warning:hover > td,").append(".table-hover > tbody > tr:hover > .warning,").append(".table-hover > tbody > tr.warning:hover > th {")
		.append("  background-color: #faf2cc; }").append(".table > thead > tr > td.danger,").append(".table > tbody > tr > td.danger,")
		.append(".table > tfoot > tr > td.danger,").append(".table > thead > tr > th.danger,").append(".table > tbody > tr > th.danger,")
		.append(".table > tfoot > tr > th.danger,").append(".table > thead > tr.danger > td,").append(".table > tbody > tr.danger > td,")
		.append(".table > tfoot > tr.danger > td,").append(".table > thead > tr.danger > th,").append(".table > tbody > tr.danger > th,")
		.append(".table > tfoot > tr.danger > th {").append("  background-color: #f2dede; }").append(".table-hover > tbody > tr > td.danger:hover,")
		.append(".table-hover > tbody > tr > th.danger:hover,").append(".table-hover > tbody > tr.danger:hover > td,").append(".table-hover > tbody > tr:hover > .danger,")
		.append(".table-hover > tbody > tr.danger:hover > th {").append("  background-color: #ebcccc;").append("}").append(".table-responsive {")
		.append("  min-height: .01%;").append("  overflow-x: auto; }").append("@media screen and (max-width: 767px) {").append("  .table-responsive {")
		.append("    width: 100%;").append("    margin-bottom: 15px;").append("    overflow-y: hidden;").append("    -ms-overflow-style: -ms-autohiding-scrollbar;")
		.append("    border: 1px solid #ddd;  }").append("  .table-responsive > .table {").append("    margin-bottom: 0;   }")
		.append("  .table-responsive > .table > thead > tr > th,").append("  .table-responsive > .table > tbody > tr > th,")
		.append("  .table-responsive > .table > tfoot > tr > th,").append("  .table-responsive > .table > thead > tr > td,")
		.append("  .table-responsive > .table > tbody > tr > td,").append("  .table-responsive > .table > tfoot > tr > td {")
		.append("    white-space: nowrap;  }").append("  .table-responsive > .table-bordered {").append("    border: 0;   }").append("  .table-responsive > .table-bordered > thead > tr > th:first-child,")
		.append("  .table-responsive > .table-bordered > tbody > tr > th:first-child,")
		.append("  .table-responsive > .table-bordered > tfoot > tr > th:first-child,")
		.append("  .table-responsive > .table-bordered > thead > tr > td:first-child,")
		.append("  .table-responsive > .table-bordered > tbody > tr > td:first-child,")
		.append("  .table-responsive > .table-bordered > tfoot > tr > td:first-child {")
		.append("    border-left: 0;  }")
		.append("  .table-responsive > .table-bordered > thead > tr > th:last-child,")
		.append("  .table-responsive > .table-bordered > tbody > tr > th:last-child,")
		.append("  .table-responsive > .table-bordered > tfoot > tr > th:last-child,")
		.append("  .table-responsive > .table-bordered > thead > tr > td:last-child,")
		.append("  .table-responsive > .table-bordered > tbody > tr > td:last-child,")
		.append("  .table-responsive > .table-bordered > tfoot > tr > td:last-child {")
		.append("    border-right: 0;  }").append("  .table-responsive > .table-bordered > tbody > tr:last-child > th,")
		.append("  .table-responsive > .table-bordered > tfoot > tr:last-child > th,")
		.append("  .table-responsive > .table-bordered > tbody > tr:last-child > td,")
		.append("  .table-responsive > .table-bordered > tfoot > tr:last-child > td {")
		.append("    border-bottom: 0;  }  }").append("		</style>");
		return str.toString();
	}
}
