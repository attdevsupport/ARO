package com.att.aro.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.att.aro.console.printstreamutils.ImHereThread;
import com.att.aro.console.printstreamutils.NullOut;
import com.att.aro.console.printstreamutils.OutSave;
import com.att.aro.console.util.UtilOut;
import com.att.aro.core.AROConfig;
import com.att.aro.core.IAROService;
import com.att.aro.core.bestpractice.pojo.BestPracticeType;
import com.att.aro.core.datacollector.IDataCollector;
import com.att.aro.core.datacollector.IDataCollectorManager;
import com.att.aro.core.datacollector.pojo.StatusResult;
import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.pojo.AROTraceData;
import com.att.aro.core.pojo.ErrorCode;
import com.beust.jcommander.JCommander;


public final class Application {
//	private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);
	private static final Logger LOGGER = Logger.getLogger(Application.class);

	private static UtilOut utilOut;


	private Application() {
	}

	/**
	 * @param args - see Help
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		Commands cmds = new Commands();
		try {
			new JCommander(cmds, args).setProgramName("aro");
		} catch (Exception ex) {
			System.err.print("Error parsing command: " + ex.getMessage());
			System.exit(1);
		}
		utilOut = cmds.isVerbose() ? new UtilOut() : new UtilOut(UtilOut.MessageThreshold.Normal);

		OutSave outSave = prepareSystemOut();
		try {
			LOGGER.debug("ARO Console app start");
		}
		finally {
			restoreSystemOut(outSave);
		}

		//no command was entered? then print usage
		if (!cmds.isListcollector() && (cmds.isHelp() || (cmds.getAnalyze() == null && cmds.getStartcollector() == null))) {
			usageHelp();
			System.exit(1);
		}
		ApplicationContext context = new AnnotationConfigApplicationContext(AROConfig.class);
		if (cmds.isListcollector()) {
			showCollector(context, cmds);
			System.exit(1);
		}
		//validate command entered
		ErrorCode error = new Validator().validate(cmds, context);
		if (error != null) {
			printError(error);
			System.exit(1);
		}

		if (cmds.getStartcollector() != null) {
			runDataCollector(context, cmds);
		} else if (cmds.getAnalyze() != null) {
			runAnalyzer(context, cmds);
		} 
		
		outSave = prepareSystemOut();
		try {
			LOGGER.debug("Console app ended");
		}
		finally {
			restoreSystemOut(outSave);
		}
	}

	/**
	 * Locates and displays any and all data collectors. Data collectors are jar
	 * files that allow controlling and collecting data on devices such as
	 * Android phone, tablets and emulators.
	 * 
	 * @param context - Spring ApplicationContext
	 * @param cmds - Not used
	 */
	static void showCollector(ApplicationContext context, Commands cmds) {
		IDataCollectorManager colmg = context.getBean(IDataCollectorManager.class);
		List<IDataCollector> list = colmg.getAvailableCollectors(context);
		if (list == null || list.size() < 1) {
			err("No data collector found");
		} else {
			for (IDataCollector coll : list) {
				out("-" + coll.getName() + " version: " + coll.getMajorVersion() + "." + coll.getMinorVersion());
			}
		}
	}

	private static OutSave prepareSystemOut() {
		OutSave outSave = new OutSave(System.out, Logger.getRootLogger().getLevel());
		if (utilOut.getThreshold().ordinal() < UtilOut.MessageThreshold.Verbose.ordinal()) {
			Logger.getRootLogger().setLevel(Level.WARN);
			System.setOut(new PrintStream(new NullOut()));
		}
		return outSave;
	}

	private static void restoreSystemOut(OutSave outSave) {
		System.setOut(outSave.getOut());
		Logger.getRootLogger().setLevel(outSave.getLevel());
	}

	/**
	 * Analyze a trace and produce a report either in json or html<br>
	 * <pre>
	 * Required command:
	 *   --analyze with path to trace directory of traffic.cap
	 *   --output output file, error if missing
	 *   --format html or json, if missing defaults to json
	 * 
	 * @param context - Spring ApplicationContext
	 * @param cmds - user commands
	 */
	static void runAnalyzer(ApplicationContext context, Commands cmds) {
		
		String trace = cmds.getAnalyze();
		IAROService serv = context.getBean(IAROService.class);
		AROTraceData results = null;

		// analyze trace file or directory?
		OutSave outSave = prepareSystemOut();
		ImHereThread imHereThread = new ImHereThread(outSave.getOut(), Logger.getRootLogger());
		try {
			if (serv.isFile(trace)) {
				try {
					results = serv.analyzeFile(getBestPractice(), trace);
				} catch (IOException e) {
					err("Error occured analyzing trace, detail: " + e.getMessage());
					System.exit(1);
				}
			} else {
				try {
					results = serv.analyzeDirectory(getBestPractice(), trace);
				} catch (IOException e) {
					err("Error occured analyzing trace directory, detail: " + e.getMessage());
					System.exit(1);
				}
			}

			if (results.isSuccess()) {
				outSave = prepareSystemOut();
				if (cmds.getFormat().equals("json")) {
					if (serv.getJSonReport(cmds.getOutput(), results)) {
						outln("Successfully produced JSON report: " + cmds.getOutput());
					} else {
						errln("Failed to produce JSON report.");
					}
				} else {
					if (serv.getHtmlReport(cmds.getOutput(), results)) {
						outln("Successfully produced HTML report: " + cmds.getOutput());
					} else {
						errln("Failed to produce HTML report.");
					}
				}
			} else {
				printError(results.getError());
			}
		}
		finally {
			imHereThread.endIndicator();
			while (imHereThread.isRunning()) {
				Thread.yield();
			}
			restoreSystemOut(outSave);
		}
		System.exit(1);
	}

	static void printError(ErrorCode error) {
		err("Error code: " + error.getCode());
		err("Error name: " + error.getName());
		err("Error description: " + error.getDescription());
	}

	/**
	 * Launches a DataCollection. Provides an input prompt for the user to stop the collection by typing "stop"
	 * <pre>Note:
	 * Do not exit collection by pressing a ctrl-c
	 * Doing so will exit ARO.Console but will not stop the trace on the device.
	 * </pre>
	 * 
	 * @param context
	 * @param cmds
	 */
	static void runDataCollector(ApplicationContext context, Commands cmds) {
		IDataCollectorManager colmg = context.getBean(IDataCollectorManager.class);
		colmg.getAvailableCollectors(context);
		IDataCollector collector = null;
		if ("rooted_android".equals(cmds.getStartcollector())) {
			collector = colmg.getRootedDataCollector();
		} else if ("vpn_android".equals(cmds.getStartcollector())) {
			collector = colmg.getNorootedDataCollector();
		} else if ("ios".equals(cmds.getStartcollector())) {
			collector = colmg.getIOSCollector();
		}
		if (collector == null) {
			printError(ErrorCodeRegistry.getCollectorNotfound());
			System.exit(1);
		}
		String password = cmds.getSudo();
		StatusResult result = null;

		OutSave outSave = prepareSystemOut();
		try {
			if (cmds.getDeviceid() != null) {
				result = collector.startCollector(true, cmds.getOutput(), cmds.getVideo().equals("yes"), false, cmds.getDeviceid(), null, password);                                 
			} else {
				result = collector.startCollector(true, cmds.getOutput(), cmds.getVideo().equals("yes"), password);
			}
		}
		finally {
			restoreSystemOut(outSave);
		}

		if (result.getError() != null) {
			printError(result.getError());
		} else {

			outSave = prepareSystemOut();
			try {
				String input = "";
				do {
					out("Data collector is running, enter stop to save trace and quit program");
					out(">");
					input = readInput();
				} while (!input.contains("stop"));
			}
			finally {
				restoreSystemOut(outSave);
			}

			out("stopping collector...");
			try {
				collector.stopCollector();
			}
			finally {
				restoreSystemOut(outSave);
			}
			out("collector stopped, trace saved to: " + cmds.getOutput());

			cleanUp(context);
			out("ARO exited");
			System.exit(1);
		}
	}

	/**
	 * Provides for user input
	 * @return user input
	 */
	static String readInput() {
		BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
		try {
			return bufferRead.readLine();
		} catch (IOException e) {
			return "";
		}
	}

	/**
	 * print string to console with new line char
	 * 
	 * @param str - output text
	 */
	static void out(String str) {
		utilOut.outMessage(str, UtilOut.MessageThreshold.Normal);
	}
	static void outln(String str) {
		utilOut.outMessageln(str, UtilOut.MessageThreshold.Normal);
	}
	static void err(String str) {
		utilOut.errMessage(str);
	}
	static void errln(String str) {
		utilOut.errMessageln(str);
	}

	/**
	 * return a list of best practice we want to run.
	 * the sequence is according to the Analyzer
	 * @return a list of best practice
	 */
	private static List<BestPracticeType> getBestPractice() {
		List<BestPracticeType> req = new ArrayList<BestPracticeType>();
		req.add(BestPracticeType.FILE_COMPRESSION);
		req.add(BestPracticeType.DUPLICATE_CONTENT);
		req.add(BestPracticeType.USING_CACHE);
		req.add(BestPracticeType.CACHE_CONTROL);
		req.add(BestPracticeType.COMBINE_CS_JSS);
		req.add(BestPracticeType.IMAGE_SIZE);
		req.add(BestPracticeType.MINIFICATION);
		req.add(BestPracticeType.SPRITEIMAGE);
		req.add(BestPracticeType.CONNECTION_OPENING);
		req.add(BestPracticeType.UNNECESSARY_CONNECTIONS);
		req.add(BestPracticeType.PERIODIC_TRANSFER);
		req.add(BestPracticeType.SCREEN_ROTATION);
		req.add(BestPracticeType.CONNECTION_CLOSING);
		req.add(BestPracticeType.HTTP_4XX_5XX);
		req.add(BestPracticeType.HTTP_3XX_CODE);
		req.add(BestPracticeType.SCRIPTS_URL);
		req.add(BestPracticeType.ASYNC_CHECK);
		req.add(BestPracticeType.HTTP_1_0_USAGE);
		req.add(BestPracticeType.FILE_ORDER);
		req.add(BestPracticeType.EMPTY_URL);
		req.add(BestPracticeType.FLASH);
		req.add(BestPracticeType.DISPLAY_NONE_IN_CSS);
		req.add(BestPracticeType.ACCESSING_PERIPHERALS);
		return req;
	}

	/**
	 * Displays user help
	 */
	private static void usageHelp() {
		StringBuilder sbuilder = new StringBuilder(1000);
		sbuilder.append("Usage: aro [commands] [arguments]")
				.append("\n  --analyze [trace location]: analyze a trace folder or file.")
				.append("\n  --startcollector [rooted_android|vpn_android|ios]: run a collector.")
				.append("\n  --deviceid [device id]: optional device id of Android or Serial Number for IOS. Default: first device connected.")
				.append("\n  --format [json|html]: optional type of report to generate. Default: json.")
				.append("\n  --video [yes|no]: optional command to record video when running collector. Default: no.")
				.append("\n  --listcollector: optional command to list available data collector.")
				.append("\n  --verbose:  optional command to enables detailed messages for '--analyze' and '--startcollector'")
				.append("\n  --help,-h,-?: show help menu.")
				.append("\n\nUsage example: ")
				.append("\n=============")
				.append("\nRun Android collector to capture trace with video: ")
				.append("\n  --startcollector rooted_android --output /User/documents/test --video yes")
				.append("\nRun iOS collector to capture trace with video: ")
				.append("\n  --startcollector ios --output /User/documents/tracefolder --video yes --sudo password")
				.append("\nAnalyze trace and produce HTML report")
				.append("\n  --analyze /User/documents/test --output /User/documents/report.html --format html")
				.append("\nAnalyze trace and produce JSON report:")
				.append("\n  --analyze /User/documents/test/traffic.cap --output /User/documents/report.json");
		outln(sbuilder.toString());
	}

	private static void cleanUp(ApplicationContext context){
		String dir = "";
		File filepath = new File(UtilOut.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		dir = filepath.getParent();
		IFileManager filemanager = context.getBean(IFileManager.class);		
		filemanager.deleteFile(dir + System.getProperty("file.separator") +  "AROCollector.apk");
		filemanager.deleteFile(dir + System.getProperty("file.separator") +  "ARODataCollector.apk");
	}
}