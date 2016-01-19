package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IAlarmDumpsysTimestampReader;
import com.att.aro.core.peripheral.pojo.AlarmDumpsysTimestamp;
import com.att.aro.core.util.Util;

public class AlarmDumpsysTimestampReaderImplTest extends BaseTest {

	AlarmDumpsysTimestampReaderImpl traceDataReader;
	
	private IFileManager filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		filereader = Mockito.mock(IFileManager.class);
		traceDataReader = (AlarmDumpsysTimestampReaderImpl)context.getBean(IAlarmDumpsysTimestampReader.class);
		traceDataReader.setFileReader(filereader);
		traceFolder = "traceFolder";
	}

	@Test
	public void readData_alarm_info_end() throws IOException {
		/*
		 *  first test using "alarm_info_end" data
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] array_alarm_info_end = getMockedFileData2();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(array_alarm_info_end);
		AlarmDumpsysTimestamp alarmDumpsysTimestamp = traceDataReader.readData(traceFolder, new Date((long) (1.412361675045E9 * 1000)), 0, "4.0.4", 65524.92);

		assertEquals(1.412361724E12, alarmDumpsysTimestamp.getDumpsysEpochTimestamp(), 0);
		assertEquals(6.5574186E7, alarmDumpsysTimestamp.getDumpsysElapsedTimestamp(), 0);
		
	}

	@Test
	public void readData_alarm_info_end2() throws IOException {
		/*
		 *  first test using "alarm_info_end" data
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		String[] array_alarm_info_end = getMockedFileData21();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(array_alarm_info_end);
		AlarmDumpsysTimestamp alarmDumpsysTimestamp = traceDataReader.readData(traceFolder, new Date((long) 1.269821752138E9 * 1000), 0, "2.1", 65524.92);

		double x = alarmDumpsysTimestamp.getDumpsysEpochTimestamp();
		double y = alarmDumpsysTimestamp.getDumpsysElapsedTimestamp();
		assertEquals(1.269821813284E12, x, 0);
		assertEquals(1.0930327E7, alarmDumpsysTimestamp.getDumpsysElapsedTimestamp(), 0);
		
	}

	@Test
	public void readData_alarm_info_start() throws IOException {
		/* 
		 * second test using "alarm_info_start" data
		 */
		Mockito.when(filereader.fileExist(traceFolder + Util.FILE_SEPARATOR + "alarm_info_end")).thenReturn(false);
		Mockito.when(filereader.fileExist(traceFolder + Util.FILE_SEPARATOR + "alarm_info_start")).thenReturn(true);
		String[] array_alarm_info_start = getMockedFileData();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(array_alarm_info_start);
		AlarmDumpsysTimestamp alarmDumpsysTimestamp = traceDataReader.readData(traceFolder, new Date((long) (1.412361675045E9 * 1000)), 0, "4.0.4", 65524.92);

		assertEquals(1.412361727E12, alarmDumpsysTimestamp.getDumpsysEpochTimestamp(), 0);
		assertEquals(6.5577447E7, alarmDumpsysTimestamp.getDumpsysElapsedTimestamp(), 0);

	}

	@Test
	public void fileNotFound() throws IOException {
		/*
		 * file not found
		 */
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(false);
		String[] array_alarm_info_start = getMockedFileData();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(array_alarm_info_start);
		AlarmDumpsysTimestamp alarmDumpsysTimestamp = null;
		alarmDumpsysTimestamp = traceDataReader.readData(traceFolder, new Date((long) (1.412361675045E9 * 1000)), 0, "4.0.4", 65524.92);

		assertEquals(alarmDumpsysTimestamp,null);
				
	}

	@Test
	public void readData_Exception_readAllLine() throws IOException {
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenThrow(new IOException("Exception_readAllLine"));

		AlarmDumpsysTimestamp alarmDumpsysTimestamp = null;
		alarmDumpsysTimestamp = traceDataReader.readData(traceFolder, new Date((long) (1.412361675045E9 * 1000)), 0, "4.0.4", 65524.92);
		assertTrue(alarmDumpsysTimestamp == null);

	}

	/*
	 * from Android 2.1
	 */
	private String[] getMockedFileData21() {
		return new String[] {"Currently running services:"
				,"  alarm"
				,"-------------------------------------------------------------------------------"
				,"DUMP OF SERVICE alarm:"
				,"Current Alarm Manager state:"
				," "
				,"  Realtime wakeup (now=1269821813284):"
				,"  RTC_WAKEUP #2: Alarm{47c8e120 type 0 com.android.providers.calendar}"
				,"    type=0 when=1269894990151 repeatInterval=0 count=0"
				,"    operation=PendingIntent{47c8e110: PendingIntentRecord{47c8e098 com.android.providers.calendar broadcastIntent}}"
				,"  RTC_WAKEUP #1: Alarm{47c36780 type 0 com.linegames.crazymouse}"
				,"    type=0 when=1210672511455 repeatInterval=14400000 count=0"
				,"    operation=PendingIntent{4799ae20: PendingIntentRecord{479fe890 com.linegames.crazymouse broadcastIntent}}"
				,"  RTC_WAKEUP #0: Alarm{47c365b8 type 0 com.linegames.crazymouse}"
				,"    type=0 when=-3547037789034714 repeatInterval=0 count=0"
				,"    operation=PendingIntent{4799cbf0: PendingIntentRecord{47a1b170 com.linegames.crazymouse broadcastIntent}}"
				,"  RTC #2: Alarm{47b5b270 type 1 com.google.android.providers.subscribedfeeds}"
				,"    type=1 when=1340596583926 repeatInterval=0 count=0"
				,"    operation=PendingIntent{47b6c200: PendingIntentRecord{47b66090 com.google.android.providers.subscribedfeeds broadcastIntent}}"
				,"  RTC #1: Alarm{4793b598 type 1 android}"
				,"    type=1 when=1269889200000 repeatInterval=0 count=0"
				,"    operation=PendingIntent{479f4620: PendingIntentRecord{479cbfd8 android broadcastIntent}}"
				,"  RTC #0: Alarm{47b26df8 type 1 android}"
				,"    type=1 when=1269821820000 repeatInterval=0 count=0"
				,"    operation=PendingIntent{4795daf0: PendingIntentRecord{479cbf70 android broadcastIntent}}"
				," "
				,"  Elapsed realtime wakeup (now=10930327):"
				,"  ELAPSED_WAKEUP #3: Alarm{47dc4198 type 2 android}"
				,"    type=2 when=59274268549 repeatInterval=0 count=0"
				,"    operation=PendingIntent{478c3730: PendingIntentRecord{47997ce8 android broadcastIntent}}"
				,"  ELAPSED_WAKEUP #2: Alarm{47d1dea0 type 2 android}"
				,"    type=2 when=129138686 repeatInterval=0 count=0"
				,"    operation=PendingIntent{478b0ef0: PendingIntentRecord{47995618 android broadcastIntent}}"
				,"  ELAPSED_WAKEUP #1: Alarm{47acbb18 type 2 com.qo.android.samsung.am3}"
				,"    type=2 when=86526542 repeatInterval=86400000 count=1"
				,"    operation=PendingIntent{47b703a8: PendingIntentRecord{47c4d510 com.qo.android.samsung.am3 broadcastIntent}}"
				,"  ELAPSED_WAKEUP #0: Alarm{47c337d8 type 2 com.google.android.location}"
				,"    type=2 when=10937510 repeatInterval=0 count=0"
				,"    operation=PendingIntent{47bd9298: PendingIntentRecord{47b22190 com.google.android.location broadcastIntent}}"
				," "
				,"  Broadcast ref count: 0"
				," "
				,"  Alarm Stats:"
				,"  com.qo.android.samsung.am3"
				,"    182ms running, 1 wakeups"
				,"    1 alarms: flg=0x4"
				,"  com.google.android.location"
				,"    44868ms running, 484 wakeups"
				,"    484 alarms: act=com.google.android.location.ALARM_WAKEUP flg=0x4"
				,"  android"
				,"    22086ms running, 5 wakeups"
				,"    181 alarms: act=android.intent.action.TIME_TICK flg=0x40000004"
				,"    1 alarms: act=android.content.syncmanager.SYNC_POLL_ALARM flg=0x4"
				,"    4 alarms: act=android.content.syncmanager.SYNC_ALARM flg=0x4"
		};
	}
	
	private String[] getMockedFileData() {
		return new String[] {"Current Alarm Manager state:",
				" ",
				"  Realtime wakeup (now=2014-10-03 11:42:07  val=1412361727528):",
				"  RTC_WAKEUP #7: Alarm{414d44e0 type 0 com.redbend.vdmc}",
				"    type=0 when=+88d12h39m52s472ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40f29930: PendingIntentRecord{412e9930 com.redbend.vdmc broadcastIntent}}",
				"  RTC_WAKEUP #6: Alarm{4105e910 type 0 com.google.android.gsf}",
				"    type=0 when=+5d10h21m14s464ms repeatInterval=565741000 count=0",
				"    operation=PendingIntent{40f6eb50: PendingIntentRecord{4133c898 com.google.android.gsf broadcastIntent}}",
				"  RTC_WAKEUP #5: Alarm{41338408 type 0 com.google.android.gsf}",
				"    type=0 when=+5d10h21m14s464ms repeatInterval=565741000 count=0",
				"    operation=PendingIntent{412b4e40: PendingIntentRecord{413e8c90 com.google.android.gsf broadcastIntent}}",
				"  RTC_WAKEUP #4: Alarm{41554510 type 0 com.google.android.partnersetup}",
				"    type=0 when=+4d2h4m6s561ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40e921e8: PendingIntentRecord{41516e78 com.google.android.partnersetup startService}}",
				"  RTC_WAKEUP #3: Alarm{4165b590 type 0 com.android.settings}",
				"    type=0 when=+18h17m52s472ms repeatInterval=86400000 count=1",
				"    operation=PendingIntent{4161c740: PendingIntentRecord{4161c6c0 com.android.settings broadcastIntent}}",
				"  RTC_WAKEUP #2: Alarm{41241318 type 0 com.android.settings}",
				"    type=0 when=+12h17m52s472ms repeatInterval=86400000 count=1",
				"    operation=PendingIntent{411ab6b0: PendingIntentRecord{4124af00 com.android.settings broadcastIntent}}",
				"  RTC_WAKEUP #1: Alarm{412c2600 type 0 com.android.providers.calendar}",
				"    type=0 when=+3h48m53s741ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40da3138: PendingIntentRecord{412ceb28 com.android.providers.calendar broadcastIntent}}",
				"  RTC_WAKEUP #0: Alarm{4152e540 type 0 com.google.android.gsf}",
				"    type=0 when=+12m13s876ms repeatInterval=1800000 count=0",
				"    operation=PendingIntent{41511698: PendingIntentRecord{415e6db8 com.google.android.gsf broadcastIntent}}",
				"  RTC #4: Alarm{4134ab60 type 1 com.htc.store}",
				"    type=1 when=+2d2h10m48s853ms repeatInterval=0 count=0",
				"    operation=PendingIntent{410c9510: PendingIntentRecord{41365848 com.htc.store startService}}",
				"  RTC #3: Alarm{41514848 type 1 com.htc.sync.provider.weather}",
				"    type=1 when=+2h59m14s636ms repeatInterval=0 count=0",
				"    operation=PendingIntent{413a8650: PendingIntentRecord{41563968 com.htc.sync.provider.weather broadcastIntent}}",
				"  RTC #2: Alarm{41297d40 type 1 android}",
				"    type=1 when=+17m52s472ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40fdd488: PendingIntentRecord{411dd770 android broadcastIntent}}",
				"  RTC #1: Alarm{41342248 type 1 com.facebook.katana}",
				"    type=1 when=+1m4s955ms repeatInterval=0 count=0",
				"    operation=PendingIntent{412b51c8: PendingIntentRecord{414808e0 com.facebook.katana broadcastIntent}}",
				"  RTC #0: Alarm{40f74c50 type 1 android}",
				"    type=1 when=+52s472ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41048468: PendingIntentRecord{411a5600 android broadcastIntent}}",
				" ",
				"  Elapsed realtime wakeup (now=+18h12m57s447ms  val=65577447):",
				"  ELAPSED_WAKEUP #7: Alarm{41571d18 type 2 com.google.android.apps.maps}",
				"    type=2 when=+Ĩ54d9h1m49s947ms repeatInterval=0 count=0",
				"    operation=PendingIntent{4127d328: PendingIntentRecord{4158e6a0 com.google.android.apps.maps broadcastIntent}}",
				"  ELAPSED_WAKEUP #6: Alarm{414bea88 type 2 com.google.android.location}",
				"    type=2 when=+23h59m41s775ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40e85ba0: PendingIntentRecord{4100d8b0 com.google.android.location broadcastIntent}}",
				"  ELAPSED_WAKEUP #5: Alarm{41347c60 type 2 com.google.android.apps.maps}",
				"    type=2 when=+7h6m22s505ms repeatInterval=0 count=0",
				"    operation=PendingIntent{416b39d0: PendingIntentRecord{41338000 com.google.android.apps.maps broadcastIntent}}",
				"  ELAPSED_WAKEUP #4: Alarm{413b9110 type 2 android}",
				"    type=2 when=+2h51m49s94ms repeatInterval=0 count=0",
				"    operation=PendingIntent{416237f0: PendingIntentRecord{40de1588 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #3: Alarm{414dc2f8 type 2 android}",
				"    type=2 when=+1h59m34s244ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41351600: PendingIntentRecord{412f2460 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #2: Alarm{415c6bb0 type 2 com.google.android.apps.maps}",
				"    type=2 when=+17m2s553ms repeatInterval=900000 count=0",
				"    operation=PendingIntent{411a6bd0: PendingIntentRecord{415dc840 com.google.android.apps.maps startService}}",
				"  ELAPSED_WAKEUP #1: Alarm{412dfdc8 type 2 com.google.android.gsf}",
				"    type=2 when=+14m5s328ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41337b20: PendingIntentRecord{411e3960 com.google.android.gsf broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{412ec968 type 2 com.facebook.katana}",
				"    type=2 when=+2m2s553ms repeatInterval=43200000 count=0",
				"    operation=PendingIntent{41275da8: PendingIntentRecord{41050268 com.facebook.katana broadcastIntent}}",
				"  ELAPSED #7: Alarm{40e84810 type 3 android}",
				"    type=3 when=+4d2h2m23s249ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41112708: PendingIntentRecord{40e8abd8 android broadcastIntent}}",
				"  ELAPSED #6: Alarm{4147da88 type 3 android}",
				"    type=3 when=+23h59m4s155ms repeatInterval=0 count=0",
				"    operation=PendingIntent{412447f8: PendingIntentRecord{41270f50 android broadcastIntent}}",
				"  ELAPSED #5: Alarm{412f2f38 type 3 com.htc.usage}",
				"    type=3 when=+5h47m45s511ms repeatInterval=21600000 count=1",
				"    operation=PendingIntent{41326070: PendingIntentRecord{4130f158 com.htc.usage startService}}",
				"  ELAPSED #4: Alarm{40f8adf8 type 3 android}",
				"    type=3 when=+2h17m34s934ms repeatInterval=10800000 count=1",
				"    operation=PendingIntent{41321b70: PendingIntentRecord{4133aa78 android broadcastIntent}}",
				"  ELAPSED #3: Alarm{412799b0 type 3 android}",
				"    type=3 when=+2h11m6s630ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40e5fe28: PendingIntentRecord{4127ab18 android broadcastIntent}}",
				"  ELAPSED #2: Alarm{414ca8d0 type 3 android}",
				"    type=3 when=+2m13s794ms repeatInterval=0 count=0",
				"    operation=PendingIntent{4117b568: PendingIntentRecord{4115e1a8 android broadcastIntent}}",
				"  ELAPSED #1: Alarm{412a8be8 type 3 android}",
				"    type=3 when=+2m2s553ms repeatInterval=1800000 count=1",
				"    operation=PendingIntent{4128dc10: PendingIntentRecord{412a3e80 android broadcastIntent}}",
				"  ELAPSED #0: Alarm{416c6830 type 3 com.android.phone}",
				"    type=3 when=+26s791ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40f57370: PendingIntentRecord{40df40c8 com.android.phone broadcastIntent}}",
				" ",
				"  Broadcast ref count: 0",
				" ",
				"  Alarm Stats:",
				"  com.google.android.gsf",
				"    1289ms running, 39 wakeups",
				"    36 alarms: flg=0x14",
				"    1 alarms: act=com.google.android.intent.action.GTALK_RECONNECT flg=0x14",
				"    1 alarms: act=com.google.android.intent.action.SEND_IDLE flg=0x14",
				"    1 alarms: act=com.google.android.intent.action.MCS_HEARTBEAT flg=0x14",
				"  com.facebook.katana",
				"    783ms running, 3 wakeups",
				"    3 alarms: flg=0x14",
				"    3 alarms: flg=0x14 cmp=com.facebook.katana/com.facebook.orca.analytics.AnalyticsPeriodicReporter$ReporterBroadcastReceiver",
				"  com.android.vending",
				"    201ms running, 9 wakeups",
				"    1 alarms: flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.DailyHygiene",
				"    8 alarms: flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.ContentSyncService",
				"  android",
				"    36454ms running, 7 wakeups",
				"    177 alarms: act=android.intent.action.TIME_TICK flg=0x40000014",
				"    36 alarms: act=com.android.server.action.NETWORK_STATS_POLL flg=0x14",
				"    73 alarms: act=com.android.server.ThrottleManager.action.POLL flg=0x14",
				"    2 alarms: act=com.android.server.WifiManager.action.UPDATE_WIFI_STATE flg=0x4",
				"    1 alarms: act=com.android.internal.location.XTRA_ALARM_TIMEOUT flg=0x14",
				"    2 alarms: act=com.htc.intent.action.Socket_RXTXGrouping flg=0x40000014",
				"    6 alarms: act=com.htc.USERBEHAVIOR_FLUSH flg=0x14",
				"    3 alarms: act=android.content.syncmanager.SYNC_ALARM flg=0x14",
				"  com.android.settings",
				"    661ms running, 6 wakeups",
				"    1 alarms: act=SMARTSYNC_SERVICE_BROADCAST_UPDATE_GOLDEN_TABLE flg=0x14",
				"    1 alarms: act=SMARTSYNC_SERVICE_BROADCAST_SCREEN_OFF_TIME flg=0x14",
				"    1 alarms: act=SMARTSYNC_TURN_OFF_WIFI flg=0x14",
				"    1 alarms: act=SMARTSYNC_TURN_ON_MOBILE flg=0x14",
				"    1 alarms: act=SMARTSYNC_TURN_OFF_MOBILE flg=0x14",
				"    1 alarms: act=SMARTSYNC_SYNC_DATA flg=0x14",
				"  com.htc.sync.provider.weather",
				"    645ms running, 0 wakeups",
				"    6 alarms: act=com.htc.sync.provider.weather.START_AUTOSYNC_SERVICE flg=0x14",
				"  com.android.systemui",
				"    22042ms running, 0 wakeups",
				"    1 alarms: act=com.android.systemui.statusbar.action.sleep_mode_start flg=0x14",
				"    1 alarms: act=com.android.systemui.statusbar.action.sleep_mode_end flg=0x14",
				"  com.google.android.apps.maps",
				"    2346ms running, 71 wakeups",
				"    69 alarms: flg=0x4 cmp=com.google.android.apps.maps/com.google.googlenav.prefetch.android.PrefetcherService",
				"    2 alarms: act=com.google.android.location.ALARM_WAKEUP_CACHE_UPDATER flg=0x14",
				"  com.android.providers.calendar",
				"    8970ms running, 1 wakeups",
				"    1 alarms: act=com.android.providers.calendar.intent.CalendarProvider2 flg=0x14",
				"  com.android.phone",
				"    9426ms running, 0 wakeups",
				"    87 alarms: act=com.android.internal.telephony.gprs-data-stall flg=0x14",
				"  com.htc.usage",
				"    71ms running, 0 wakeups",
				"    3 alarms: flg=0x4 cmp=com.htc.usage/.service.UsageULogService"
};
	}
	
	private String[] getMockedFileData2() {
		return new String[] {"Current Alarm Manager state:",
				" ",
				"  Realtime wakeup (now=2014-10-03 11:42:04  val=1412361724278):",
				"  RTC_WAKEUP #7: Alarm{414d44e0 type 0 com.redbend.vdmc}",
				"    type=0 when=+88d12h39m55s722ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40f29930: PendingIntentRecord{412e9930 com.redbend.vdmc broadcastIntent}}",
				"  RTC_WAKEUP #6: Alarm{4105e910 type 0 com.google.android.gsf}",
				"    type=0 when=+5d10h21m17s714ms repeatInterval=565741000 count=0",
				"    operation=PendingIntent{40f6eb50: PendingIntentRecord{4133c898 com.google.android.gsf broadcastIntent}}",
				"  RTC_WAKEUP #5: Alarm{41338408 type 0 com.google.android.gsf}",
				"    type=0 when=+5d10h21m17s714ms repeatInterval=565741000 count=0",
				"    operation=PendingIntent{412b4e40: PendingIntentRecord{413e8c90 com.google.android.gsf broadcastIntent}}",
				"  RTC_WAKEUP #4: Alarm{41554510 type 0 com.google.android.partnersetup}",
				"    type=0 when=+4d2h4m9s811ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40e921e8: PendingIntentRecord{41516e78 com.google.android.partnersetup startService}}",
				"  RTC_WAKEUP #3: Alarm{4165b590 type 0 com.android.settings}",
				"    type=0 when=+18h17m55s722ms repeatInterval=86400000 count=1",
				"    operation=PendingIntent{4161c740: PendingIntentRecord{4161c6c0 com.android.settings broadcastIntent}}",
				"  RTC_WAKEUP #2: Alarm{41241318 type 0 com.android.settings}",
				"    type=0 when=+12h17m55s722ms repeatInterval=86400000 count=1",
				"    operation=PendingIntent{411ab6b0: PendingIntentRecord{4124af00 com.android.settings broadcastIntent}}",
				"  RTC_WAKEUP #1: Alarm{412c2600 type 0 com.android.providers.calendar}",
				"    type=0 when=+3h48m56s991ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40da3138: PendingIntentRecord{412ceb28 com.android.providers.calendar broadcastIntent}}",
				"  RTC_WAKEUP #0: Alarm{4152e540 type 0 com.google.android.gsf}",
				"    type=0 when=+12m17s126ms repeatInterval=1800000 count=0",
				"    operation=PendingIntent{41511698: PendingIntentRecord{415e6db8 com.google.android.gsf broadcastIntent}}",
				"  RTC #4: Alarm{4134ab60 type 1 com.htc.store}",
				"    type=1 when=+2d2h10m52s103ms repeatInterval=0 count=0",
				"    operation=PendingIntent{410c9510: PendingIntentRecord{41365848 com.htc.store startService}}",
				"  RTC #3: Alarm{41514848 type 1 com.htc.sync.provider.weather}",
				"    type=1 when=+2h59m17s886ms repeatInterval=0 count=0",
				"    operation=PendingIntent{413a8650: PendingIntentRecord{41563968 com.htc.sync.provider.weather broadcastIntent}}",
				"  RTC #2: Alarm{41297d40 type 1 android}",
				"    type=1 when=+17m55s722ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40fdd488: PendingIntentRecord{411dd770 android broadcastIntent}}",
				"  RTC #1: Alarm{41342248 type 1 com.facebook.katana}",
				"    type=1 when=+1m8s205ms repeatInterval=0 count=0",
				"    operation=PendingIntent{412b51c8: PendingIntentRecord{414808e0 com.facebook.katana broadcastIntent}}",
				"  RTC #0: Alarm{40f74c50 type 1 android}",
				"    type=1 when=+55s722ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41048468: PendingIntentRecord{411a5600 android broadcastIntent}}",
				" ",
				"  Elapsed realtime wakeup (now=+18h12m54s186ms  val=65574186):",
				"  ELAPSED_WAKEUP #7: Alarm{41571d18 type 2 com.google.android.apps.maps}",
				"    type=2 when=+Ĩ54d9h1m53s208ms repeatInterval=0 count=0",
				"    operation=PendingIntent{4127d328: PendingIntentRecord{4158e6a0 com.google.android.apps.maps broadcastIntent}}",
				"  ELAPSED_WAKEUP #6: Alarm{414bea88 type 2 com.google.android.location}",
				"    type=2 when=+23h59m45s36ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40e85ba0: PendingIntentRecord{4100d8b0 com.google.android.location broadcastIntent}}",
				"  ELAPSED_WAKEUP #5: Alarm{41347c60 type 2 com.google.android.apps.maps}",
				"    type=2 when=+7h6m25s766ms repeatInterval=0 count=0",
				"    operation=PendingIntent{416b39d0: PendingIntentRecord{41338000 com.google.android.apps.maps broadcastIntent}}",
				"  ELAPSED_WAKEUP #4: Alarm{413b9110 type 2 android}",
				"    type=2 when=+2h51m52s355ms repeatInterval=0 count=0",
				"    operation=PendingIntent{416237f0: PendingIntentRecord{40de1588 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #3: Alarm{414dc2f8 type 2 android}",
				"    type=2 when=+1h59m37s505ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41351600: PendingIntentRecord{412f2460 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #2: Alarm{415c6bb0 type 2 com.google.android.apps.maps}",
				"    type=2 when=+17m5s814ms repeatInterval=900000 count=0",
				"    operation=PendingIntent{411a6bd0: PendingIntentRecord{415dc840 com.google.android.apps.maps startService}}",
				"  ELAPSED_WAKEUP #1: Alarm{412dfdc8 type 2 com.google.android.gsf}",
				"    type=2 when=+14m8s589ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41337b20: PendingIntentRecord{411e3960 com.google.android.gsf broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{412ec968 type 2 com.facebook.katana}",
				"    type=2 when=+2m5s814ms repeatInterval=43200000 count=0",
				"    operation=PendingIntent{41275da8: PendingIntentRecord{41050268 com.facebook.katana broadcastIntent}}",
				"  ELAPSED #7: Alarm{40e84810 type 3 android}",
				"    type=3 when=+4d2h2m26s510ms repeatInterval=0 count=0",
				"    operation=PendingIntent{41112708: PendingIntentRecord{40e8abd8 android broadcastIntent}}",
				"  ELAPSED #6: Alarm{4147da88 type 3 android}",
				"    type=3 when=+23h59m7s416ms repeatInterval=0 count=0",
				"    operation=PendingIntent{412447f8: PendingIntentRecord{41270f50 android broadcastIntent}}",
				"  ELAPSED #5: Alarm{412f2f38 type 3 com.htc.usage}",
				"    type=3 when=+5h47m48s772ms repeatInterval=21600000 count=1",
				"    operation=PendingIntent{41326070: PendingIntentRecord{4130f158 com.htc.usage startService}}",
				"  ELAPSED #4: Alarm{40f8adf8 type 3 android}",
				"    type=3 when=+2h17m38s195ms repeatInterval=10800000 count=1",
				"    operation=PendingIntent{41321b70: PendingIntentRecord{4133aa78 android broadcastIntent}}",
				"  ELAPSED #3: Alarm{412799b0 type 3 android}",
				"    type=3 when=+2h11m9s891ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40e5fe28: PendingIntentRecord{4127ab18 android broadcastIntent}}",
				"  ELAPSED #2: Alarm{414ca8d0 type 3 android}",
				"    type=3 when=+2m17s55ms repeatInterval=0 count=0",
				"    operation=PendingIntent{4117b568: PendingIntentRecord{4115e1a8 android broadcastIntent}}",
				"  ELAPSED #1: Alarm{412a8be8 type 3 android}",
				"    type=3 when=+2m5s814ms repeatInterval=1800000 count=1",
				"    operation=PendingIntent{4128dc10: PendingIntentRecord{412a3e80 android broadcastIntent}}",
				"  ELAPSED #0: Alarm{416c6830 type 3 com.android.phone}",
				"    type=3 when=+30s52ms repeatInterval=0 count=0",
				"    operation=PendingIntent{40f57370: PendingIntentRecord{40df40c8 com.android.phone broadcastIntent}}",
				" ",
				"  Broadcast ref count: 0",
				" ",
				"  Alarm Stats:",
				"  com.google.android.gsf",
				"    1289ms running, 39 wakeups",
				"    36 alarms: flg=0x14",
				"    1 alarms: act=com.google.android.intent.action.GTALK_RECONNECT flg=0x14",
				"    1 alarms: act=com.google.android.intent.action.SEND_IDLE flg=0x14",
				"    1 alarms: act=com.google.android.intent.action.MCS_HEARTBEAT flg=0x14",
				"  com.facebook.katana",
				"    783ms running, 3 wakeups",
				"    3 alarms: flg=0x14",
				"    3 alarms: flg=0x14 cmp=com.facebook.katana/com.facebook.orca.analytics.AnalyticsPeriodicReporter$ReporterBroadcastReceiver",
				"  com.android.vending",
				"    201ms running, 9 wakeups",
				"    1 alarms: flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.DailyHygiene",
				"    8 alarms: flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.ContentSyncService",
				"  android",
				"    36454ms running, 7 wakeups",
				"    177 alarms: act=android.intent.action.TIME_TICK flg=0x40000014",
				"    36 alarms: act=com.android.server.action.NETWORK_STATS_POLL flg=0x14",
				"    73 alarms: act=com.android.server.ThrottleManager.action.POLL flg=0x14",
				"    2 alarms: act=com.android.server.WifiManager.action.UPDATE_WIFI_STATE flg=0x4",
				"    1 alarms: act=com.android.internal.location.XTRA_ALARM_TIMEOUT flg=0x14",
				"    2 alarms: act=com.htc.intent.action.Socket_RXTXGrouping flg=0x40000014",
				"    6 alarms: act=com.htc.USERBEHAVIOR_FLUSH flg=0x14",
				"    3 alarms: act=android.content.syncmanager.SYNC_ALARM flg=0x14",
				"  com.android.settings",
				"    661ms running, 6 wakeups",
				"    1 alarms: act=SMARTSYNC_SERVICE_BROADCAST_UPDATE_GOLDEN_TABLE flg=0x14",
				"    1 alarms: act=SMARTSYNC_SERVICE_BROADCAST_SCREEN_OFF_TIME flg=0x14",
				"    1 alarms: act=SMARTSYNC_TURN_OFF_WIFI flg=0x14",
				"    1 alarms: act=SMARTSYNC_TURN_ON_MOBILE flg=0x14",
				"    1 alarms: act=SMARTSYNC_TURN_OFF_MOBILE flg=0x14",
				"    1 alarms: act=SMARTSYNC_SYNC_DATA flg=0x14",
				"  com.htc.sync.provider.weather",
				"    645ms running, 0 wakeups",
				"    6 alarms: act=com.htc.sync.provider.weather.START_AUTOSYNC_SERVICE flg=0x14",
				"  com.android.systemui",
				"    22042ms running, 0 wakeups",
				"    1 alarms: act=com.android.systemui.statusbar.action.sleep_mode_start flg=0x14",
				"    1 alarms: act=com.android.systemui.statusbar.action.sleep_mode_end flg=0x14",
				"  com.google.android.apps.maps",
				"    2346ms running, 71 wakeups",
				"    69 alarms: flg=0x4 cmp=com.google.android.apps.maps/com.google.googlenav.prefetch.android.PrefetcherService",
				"    2 alarms: act=com.google.android.location.ALARM_WAKEUP_CACHE_UPDATER flg=0x14",
				"  com.android.providers.calendar",
				"    8970ms running, 1 wakeups",
				"    1 alarms: act=com.android.providers.calendar.intent.CalendarProvider2 flg=0x14",
				"  com.android.phone",
				"    9426ms running, 0 wakeups",
				"    87 alarms: act=com.android.internal.telephony.gprs-data-stall flg=0x14",
				"  com.htc.usage",
				"    71ms running, 0 wakeups",
				"    3 alarms: flg=0x4 cmp=com.htc.usage/.service.UsageULogService"
};
	}
	
}
