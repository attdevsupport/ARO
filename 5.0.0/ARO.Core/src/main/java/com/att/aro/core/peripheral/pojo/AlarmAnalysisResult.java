package com.att.aro.core.peripheral.pojo;

import java.util.List;
import java.util.Map;

import com.att.aro.core.packetanalysis.pojo.ScheduledAlarmInfo;

public class AlarmAnalysisResult {
	Map<String, List<ScheduledAlarmInfo>> scheduledAlarms;
	List<AlarmAnalysisInfo> statistics;
	public Map<String, List<ScheduledAlarmInfo>> getScheduledAlarms() {
		return scheduledAlarms;
	}
	public void setScheduledAlarms(
			Map<String, List<ScheduledAlarmInfo>> scheduledAlarms) {
		this.scheduledAlarms = scheduledAlarms;
	}
	public List<AlarmAnalysisInfo> getStatistics() {
		return statistics;
	}
	public void setStatistics(List<AlarmAnalysisInfo> statistics) {
		this.statistics = statistics;
	}
	
}
