package com.att.aro.core.peripheral.impl;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.peripheral.IAlarmAnalysisInfoParser;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisInfo;
import com.att.aro.core.peripheral.pojo.AlarmAnalysisResult;

import static org.junit.Assert.*;

public class AlarmAnalysisInfoParserImplTest extends BaseTest {
	
	AlarmAnalysisInfoParserImpl parser;	


	@Test  
	public void readData() throws IOException{
		parser = (AlarmAnalysisInfoParserImpl) context.getBean(IAlarmAnalysisInfoParser.class);
		IFileManager filereader = Mockito.mock(IFileManager.class);
		String[] arr = getData();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(arr);
		parser.setFileReader(filereader);
		Date date = new Date(2014,01,06,12,0,26);
		AlarmAnalysisResult result = null;
		boolean hasdata = false;
		
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		result = parser.parse("/", "alarm_info_end", "2.3", 3015093, 3064068, date);
		hasdata = result.getStatistics().size() > 0;
		assertTrue(hasdata);
		
		String[] startarr = getDataStart();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(startarr);
		date = new Date(2014,01,06,12,0,29);
		AlarmAnalysisResult result2 = parser.parse("/","alarm_info_start","2.3",3047197,3064068,date);
		
		List<AlarmAnalysisInfo> alarmStatisticsInfosEnd = result.getStatistics();
		List<AlarmAnalysisInfo> alarmStatisticsInfosStart = result2.getStatistics();
		List<AlarmAnalysisInfo> alarmlist =  parser.compareAlarmAnalysis(alarmStatisticsInfosEnd, alarmStatisticsInfosStart);
		hasdata = alarmlist.size() > 0;
		assertTrue(hasdata);

	}
	

	@Test 
	public void readData_Test2()throws IOException{
		parser = (AlarmAnalysisInfoParserImpl) context.getBean(IAlarmAnalysisInfoParser.class);
		IFileManager filereader = Mockito.mock(IFileManager.class);
		String[] startarr2 = getDataStart2();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(startarr2);
		parser.setFileReader(filereader);
		Date date = new Date(2014,01,06,12,0,30); 
		AlarmAnalysisResult result4 = null;
		boolean hasdata = false;
		Mockito.when(filereader.fileExist(Mockito.anyString())).thenReturn(true);
		result4 = parser.parse("/", "alarm_info_end", "3.0", 3015093, 3064068, date);

		String[] startarr3 = getDataStart3();
		Mockito.when(filereader.readAllLine(Mockito.anyString())).thenReturn(startarr3);
		AlarmAnalysisResult result3 = parser.parse("/","alarm_info_start","3.0",3047197,3064068,date);	
		List<AlarmAnalysisInfo> alarmStatisticsInfosStart1 = result4.getStatistics();
		List<AlarmAnalysisInfo> alarmStatisticsInfosEnd1 = result3.getStatistics();
		List<AlarmAnalysisInfo> alarmlist1 =  
				parser.compareAlarmAnalysis(alarmStatisticsInfosEnd1, alarmStatisticsInfosStart1);
		hasdata = alarmlist1.size() > 0;
		assertTrue(hasdata);

	}
	

	String[] getDataStart(){
		return new String[]{
				"Current Alarm Manager state:",
				"nowRTC=1389038429079=2014-01-06 12:00:29 nowELAPSED=3016276",
				"Next alarm: 3047197 = 2014-01-06 12:01:00",
				"Next wakeup: 3064068 = 2014-01-06 12:01:16\r\n\r\n",

				"Pending alarm batches: 35",
				"Batch{64e893d8 num=1 start=3047197 end=3047197 STANDALONE}:",
				"  ELAPSED #0: Alarm{64fd9cc0 type 3 android}",
				"    type=3 when=+30s921ms repeatInterval=0 count=0",
				"    operation=PendingIntent{64c6fb20: PendingIntentRecord{64ca3f88 android broadcastIntent}}",
				"Batch{64e81a30 num=3 start=3064068 end=3078907}:",
				"  RTC_WAKEUP #2: Alarm{64fd5848 type 0 android}",
				"    type=0 whenElapsed=3041861 when=+25s586ms window=-1 repeatInterval=300000 count=0",
				"    operation=PendingIntent{64c47258: PendingIntentRecord{64dad1e0 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fd9a00 type 2 com.android.phone}",
				"    type=2 whenElapsed=3064068 when=+47s792ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e22338: PendingIntentRecord{6514aa20 com.android.phone broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fde738 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=3050836 when=+34s560ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fffdf0: PendingIntentRecord{64f66b68 com.google.android.gms broadcastIntent}}",
				"Batch{64e7fd18 num=4 start=3226466 end=3226466}:",
				"  RTC #3: Alarm{64fcb788 type 1 com.android.chrome}",
				"    type=1 whenElapsed=3226466 when=+3m30s191ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{650a2a78: PendingIntentRecord{65114e30 com.android.chrome broadcastIntent}}",
				"  ELAPSED_WAKEUP #2: Alarm{64fcec68 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3196273 when=+2m59s997ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{651192a0: PendingIntentRecord{64fb18f8 com.google.android.googlequicksearchbox startService}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fcf8f0 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3182384 when=+2m46s108ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fce530: PendingIntentRecord{65191dd8 com.google.android.googlequicksearchbox startService}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fd23a8 type 2 android}",
				"    type=2 whenElapsed=3148531 when=+2m12s255ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ba9700: PendingIntentRecord{64de3b10 android broadcastIntent}}",
				"Batch{64e7a2c0 num=1 start=3636744 end=3636744}:",
				"  RTC #0: Alarm{64fcb070 type 1 com.android.chrome}",
				"    type=1 whenElapsed=3636744 when=+10m20s469ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e4e350: PendingIntentRecord{65144070 com.android.chrome broadcastIntent}}",
				"Batch{64e78b00 num=3 start=3695542 end=3695542}:",
				"  RTC #2: Alarm{64fb6b20 type 1 com.google.android.inputmethod.latin}",
				"    type=1 whenElapsed=3649376 when=+10m33s100ms window=-1 repeatInterval=3600000 count=0",
				"    operation=PendingIntent{64d341e0: PendingIntentRecord{64e76ff0 com.google.android.inputmethod.latin broadcastIntent}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fbd130 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3695542 when=+11m19s266ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f22b20: PendingIntentRecord{64eae9b0 com.google.android.googlequicksearchbox broadcastIntent}}",
				"  ELAPSED #0: Alarm{64fc8010 type 3 android}",
				"    type=3 whenElapsed=3644517 when=+10m28s241ms window=-1 repeatInterval=1800000 count=0",
				"    operation=PendingIntent{64f31240: PendingIntentRecord{64f31110 android broadcastIntent}}",
				"Batch{64e73e60 num=1 start=3706847 end=3706847}:",
				"  ELAPSED_WAKEUP #0: Alarm{64fb5d20 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3706847 when=+11m30s571ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ea1910: PendingIntentRecord{64ea00d0 com.google.android.googlequicksearchbox startService}}",
				"Batch{64e6d520 num=5 start=3888197 end=3888197}:",
				"  RTC #4: Alarm{64fad078 type 1 com.android.deskclock}",
				"    type=1 whenElapsed=3888197 when=+14m31s921ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{651249e8: PendingIntentRecord{65188308 com.android.deskclock broadcastIntent}}",
				"  RTC_WAKEUP #3: Alarm{64faf188 type 0 android}",
				"    type=0 whenElapsed=3798111 when=+13m1s835ms window=-1 repeatInterval=3629637 count=0",
				"    operation=PendingIntent{64dd0e88: PendingIntentRecord{64d21a78 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #2: Alarm{64fb31b0 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=3715385 when=+11m39s109ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64e659c8: PendingIntentRecord{64e61710 com.google.android.talk broadcastIntent}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fb4aa0 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=3714934 when=+11m38s658ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64f9b550: PendingIntentRecord{64fd3978 com.google.android.talk broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fb59b8 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=3714560 when=+11m38s284ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64ce1c18: PendingIntentRecord{64f0fb18 com.google.android.talk broadcastIntent}}",
				"Batch{64e60700 num=1 start=4037568 end=4037568}:",
				"  ELAPSED_WAKEUP #0: Alarm{64faaca8 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=4037568 when=+17m1s292ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{6523add8: PendingIntentRecord{64fc8c90 com.google.android.gms broadcastIntent}}",
				"Batch{64e56120 num=1 start=4078894 end=4078894}:",
				"  RTC #0: Alarm{64fa2568 type 1 com.android.chrome}",
				"    type=1 whenElapsed=4078894 when=+17m42s618ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{65191eb0: PendingIntentRecord{651be7f8 com.android.chrome broadcastIntent}}",
				"Batch{64e55a60 num=1 start=4668696 end=4668696}:",
				"  RTC_WAKEUP #0: Alarm{64fa1480 type 0 com.cyanogenmod.lockclock}",
				"    type=0 whenElapsed=4668696 when=+27m32s420ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{65108bd0: PendingIntentRecord{64fe4570 com.cyanogenmod.lockclock startService}}",
				"Batch{64e528b0 num=3 start=5738198 end=5738198}:",
				"  RTC_WAKEUP #2: Alarm{64f92440 type 0 com.google.android.gsf}",
				"    type=0 whenElapsed=4730206 when=+28m33s930ms window=-1 repeatInterval=1800000 count=0",
				"    operation=PendingIntent{6498a880: PendingIntentRecord{64eaab08 com.google.android.gsf broadcastIntent}}",
				"  RTC_WAKEUP #1: Alarm{64f99a40 type 0 com.android.settings}",
				"    type=0 whenElapsed=4668897 when=+27m32s621ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{65124608: PendingIntentRecord{65124568 com.android.settings broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fa0b98 type 2 android}",
				"    type=2 whenElapsed=5738198 when=+45m21s922ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64b041b8: PendingIntentRecord{64e2e728 android broadcastIntent}}",
				"Batch{64e32238 num=1 start=6092810 end=6092810}:",
				"  RTC_WAKEUP #0: Alarm{64f7a4e0 type 0 com.cleanmaster.mguard}",
				"    type=0 whenElapsed=6092810 when=+51m16s534ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{650583b8: PendingIntentRecord{64fdb978 com.cleanmaster.mguard startService}}",
				"Batch{64e304d0 num=1 start=6159264 end=6159264}:",
				"  ELAPSED_WAKEUP #0: Alarm{64f78088 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=6159264 when=+52m22s988ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ff81f8: PendingIntentRecord{64e70148 com.google.android.gms broadcastIntent}}",
				"Batch{64e2fc68 num=2 start=10861583 end=10861583}:",
				"  RTC_WAKEUP #1: Alarm{64f66f80 type 0 com.google.android.googlequicksearchbox}",
				"    type=0 whenElapsed=9844000 when=+1h53m47s725ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{650ec9f8: PendingIntentRecord{6501ecf8 com.google.android.googlequicksearchbox startService}}",
				"  ELAPSED_WAKEUP #0: Alarm{64f6c438 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=10861583 when=+2h10m45s307ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f7f270: PendingIntentRecord{64f9ff88 com.google.android.gms broadcastIntent}}",
				"Batch{64e13cd8 num=1 start=16108037 end=16108037}:",
				"  ELAPSED #0: Alarm{64f63c90 type 3 mobi.mgeek.TunnyBrowser}",
				"    type=3 whenElapsed=16108037 when=+3h38m11s761ms window=0 repeatInterval=14400000 count=0",
				"    operation=PendingIntent{64fbeef0: PendingIntentRecord{651524a8 mobi.mgeek.TunnyBrowser startService}}",
				"Batch{64de4400 num=2 start=19095475 end=19095475}:",
				"  RTC #1: Alarm{64f5f0b0 type 1 com.android.chrome}", 
				"    type=1 whenElapsed=19095475 when=+4h27m59s200ms window=0 repeatInterval=18000000 count=0",
				"    operation=PendingIntent{64ea9fd8: PendingIntentRecord{6515fd40 com.android.chrome startService}}",
				"  RTC #0: Alarm{64f326b0 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=17407196 when=+3h59m50s921ms window=0 repeatInterval=14400000 count=0",
				"    operation=PendingIntent{64ad68d8: PendingIntentRecord{64fb1718 com.cleanmaster.mguard broadcastIntent}}",
				"Batch{64ddfdf8 num=1 start=21595187 end=21595187}:",
				"  RTC #0: Alarm{64f57f20 type 1 android}",
				"    type=1 whenElapsed=21595187 when=+5h9m38s912ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{65064480: PendingIntentRecord{64e98898 android broadcastIntent}}",
				"Batch{64d90118 num=2 start=28851294 end=40679503}:",
				"  RTC_WAKEUP #1: Alarm{64f51268 type 0 com.cleanmaster.mguard}",
				"    type=0 whenElapsed=24479503 when=+5h57m43s227ms window=0 repeatInterval=21600000 count=0",
				"    operation=PendingIntent{64f50d28: PendingIntentRecord{64e8a730 com.cleanmaster.mguard startService}}",
				"  ELAPSED #0: Alarm{64f51d40 type 3 com.android.phone}",
				"    type=3 whenElapsed=28851294 when=+7h10m35s18ms window=-1 repeatInterval=28800000 count=0",
				"    operation=PendingIntent{64e81a90: PendingIntentRecord{64f181a0 com.android.phone broadcastIntent}}",
				"Batch{64d81648 num=1 start=45068197 end=45068197}:",
				"  RTC_WAKEUP #0: Alarm{64f503f8 type 0 com.google.android.googlequicksearchbox}",
				"    type=0 whenElapsed=45068197 when=+11h40m51s921ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e43d08: PendingIntentRecord{651f0f58 com.google.android.googlequicksearchbox startService}}",
				"Batch{64d57198 num=1 start=46187197 end=46187197 STANDALONE}:",
				"  RTC #0: Alarm{64f38250 type 1 android}",
				"    type=1 whenElapsed=46187197 when=+11h59m30s921ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64bb92d8: PendingIntentRecord{64cc17b0 android broadcastIntent}}",
				"Batch{64d66ee0 num=2 start=46187197 end=46187197}:",
				"  RTC_WAKEUP #1: Alarm{64f4c7d0 type 0 com.google.android.keep}",
				"    type=0 whenElapsed=46187197 when=+11h59m30s921ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e746b8: PendingIntentRecord{64fe42a0 com.google.android.keep broadcastIntent}}",
				"  RTC #0: Alarm{64f49608 type 1 com.google.android.calendar}",
				"    type=1 whenElapsed=46187197 when=+11h59m30s921ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{650ad6f8: PendingIntentRecord{64f9f218 com.google.android.calendar broadcastIntent}}",
				"Batch{64d2e050 num=1 start=54952832 end=54952832}:",
				"  RTC_WAKEUP #0: Alarm{64f30bc8 type 0 com.google.android.gms}",
				"    type=0 whenElapsed=54952832 when=+14h25m36s556ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64bf71c0: PendingIntentRecord{64f99d40 com.google.android.gms startService}}",
				"Batch{64d2d958 num=1 start=79443547 end=79443547}:",
				"  RTC_WAKEUP #0: Alarm{64f2ea38 type 0 com.android.providers.calendar}",
				"    type=0 whenElapsed=79443547 when=+21h13m47s271ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64eb3f48: PendingIntentRecord{6510a220 com.android.providers.calendar broadcastIntent}}",
				"Batch{64d22d48 num=1 start=86476776 end=86476776}:",
				"  RTC_WAKEUP #0: Alarm{64f0f918 type 0 com.google.android.gms}",
				"    type=0 whenElapsed=86476776 when=+23h11m0s500ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ad6b10: PendingIntentRecord{64d22100 com.google.android.gms broadcastIntent}}",
				"Batch{64d16b80 num=2 start=89273846 end=89273846}:",
				"  RTC #1: Alarm{64f07a58 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=89273846 when=+23h57m37s571ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{651ff630: PendingIntentRecord{64f63a00 com.cleanmaster.mguard broadcastIntent}}",
				"  RTC_WAKEUP #0: Alarm{64f09238 type 0 com.koushikdutta.rommanager}",
				"    type=0 whenElapsed=86532111 when=+23h11m55s836ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64d07350: PendingIntentRecord{64d072f0 com.koushikdutta.rommanager broadcastIntent}}",
				"Batch{64d0b510 num=2 start=95552474 end=95552474}:",
				"  RTC_WAKEUP #1: Alarm{64f03808 type 0 com.android.vending}",
				"    type=0 whenElapsed=95552474 when=+1d1h42m16s199ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{6516fb70: PendingIntentRecord{65063840 com.android.vending startService}}",
				"  RTC_WAKEUP #0: Alarm{64f04870 type 0 com.cleanmaster.mguard}",
				"    type=0 whenElapsed=89279523 when=+23h57m43s248ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{6505f030: PendingIntentRecord{64d0cf38 com.cleanmaster.mguard startService}}",
				"Batch{64d0af10 num=1 start=109539073 end=109539073}:",
				"  RTC_WAKEUP #0: Alarm{64efb5f0 type 0 com.cyanogenmod.updater}",
				"    type=0 whenElapsed=109539073 when=+1d5h35m22s798ms window=0 repeatInterval=604800000 count=0",
				"    operation=PendingIntent{6498a538: PendingIntentRecord{64f0a850 com.cyanogenmod.updater startService}}",
				"Batch{64cfecd0 num=1 start=184621989 end=184621989}:",
				"  RTC #0: Alarm{64efa180 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=184621989 when=+2d2h26m45s713ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f9cbe8: PendingIntentRecord{64f40878 com.cleanmaster.mguard broadcastIntent}}",
				"Batch{64cfd448 num=3 start=351766245 end=351766245}:",
				"  RTC_WAKEUP #2: Alarm{64eb7c20 type 0 com.google.android.gsf}",
				"    type=0 whenElapsed=203611099 when=+2d7h43m14s823ms window=-1 repeatInterval=522504000 count=0",
				"    operation=PendingIntent{64f55760: PendingIntentRecord{6505b690 com.google.android.gsf broadcastIntent}}",
				"  ELAPSED #1: Alarm{64ebf5a8 type 3 ch.bitspin.timely}",
				"    type=3 whenElapsed=351766245 when=+4d0h52m29s969ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ed4b98: PendingIntentRecord{64ffaf48 ch.bitspin.timely broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64ef3d50 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=259314935 when=+2d23h11m38s659ms window=-1 repeatInterval=259200000 count=0",
				"    operation=PendingIntent{64ef9e90: PendingIntentRecord{64f42498 com.google.android.talk broadcastIntent}}",
				"Batch{64ce6af0 num=1 start=438379461 end=438379461}:",
				"  RTC_WAKEUP #0: Alarm{64eb7138 type 0 com.google.android.gms}",
				"    type=0 whenElapsed=438379461 when=+5d0h56m3s185ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64b7d2e8: PendingIntentRecord{65029010 com.google.android.gms broadcastIntent}}",
				"Batch{64ce45b8 num=3 start=875821988 end=875821988}:",
				"  RTC #2: Alarm{64eada88 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=875821988 when=+10d2h26m45s713ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{650667a8: PendingIntentRecord{650094b0 com.cleanmaster.mguard broadcastIntent}}",
				"  RTC_WAKEUP #1: Alarm{64eb2418 type 0 com.google.android.partnersetup}",
				"    type=0 whenElapsed=604893392 when=+6d23h11m17s116ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fb5b30: PendingIntentRecord{64ff4820 com.google.android.partnersetup startService}}",
				"  ELAPSED #0: Alarm{64eb3780 type 3 android}",
				"    type=3 whenElapsed=864177065 when=+9d23h12m40s789ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ccaa58: PendingIntentRecord{64cf76e8 android broadcastIntent}}",
				"Batch{64cc7288 num=1 start=1827447960 end=1827447960}:",
				"  ELAPSED_WAKEUP #0: Alarm{64eac760 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=1827447960 when=+21d2h47m11s684ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f706c0: PendingIntentRecord{64f5d258 com.google.android.gms startService}}",
				"Batch{64cb1a68 num=1 start=1827451644 end=1827451644}:",
				"  ELAPSED_WAKEUP #0: Alarm{64ea0598 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=1827451644 when=+21d2h47m15s368ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64d7fc88: PendingIntentRecord{64f61c18 com.google.android.gms startService}}",
				"Batch{64cb0710 num=1 start=1827451924 end=1827451924}:",
				"  ELAPSED_WAKEUP #0: Alarm{64e99420 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=1827451924 when=+21d2h47m15s648ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64eb1d90: PendingIntentRecord{64e0eac0 com.google.android.gms startService}}",
				"Batch{64ca2620 num=1 start=2171821989 end=2171821989}:",
				"  RTC #0: Alarm{64e8a178 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=2171821989 when=+25d2h26m45s713ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fd5740: PendingIntentRecord{64e1cd20 com.cleanmaster.mguard broadcastIntent}}\r\n\r\n",

				"  Broadcast ref count: 1\r\n\r\n",
 
				"  Top Alarms:",
				"    +1m44s832ms running, 2 wakeups, 2 alarms: com.android.providers.calendar",
				"       act=com.android.providers.calendar.intent.CalendarProvider2",
				"    +1m43s431ms running, 0 wakeups, 3 alarms: com.cleanmaster.mguard",
				"       act=com.ijinshan.common.kinfoc.ActivityTimer",
				"    +1m43s298ms running, 0 wakeups, 2 alarms: android",
				"       act=com.android.server.action.NETWORK_STATS_POLL",
				"    +1m39s763ms running, 1 wakeups, 1 alarms: com.android.settings",
				"       act=android.net.conn.CONNECTIVITY_CHANGE cmp={com.android.settings/com.android.settings.cmstats.ReportingServiceManager}",
				"    +1m28s987ms running, 5 wakeups, 5 alarms: com.google.android.gms",
				"       act=com.google.android.intent.action.GCM_RECONNECT",
				"    +1m17s554ms running, 4 wakeups, 4 alarms: android",
				"       act=android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED",
				"    +1m7s900ms running, 0 wakeups, 1 alarms: android",
				"       act=com.android.server.NetworkTimeUpdateService.action.POLL",
				"    +1m4s455ms running, 5 wakeups, 5 alarms: com.google.android.gms",
				"       act=com.google.android.intent.action.SEND_IDLE",
				"    +1m1s687ms running, 3 wakeups, 3 alarms: com.google.android.talk",
				"       act=com.google.android.apps.babel.UPDATE_NOTIFICATION",
				"    *ACTIVE* +1m1s132ms running, 0 wakeups, 41 alarms: com.android.chrome",
				"       cmp={com.android.chrome/com.google.ipc.invalidation.ticl.android2.AndroidInternalScheduler$AlarmReceiver}",
				" ",
				"  Alarm Stats:",
				"  com.google.android.talk +1m1s700ms running, 4 wakeups:",
				"    +1m1s687ms 3 wakes 3 alarms: act=com.google.android.apps.babel.UPDATE_NOTIFICATION",
				"    +53s195ms 1 wakes 1 alarms: act=com.google.android.apps.babel.RENEW_ACCOUNT_REGISTRATION",
				"  android +2m23s196ms running, 19 wakeups:",
				"    +1m43s298ms 0 wakes 2 alarms: act=com.android.server.action.NETWORK_STATS_POLL",
				"    +1m17s554ms 4 wakes 4 alarms: act=android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED",
				"    +1m7s900ms 0 wakes 1 alarms: act=com.android.server.NetworkTimeUpdateService.action.POLL",
				"    +29s179ms 0 wakes 39 alarms: act=android.intent.action.TIME_TICK",
				"    +4s428ms 9 wakes 9 alarms: act=com.android.server.WifiManager.action.START_SCAN",
				"    +630ms 6 wakes 6 alarms: act=android.content.syncmanager.SYNC_ALARM",
				"  com.android.settings +1m39s763ms running, 1 wakeups:",
				"    +1m39s763ms 1 wakes 1 alarms: act=android.net.conn.CONNECTIVITY_CHANGE cmp={com.android.settings/com.android.settings.cmstats.ReportingServiceManager}",
				"  com.cyanogenmod.lockclock +1ms running, 1 wakeups:",
				"    +1ms 1 wakes 1 alarms: cmp={com.cyanogenmod.lockclock/com.cyanogenmod.lockclock.weather.WeatherUpdateService}",
				"  com.android.deskclock +13s108ms running, 0 wakeups:",
				"    +13s108ms 0 wakes 4 alarms: act=com.android.deskclock.ON_QUARTER_HOUR",
				"  com.android.phone +20s819ms running, 14 wakeups:",
				"    +20s819ms 14 wakes 14 alarms: act=com.android.internal.telephony.data-stall",
				"  com.google.android.gsf +2s676ms running, 2 wakeups:",
				"    +2s676ms 2 wakes 2 alarms: cmp={com.google.android.gsf/com.google.android.gsf.checkin.EventLogService$Receiver}",
				"  com.android.keyguard +3s79ms running, 6 wakeups:",
				"    +3s79ms 6 wakes 6 alarms: act=com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD",
				"  com.google.android.googlequicksearchbox +8s780ms running, 8 wakeups:",
				"    +8s478ms 3 wakes 3 alarms: act=com.google.android.sidekick.main.calendar.CalendarIntentService.UPDATE_CALENDAR_ACTION cmp={com.google.android.googlequicksearchbox/com.google.android.sidekick.main.calendar.CalendarIntentService$CalendarReceiver}",
				"    +180ms 2 wakes 2 alarms: act=ACTION_UPDATE_APP_SCORES cmp={com.google.android.googlequicksearchbox/com.google.android.search.core.summons.icing.InternalIcingCorporaProvider$UpdateCorporaService}",
				"    +72ms 1 wakes 1 alarms: act=com.google.android.apps.sidekick.REFRESH cmp={com.google.android.googlequicksearchbox/com.google.android.sidekick.main.entry.EntriesRefreshIntentService}",
				"    +50ms 2 wakes 2 alarms: cmp={com.google.android.googlequicksearchbox/com.google.android.sidekick.main.TrafficIntentService}",
				"  *ACTIVE* com.android.chrome +1m25s562ms running, 0 wakeups:",
				"    *ACTIVE* +1m1s132ms 0 wakes 41 alarms: cmp={com.android.chrome/com.google.ipc.invalidation.ticl.android2.AndroidInternalScheduler$AlarmReceiver}",
				"    +30s122ms 0 wakes 75 alarms: cmp={com.android.chrome/com.google.ipc.invalidation.external.client.contrib.AndroidListener$AlarmReceiver}",
				"    +1ms 0 wakes 1 alarms: act=com.google.android.apps.chrome.omaha.ACTION_REGISTER_REQUEST cmp={com.android.chrome/com.google.android.apps.chrome.omaha.OmahaClient}",
				"  com.android.vending +409ms running, 5 wakeups:",
				"    +305ms 2 wakes 2 alarms: cmp={com.android.vending/com.google.android.finsky.services.DailyHygiene}",
				"    +104ms 3 wakes 3 alarms: cmp={com.android.vending/com.google.android.finsky.services.ContentSyncService}",
				"  com.cleanmaster.mguard +1m43s468ms running, 4 wakeups:",
				"    +1m43s431ms 0 wakes 3 alarms: act=com.ijinshan.common.kinfoc.ActivityTimer",
				"    +35ms 2 wakes 2 alarms: act=com.cleanmaster.service.ACTION_UPDATE_CLOUD_CFG cmp={com.cleanmaster.mguard/com.cleanmaster.cloudconfig.CloudCfgIntentService}",
				"    +8ms 2 wakes 2 alarms: cmp={com.cleanmaster.mguard/com.cleanmaster.service.PermanentService}",
				"  com.android.providers.calendar +1m44s832ms running, 2 wakeups:",
				"    +1m44s832ms 2 wakes 2 alarms: act=com.android.providers.calendar.intent.CalendarProvider2",
				"  com.google.android.gms +1m32s227ms running, 18 wakeups:",
				"    +1m28s987ms 5 wakes 5 alarms: act=com.google.android.intent.action.GCM_RECONNECT",
				"    +1m4s455ms 5 wakes 5 alarms: act=com.google.android.intent.action.SEND_IDLE",
				"    +504ms 8 wakes 8 alarms: act=com.google.android.gms.nlp.ALARM_WAKEUP_LOCATOR"	
		};
	}
	String[] getData(){
		return new String[]{ 
				"Current Alarm Manager state:",
				"nowRTC=1389038426707=2014-01-06 12:00:26 nowELAPSED=3013904",
				"Next alarm: 3015093 = 2014-01-06 12:00:27",
				"Next wakeup: 3064068 = 2014-01-06 12:01:16\r\n\r\n",
				"Pending alarm batches: 36",
				"Batch{64e89ba0 num=2 start=3015093 end=3015093}:",
				"  RTC #1: Alarm{64fdc4e8 type 1 com.android.chrome}",
				"    type=1 when=+1s190ms repeatInterval=0 count=0",
				"    operation=PendingIntent{65074a88: PendingIntentRecord{65249350 com.android.chrome broadcastIntent}}",
				"  RTC #0: Alarm{64fdce60 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=3007196 when=-6s707ms window=0 repeatInterval=14400000 count=0",
				"    operation=PendingIntent{64ad68d8: PendingIntentRecord{64fb1718 com.cleanmaster.mguard broadcastIntent}}",
				"Batch{64e893d8 num=1 start=3047197 end=3047197 STANDALONE}:",
				"  ELAPSED #0: Alarm{64fd9cc0 type 3 android}",
				"    type=3 whenElapsed=3047197 when=+33s293ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64c6fb20: PendingIntentRecord{64ca3f88 android broadcastIntent}}",
				"Batch{64e81a30 num=3 start=3064068 end=3078907}:",
				"  RTC_WAKEUP #2: Alarm{64fd5848 type 0 android}",
				"    type=0 whenElapsed=3041861 when=+27s958ms window=-1 repeatInterval=300000 count=0",
				"    operation=PendingIntent{64c47258: PendingIntentRecord{64dad1e0 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fd9a00 type 2 com.android.phone}",
				"    type=2 whenElapsed=3064068 when=+50s164ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e22338: PendingIntentRecord{6514aa20 com.android.phone broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fde738 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=3050836 when=+36s932ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fffdf0: PendingIntentRecord{64f66b68 com.google.android.gms broadcastIntent}}",
				"Batch{64e7fd18 num=4 start=3226466 end=3226466}:",
				"  RTC #3: Alarm{64fcb788 type 1 com.android.chrome}",
				"    type=1 whenElapsed=3226466 when=+3m32s563ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{650a2a78: PendingIntentRecord{65114e30 com.android.chrome broadcastIntent}}",
				"  ELAPSED_WAKEUP #2: Alarm{64fcec68 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3196273 when=+3m2s369ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{651192a0: PendingIntentRecord{64fb18f8 com.google.android.googlequicksearchbox startService}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fcf8f0 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3182384 when=+2m48s480ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fce530: PendingIntentRecord{65191dd8 com.google.android.googlequicksearchbox startService}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fd23a8 type 2 android}",
				"    type=2 whenElapsed=3148531 when=+2m14s627ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ba9700: PendingIntentRecord{64de3b10 android broadcastIntent}}",
				"Batch{64e7a2c0 num=1 start=3636744 end=3636744}:",
				"  RTC #0: Alarm{64fcb070 type 1 com.android.chrome}",
				"    type=1 whenElapsed=3636744 when=+10m22s841ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e4e350: PendingIntentRecord{65144070 com.android.chrome broadcastIntent}}",
				"Batch{64e78b00 num=3 start=3695542 end=3695542}:",
				"  RTC #2: Alarm{64fb6b20 type 1 com.google.android.inputmethod.latin}",
				"    type=1 whenElapsed=3649376 when=+10m35s472ms window=-1 repeatInterval=3600000 count=0",
				"    operation=PendingIntent{64d341e0: PendingIntentRecord{64e76ff0 com.google.android.inputmethod.latin broadcastIntent}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fbd130 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3695542 when=+11m21s638ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f22b20: PendingIntentRecord{64eae9b0 com.google.android.googlequicksearchbox broadcastIntent}}",
				"  ELAPSED #0: Alarm{64fc8010 type 3 android}",
				"    type=3 whenElapsed=3644517 when=+10m30s613ms window=-1 repeatInterval=1800000 count=0",
				"    operation=PendingIntent{64f31240: PendingIntentRecord{64f31110 android broadcastIntent}}",
				"Batch{64e73e60 num=1 start=3706847 end=3706847}:",
				"  ELAPSED_WAKEUP #0: Alarm{64fb5d20 type 2 com.google.android.googlequicksearchbox}",
				"    type=2 whenElapsed=3706847 when=+11m32s943ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ea1910: PendingIntentRecord{64ea00d0 com.google.android.googlequicksearchbox startService}}",
				"Batch{64e6d520 num=5 start=3888197 end=3888197}:",
				"  RTC #4: Alarm{64fad078 type 1 com.android.deskclock}",
				"    type=1 whenElapsed=3888197 when=+14m34s293ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{651249e8: PendingIntentRecord{65188308 com.android.deskclock broadcastIntent}}",
				"  RTC_WAKEUP #3: Alarm{64faf188 type 0 android}",
				"    type=0 whenElapsed=3798111 when=+13m4s207ms window=-1 repeatInterval=3629637 count=0",
				"    operation=PendingIntent{64dd0e88: PendingIntentRecord{64d21a78 android broadcastIntent}}",
				"  ELAPSED_WAKEUP #2: Alarm{64fb31b0 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=3715385 when=+11m41s481ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64e659c8: PendingIntentRecord{64e61710 com.google.android.talk broadcastIntent}}",
				"  ELAPSED_WAKEUP #1: Alarm{64fb4aa0 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=3714934 when=+11m41s30ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64f9b550: PendingIntentRecord{64fd3978 com.google.android.talk broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fb59b8 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=3714560 when=+11m40s656ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64ce1c18: PendingIntentRecord{64f0fb18 com.google.android.talk broadcastIntent}}",
				"Batch{64e60700 num=1 start=4037568 end=4037568}:",
				"  ELAPSED_WAKEUP #0: Alarm{64faaca8 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=4037568 when=+17m3s664ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{6523add8: PendingIntentRecord{64fc8c90 com.google.android.gms broadcastIntent}}",
				"Batch{64e56120 num=1 start=4078894 end=4078894}:",
				"  RTC #0: Alarm{64fa2568 type 1 com.android.chrome}",
				"    type=1 whenElapsed=4078894 when=+17m44s990ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{65191eb0: PendingIntentRecord{651be7f8 com.android.chrome broadcastIntent}}",
				"Batch{64e55a60 num=1 start=4668696 end=4668696}:",
				"  RTC_WAKEUP #0: Alarm{64fa1480 type 0 com.cyanogenmod.lockclock}",
				"    type=0 whenElapsed=4668696 when=+27m34s792ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{65108bd0: PendingIntentRecord{64fe4570 com.cyanogenmod.lockclock startService}}",
				"Batch{64e528b0 num=3 start=5738198 end=5738198}:",
				"  RTC_WAKEUP #2: Alarm{64f92440 type 0 com.google.android.gsf}",
				"    type=0 whenElapsed=4730206 when=+28m36s302ms window=-1 repeatInterval=1800000 count=0",
				"    operation=PendingIntent{6498a880: PendingIntentRecord{64eaab08 com.google.android.gsf broadcastIntent}}",
				"  RTC_WAKEUP #1: Alarm{64f99a40 type 0 com.android.settings}",
				"    type=0 whenElapsed=4668897 when=+27m34s993ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{65124608: PendingIntentRecord{65124568 com.android.settings broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64fa0b98 type 2 android}",
				"    type=2 whenElapsed=5738198 when=+45m24s294ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64b041b8: PendingIntentRecord{64e2e728 android broadcastIntent}}",
				"Batch{64e32238 num=1 start=6092810 end=6092810}:",
				"  RTC_WAKEUP #0: Alarm{64f7a4e0 type 0 com.cleanmaster.mguard}",
				"    type=0 whenElapsed=6092810 when=+51m18s906ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{650583b8: PendingIntentRecord{64fdb978 com.cleanmaster.mguard startService}}",
				"Batch{64e304d0 num=1 start=6159264 end=6159264}:",
				"  ELAPSED_WAKEUP #0: Alarm{64f78088 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=6159264 when=+52m25s360ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ff81f8: PendingIntentRecord{64e70148 com.google.android.gms broadcastIntent}}",
				"Batch{64e2fc68 num=2 start=10861583 end=10861583}:",
				"  RTC_WAKEUP #1: Alarm{64f66f80 type 0 com.google.android.googlequicksearchbox}",
				"    type=0 whenElapsed=9844000 when=+1h53m50s97ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{650ec9f8: PendingIntentRecord{6501ecf8 com.google.android.googlequicksearchbox startService}}",
				"  ELAPSED_WAKEUP #0: Alarm{64f6c438 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=10861583 when=+2h10m47s679ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f7f270: PendingIntentRecord{64f9ff88 com.google.android.gms broadcastIntent}}",
				"Batch{64e13cd8 num=1 start=16108037 end=16108037}:",
				"  ELAPSED #0: Alarm{64f63c90 type 3 mobi.mgeek.TunnyBrowser}",
				"    type=3 whenElapsed=16108037 when=+3h38m14s133ms window=0 repeatInterval=14400000 count=0",
				"    operation=PendingIntent{64fbeef0: PendingIntentRecord{651524a8 mobi.mgeek.TunnyBrowser startService}}",
				"Batch{64de4400 num=1 start=19095475 end=19095475}:",
				"  RTC #0: Alarm{64f5f0b0 type 1 com.android.chrome}",
				"    type=1 whenElapsed=19095475 when=+4h28m1s572ms window=0 repeatInterval=18000000 count=0",
				"    operation=PendingIntent{64ea9fd8: PendingIntentRecord{6515fd40 com.android.chrome startService}}",
				"Batch{64ddfdf8 num=1 start=21595187 end=21595187}:",
				"  RTC #0: Alarm{64f57f20 type 1 android}",
				"    type=1 whenElapsed=21595187 when=+5h9m41s284ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{65064480: PendingIntentRecord{64e98898 android broadcastIntent}}",
				"Batch{64d90118 num=2 start=28851294 end=40679503}:",
				"  RTC_WAKEUP #1: Alarm{64f51268 type 0 com.cleanmaster.mguard}",
				"    type=0 whenElapsed=24479503 when=+5h57m45s599ms window=0 repeatInterval=21600000 count=0",
				"    operation=PendingIntent{64f50d28: PendingIntentRecord{64e8a730 com.cleanmaster.mguard startService}}",
				"  ELAPSED #0: Alarm{64f51d40 type 3 com.android.phone}",
				"    type=3 whenElapsed=28851294 when=+7h10m37s390ms window=-1 repeatInterval=28800000 count=0",
				"    operation=PendingIntent{64e81a90: PendingIntentRecord{64f181a0 com.android.phone broadcastIntent}}",
				"Batch{64d81648 num=1 start=45068197 end=45068197}:",
				"  RTC_WAKEUP #0: Alarm{64f503f8 type 0 com.google.android.googlequicksearchbox}",
				"    type=0 whenElapsed=45068197 when=+11h40m54s293ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e43d08: PendingIntentRecord{651f0f58 com.google.android.googlequicksearchbox startService}}",
				"Batch{64d57198 num=1 start=46187197 end=46187197 STANDALONE}:",
				"  RTC #0: Alarm{64f38250 type 1 android}",
				"    type=1 whenElapsed=46187197 when=+11h59m33s293ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64bb92d8: PendingIntentRecord{64cc17b0 android broadcastIntent}}",
				"Batch{64d66ee0 num=2 start=46187197 end=46187197}:",
				"  RTC_WAKEUP #1: Alarm{64f4c7d0 type 0 com.google.android.keep}",
				"    type=0 whenElapsed=46187197 when=+11h59m33s293ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64e746b8: PendingIntentRecord{64fe42a0 com.google.android.keep broadcastIntent}}",
				"  RTC #0: Alarm{64f49608 type 1 com.google.android.calendar}",
				"    type=1 whenElapsed=46187197 when=+11h59m33s293ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{650ad6f8: PendingIntentRecord{64f9f218 com.google.android.calendar broadcastIntent}}",
				"Batch{64d2e050 num=1 start=54952832 end=54952832}:",
				"  RTC_WAKEUP #0: Alarm{64f30bc8 type 0 com.google.android.gms}",
				"    type=0 whenElapsed=54952832 when=+14h25m38s928ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64bf71c0: PendingIntentRecord{64f99d40 com.google.android.gms startService}}",
				"Batch{64d2d958 num=1 start=79443547 end=79443547}:",
				"  RTC_WAKEUP #0: Alarm{64f2ea38 type 0 com.android.providers.calendar}",
				"    type=0 whenElapsed=79443547 when=+21h13m49s643ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64eb3f48: PendingIntentRecord{6510a220 com.android.providers.calendar broadcastIntent}}",
				"Batch{64d22d48 num=1 start=86476776 end=86476776}:",
				"  RTC_WAKEUP #0: Alarm{64f0f918 type 0 com.google.android.gms}",
				"    type=0 whenElapsed=86476776 when=+23h11m2s872ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ad6b10: PendingIntentRecord{64d22100 com.google.android.gms broadcastIntent}}",
				"Batch{64d16b80 num=2 start=89273846 end=89273846}:",
				"  RTC #1: Alarm{64f07a58 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=89273846 when=+23h57m39s943ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{651ff630: PendingIntentRecord{64f63a00 com.cleanmaster.mguard broadcastIntent}}",
				"  RTC_WAKEUP #0: Alarm{64f09238 type 0 com.koushikdutta.rommanager}",
				"    type=0 whenElapsed=86532111 when=+23h11m58s208ms window=-1 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{64d07350: PendingIntentRecord{64d072f0 com.koushikdutta.rommanager broadcastIntent}}",
				"Batch{64d0b510 num=2 start=95552474 end=95552474}:",
				"  RTC_WAKEUP #1: Alarm{64f03808 type 0 com.android.vending}",
				"    type=0 whenElapsed=95552474 when=+1d1h42m18s571ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{6516fb70: PendingIntentRecord{65063840 com.android.vending startService}}",
				"  RTC_WAKEUP #0: Alarm{64f04870 type 0 com.cleanmaster.mguard}",
				"    type=0 whenElapsed=89279523 when=+23h57m45s620ms window=0 repeatInterval=86400000 count=0",
				"    operation=PendingIntent{6505f030: PendingIntentRecord{64d0cf38 com.cleanmaster.mguard startService}}",
				"Batch{64d0af10 num=1 start=109539073 end=109539073}:",
				"  RTC_WAKEUP #0: Alarm{64efb5f0 type 0 com.cyanogenmod.updater}",
				"    type=0 whenElapsed=109539073 when=+1d5h35m25s170ms window=0 repeatInterval=604800000 count=0",
				"    operation=PendingIntent{6498a538: PendingIntentRecord{64f0a850 com.cyanogenmod.updater startService}}",
				"Batch{64cfecd0 num=1 start=184621989 end=184621989}:",
				"  RTC #0: Alarm{64efa180 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=184621989 when=+2d2h26m48s85ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f9cbe8: PendingIntentRecord{64f40878 com.cleanmaster.mguard broadcastIntent}}",
				"Batch{64cfd448 num=3 start=351766245 end=351766245}:",
				"  RTC_WAKEUP #2: Alarm{64eb7c20 type 0 com.google.android.gsf}",
				"    type=0 whenElapsed=203611099 when=+2d7h43m17s195ms window=-1 repeatInterval=522504000 count=0",
				"    operation=PendingIntent{64f55760: PendingIntentRecord{6505b690 com.google.android.gsf broadcastIntent}}",
				"  ELAPSED #1: Alarm{64ebf5a8 type 3 ch.bitspin.timely}",
				"    type=3 whenElapsed=351766245 when=+4d0h52m32s341ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ed4b98: PendingIntentRecord{64ffaf48 ch.bitspin.timely broadcastIntent}}",
				"  ELAPSED_WAKEUP #0: Alarm{64ef3d50 type 2 com.google.android.talk}",
				"    type=2 whenElapsed=259314935 when=+2d23h11m41s31ms window=-1 repeatInterval=259200000 count=0",
				"    operation=PendingIntent{64ef9e90: PendingIntentRecord{64f42498 com.google.android.talk broadcastIntent}}",
				"Batch{64ce6af0 num=1 start=438379461 end=438379461}:",
				"  RTC_WAKEUP #0: Alarm{64eb7138 type 0 com.google.android.gms}",
				"    type=0 whenElapsed=438379461 when=+5d0h56m5s557ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64b7d2e8: PendingIntentRecord{65029010 com.google.android.gms broadcastIntent}}",
				"Batch{64ce45b8 num=3 start=875821988 end=875821988}:",
				"  RTC #2: Alarm{64eada88 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=875821988 when=+10d2h26m48s85ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{650667a8: PendingIntentRecord{650094b0 com.cleanmaster.mguard broadcastIntent}}",
				"  RTC_WAKEUP #1: Alarm{64eb2418 type 0 com.google.android.partnersetup}",
				"    type=0 whenElapsed=604893392 when=+6d23h11m19s488ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fb5b30: PendingIntentRecord{64ff4820 com.google.android.partnersetup startService}}",
				"  ELAPSED #0: Alarm{64eb3780 type 3 android}",
				"    type=3 whenElapsed=864177065 when=+9d23h12m43s161ms window=-1 repeatInterval=0 count=0",
				"    operation=PendingIntent{64ccaa58: PendingIntentRecord{64cf76e8 android broadcastIntent}}",
				"Batch{64cc7288 num=1 start=1827447960 end=1827447960}:",
				"  ELAPSED_WAKEUP #0: Alarm{64eac760 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=1827447960 when=+21d2h47m14s56ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64f706c0: PendingIntentRecord{64f5d258 com.google.android.gms startService}}",
				"Batch{64cb1a68 num=1 start=1827451644 end=1827451644}:",
				"  ELAPSED_WAKEUP #0: Alarm{64ea0598 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=1827451644 when=+21d2h47m17s740ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64d7fc88: PendingIntentRecord{64f61c18 com.google.android.gms startService}}",
				"Batch{64cb0710 num=1 start=1827451924 end=1827451924}:",
				"  ELAPSED_WAKEUP #0: Alarm{64e99420 type 2 com.google.android.gms}",
				"    type=2 whenElapsed=1827451924 when=+21d2h47m18s20ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64eb1d90: PendingIntentRecord{64e0eac0 com.google.android.gms startService}}",
				"Batch{64ca2620 num=1 start=2171821989 end=2171821989}:",
				"  RTC #0: Alarm{64e8a178 type 1 com.cleanmaster.mguard}",
				"    type=1 whenElapsed=2171821989 when=+25d2h26m48s85ms window=0 repeatInterval=0 count=0",
				"    operation=PendingIntent{64fd5740: PendingIntentRecord{64e1cd20 com.cleanmaster.mguard broadcastIntent}}\r\n\r\n",

				"  Broadcast ref count: 0\r\n\r\n",

				"  Top Alarms:",
				"    +1m44s832ms running, 2 wakeups, 2 alarms: com.android.providers.calendar",
				"       act=com.android.providers.calendar.intent.CalendarProvider2",
				"    +1m43s298ms running, 0 wakeups, 2 alarms: android",
				"       act=com.android.server.action.NETWORK_STATS_POLL",
				"    +1m43s242ms running, 0 wakeups, 2 alarms: com.cleanmaster.mguard",
				"       act=com.ijinshan.common.kinfoc.ActivityTimer",
				"    +1m39s763ms running, 1 wakeups, 1 alarms: com.android.settings",
				"       act=android.net.conn.CONNECTIVITY_CHANGE cmp={com.android.settings/com.android.settings.cmstats.ReportingServiceManager}",
				"    +1m28s987ms running, 5 wakeups, 5 alarms: com.google.android.gms",
				"       act=com.google.android.intent.action.GCM_RECONNECT",
				"    +1m17s554ms running, 4 wakeups, 4 alarms: android",
				"       act=android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED",
				"    +1m7s900ms running, 0 wakeups, 1 alarms: android",
				"       act=com.android.server.NetworkTimeUpdateService.action.POLL",
				"    +1m4s455ms running, 5 wakeups, 5 alarms: com.google.android.gms",
				"       act=com.google.android.intent.action.SEND_IDLE",
				"    +1m1s687ms running, 3 wakeups, 3 alarms: com.google.android.talk",
				"       act=com.google.android.apps.babel.UPDATE_NOTIFICATION",
				"    +1m1s132ms running, 0 wakeups, 40 alarms: com.android.chrome",
				"       cmp={com.android.chrome/com.google.ipc.invalidation.ticl.android2.AndroidInternalScheduler$AlarmReceiver}",
				" ",
				"  Alarm Stats:",
				"  com.google.android.talk +1m1s700ms running, 4 wakeups:",
				"    +1m1s687ms 3 wakes 3 alarms: act=com.google.android.apps.babel.UPDATE_NOTIFICATION",
				"    +53s195ms 1 wakes 1 alarms: act=com.google.android.apps.babel.RENEW_ACCOUNT_REGISTRATION",
				"  android +2m23s196ms running, 19 wakeups:",
				"    +1m43s298ms 0 wakes 2 alarms: act=com.android.server.action.NETWORK_STATS_POLL",
				"    +1m17s554ms 4 wakes 4 alarms: act=android.net.ConnectivityService.action.PKT_CNT_SAMPLE_INTERVAL_ELAPSED",
				"    +1m7s900ms 0 wakes 1 alarms: act=com.android.server.NetworkTimeUpdateService.action.POLL",
				"    +29s179ms 0 wakes 39 alarms: act=android.intent.action.TIME_TICK",
				"    +4s428ms 9 wakes 9 alarms: act=com.android.server.WifiManager.action.START_SCAN",
				"    +630ms 6 wakes 6 alarms: act=android.content.syncmanager.SYNC_ALARM",
				"  com.android.settings +1m39s763ms running, 1 wakeups:",
				"    +1m39s763ms 1 wakes 1 alarms: act=android.net.conn.CONNECTIVITY_CHANGE cmp={com.android.settings/com.android.settings.cmstats.ReportingServiceManager}",
				"  com.cyanogenmod.lockclock +1ms running, 1 wakeups:",
				"    +1ms 1 wakes 1 alarms: cmp={com.cyanogenmod.lockclock/com.cyanogenmod.lockclock.weather.WeatherUpdateService}",
				"  com.android.deskclock +13s108ms running, 0 wakeups:",
				"    +13s108ms 0 wakes 4 alarms: act=com.android.deskclock.ON_QUARTER_HOUR",
				"  com.android.phone +20s819ms running, 14 wakeups:",
				"    +20s819ms 14 wakes 14 alarms: act=com.android.internal.telephony.data-stall",
				"  com.google.android.gsf +2s676ms running, 2 wakeups:",
				"    +2s676ms 2 wakes 2 alarms: cmp={com.google.android.gsf/com.google.android.gsf.checkin.EventLogService$Receiver}",
				"  com.android.keyguard +3s79ms running, 6 wakeups:",
				"    +3s79ms 6 wakes 6 alarms: act=com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD",
				"  com.google.android.googlequicksearchbox +8s780ms running, 8 wakeups:",
				"    +8s478ms 3 wakes 3 alarms: act=com.google.android.sidekick.main.calendar.CalendarIntentService.UPDATE_CALENDAR_ACTION cmp={com.google.android.googlequicksearchbox/com.google.android.sidekick.main.calendar.CalendarIntentService$CalendarReceiver}",
				"    +180ms 2 wakes 2 alarms: act=ACTION_UPDATE_APP_SCORES cmp={com.google.android.googlequicksearchbox/com.google.android.search.core.summons.icing.InternalIcingCorporaProvider$UpdateCorporaService}",
				"    +72ms 1 wakes 1 alarms: act=com.google.android.apps.sidekick.REFRESH cmp={com.google.android.googlequicksearchbox/com.google.android.sidekick.main.entry.EntriesRefreshIntentService}",
				"    +50ms 2 wakes 2 alarms: cmp={com.google.android.googlequicksearchbox/com.google.android.sidekick.main.TrafficIntentService}",
				"  com.android.chrome +1m25s562ms running, 0 wakeups:",
				"    +1m1s132ms 0 wakes 40 alarms: cmp={com.android.chrome/com.google.ipc.invalidation.ticl.android2.AndroidInternalScheduler$AlarmReceiver}",
				"    +30s122ms 0 wakes 75 alarms: cmp={com.android.chrome/com.google.ipc.invalidation.external.client.contrib.AndroidListener$AlarmReceiver}",
				"    +1ms 0 wakes 1 alarms: act=com.google.android.apps.chrome.omaha.ACTION_REGISTER_REQUEST cmp={com.android.chrome/com.google.android.apps.chrome.omaha.OmahaClient}",
				"  com.android.vending +409ms running, 5 wakeups:",
				"    +305ms 2 wakes 2 alarms: cmp={com.android.vending/com.google.android.finsky.services.DailyHygiene}",
				"    +104ms 3 wakes 3 alarms: cmp={com.android.vending/com.google.android.finsky.services.ContentSyncService}",
				"  com.cleanmaster.mguard +1m43s279ms running, 4 wakeups:",
				"    +1m43s242ms 0 wakes 2 alarms: act=com.ijinshan.common.kinfoc.ActivityTimer",
				"    +35ms 2 wakes 2 alarms: act=com.cleanmaster.service.ACTION_UPDATE_CLOUD_CFG cmp={com.cleanmaster.mguard/com.cleanmaster.cloudconfig.CloudCfgIntentService}",
				"    +8ms 2 wakes 2 alarms: cmp={com.cleanmaster.mguard/com.cleanmaster.service.PermanentService}",
				"  com.android.providers.calendar +1m44s832ms running, 2 wakeups:",
				"    +1m44s832ms 2 wakes 2 alarms: act=com.android.providers.calendar.intent.CalendarProvider2",
				"  com.google.android.gms +1m32s227ms running, 18 wakeups:",
				"    +1m28s987ms 5 wakes 5 alarms: act=com.google.android.intent.action.GCM_RECONNECT",
				"    +1m4s455ms 5 wakes 5 alarms: act=com.google.android.intent.action.SEND_IDLE",
				"    +504ms 8 wakes 8 alarms: act=com.google.android.gms.nlp.ALARM_WAKEUP_LOCATOR"
		};
	} 
	
	String[] getDataStart2(){   
		return new String[]{
				"Current Alarm Manager state:"					 
				  ,"Realtime wakeup (now=2014-08-13 14:31:42):"
				  ,"RTC_WAKEUP #34: Alarm{4248ca88 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+43d0h8m9s666ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{41b1f4e0: PendingIntentRecord{43c6adc8 com.wssyncmldm broadcastIntent}}"
				  ," RTC_WAKEUP #33: Alarm{43be6470 type 0 com.google.android.partnersetup}"
				  ,"  type=0 when=+6d18h53m5s720ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43be6388: PendingIntentRecord{43be4ef0 com.google.android.partnersetup startService}}"
				  ,"RTC_WAKEUP #32: Alarm{43d71d18 type 0 com.google.android.gms}"
				  ,"  type=0 when=+5d21h52m33s992ms repeatInterval=529166000 count=0"
				  ,"  operation=PendingIntent{43c908f0: PendingIntentRecord{43c9d8d8 com.google.android.gms broadcastIntent}}"
				  ,"RTC_WAKEUP #31: Alarm{43caac68 type 0 com.google.android.gms}"
				  ,"  type=0 when=+5d1h8m9s811ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43caab20: PendingIntentRecord{43c838e0 com.google.android.gms broadcastIntent}}"
				  ,"RTC_WAKEUP #30: Alarm{43e6f430 type 0 com.sec.tetheringprovision}"
				  ,"  type=0 when=+21h29m58s602ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43e30b68: PendingIntentRecord{43e686b0 com.sec.tetheringprovision broadcastIntent}}"
				  ,"RTC_WAKEUP #29: Alarm{43bb09d8 type 0 com.sec.tetheringprovision}"
				  ,"  type=0 when=+21h28m11s882ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43c03b70: PendingIntentRecord{43bb1128 com.sec.tetheringprovision broadcastIntent}}"
				  ,"RTC_WAKEUP #28: Alarm{423be210 type 0 com.android.providers.calendar}"
				  ,"  type=0 when=+16h54m25s649ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{423cf3f8: PendingIntentRecord{423cd3d0 com.android.providers.calendar broadcastIntent}}"
				  ,"RTC_WAKEUP #27: Alarm{43bef9f8 type 0 com.android.vending}"
				  ,"  type=0 when=+16h33m4s216ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43c55b58: PendingIntentRecord{43c47b98 com.android.vending startService}}"
				  ,"RTC_WAKEUP #26: Alarm{43c585f8 type 0 com.google.android.gms}"
				  ,"  type=0 when=+13h15m44s258ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43bffd60: PendingIntentRecord{43c4e358 com.google.android.gms broadcastIntent}}"
				  ,"RTC_WAKEUP #25: Alarm{43ccce58 type 0 com.google.android.gms}"
				  ,"  type=0 when=+13h4m3s43ms repeatInterval=83645966 count=16832"
				   ," operation=PendingIntent{42416280: PendingIntentRecord{43d29318 com.google.android.gms startService}}"
				  ,"RTC_WAKEUP #24: Alarm{43c793a0 type 0 com.google.android.gms}"
				  ,"  type=0 when=+9h40m32s217ms repeatInterval=86400000 count=0"
				  ,"  operation=PendingIntent{4231eec8: PendingIntentRecord{43bdc770 com.google.android.gms startService}}"
				  ,"RTC_WAKEUP #23: Alarm{43cefe90 type 0 com.google.android.googlequicksearchbox}"
				  ,"  type=0 when=+7h8m16s365ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{43cabfd0: PendingIntentRecord{43cefd98 com.google.android.googlequicksearchbox startService}}"
				  ,"RTC_WAKEUP #22: Alarm{43da2160 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h59m42s834ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43d9bf90: PendingIntentRecord{43d9bda8 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #21: Alarm{43d21d50 type 0 com.wssyncmldm}"
				   ," type=0 when=+3h59m42s820ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{41b31e40: PendingIntentRecord{42445880 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #20: Alarm{43cb22d8 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h54m5s45ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{420dd8f8: PendingIntentRecord{4211fab8 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #19: Alarm{43bf6500 type 0 com.wssyncmldm}"
				   ," type=0 when=+3h33m55s910ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43d266e8: PendingIntentRecord{43bca000 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #18: Alarm{43d791f0 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h33m55s889ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{42455ee0: PendingIntentRecord{43d264d0 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #17: Alarm{423ed2d8 type 0 com.wssyncmldm}"
				 ,"   type=0 when=+3h33m50s376ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43d0a0e8: PendingIntentRecord{43cbe198 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #16: Alarm{43293628 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h33m50s353ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43d05120: PendingIntentRecord{4246b0d0 com.wssyncmldm broadcastIntent}}"
				,"  RTC_WAKEUP #15: Alarm{43cc28f8 type 0 com.wssyncmldm}"
				 ,"   type=0 when=+3h30m15s109ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43bbd3c8: PendingIntentRecord{43bf32f0 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #14: Alarm{43c085a8 type 0 com.wssyncmldm}"
				 ,"   type=0 when=+3h30m15s93ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{424181b0: PendingIntentRecord{424180b0 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #13: Alarm{43b7f740 type 0 com.wssyncmldm}"
				 ,"   type=0 when=+3h30m3s988ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43e5dff0: PendingIntentRecord{42479700 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #12: Alarm{43bf2aa0 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h30m3s977ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43c76408: PendingIntentRecord{432bbba8 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #11: Alarm{43df6ae8 type 0 com.wssyncmldm}"
				 ,"   type=0 when=+3h21m28s689ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43c14708: PendingIntentRecord{43356398 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #10: Alarm{43d8d300 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h21m28s670ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{43d145f0: PendingIntentRecord{43dd5f00 com.wssyncmldm broadcastIntent}}"
				 ," RTC_WAKEUP #9: Alarm{43e016e8 type 0 com.wssyncmldm}"
				  ,"  type=0 when=+3h21m15s385ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43cb2ce8: PendingIntentRecord{43db72b8 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #8: Alarm{43cc3290 type 0 com.wssyncmldm}"
				   ," type=0 when=+3h18m10s419ms repeatInterval=0 count=0"
				  ,"  operation=PendingIntent{43cd3438: PendingIntentRecord{43cd3358 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #7: Alarm{43d6cef0 type 0 com.wssyncmldm}"
				    ,"type=0 when=+3h18m10s397ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{43df1de8: PendingIntentRecord{43dd93c8 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #6: Alarm{43cd2740 type 0 com.wssyncmldm}"
				    ,"type=0 when=+3h18m1s9ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{422d0928: PendingIntentRecord{43c7e780 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #5: Alarm{43ce4168 type 0 com.wssyncmldm}"
				    ,"type=0 when=+3h18m0s981ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{423048c8: PendingIntentRecord{43cbdc28 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #4: Alarm{43bb2970 type 0 com.google.android.googlequicksearchbox}"
				    ,"type=0 when=+1h23m30s947ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{43e484f0: PendingIntentRecord{43bc40d8 com.google.android.googlequicksearchbox startService}}"
				  ,"RTC_WAKEUP #3: Alarm{43bb9040 type 0 com.wssyncmldm}"
				    ,"type=0 when=+1h2m31s117ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{43c9ade0: PendingIntentRecord{43cba148 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #2: Alarm{43bd6890 type 0 com.wssyncmldm}"
				    ,"type=0 when=+1h2m31s106ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{43ca8af0: PendingIntentRecord{41c307f0 com.wssyncmldm broadcastIntent}}"
				  ,"RTC_WAKEUP #1: Alarm{422ad4e0 type 0 android}"
				    ,"type=0 when=+1h0m10s678ms repeatInterval=3651715 count=1"
				   ," operation=PendingIntent{42495020: PendingIntentRecord{42494f80 android broadcastIntent}}"
				  ,"RTC_WAKEUP #0: Alarm{43e08338 type 0 com.google.android.gms}"
				   ," type=0 when=+23m15s482ms repeatInterval=1800000 count=0"
				    ,"operation=PendingIntent{43de3208: PendingIntentRecord{43c946e0 com.google.android.gms broadcastIntent}}"
				  ,"RTC #3: Alarm{43c53b88 type 1 com.houzz.app}"
				   ," type=1 when=+3d19h20m57s801ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{43c5cc30: PendingIntentRecord{43c1f070 com.houzz.app broadcastIntent}}"
				 ," RTC #2: Alarm{43e17af0 type 1 com.google.android.gms}"
				   ," type=1 when=+18h53m25s505ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{43e17a20: PendingIntentRecord{43d4c230 com.google.android.gms broadcastIntent}}"
				  ,"RTC #1: Alarm{422c1130 type 1 android}"
				   ," type=1 when=+9h28m17s365ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{4211d0f8: PendingIntentRecord{422c0040 android broadcastIntent}}"
				 ," RTC #0: Alarm{43bd58a0 type 1 com.google.android.gms}"
				  ,"  type=1 when=+22m36s369ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43bdd4e8: PendingIntentRecord{43bd8488 com.google.android.gms broadcastIntent}}"
				  
				  ,"Elapsed realtime wakeup (now=+5h7m28s99ms):"
				  ,"ELAPSED_WAKEUP #22: Alarm{43d40f78 type 2 com.google.android.gms}"
				  ,"  type=2 when=+21d3h11m26s127ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43da82b0: PendingIntentRecord{43c19780 com.google.android.gms startService}}"
				 ," ELAPSED_WAKEUP #21: Alarm{43bbfe10 type 2 com.google.android.gms}"
				   ," type=2 when=+21d3h11m26s112ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{43bc0720: PendingIntentRecord{41e4e110 com.google.android.gms startService}}"
				 ," ELAPSED_WAKEUP #20: Alarm{43c176a0 type 2 com.google.android.gms}"
				   ," type=2 when=+21d3h11m26s107ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{43c19e68: PendingIntentRecord{4214a7b0 com.google.android.gms startService}}"
				  ,"ELAPSED_WAKEUP #19: Alarm{4229e378 type 2 com.google.android.gms}"
				    ,"type=2 when=+21d3h11m25s833ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{422b6de8: PendingIntentRecord{43d31710 com.google.android.gms startService}}"
				  ,"ELAPSED_WAKEUP #18: Alarm{42495968 type 2 com.google.android.gms}"
				    ,"type=2 when=+21d3h11m25s608ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{41b3fec8: PendingIntentRecord{43d34a38 com.google.android.gms startService}}"
				  ,"ELAPSED_WAKEUP #17: Alarm{420c96f8 type 2 com.google.android.apps.plus}"
				   ," type=2 when=+5d9h52m31s901ms repeatInterval=43200000 count=0"
				    ,"operation=PendingIntent{42130e48: PendingIntentRecord{43c7e678 com.google.android.apps.plus broadcastIntent}}"
				  ,"ELAPSED_WAKEUP #16: Alarm{43c01e40 type 2 com.google.android.location}"
				    ,"type=2 when=+23h34m44s715ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{43d7d7f8: PendingIntentRecord{43ba0f28 com.google.android.location broadcastIntent}}"
				  ,"ELAPSED_WAKEUP #15: Alarm{423a5820 type 2 com.google.android.location}"
				    ,"type=2 when=+23h11m16s336ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{41d165d0: PendingIntentRecord{43ba19f8 com.google.android.location broadcastIntent}}"
				  ,"ELAPSED_WAKEUP #14: Alarm{43d6b038 type 2 com.google.android.gms}"
				    ,"type=2 when=+21h5m6s529ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{41c86478: PendingIntentRecord{43c3c470 com.google.android.gms broadcastIntent}}"
				 ," ELAPSED_WAKEUP #13: Alarm{422ec8d0 type 2 com.google.android.gms}"
				   ," type=2 when=+18h52m31s901ms repeatInterval=86400000 count=0"
				  ,"  operation=PendingIntent{43dccfd8: PendingIntentRecord{43cb3798 com.google.android.gms startService}}"
				  ,"ELAPSED_WAKEUP #12: Alarm{42451110 type 2 com.google.android.gms}"
				    ,"type=2 when=+15h47m48s939ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{4327d288: PendingIntentRecord{43c3ebb0 com.google.android.gms broadcastIntent}}"
				  ,"ELAPSED_WAKEUP #11: Alarm{43cdad10 type 2 com.google.android.gms}"
				   ," type=2 when=+12h28m17s312ms repeatInterval=0 count=0"
				   ," operation=PendingIntent{42458358: PendingIntentRecord{43c3c6f8 com.google.android.gms broadcastIntent}}"
				,"  ELAPSED_WAKEUP #10: Alarm{43c52238 type 2 com.google.android.googlequicksearchbox}"
				,"    type=2 when=+7h2m20s721ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43c5c0c8: PendingIntentRecord{43dfede8 com.google.android.googlequicksearchbox startService}}"
				,"  ELAPSED_WAKEUP #9: Alarm{43bf9358 type 2 com.google.android.gms}"
				,"    type=2 when=+1h35m59s301ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43d27730: PendingIntentRecord{43c3cbf0 com.google.android.gms broadcastIntent}}"
				,"  ELAPSED_WAKEUP #8: Alarm{41f2db70 type 2 com.google.android.gms}"
				,"    type=2 when=+1h28m17s312ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{422ed240: PendingIntentRecord{43c3c970 com.google.android.gms broadcastIntent}}"
				,"  ELAPSED_WAKEUP #7: Alarm{43c058e0 type 2 com.google.android.gms}"
				,"    type=2 when=+1h7m31s901ms repeatInterval=3600000 count=0"
				,"    operation=PendingIntent{43bfa308: PendingIntentRecord{43d073a0 com.google.android.gms startService}}"
				,"  ELAPSED_WAKEUP #6: Alarm{43c85028 type 2 com.google.android.gsf}"
				,"    type=2 when=+27m40s949ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{42d791e8: PendingIntentRecord{42437b78 com.google.android.gsf broadcastIntent}}"
				,"  ELAPSED_WAKEUP #5: Alarm{43bfe5e8 type 2 android}"
				,"    type=2 when=+22m35s314ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{41ea6650: PendingIntentRecord{42376220 android broadcastIntent}}"
				 ," ELAPSED_WAKEUP #4: Alarm{43c986d0 type 2 com.google.android.googlequicksearchbox}"
				,"    type=2 when=+8m29s176ms repeatInterval=0 count=0"
			,"	    operation=PendingIntent{43caba78: PendingIntentRecord{43d31a90 com.google.android.googlequicksearchbox broadcastIntent}}"
				,"  ELAPSED_WAKEUP #3: Alarm{424917e8 type 2 android}"
				 ,"   type=2 when=+7m53s453ms repeatInterval=600000 count=1"
				  ,"  operation=PendingIntent{42370820: PendingIntentRecord{423715a8 android broadcastIntent}}"
				 ," ELAPSED_WAKEUP #2: Alarm{43c30d08 type 2 android}"
				 ,"   type=2 when=+7m31s901ms repeatInterval=1800000 count=1"
				,"    operation=PendingIntent{43c30c88: PendingIntentRecord{43c30ba8 android broadcastIntent}}"
				 ," ELAPSED_WAKEUP #1: Alarm{43c7b678 type 2 com.google.android.gms}"
				,"    type=2 when=+21s12ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43def060: PendingIntentRecord{43c3cec0 com.google.android.gms broadcastIntent}}"
				,"  ELAPSED_WAKEUP #0: Alarm{43cb1ef8 type 2 com.google.android.gms}"
				,"    type=2 when=+20s12ms repeatInterval=0 count=0"
				 ,"   operation=PendingIntent{43baabb0: PendingIntentRecord{43c39c78 com.google.android.gms broadcastIntent}}"
				,"  ELAPSED #10: Alarm{43e681d8 type 3 com.sec.android.app.sysscope}"
				,"    type=3 when=+23h47m24s429ms repeatInterval=86400000 count=1"
				,"    operation=PendingIntent{43db6b70: PendingIntentRecord{4245fd88 com.sec.android.app.sysscope startService}}"
				,"  ELAPSED #9: Alarm{43d33b28 type 3 android}"
				,"    type=3 when=+18h53m54s769ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{41ff0ab8: PendingIntentRecord{422d7520 android broadcastIntent}}"
				,"  ELAPSED #8: Alarm{43c3aa20 type 3 com.google.android.gms}"
				,"    type=3 when=+6h53m25s944ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43b84450: PendingIntentRecord{43c3a928 com.google.android.gms broadcastIntent}}"
				,"  ELAPSED #7: Alarm{43dc12f0 type 3 com.sec.pcw}"
				,"    type=3 when=+5h37m31s901ms repeatInterval=21600000 count=0"
				,"    operation=PendingIntent{43b98ce0: PendingIntentRecord{43bc1da0 com.sec.pcw broadcastIntent}}"
				,"  ELAPSED #6: Alarm{43bb2198 type 3 com.android.phone}"
				,"    type=3 when=+3h7m31s901ms repeatInterval=28800000 count=0"
				,"    operation=PendingIntent{43bb2118: PendingIntentRecord{43bafd58 com.android.phone broadcastIntent}}"
				,"  ELAPSED #5: Alarm{43e68a20 type 3 com.android.vending}"
				,"    type=3 when=+55m17s102ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43e49648: PendingIntentRecord{43e232a0 com.android.vending startService}}"
				,"  ELAPSED #4: Alarm{43c30838 type 3 com.tgrape.android.radar}"
				,"    type=3 when=+53m6s879ms repeatInterval=3600000 count=1"
				,"    operation=PendingIntent{43c8fa08: PendingIntentRecord{43baeba8 com.tgrape.android.radar broadcastIntent}}"
				,"  ELAPSED #3: Alarm{43ccedf8 type 3 android}"
				,"    type=3 when=+9m18s857ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{41ee6dc0: PendingIntentRecord{423ef708 android broadcastIntent}}"
				,"  ELAPSED #2: Alarm{422fd9f0 type 3 android}"
				 ,"   type=3 when=+7m31s901ms repeatInterval=1800000 count=1"
				,"    operation=PendingIntent{41fdf008: PendingIntentRecord{422fc700 android broadcastIntent}}"
				  ,"ELAPSED #1: Alarm{43cab4a0 type 3 com.android.phone}"
				 ,"   type=3 when=+38s978ms repeatInterval=0 count=0"
				,"    operation=PendingIntent{43c19298: PendingIntentRecord{43c1cc20 com.android.phone broadcastIntent}}"
				,"  ELAPSED #0: Alarm{423bfcf8 type 3 android}"
				,"    type=3 when=+17s311ms repeatInterval=0 count=0"
				    ,"operation=PendingIntent{422ee608: PendingIntentRecord{422bfe28 android broadcastIntent}}"			 

				,"  Broadcast ref count: 0"
				,"  Alarm Stats:"
				,"com.google.android.gsf"
				," 1252ms running, 12 wakeups"
				," 4 alarms: ","act=com.google.android.intent.action.SEND_IDLE flg=0x14"
				,"8 alarms: ","act=com.google.android.intent.action.MCS_HEARTBEAT flg=0x14"
				,"android"
				," 103543ms running, 409 wakeups"
				,"379 alarms: ","act=com.android.server.LightsService.action.UPDATE_SVC_LED flg=0x14"
				,"307 alarms: ","act=android.intent.action.TIME_TICK flg=0x40000014"
				,"10 alarms: ","act=com.android.server.action.NETWORK_STATS_POLL flg=0x14"
				,"30 alarms: ","act=com.android.server.ThrottleManager.action.POLL flg=0x14"
				,"9 alarms: ","act=android.appwidget.action.APPWIDGET_UPDATE flg=0x14"
				,"5 alarms: ","act=com.android.internal.policy.impl.PhoneWindowManager.DELAYED_KEYGUARD flg=0x14"
				,"1 alarms: ","act=com.android.server.NetworkTimeUpdateService.action.POLL flg=0x14"
				,"5 alarms: ","act=android.app.backup.intent.RUN flg=0x40000014"
				,"11 alarms: act=android.content.syncmanager.SYNC_ALARM flg=0x14"
				,"com.android.vending"
				," 544ms running, 3 wakeups"
				,"1 alarms: ","flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.DailyHygiene"
				,"5 alarms: ","flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.CheckWifiAndAutoUpdate"
				,"2 alarms: ","flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.ContentSyncService"
				,"com.tgrape.android.radar"
				," 16433ms running, 0 wakeups"
				,"7 alarms: ","act=com.tgrape.android.radar.alarm.background flg=0x14"
				,"com.android.providers.calendar"
				," 12654ms running, 1 wakeups"
				,"1 alarms: ","act=com.android.providers.calendar.intent.CalendarProvider2 flg=0x14"
				,"com.sec.android.app.sysscope"
				," 17ms running, 0 wakeups"
				,"3 alarms: ","act=com.sec.intent.action.SYSSCOPE flg=0x4"
				,"com.google.android.gms"
				," 19980ms running, 344 wakeups"
				,"23 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_ACTIVITY_DETECTION flg=0x14"
				,"13 alarms: ","flg=0x14"
				,"1 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_CACHE_UPDATER flg=0x14"
				,"4 alarms: ","act=com.google.android.location.reporting.ACTION_UPDATE_WORLD flg=0x4 cmp=com.google.android.gms/com.google.android.location.reporting.service.DispatchingService"
				,"5 alarms: ","act=com.google.android.intent.action.SEND_IDLE flg=0x14"
				,"2 alarms: ","act=com.google.android.intent.action.GCM_RECONNECT flg=0x14"
				,"286 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_LOCATOR flg=0x14"
				,"5 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flg=0x14"
				,"1 alarms: ","flg=0x4 cmp=com.google.android.gms/.security.snet.SnetService"
				,"2 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_S_COLLECTOR flg=0x14"
				,"5 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_IN_OUT_DOOR_COLLECTOR flg=0x14"
				,"com.android.phone"
				," 5231ms running, 0 wakeups"
				,"78 alarms: ","act=com.android.internal.telephony.gprs-data-stall flg=0x14"
		}; 
	}
		 
	String[] getDataStart3(){
		return new String[]{
				"Current Alarm Manager state:",					 
				"Realtime wakeup (now=2014-09-10 11:56:24):",
				"RTC_WAKEUP #11: Alarm{43d01c00 type 0 PendingIntent{43d12d30: PendingIntentRecord{43d0cc38 startService Key{startService pkg=com.google.android.partnersetup intent=cmp=com.google.android.partnersetup/.RlzPingService flags=0x0}}}}",
					  "type=0 when=+6d23h12m57s846ms repeatInterval=0 count=0",
					  "operation=PendingIntent{43d12d30: PendingIntentRecord{43d0cc38 startService Key{startService pkg=com.google.android.partnersetup intent=cmp=com.google.android.partnersetup/.RlzPingService flags=0x0}}}",
					  "RTC_WAKEUP #10: Alarm{43cf6558 type 0 PendingIntent{43cfc908: PendingIntentRecord{43ce21f0 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.recovery.WAKEUP flags=0x0}}}}",
					  "type=0 when=+3d13h18m54s220ms repeatInterval=0 count=0",
					  "operation=PendingIntent{43cfc908: PendingIntentRecord{43ce21f0 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.recovery.WAKEUP flags=0x0}}}",
					  "RTC_WAKEUP #9: Alarm{43e1ec10 type 0 PendingIntent{43e0e448: PendingIntentRecord{43e1e998 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.auth.authzen.CHECK_REGISTRATION flags=0x0}}}}",
					  "type=0 when=+22h14m8s73ms repeatInterval=0 count=0",
					  "operation=PendingIntent{43e0e448: PendingIntentRecord{43e1e998 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.auth.authzen.CHECK_REGISTRATION flags=0x0}}}",
					  "RTC_WAKEUP #8: Alarm{43e7d150 type 0 PendingIntent{43de70b8: PendingIntentRecord{434c0810 startService Key{startService pkg=com.android.vending intent=cmp=com.android.vending/com.google.android.finsky.services.DailyHygiene flags=0x0}}}}",
					  "type=0 when=+21h39m10s922ms repeatInterval=0 count=0",
					  "operation=PendingIntent{43de70b8: PendingIntentRecord{434c0810 startService Key{startService pkg=com.android.vending intent=cmp=com.android.vending/com.google.android.finsky.services.DailyHygiene flags=0x0}}}",
					  "RTC_WAKEUP #7: Alarm{43df9dc0 type 0 PendingIntent{43d24f68: PendingIntentRecord{43e53518 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/.service.PullNotificationService flags=0x0}}}}",
					  "type=0 when=+21h29m7s514ms repeatInterval=86400000 count=1",
					  "operation=PendingIntent{43d24f68: PendingIntentRecord{43e53518 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/.service.PullNotificationService flags=0x0}}}",
					  "RTC_WAKEUP #6: Alarm{43d8e310 type 0 PendingIntent{43e61320: PendingIntentRecord{43d85498 broadcastIntent Key{broadcastIntent pkg=com.android.providers.calendar intent=act=com.android.providers.calendar.SCHEDULE_ALARM cmp=com.android.providers.calendar/.CalendarReceiver flags=0x0}}}}",
					  "type=0 when=+21h14m20s756ms repeatInterval=0 count=0",
					  "operation=PendingIntent{43e61320: PendingIntentRecord{43d85498 broadcastIntent Key{broadcastIntent pkg=com.android.providers.calendar intent=act=com.android.providers.calendar.SCHEDULE_ALARM cmp=com.android.providers.calendar/.CalendarReceiver flags=0x0}}}",
					  "RTC_WAKEUP #5: Alarm{43e815b0 type 0 PendingIntent{43d2eb98: PendingIntentRecord{43d35db0 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.checkin.CheckinService$Receiver flags=0x0}}}}",
					  "type=0 when=+14h21m6s774ms repeatInterval=558088000 count=0",
					  "operation=PendingIntent{43d2eb98: PendingIntentRecord{43d35db0 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.checkin.CheckinService$Receiver flags=0x0}}}",
					  "RTC_WAKEUP #4: Alarm{4399ec80 type 0 PendingIntent{434c22a8: PendingIntentRecord{4399e968 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/.service.PurgeItemsService flags=0x0}}}}",
					  "type=0 when=+13h55m7s547ms repeatInterval=86400000 count=0",
					  " operation=PendingIntent{434c22a8: PendingIntentRecord{4399e968 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/.service.PurgeItemsService flags=0x0}}}",
					  "RTC_WAKEUP #3: Alarm{43d1e1a8 type 0 PendingIntent{43d96c90: PendingIntentRecord{43cf51c8 startService Key{startService pkg=com.google.android.gms intent=act=com.google.android.gms.icing.INDEX_RECURRING_MAINTENANCE cmp=com.google.android.gms/.icing.service.IndexWorkerService flags=0x0}}}}",
					  "type=0 when=+12h37m25s279ms repeatInterval=86400000 count=0",
					  "operation=PendingIntent{43d96c90: PendingIntentRecord{43cf51c8 startService Key{startService pkg=com.google.android.gms intent=act=com.google.android.gms.icing.INDEX_RECURRING_MAINTENANCE cmp=com.google.android.gms/.icing.service.IndexWorkerService flags=0x0}}}",
					  "RTC_WAKEUP #2: Alarm{43d89668 type 0 PendingIntent{43ddcfb0: PendingIntentRecord{43d89248 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/.security.snet.SnetService flags=0x0}}}}",
					  "type=0 when=+12h33m13s813ms repeatInterval=84279688 count=16734",
					  "operation=PendingIntent{43ddcfb0: PendingIntentRecord{43d89248 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/.security.snet.SnetService flags=0x0}}}",
					  "RTC_WAKEUP #1: Alarm{43dfff28 type 0 PendingIntent{43dd65d0: PendingIntentRecord{43d11528 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.checkin.EventLogService$Receiver flags=0x0}}}}",
					  "type=0 when=+22m43s848ms repeatInterval=1800000 count=0",
					  "operation=PendingIntent{43dd65d0: PendingIntentRecord{43d11528 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.checkin.EventLogService$Receiver flags=0x0}}}",
					  "RTC_WAKEUP #0: Alarm{4295ab68 type 0 PendingIntent{42a1a560: PendingIntentRecord{42a1a4c0 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.app.backup.intent.RUN flg=0x40000000 flags=0x0}}}}",
					  "type=0 when=+12m48s334ms repeatInterval=3840944 count=0",
					  " operation=PendingIntent{42a1a560: PendingIntentRecord{42a1a4c0 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.app.backup.intent.RUN flg=0x40000000 flags=0x0}}}",
					  "RTC #4: Alarm{43d48fa8 type 1 PendingIntent{43d48d70: PendingIntentRecord{43f64e78 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.playlog.service.MonitorAlarmReceiver flags=0x0}}}}",
					   " type=1 when=+23h13m20s924ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d48d70: PendingIntentRecord{43f64e78 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.playlog.service.MonitorAlarmReceiver flags=0x0}}}",
					  "RTC #3: Alarm{43debb10 type 1 PendingIntent{43da5438: PendingIntentRecord{43da5398 broadcastIntent Key{broadcastIntent pkg=com.groupon intent=cmp=com.groupon/.receiver.DailySyncer flags=0x0}}}}",
					    "type=1 when=+17h57m2s538ms repeatInterval=86400000 count=0",
					    "operation=PendingIntent{43da5438: PendingIntentRecord{43da5398 broadcastIntent Key{broadcastIntent pkg=com.groupon intent=cmp=com.groupon/.receiver.DailySyncer flags=0x0}}}",
					  "RTC #2: Alarm{439cea50 type 1 PendingIntent{429ff7d0: PendingIntentRecord{43d24cc0 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/.tracking.mobile.internal.LogClientService flags=0x0}}}}",
					   " type=1 when=+17h27m2s538ms repeatInterval=86400000 count=0",
					    "operation=PendingIntent{429ff7d0: PendingIntentRecord{43d24cc0 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/.tracking.mobile.internal.LogClientService flags=0x0}}}",
					  "RTC #1: Alarm{4296b968 type 1 PendingIntent{4279a8c8: PendingIntentRecord{42969518 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.intent.action.DATE_CHANGED flg=0x20000000 flags=0x0}}}}",
					   " type=1 when=+12h3m35s133ms repeatInterval=0 count=0",
					    "operation=PendingIntent{4279a8c8: PendingIntentRecord{42969518 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.intent.action.DATE_CHANGED flg=0x20000000 flags=0x0}}}",
					  "RTC #0: Alarm{43cf2f68 type 1 PendingIntent{43f7a2d0: PendingIntentRecord{43dc5be0 startService Key{startService pkg=com.sirius intent=act=ACTION_SYNC_NOW cmp=com.sirius/com.qp.sxm.exposed.FavoritesShowsSyncService flags=0x0}}}}",
					   " type=1 when=+3m35s133ms repeatInterval=900000 count=1",
					    "operation=PendingIntent{43f7a2d0: PendingIntentRecord{43dc5be0 startService Key{startService pkg=com.sirius intent=act=ACTION_SYNC_NOW cmp=com.sirius/com.qp.sxm.exposed.FavoritesShowsSyncService flags=0x0}}}",
					 
					  "Elapsed realtime wakeup (now=+47m57s495ms):",
					  "ELAPSED_WAKEUP #23: Alarm{43f37b08 type 2 PendingIntent{43ebef58: PendingIntentRecord{43e5ff20 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_LOCATOR flags=0x0}}}}",
					   " type=2 when=+Ĩ55d2h54m29s362ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43ebef58: PendingIntentRecord{43e5ff20 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_LOCATOR flags=0x0}}}",
					  "ELAPSED_WAKEUP #22: Alarm{43e97938 type 2 PendingIntent{42775a48: PendingIntentRecord{43dfb7b0 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.server.GoogleLocationService flags=0x0}}}}",
					   " type=2 when=+21d3h16m9s464ms repeatInterval=0 count=0",
					    "operation=PendingIntent{42775a48: PendingIntentRecord{43dfb7b0 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.server.GoogleLocationService flags=0x0}}}",
					  "ELAPSED_WAKEUP #21: Alarm{42948950 type 2 PendingIntent{42b52820: PendingIntentRecord{434c0118 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}}",
					   " type=2 when=+21d3h16m9s458ms repeatInterval=0 count=0",
					    "operation=PendingIntent{42b52820: PendingIntentRecord{434c0118 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}",
					  "ELAPSED_WAKEUP #20: Alarm{43cd78e8 type 2 PendingIntent{43d3f908: PendingIntentRecord{43e5ffc0 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}}",
					   " type=2 when=+21d3h16m9s454ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d3f908: PendingIntentRecord{43e5ffc0 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}",
					  "ELAPSED_WAKEUP #19: Alarm{43cfad38 type 2 PendingIntent{43d38370: PendingIntentRecord{43db85f8 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}}",
					   " type=2 when=+21d3h16m9s278ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d38370: PendingIntentRecord{43db85f8 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}",
					  "ELAPSED_WAKEUP #18: Alarm{42ab8cd8 type 2 PendingIntent{426d7a90: PendingIntentRecord{43eb3840 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}}",
					   " type=2 when=+21d3h16m9s57ms repeatInterval=0 count=0",
					    "operation=PendingIntent{426d7a90: PendingIntentRecord{43eb3840 startService Key{startService pkg=com.google.android.gms intent=cmp=com.google.android.gms/com.google.android.location.internal.GoogleLocationManagerService flags=0x0}}}",
					  "ELAPSED_WAKEUP #17: Alarm{43e661c0 type 2 PendingIntent{43e60b78: PendingIntentRecord{43e64990 broadcastIntent Key{broadcastIntent pkg=com.pinterest intent=cmp=com.pinterest/.receiver.GlobalDataUpdateReceiver flags=0x0}}}}",
					   " type=2 when=+23h42m2s505ms repeatInterval=86400000 count=1",
					    "operation=PendingIntent{43e60b78: PendingIntentRecord{43e64990 broadcastIntent Key{broadcastIntent pkg=com.pinterest intent=cmp=com.pinterest/.receiver.GlobalDataUpdateReceiver flags=0x0}}}",
					  "ELAPSED_WAKEUP #16: Alarm{43cf3e00 type 2 PendingIntent{43e6bab8: PendingIntentRecord{43ccf7d8 broadcastIntent Key{broadcastIntent pkg=com.google.android.location intent=act=com.google.android.location.nlp.ALARM_WAKEUP_LOCATOR flags=0x0}}}}",
					   " type=2 when=+23h39m47s449ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43e6bab8: PendingIntentRecord{43ccf7d8 broadcastIntent Key{broadcastIntent pkg=com.google.android.location intent=act=com.google.android.location.nlp.ALARM_WAKEUP_LOCATOR flags=0x0}}}",
					  "ELAPSED_WAKEUP #15: Alarm{43d815e8 type 2 PendingIntent{43d81348: PendingIntentRecord{43d812a8 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.appwidget.action.APPWIDGET_UPDATE cmp=com.sec.android.app.snotebook/com.infraware.widget.SNoteWidgetProvider flags=0x0}}}}",
					   " type=2 when=+23h27m2s505ms repeatInterval=86400000 count=0",
					    "operation=PendingIntent{43d81348: PendingIntentRecord{43d812a8 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.appwidget.action.APPWIDGET_UPDATE cmp=com.sec.android.app.snotebook/com.infraware.widget.SNoteWidgetProvider flags=0x0}}}",
					  "ELAPSED_WAKEUP #14: Alarm{43ec64c8 type 2 PendingIntent{43e6cc30: PendingIntentRecord{43e68100 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_IN_OUT_DOOR_COLLECTOR flags=0x0}}}}",
					   " type=2 when=+18h20m48s805ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43e6cc30: PendingIntentRecord{43e68100 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_IN_OUT_DOOR_COLLECTOR flags=0x0}}}",
					  "ELAPSED_WAKEUP #13: Alarm{43eca170 type 2 PendingIntent{43e6f240: PendingIntentRecord{43e64490 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_CALIBRATION_COLLECTOR flags=0x0}}}}",
					   " type=2 when=+15h3m35s100ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43e6f240: PendingIntentRecord{43e64490 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_CALIBRATION_COLLECTOR flags=0x0}}}",
					  "ELAPSED_WAKEUP #12: Alarm{43d63d60 type 2 PendingIntent{43d20f78: PendingIntentRecord{43def250 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_CACHE_UPDATER flags=0x0}}}}",
					   " type=2 when=+3h59m52s89ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d20f78: PendingIntentRecord{43def250 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_CACHE_UPDATER flags=0x0}}}",
					  "ELAPSED_WAKEUP #11: Alarm{43f52780 type 2 PendingIntent{43f52440: PendingIntentRecord{43e641e8 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_CACHE_UPDATER flags=0x0}}}}",
					   " type=2 when=+3h35m33s916ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43f52440: PendingIntentRecord{43e641e8 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_CACHE_UPDATER flags=0x0}}}",
					  "ELAPSED_WAKEUP #10: Alarm{43d5b850 type 2 PendingIntent{43d5b570: PendingIntentRecord{428be668 broadcastIntent Key{broadcastIntent pkg=com.google.android.location intent=act=com.google.android.location.nlp.ALARM_WAKEUP_CACHE_UPDATER flags=0x0}}}}",
					   " type=2 when=+2h6m18s211ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d5b570: PendingIntentRecord{428be668 broadcastIntent Key{broadcastIntent pkg=com.google.android.location intent=act=com.google.android.location.nlp.ALARM_WAKEUP_CACHE_UPDATER flags=0x0}}}",
					  "ELAPSED_WAKEUP #9: Alarm{44009e40 type 2 PendingIntent{43e882c8: PendingIntentRecord{43e66888 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flags=0x0}}}}",
					   " type=2 when=+1h41m22s268ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43e882c8: PendingIntentRecord{43e66888 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flags=0x0}}}",
					  "ELAPSED_WAKEUP #8: Alarm{43e20a58 type 2 PendingIntent{43f0ff08: PendingIntentRecord{43d38d60 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flags=0x0}}}}",
					   " type=2 when=+1h40m42s603ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43f0ff08: PendingIntentRecord{43d38d60 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flags=0x0}}}",
					  "ELAPSED_WAKEUP #7: Alarm{43ecd740 type 2 PendingIntent{428efae8: PendingIntentRecord{42906860 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.content.syncmanager.SYNC_ALARM flags=0x0}}}}",
					   " type=2 when=+1h12m46s617ms repeatInterval=0 count=0",
					    "operation=PendingIntent{428efae8: PendingIntentRecord{42906860 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.content.syncmanager.SYNC_ALARM flags=0x0}}}",
					  "ELAPSED_WAKEUP #6: Alarm{43f41ec0 type 2 PendingIntent{429dcce8: PendingIntentRecord{43cdef08 startService Key{startService pkg=com.google.android.apps.maps intent=cmp=com.google.android.apps.maps/com.google.googlenav.prefetch.android.PrefetcherService flags=0x0}}}}",
					   " type=2 when=+1h12m2s505ms repeatInterval=3600000 count=0",
					    "operation=PendingIntent{429dcce8: PendingIntentRecord{43cdef08 startService Key{startService pkg=com.google.android.apps.maps intent=cmp=com.google.android.apps.maps/com.google.googlenav.prefetch.android.PrefetcherService flags=0x0}}}",
					  "ELAPSED_WAKEUP #5: Alarm{429fa980 type 2 PendingIntent{42aaaa48: PendingIntentRecord{43d07de8 broadcastIntent Key{broadcastIntent pkg=com.google.android.gsf intent=act=com.google.android.intent.action.MCS_HEARTBEAT flags=0x0}}}}",
					   " type=2 when=+27m36s244ms repeatInterval=0 count=0",
					    "operation=PendingIntent{42aaaa48: PendingIntentRecord{43d07de8 broadcastIntent Key{broadcastIntent pkg=com.google.android.gsf intent=act=com.google.android.intent.action.MCS_HEARTBEAT flags=0x0}}}",
					  "ELAPSED_WAKEUP #4: Alarm{43d86380 type 2 PendingIntent{43d861e0: PendingIntentRecord{43d86140 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.appwidget.action.APPWIDGET_UPDATE cmp=com.sec.android.widgetapp.favoriteswidget/.SeniorFavoriteWidgetProviderSmall flags=0x0}}}}",
					   " type=2 when=+27m2s505ms repeatInterval=1800000 count=1",
					    "operation=PendingIntent{43d861e0: PendingIntentRecord{43d86140 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.appwidget.action.APPWIDGET_UPDATE cmp=com.sec.android.widgetapp.favoriteswidget/.SeniorFavoriteWidgetProviderSmall flags=0x0}}}",
					  "ELAPSED_WAKEUP #3: Alarm{43ddbe88 type 2 PendingIntent{43ddbce8: PendingIntentRecord{43ddbb68 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/com.littlefluffytoys.littlefluffylocationlibrary.LocationBroadcastService flags=0x0}}}}",
					   " type=2 when=+12m2s505ms repeatInterval=900000 count=1",
					    "operation=PendingIntent{43ddbce8: PendingIntentRecord{43ddbb68 startService Key{startService pkg=com.groupon intent=cmp=com.groupon/com.littlefluffytoys.littlefluffylocationlibrary.LocationBroadcastService flags=0x0}}}",
					  "ELAPSED_WAKEUP #2: Alarm{43ecb868 type 2 PendingIntent{43e70a08: PendingIntentRecord{43e65700 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_S_COLLECTOR flags=0x0}}}}",
					   " type=2 when=+8m13s669ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43e70a08: PendingIntentRecord{43e65700 broadcastIntent Key{broadcastIntent pkg=com.google.android.apps.maps intent=act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_S_COLLECTOR flags=0x0}}}",
					  "ELAPSED_WAKEUP #1: Alarm{43ebeaf0 type 2 PendingIntent{43eb56b8: PendingIntentRecord{43cf8530 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_ACTIVITY_DETECTION flags=0x0}}}}",
					   " type=2 when=+1m42s238ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43eb56b8: PendingIntentRecord{43cf8530 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_ACTIVITY_DETECTION flags=0x0}}}",
					  "ELAPSED_WAKEUP #0: Alarm{42962c20 type 2 PendingIntent{43d544f8: PendingIntentRecord{43d1d690 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_LOCATOR flags=0x0}}}}",
					   " type=2 when=+43s569ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d544f8: PendingIntentRecord{43d1d690 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=act=com.google.android.gms.nlp.ALARM_WAKEUP_LOCATOR flags=0x0}}}",
					  "ELAPSED #8: Alarm{43d6fe40 type 3 PendingIntent{428bde50: PendingIntentRecord{428bdd50 broadcastIntent Key{broadcastIntent pkg=android intent=act=com.android.server.NetworkTimeUpdateService.action.POLL flags=0x0}}}}",
					   " type=3 when=+23h13m40s278ms repeatInterval=0 count=0",
					    "operation=PendingIntent{428bde50: PendingIntentRecord{428bdd50 broadcastIntent Key{broadcastIntent pkg=android intent=act=com.android.server.NetworkTimeUpdateService.action.POLL flags=0x0}}}",
					  "ELAPSED #7: Alarm{42aa7fc8 type 3 PendingIntent{426aa460: PendingIntentRecord{43e1e388 startService Key{startService pkg=com.sec.android.app.sysscope intent=act=com.sec.intent.action.SYSSCOPE flags=0x0}}}}",
					   " type=3 when=+23h13m9s672ms repeatInterval=86400000 count=1",
					    "operation=PendingIntent{426aa460: PendingIntentRecord{43e1e388 startService Key{startService pkg=com.sec.android.app.sysscope intent=act=com.sec.intent.action.SYSSCOPE flags=0x0}}}",
					  "ELAPSED #6: Alarm{43d1f678 type 3 PendingIntent{43d63ba0: PendingIntentRecord{43de92f8 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.common.download.DownloadAlarmReceiver flags=0x0}}}}",
					   " type=3 when=+11h13m21s485ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43d63ba0: PendingIntentRecord{43de92f8 broadcastIntent Key{broadcastIntent pkg=com.google.android.gms intent=cmp=com.google.android.gms/.common.download.DownloadAlarmReceiver flags=0x0}}}",
					  "ELAPSED #5: Alarm{439ca260 type 3 PendingIntent{439d5b58: PendingIntentRecord{439cc708 broadcastIntent Key{broadcastIntent pkg=com.android.phone intent=act=com.android.phone.UPDATE_CALLER_INFO_CACHE cmp=com.android.phone/.CallerInfoCacheUpdateReceiver flags=0x0}}}}",
					   " type=3 when=+7h27m2s505ms repeatInterval=28800000 count=0",
					    "operation=PendingIntent{439d5b58: PendingIntentRecord{439cc708 broadcastIntent Key{broadcastIntent pkg=com.android.phone intent=act=com.android.phone.UPDATE_CALLER_INFO_CACHE cmp=com.android.phone/.CallerInfoCacheUpdateReceiver flags=0x0}}}",
					  "ELAPSED #4: Alarm{429dfe48 type 3 PendingIntent{4295d5b8: PendingIntentRecord{429af098 broadcastIntent Key{broadcastIntent pkg=android intent=act=com.android.server.action.NETWORK_STATS_POLL flags=0x0}}}}",
					   " type=3 when=+27m2s505ms repeatInterval=1800000 count=1",
					    "operation=PendingIntent{4295d5b8: PendingIntentRecord{429af098 broadcastIntent Key{broadcastIntent pkg=android intent=act=com.android.server.action.NETWORK_STATS_POLL flags=0x0}}}",
					  "ELAPSED #3: Alarm{43cd5f18 type 3 PendingIntent{43d80fa0: PendingIntentRecord{439ce8b0 broadcastIntent Key{broadcastIntent pkg=com.tgrape.android.radar intent=act=com.tgrape.android.radar.alarm.background flags=0x0}}}}",
					   " type=3 when=+12m58s152ms repeatInterval=3600000 count=1",
					    "operation=PendingIntent{43d80fa0: PendingIntentRecord{439ce8b0 broadcastIntent Key{broadcastIntent pkg=com.tgrape.android.radar intent=act=com.tgrape.android.radar.alarm.background flags=0x0}}}",
					  "ELAPSED #2: Alarm{428298a0 type 3 PendingIntent{428d7858: PendingIntentRecord{428a2ba0 broadcastIntent Key{broadcastIntent pkg=android intent=act=com.android.server.ThrottleManager.action.POLL flags=0x0}}}}",
					   " type=3 when=+4m10s383ms repeatInterval=0 count=0",
					    "operation=PendingIntent{428d7858: PendingIntentRecord{428a2ba0 broadcastIntent Key{broadcastIntent pkg=android intent=act=com.android.server.ThrottleManager.action.POLL flags=0x0}}}",
					  "ELAPSED #1: Alarm{43e22f40 type 3 PendingIntent{43da7080: PendingIntentRecord{43dc1c70 broadcastIntent Key{broadcastIntent pkg=com.android.phone intent=act=com.android.internal.telephony.gprs-data-stall flags=0x0}}}}",
					   " type=3 when=+49s409ms repeatInterval=0 count=0",
					    "operation=PendingIntent{43da7080: PendingIntentRecord{43dc1c70 broadcastIntent Key{broadcastIntent pkg=com.android.phone intent=act=com.android.internal.telephony.gprs-data-stall flags=0x0}}}",
					  "ELAPSED #0: Alarm{43ced858 type 3 PendingIntent{4279ad08: PendingIntentRecord{429692a0 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.intent.action.TIME_TICK flg=0x40000000 flags=0x0}}}}",
					   " type=3 when=+35s100ms repeatInterval=0 count=0",
					    "operation=PendingIntent{4279ad08: PendingIntentRecord{429692a0 broadcastIntent Key{broadcastIntent pkg=android intent=act=android.intent.action.TIME_TICK flg=0x40000000 flags=0x0}}}",
				"  Broadcast ref count: 0"
				," ",
				"  Alarm Stats:",
				  "com.google.android.gsf",
				   " 23ms running, 1 wakeups",
				    "1 alarms: ","act=com.google.android.intent.action.MCS_HEARTBEAT flg=0x14",
				  "android",
				    " 18837ms running, 1 wakeups",
				    "47 alarms: ","act=android.intent.action.TIME_TICK flg=0x40000014",
				    "2 alarms: ","act=com.android.server.action.NETWORK_STATS_POLL flg=0x14",
				    "4 alarms: ","act=com.android.server.ThrottleManager.action.POLL flg=0x14",
				    "1 alarms: ","act=com.android.server.NetworkTimeUpdateService.action.POLL flg=0x14",
				    "1 alarms: ","act=android.appwidget.action.APPWIDGET_UPDATE flg=0x14",
				  "com.android.vending",
				    " 741ms running, 2 wakeups",
				    "1 alarms: ","flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.DailyHygiene",
				    "1 alarms: ","flg=0x4 cmp=com.android.vending/com.google.android.finsky.services.ContentSyncService",
				  "com.tgrape.android.radar",
				    " 21413ms ","running, 0 wakeups",
				   "1 alarms: ","act=com.tgrape.android.radar.alarm.background flg=0x14",
				  "com.android.providers.calendar",
				    " 17377ms running, 1 wakeups",
				    "1 alarms: ","act=com.android.providers.calendar.intent.CalendarProvider2 flg=0x14",
				  "com.google.android.apps.maps",
				    " 25651ms running, 5 wakeups",
				    "1 alarms: ","flg=0x4 cmp=com.google.android.apps.maps/com.google.googlenav.prefetch.android.PrefetcherService",
				    "2 alarms: ","act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_SENSOR_COLLECTION_POLICY flg=0x14",
				    "2 alarms: ","act=com.google.android.apps.maps.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flg=0x14",
				 "com.sec.android.app.sysscope",
				    " 1ms running, 0 wakeups",
				   "1 alarms: ","act=com.sec.intent.action.SYSSCOPE flg=0x4",
				  "com.google.android.gms",
				    " 1881ms running, 59 wakeups",
				   "4 alarms: flg=0x14",
				    "7 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_ACTIVITY_DETECTION flg=0x14",
				    "1 alarms: ","act=com.google.android.intent.action.GCM_RECONNECT flg=0x14",
				    "45 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_LOCATOR flg=0x14",
				    "3 alarms: ","act=com.google.android.gms.nlp.ALARM_WAKEUP_SENSOR_UPLOADER flg=0x14",
				    "1 alarms: ","flg=0x4 cmp=com.google.android.gms/.security.snet.SnetService",
				  "com.android.phone",
				    " 2972ms running, 0 wakeups",
				    "47 alarms: ","act=com.android.internal.telephony.gprs-data-stall flg=0x14",
				  "com.pinterest",
				    " 539ms running, 2 wakeups",
				    "2 alarms: ","flg=0x14",
					"com.android.phone"
					,"6231ms running, 0 wakeups"
					,"78 alarms: ","act=com.android.internal.telephony.gprs-data-stall flg=0x14"

		}; 
	}
	
}//end
