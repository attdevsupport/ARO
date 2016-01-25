/**
 * 
 */
package com.att.aro.json;

import java.util.Collection;

import com.att.aro.model.ApplicationPacketSummary;
import com.att.aro.model.IPPacketSummary;
import com.att.aro.model.TraceData;

/**
 * @author hy0910
 *
 */
public class EndpointSummaryGenerator {
	
	Collection<ApplicationPacketSummary> appPacketSummary;
	Collection<IPPacketSummary> ipEndpointSummary;
	
	public EndpointSummaryGenerator(){
		
	}
	
	public void refresh(TraceData.Analysis analysis){
		appPacketSummary = analysis.getApplicationPacketSummary();
		ipEndpointSummary = analysis.getIpPacketSummary();
	}
	
	public ApplicationPacketSummary[] getAppPacketSummary(){
		return appPacketSummary.toArray(new ApplicationPacketSummary[appPacketSummary.size()]);
	}
	
	public IPPacketSummary[] getIPPacketSummary(){
		return ipEndpointSummary.toArray(new IPPacketSummary[ipEndpointSummary.size()]);
	}

}
