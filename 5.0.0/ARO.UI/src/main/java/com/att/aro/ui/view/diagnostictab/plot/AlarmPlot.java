package com.att.aro.ui.view.diagnostictab.plot;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.jfree.chart.annotations.XYPointerAnnotation;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYIntervalSeries;
import org.jfree.data.xy.XYIntervalSeriesCollection;

import com.att.aro.core.ILogger;
import com.att.aro.core.packetanalysis.pojo.ScheduledAlarmInfo;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.peripheral.pojo.AlarmInfo;
import com.att.aro.core.peripheral.pojo.AlarmInfo.AlarmType;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.ui.commonui.ContextAware;
import com.att.aro.ui.utils.ResourceBundleHelper;

public class AlarmPlot implements IPlot {
	private ILogger logger = ContextAware.getAROConfigContext().getBean(
			ILogger.class);

	private XYIntervalSeriesCollection alarmDataCollection = new XYIntervalSeriesCollection();
	private List<XYPointerAnnotation> pointerAnnotation = new ArrayList<XYPointerAnnotation>();
	private Map<AlarmType, XYIntervalSeries> seriesMap = new EnumMap<AlarmType, XYIntervalSeries>(
			AlarmType.class);
	private Map<Double, AlarmInfo> eventMap = new HashMap<Double, AlarmInfo>();
	private Map<Double, ScheduledAlarmInfo> eventMapPending = new HashMap<Double, ScheduledAlarmInfo>();

	@Override
	public void populate(XYPlot plot, AROTraceData analysis) {
		if (analysis == null) {
			logger.info("analysis data is null");
		} else {
			alarmDataCollection.removeAllSeries();
			pointerAnnotation.clear();

			TraceResultType resultType = analysis.getAnalyzerResult()
					.getTraceresult().getTraceResultType();
			if (resultType.equals(TraceResultType.TRACE_FILE)) {
				logger.info("didn't get analysis trace data!");

			} else {
				// Remove old annotation from previous plots
				Iterator<XYPointerAnnotation> pointers = pointerAnnotation
						.iterator();
				while (pointers.hasNext()) {
					plot.removeAnnotation(pointers.next());
				}

				for (AlarmType eventType : AlarmType.values()) {
					XYIntervalSeries series = new XYIntervalSeries(eventType);
					seriesMap.put(eventType, series);
					alarmDataCollection.addSeries(series);
				}
				TraceDirectoryResult traceresult = (TraceDirectoryResult) analysis
						.getAnalyzerResult().getTraceresult();
				List<AlarmInfo> alarmInfos = traceresult.getAlarmInfos();
				List<ScheduledAlarmInfo> pendingAlarms = getHasFiredAlarms(traceresult
						.getScheduledAlarms());
				Iterator<ScheduledAlarmInfo> iterPendingAlarms = pendingAlarms
						.iterator();
				double firedTime = 0;
				while (iterPendingAlarms.hasNext()) {
					ScheduledAlarmInfo scheduledEvent = iterPendingAlarms
							.next();
					AlarmType pendingAlarmType = scheduledEvent.getAlarmType();
					if (pendingAlarmType != null) {
						firedTime = (scheduledEvent.getTimeStamp() - scheduledEvent
								.getRepeatInterval()) / 1000;
						seriesMap.get(pendingAlarmType).add(firedTime,
								firedTime, firedTime, 1, 0.8, 1);
						eventMapPending.put(firedTime, scheduledEvent);
						// logger.fine("populateAlarmScheduledPlot type:\n" +
						// pendingAlarmType
						// + "\ntime " + scheduledEvent.getTimeStamp()
						// + "\nrepeating " + firedTime);
					}
				}

				Iterator<AlarmInfo> iter = alarmInfos.iterator();
				while (iter.hasNext()) {
					AlarmInfo currEvent = iter.next();
					if (currEvent != null) {
						AlarmType alarmType = currEvent.getAlarmType();
						if (alarmType != null) {
							firedTime = currEvent.getTimeStamp() / 1000;

							/*
							 * Catching any alarms align to quanta as being
							 * inexactRepeating alarms
							 */
							if ((currEvent.getTimestampElapsed() / 1000) % 900 < 1) {
								seriesMap.get(alarmType).add(firedTime,
										firedTime, firedTime, 1, 0, 0.7);

								// Adding an arrow to mark these
								// inexactRepeating alarms
								XYPointerAnnotation xypointerannotation = new XYPointerAnnotation(
										alarmType.name(), firedTime, 0.6,
										3.92699082D);
								xypointerannotation.setBaseRadius(20D);
								xypointerannotation.setTipRadius(1D);
								pointerAnnotation.add(xypointerannotation);
								plot.addAnnotation(xypointerannotation);

								// logger.info("SetInexactRepeating alarm type: "
								// + alarmType
								// + " time " + firedTime
								// + " epoch " + currEvent.getTimestampEpoch()
								// + " elapsed:\n" +
								// currEvent.getTimestampElapsed()/1000);
							} else {
								seriesMap.get(alarmType).add(firedTime,
										firedTime, firedTime, 1, 0, 0.5);
							}
							eventMap.put(firedTime, currEvent);
						}
					}
				}
				XYItemRenderer renderer = plot.getRenderer();
				renderer.setSeriesPaint(
						alarmDataCollection.indexOf(AlarmType.RTC_WAKEUP),
						Color.red);

				renderer.setSeriesPaint(
						alarmDataCollection.indexOf(AlarmType.RTC), Color.pink);

				renderer.setSeriesPaint(alarmDataCollection
						.indexOf(AlarmType.ELAPSED_REALTIME_WAKEUP), Color.blue);

				renderer.setSeriesPaint(
						alarmDataCollection.indexOf(AlarmType.ELAPSED_REALTIME),
						Color.cyan);

				renderer.setSeriesPaint(
						alarmDataCollection.indexOf(AlarmType.UNKNOWN),
						Color.black);

				// Assign ToolTip to renderer
				renderer.setBaseToolTipGenerator(new XYToolTipGenerator() {
					@Override
					public String generateToolTip(XYDataset dataset,
							int series, int item) {
						AlarmInfo info = eventMap.get(dataset
								.getX(series, item));
						Date epochTime = new Date();
						if (info != null) {

							epochTime.setTime((long) info.getTimestampEpoch());

							StringBuffer displayInfo = new StringBuffer(
									ResourceBundleHelper
											.getMessageString("alarm.tooltip.prefix"));
							displayInfo.append(MessageFormat.format(
									ResourceBundleHelper
											.getMessageString("alarm.tooltip.content"),
									info.getAlarmType(),
									info.getTimeStamp() / 1000, epochTime
											.toString()));
							if ((info.getTimestampElapsed() / 1000) % 900 < 1) {
								displayInfo.append(ResourceBundleHelper
										.getMessageString("alarm.tooltip.setInexactRepeating"));
							}
							displayInfo.append(ResourceBundleHelper
									.getMessageString("alarm.tooltip.suffix"));
							return displayInfo.toString();
						}
						ScheduledAlarmInfo infoPending = eventMapPending
								.get(dataset.getX(series, item));
						if (infoPending != null) {

							epochTime.setTime((long) (infoPending
									.getTimestampEpoch() - infoPending
									.getRepeatInterval()));

							StringBuffer displayInfo = new StringBuffer(
									ResourceBundleHelper
											.getMessageString("alarm.tooltip.prefix"));
							displayInfo.append(MessageFormat.format(
									ResourceBundleHelper
											.getMessageString("alarm.tooltip.contentWithName"),
									infoPending.getAlarmType(), (infoPending
											.getTimeStamp() - infoPending
											.getRepeatInterval()) / 1000,
									epochTime.toString(), infoPending
											.getApplication(), infoPending
											.getRepeatInterval() / 1000));
							displayInfo.append(ResourceBundleHelper
									.getMessageString("alarm.tooltip.suffix"));
							return displayInfo.toString();
						}
						return null;
					}
				});

			}
		}
		plot.setDataset(alarmDataCollection);
//		return plot;
	}

	private List<ScheduledAlarmInfo> getHasFiredAlarms(
			Map<String, List<ScheduledAlarmInfo>> pendingAlarms) {
		List<ScheduledAlarmInfo> result = new ArrayList<ScheduledAlarmInfo>();
		for (Map.Entry<String, List<ScheduledAlarmInfo>> entry : pendingAlarms
				.entrySet()) {
			List<ScheduledAlarmInfo> alarms = entry.getValue();
			ListIterator itrAlarms = alarms.listIterator();
			while (itrAlarms.hasNext()) {
				ScheduledAlarmInfo alarm = (ScheduledAlarmInfo) itrAlarms
						.next();
				if (alarm.getHasFired() > 0) {
					result.add(alarm);
				}
			}
		}
		return result;
	}

}
