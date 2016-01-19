package com.att.aro.core.peripheral.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.fileio.impl.FileManagerImpl;
import com.att.aro.core.peripheral.pojo.VideoTime;

public class VideoTimeReaderImplTest extends BaseTest {

	@InjectMocks
	VideoTimeReaderImpl videoTimeReaderImpl;

	@InjectMocks
	private FileManagerImpl filereader;
	private String traceFolder = "traceFolder";

	@Before
	public void setup() {
		MockitoAnnotations.initMocks(this);
		videoTimeReaderImpl.setFileReader(filereader);
		tracePath = folder.getRoot().toString();
		
	}

	@After
	public void destroy() {
		folder.delete();
	}
	
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private File exVideoDisplayFileName;
	private File nativeVideoFileOnDevice;
	private File nativeVideoDisplayfile;
	private File VIDEO_TIME_FILE;
	private File EXVIDEO_TIME_FILE;
	private String tracePath;

	/**
	 * create & populate file
	 * 
	 * @param fileNameStr
	 * @param string_data to populate file
	 * @return filename
	 */
	private File makeFile(String fileNameStr, String[] strings) {
		File fileName = null;
		BufferedWriter out;
		try {
			fileName = folder.newFile(fileNameStr);
			if (strings != null) {
				out = new BufferedWriter(new FileWriter(fileName));
				for (String dataLine : strings) {
					out.write(dataLine);
				}
				out.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileName;
	}


	/*
	 * no exVideo_time to find
	 */
	@Test
	public void readData0() throws IOException {
		

		Date traceDateTime = new Date((long) 1414092264446.0);
		VideoTime videoTime = null;

		nativeVideoDisplayfile = makeFile("exvideo.mov", null);
		videoTime = videoTimeReaderImpl.readData(tracePath, traceDateTime);
		assertEquals(true, videoTime.isExVideoTimeFileNotFound());
		nativeVideoDisplayfile.delete();
		nativeVideoDisplayfile = null;
	}

	/*
	 * run test via Mockito with full data "File", has start and stop time
	 * mock: "video_time"
	 */
	@Test
	public void readData1() throws IOException {
		

		Date traceDateTime = new Date((long) 1414092264446.0);
		VideoTime videoTime = null;

		VIDEO_TIME_FILE = makeFile("video_time", new String[] { "1.41409226371E9 1.414092261198E9" });
		videoTime = videoTimeReaderImpl.readData(tracePath, traceDateTime);
		assertEquals(1.4140922669580002E9, videoTime.getVideoStartTime(), 0);
		assertEquals(true, videoTime.isNativeVideo());
		assertEquals(false, videoTime.isExVideoFound());
		assertEquals(false, videoTime.isExVideoTimeFileNotFound());
		VIDEO_TIME_FILE.delete();
		VIDEO_TIME_FILE = null;

	}

	/*
	 * run test via Mockito with full data "File", has start and stop time
	 * mock: "video.mov" "video_time"
	 */
	@Test
	public void readData2() throws IOException {
		

		Date traceDateTime = new Date((long) 1414092264446.0);
		VideoTime videoTime = null;

		nativeVideoDisplayfile = makeFile("video.mov", null);
		VIDEO_TIME_FILE = makeFile("video_time", new String[] { "1.41409226371E9 1.414092261198E9" });
		videoTime = videoTimeReaderImpl.readData(tracePath, traceDateTime);
		assertEquals(1.4140922669580002E9, videoTime.getVideoStartTime(), 0);
		assertEquals(true, videoTime.isNativeVideo());
		assertEquals(false, videoTime.isExVideoFound());
		assertEquals(false, videoTime.isExVideoTimeFileNotFound());
		nativeVideoDisplayfile.delete();
		nativeVideoDisplayfile = null;
		VIDEO_TIME_FILE.delete();
		VIDEO_TIME_FILE = null;

	}
	
	/*
	 * run test via Mockito with full data "File", has start and stop time
	 * mock: "exvideo.mov" "exVideo_time"
	 */
	@Test
	public void readData3() throws IOException {
		

		Date traceDateTime = new Date((long) 1414092264446.0);
		VideoTime videoTime = null;

		
		exVideoDisplayFileName = makeFile("exvideo.mov", null);
		EXVIDEO_TIME_FILE = makeFile("exVideo_time", new String[] { "1.41409226371E9 1.414092261198E9" });
		videoTime = videoTimeReaderImpl.readData(tracePath, traceDateTime);
		assertEquals(1.4140922669580002E9, videoTime.getVideoStartTime(), 0);
		assertEquals(false, videoTime.isNativeVideo());
		assertEquals(true, videoTime.isExVideoFound());
		assertEquals(false, videoTime.isExVideoTimeFileNotFound());
		boolean r = videoTimeReaderImpl.isExternalVideoSourceFilePresent("video.mp4", "video.mov", true, tracePath);
		exVideoDisplayFileName.delete();
		exVideoDisplayFileName = null;
		EXVIDEO_TIME_FILE.delete();
		EXVIDEO_TIME_FILE = null;


	}
	
	/*
	 * testing an embedded protected function
	 * boolean isExternalVideoSourceFilePresent(String nativeVideoFileOnDevice, String nativeVideoDisplayfile,boolean isPcap, String traceDirectory)
	 */
	@Test
	public void readData4() throws IOException {
		

		Date traceDateTime = new Date((long) 1414092264446.0);
		VideoTime videoTime = null;

		makeFile("video.dv", null);
		makeFile("video.qt", null);
		makeFile("video.mev", null);
		makeFile("video.m4", null);
		makeFile("video_time", new String[] { "1.41409226371E9 1.414092261198E9" });

		boolean r = videoTimeReaderImpl.isExternalVideoSourceFilePresent("video.mp4", "video.mov", true, tracePath);
		assertTrue("should not have found \"video.mov\" or \"video.mp4\"", !r);

		makeFile("video.mov", null);
		r = videoTimeReaderImpl.isExternalVideoSourceFilePresent("video.mp4", "video.mov", true, tracePath);
		assertTrue("should have found \"video.mov\"", r);

	}
}
