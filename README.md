
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
Forum:  http://developerboards.att.lithium.com/t5/AT-T-Application-Resource/bd-p/ARO  
Blog:  http://developerboards.att.lithium.com/t5/AT-T-Developer-Program-Blogs/bg-p/Blogs  
FAQ:  http://developer.att.com/developer/forward.jsp?passedItemId=10100053  
Contact Us:  http://developer.att.com/developer/contact_us.jsp


**Version:**  
ARO Data Collector 3.0 
ARO Data Analyzer  3.0


**What’s new in Release 3.0?**  

+  **New options added to the Diagnostic Chart in the ARO Analyzer**  
	+  The ARO Analyzer Diagnostic Chart now gives you the option of plotting the wake and lock states of the device and any alarms generated by the application.  


**What are the known issues in Release 3.0**  
 
+  **ARO Data Collector feature:**  Running the My Radar app while collecting a trace with the ARO Data Collector, causes the error "AT&T ARO Data Collector trace Collection stopped" to be thrown.  
  
+  **Video Sync/Video Correlation feature:**  This feature allows you to sync externally captured video to PCAP data by placing it in the trace folder. When using this feature in ARO 2.3, note the following:  
	+  Please ensure that the video duration is at least as long as the trace duration.  
	+  Avoid clicking in the Diagnostics Chart when the video is playing; the timing of response may be affected.  
	+  Certain 64 bit Windows 7 computers may have unexpected/random video sync issues. If this occurs, Re-sync the trace to the video.  
	+  Do not use a High Definition (HD) video file with the Video Sync feature.  
	+  Avoid disconnecting the USB cable during the collection of a trace, it may cause unexpected behavior.  
	+  On a Mac computer, the Trace Summary notification window may appear distorted. This will be fixed in a future release.  

+  **USB Video Feature:**  In some cases, the following notification may appear incorrectly when the trace is completed: "Unexpected error accessing device SD card. ADB Connection Error" When this occurs, the trace is intact and the error message can be disregarded.  

+  **Recording video in a trace:**  Some devices may not record video during a trace due to hardware limitations. For these devices, use the USB Video feature to record a video for the trace.  

+  **Loading a trace:**  While loading a trace in the ARO Analyzer, an Out of Memory (OOM) notification (application heap size issue) error may occur, or the message: “ARO Analyzer has reached the maximum memory heap size. Please close the ARO Analyzer and try again.” may appear. This can also occur when the same trace is re-loaded.  



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
**ARODataCollector_OpenSource_v3.0.0.2.apk** - Compiled open source version of the ARO Data Collector.  
**Adding Custom Best Practices in Open Source ARO.pdf** - Describes how to add custom Best Practices to the ARO Data Analyzer.  
**README.md** - This file.


  
##Running the pre-built Open Source ARO Analyzer:  
  
To launch the pre-built version of ARO Analyzer that is included in this open source download, do the following:

**On Mac:**  
  
1. Download the zip file for this repository, and un-zip the files to the desired location. (e.g: Desktop)  
2. Confirm that there is one main folder in the unzipped files: ARO, and two sub-folders inside ARO: bin and lib.  
3. Open terminal and change directory to the bin folder in the ARO location. (e.g: cd Desktop/ARO/bin)  
4. Inside the bin directory, type: ./aro  
  

**On windows:**  
 
1. Download the zip file for this repository, and un-zip the files to the desired location. (e.g: Desktop)  
2. Confirm that there is one main folder in the unzipped files: ARO, and two sub-folders inside ARO: bin and lib.  
3. Open a command prompt (cmd) and change directory to the bin folder in the ARO location. (e.g: cd Desktop/ARO/bin)
4. Inside the bin directory, type: aro  

 

##Compiling and Building ARO
The following sections describe the basic steps for compiling and building the ARO Data Collector and ARO Data Analyzer. For more detailed information, see the ARO Compilation and Build Guide.


###ARO Data Collector
This section describes how to setup the development environment, and then compile and build the ARO Data Collector into an Android Application Package (APK) using the Android SDK and Apache Ant. The APK file can then be installed on a device via the Android Debug Bridge (ADB).

Note: In order for the ARO Data Collector to be fully functional, it must be installed on a rooted device. 


**Setup the Development Environment**

1. Download and setup the Android SDK. Go to the Android Developers website http://developer.android.com/sdk/index.html, download the Android package for your platform, and follow the steps to setup the Android SDK.

2. Add additional components. Follow the installation instructions at http://developer.android.com/sdk/installing.html, especially the step: Adding Platforms and Other Components.

3. Download Apache ANT. Go to http://ant.apache.org and follow the download instructions.


**Compile and Build the ARO Data Collector**

1. Checkout the ARO Data Collector source code from the ARODataCollector folder to a local folder.

2. Update the project.properties to match your local configuration and generate a build.xml by using the following command syntax:  

		android update project --name <project_name> --target <target_ID> --path <path_to_your_project>

	Note: ARO Data Collector supports Android 2.1 and above, so the target id should be set to a value greater than 2.1. In the following example, it is set to 7.

		android update project --name ARODataCollector --target 7 --path ../ARODataCollector

3. Build the ARO Data Collector project for debug using the command "ant debug", or for release using the command "ant release".

4. Transfer the ARO Data Collector APK file to a rooted device using the ADB, with the command:  "adb aro"



###ARO Data Analyzer
To build the ARO Data Analyzer, follow these steps to setup the development environment for your OS and compile and build the code.

Note: A build of the ARO Data Analyzer application (aro.jar) is included in the ARODataAnalyzer\lib directory. 

**Windows**  
The system requirements for compiling, building, and running the ARO Data Analyzer on the Windows OS are:

+  A computer running Windows XP, Windows Vista, or Windows Seven.

+  At least 1GB of RAM.

+  Java Runtime Environment (JRE) version 1.6 or greater is required to run the ARO Data Analyzer. To compile and build the ARO Data Analyzer in the same environment, install the Java Development Kit (JDK) version 1.6 or greater which includes the JRE.

+  WinPcap, the "industry-standard windows packet capture library".

+  ANT, An Apache Java build tool.

Note: If needed, configure the JAVA_HOME system variable so that it points to your Java installation directory. This can be done in the Advanced tab of System Properties, by editing the Environment Variables.


**Mac**  
The system requirements for compiling, building, and running the ARO Data Analyzer on the Mac OS are:

+  A computer running Mac OS X 10.6 and above.

+  At least 1GB of RAM.

+  Java Runtime Environment (JRE) version 1.6 or greater is required to run the ARO Data Analyzer. To compile and build the ARO Data Analyzer in the same environment, install the Java Development Kit (JDK) version 1.6 or greater which includes the JRE.

+  ANT, An Apache Java build tool.


**Compile and Build the ARO Data Analyzer**  
To compile and build the open source ARO Data Analyzer code for Java, do the following:

Checkout the ARO Data Analyzer source code including the build.xml file, from the ARODataAnalyzer folder to a local folder, and use ANT to compile and build the application. 

+  The build.xml file builds the .jar file that contains the compiled classes for the ARO Data Analyzer.

+  The files aro.bat and aro are included in the ARODataAnalyzer\lib directory of the ARO Open Source Package to help you run the .jar file with default run settings:

+  To launch the .jar file in the Windows OS, run aro.bat from the command line.

+  To launch the .jar file in the Mac OS, run aro.  



------------------------------------------------------------------------------  


<img src="http://www.sillarsfamily.com/ARO.gif" alt="ARO Image" />
