#Application Resource Optimizer (ARO)


All works distributed in this package are covered by the Apache 2.0 License unless otherwise stated.

> Copyright 2012 AT&T Intellectual Property

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at 

> http://www.apache.org/licenses/LICENSE-2.0

> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.


AT&T Application Resource Optimizer contains the following open source libraries or binaries within its distribution package.  For more information on any item listed below, please contact developer.program@att.com.


**JFreeChart**  
> The AT&T Application Resource Optimizer(ARO) uses Open Source Software that is licensed under the GNU Lesser General Public License (LGPL) version 3 (the "License"), and you may not use this file except in compliance with the License. You may obtain a copy of the Licenses at: http://www.jfree.org/jfreechart/. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licenses for the specific language governing permissions and limitations under the Licenses.  

**JCommon**  
> The AT&T Application Resource Optimizer(ARO) uses Open Source Software that is licensed under the GNU Lesser General Public License (LGPL) version 2.1 (the "License"), and you may not use this file except in compliance with the License. You may obtain a copy of the Licenses at: http://www.jfree.org/jcommon/. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licenses for the specific language governing permissions and limitations under the Licenses.  

**FFmpeg**  
> The AT&T Application Resource Optimizer(ARO) uses Open Source Software that is licensed under the GNU Lesser General Public License (LGPL) version 2.1 (the "License"), and you may not use this file except in compliance with the License. You may obtain a copy of the Licenses at: http://ffmpeg.org/download.html. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licenses for the specific language governing permissions and limitations under the Licenses.  

**TCPDUMP/LIBPCAP**  
> The AT&T Application Resource Optimizer(ARO) uses Open Source Software that is licensed under the following BSD (the "License"), and you may not use this file except in compliance with the License. You may obtain a copy of the Licenses at: http://www.tcpdump.org/#contribute. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licenses for the specific language governing permissions and limitations under the Licenses. License: BSD Redistribution and use in source and binary forms, with or withoutmodification, are permitted provided that the following conditionsare met: 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/o> rials provided with the distribution. 3. The names of the authors may not be used to endorse or promote products derived from this software without specific prior written permission.  THIS SOFTWARE IS PROVIDED "AS IS" AND WITHOUT ANY EXPRESS ORIMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIEDWARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.  

**Android Open Source Project**   
> The AT&T Application Resource Optimizer(ARO) uses Open Source Software that is licensed under the Apache Software License 2.0  (the "License"), and you may not use this file except in compliance with the License. You may obtain a copy of the Licenses at: http://source.android.com/source/licenses.html. Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the Licenses for the specific language governing permissions and limitations under the Licenses.




##Open Source Code Package

The Application Resource Optimizer (ARO) is a diagnostic tool for analyzing mobile web application performance developed by AT&T. ARO allows you to automatically profile an application to optimize performance, make battery utilization more efficient, and reduce network impact.

The ARO Data Collector is the component of the ARO application that captures the data traffic of a mobile device and stores that information in trace files that can be analyzed using the ARO Data Analyzer.

The ARO Data Analyzer is the component of the ARO application that evaluates the trace data collected from an application and generates statistical and analytical results based on recommended best practices.

This Open Source Code Package contains all of the code needed to build both of these ARO components.


**ARO Web Resources:**  
Application Resource Optimizer (ARO):  http://developer.att.com/ARO  
ARO Support:  http://developer.att.com/ARO/support  
Forum:  http://developerboards.att.lithium.com/t5/AT-T-Application-Resource/bd-p/ARO  
Blog:  http://developerboards.att.lithium.com/t5/AT-T-Developer-Program-Blogs/bg-p/Blogs  
FAQ:  http://developer.att.com/ARO/FAQ  
Contact Us:  http://developer.att.com/developer/contact_us.jsp


**Version:**  
ARO Data Analyzer 3.2  
ARO Data Collector 3.1.1.7  



**What�s new in Release 3.2?  (8/13/14)**  

+  **ARO Analyzer Diagnostics Chart Enhancements**  
The following enhancements have been made to the ARO Analyzer, allowing you to select the set of data that you view on the Diagnostics Chart more precisely:  
	+  On the TCP/UDP Flows table, a check box has been added in the header of the table so that you can select/deselect all of the TCP/UDP flows.  
	+  Check boxes have been added to each row in the table so that you can select/deselect individual TCP/UDP flows.  
	+  A Refresh button has been added on the right side of the Diagnostics Chart so that the graph can be updated after TCP/UDP flows have been selected or deselected.  
	+  The Remote Port column has been expanded into two columns; Remote IP Endpoint and Remote Port Number.  
	+  A Byte Count column has been added that displays the total byte count for each TCP/UDP flow.  
	+  A Domain Name column has been added to the Select Applications/IPs dialog box that is accessed from the View menu.  
  

+  **Downloads Option Added to the Help Menu**  
You can now download the ARO Installer and the ARO Collector App for Android from developer.att.com/ARO, by just opening ARO and clicking on the Downloads option on the Help menu.  
  


**What are the known issues in Release 3.2?** 

+  **ARO Analyzer Diagnostics Chart:**   

	+  Alarm Triggers are not displayed in the Diagnostics chart of the ARO Analyzer for traces collected from the HTC One X.  
	+  The type of Burst may be reported differently, for the same testing scenario steps, on the Diagnostics Chart for traces captured from an Android device and an iPhone.  
	+  When a TCP/UDP flow is deselected and the Diagnostics Chart is refreshed more than three times, the deselected packet information displays on the chart and the burst length changes.  

+  **AT&T ARO Windows 8 Data Collector:**  The current version of the AT&T ARO Windows 8 Data Collector (version 2.2.1) does not support trace collection from Windows 8.1.  

+  **ARO Analyzer Best Practices "Resize images for Mobile":**  Due to limitations in the structure of pcap files, the results for the �Resize Images for Mobile� test will appear differently for ARO trace files and pcap files.  

+  **Set ADB Path Option:**  When using the Set ADB Path option on the File menu, the Cancel button does not work correctly, it saves the path name that is entered in the dialog.  

+  **�	iOS Traces:**  Some devices may not record video during a trace due to hardware limitations. For these devices, use the USB Video feature to record a video for the trace.  


**What are the Other known issues from previous releases?**
 
+  **Video Sync/Video Correlation feature:**  This feature allows you to sync externally captured video to PCAP data by placing it in the trace folder. When using this feature in ARO 2.3, note the following:  
	+  Please ensure that the video duration is at least as long as the trace duration.  
	+  Avoid clicking in the Diagnostics Chart when the video is playing; the timing of response may be affected.  
	+  Certain 64 bit Windows 7 computers may have unexpected/random video sync issues. If this occurs, Re-sync the trace to the video.  
	+  Do not use a High Definition (HD) video file with the Video Sync feature.  
	+  Avoid disconnecting the USB cable during the collection of a trace, it may cause unexpected behavior.  
	+  On a Mac computer, the Trace Summary notification window may appear distorted. This will be fixed in a future release.  

+  **USB Video Feature:**  In some cases, the following notification may appear incorrectly when the trace is completed: "Unexpected error accessing device SD card. ADB Connection Error" When this occurs, the trace is intact and the error message can be disregarded.  

+  **Recording video in a trace:**  Some devices may not record video during a trace due to hardware limitations. For these devices, use the USB Video feature to record a video for the trace.  

+  **Loading a trace:**  While loading a trace in the ARO Analyzer, an Out of Memory (OOM) notification (application heap size issue) error may occur, or the message: �ARO Analyzer has reached the maximum memory heap size. Close ARO Analyzer and try again or increase ARO Analyzer�s heap size. Also consider collecting multiple, smaller, more isolated traces.� may appear. This can also occur when the same trace is re-loaded.  
  
+  **AT&T ARO Data Collector support for iOS:**  The following issues can occur on the specified iPhone devices if they are disconnected from a Mac running OS X Mountain Lion 10.8 and OS X Mavericks 10.9 while a trace is being collected:  
	+  On the iPhone 5c, the ARO Data Analyzer may hang and display an empty pop-up, requiring the Analyzer to be closed.  
	+  On the iPhone 5 and iPhone 4s, the error message �No data Packet captured� may be displayed. The correct error message is �Device got disconnected�.  
	+  Traces collected from an iPhone connected to a Mac running OS X Mavericks 10.9 are not supported by the AT&T ARO Analyzer when it is running on a Windows OS. 



##Documentation:
ARO Compilation and Build Guide - Describes how to compile and build the ARO components.  
ARO Data Collector API Reference - To read this reference, open the file **index.html** in the folder ARODataCollector\docs\api  
ARO Data Analyzer API Reference - To read this reference, open the file **index.html** in the folder ARODataAnalyzer\docs\api  
Adding Custom Best Practices in Open Source ARO - Describes how to add custom Best Practices to the ARO Data Analyzer.  


##Contents:
The ARO Open Source code package contains the following:


**2013DevSummitTurbocharge** - Main folder for ARO sample applications that were introduced and discussed at the 2013 Developer Summit.
+  **com.example.android.multires.MultiRes.caching** - Contains a sample application that demonstrates caching.  
+  **com.example.android.multires.MultiRes.closing** - Contains a sample application that demonstrates the difference between properly and improperly closing connections.


**ARODataAnalyzer** - Main folder for the ARO Data Analyzer open source code.  
+  **bin** - Contains the Jpcap dlls.  
+  **c** - Contains C code used to build the Jpcap dlls in the bin folder.  
+  **docs** - API reference for the ARO Data Analyzer. (Open docs\api\index.html)  
+  **lib** - .jar files for ARO and its dependencies (ddmlib, jcommon, jfreechart, and jmf).  
+  **src** - Source code for the ARO Data Analyzer.  
+  *build.xml* - Used to build the .jar file for the ARO Data Analyzer.  

  
**ARODataCollector** - Main folder for the ARO Data Analyzer open source code.  
+  **docs** - API reference for the ARO Data Collector (Open docs\api\index.html)  
+  **external** - Source code for external dependencies: tcpdump and libpcap.  
+  **libs** - .jar file for FlurryAgent. 
+  **res** - Resources for an Android project.  
+  **src** - Source code for the ARO Data Collector.  
+  *AndroidManifest.xml* - Manifest file for an Android project.


**ARO Compilation and Build Guide.pdf** - Describes how to compile and build the ARO components.  
**ARODataCollector_OpenSource_v3.1.1.7.apk.zip** - A zip package containing a compiled open source version of the ARO Data Collector.  
**Adding Custom Best Practices in Open Source ARO.pdf** - Describes how to add custom Best Practices to the ARO Data Analyzer.  
**README.md** - This file.


###ARO Data Collector: Build + Install
ARO Data Collector is an Android application (.apk) that runs on your phone. To build the apk you will need [Android SDK](http://developer.android.com/sdk/index.html) and Apache ANT.
```
cd ARODataCollector
android update project --name ARODataCollector --target 7 --path ../ARODataCollector
adb install bin/ARODataCollector-debug.apk
```
***Note:*** Replace `--target 7` with an appropriate target from the list of targets `android list targets`

###ARO Analyzer: Run Pre-Compiled 
This repo includes a pre-built version of ARO Analyzer for Mac, Windows, or Linux (x64)
```
cd bin
./aro or aro.bat
```
###ARO Analyzer: Build + Run
Environment requirements:
+ 1GB or more of RAM.
+ JRE 1.6 and above to run
+ JDK 1.6 and above to compile
+ ANT, An Apache Java build tool
+ Mac OS X 10.6 and above OR Ubuntu 12.04 and above OR Windows XP, Windows Vista, or Windows Seven.
+ WinPcap if on windows

**Note:** If needed, configure the JAVA_HOME system variable so that it points to your Java installation directory. This can be done in the Advanced tab of System Properties, by editing the Environment Variables in windows.

```
cd ARODataAnalyzer
ant
./aro or aro.bat
```

------------------------------------------------------------------------------  
<img src="http://www.sillarsfamily.com/ARO.gif" alt="ARO Image" />
