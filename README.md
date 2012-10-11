
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
ARO Data Collector 1.5.0  
ARO Data Analyzer  1.5.0

**New Features:**  
In this release, the ARO Data Collector can record when network bearers are switched without interrupting the capture of trace data. A new Waterfall View that displays connection data spread over time has been added to the ARO Data Analyzer, a Data Dump option now allows you to export all of the analytical data from a group of traces into one .csv file, and generic profiles have been added for 3G, LTE, and Wi-Fi. Also, the ARO Data Analyzer now displays a Trace Score which provides a comparative evaluation of how an application has handled the common causes of inefficiency, and mitigated the effects.

For Open Source ARO developers, new classes have been added to provide an easy way to add custom Best Practices to the ARO Data Analyzer and to add the ARO Data Collector to an existing application. 


**Known Issues:**

+  All known issues from previous releases have been fixed in this release.
+  When creating a new Android Virtual Device (AVD) during the configuration of the Android Emulator for use with the ARO Data Collector, selecting a CPU/ABI type of “Intel Atom (x86)” will cause an “ARO Data Collector failed to start” error.
+  On certain devices with a hardware limitation, the ARO Data Collector is unable read the screen buffer and cannot collect a video file for the trace. In this case, it displays the error message "Video failed to start".
+  To analyze a trace file in the ARO Data Analyzer that was collected using Microsoft Network Monitor, you must have Microsoft Network Monitor installed on the same machine.
+  On the Mac OS, the Data Dump feature in the ARO Data Analyzer that is new for version 1.5, will only work for one level of trace folders. Nested trace folders will not be displayed in the exported .csv file.
+  On the Mac or Windows OS, the Data Dump feature in the ARO Data Analyzer will throw an exception if any of the trace folders included in the export is corrupted. The exception can be closed and the export operation will continue, however, the csv file that is created will only include data from the trace folders that were read before the corrupted file was encountered and will not include data from the corrupted file.
+  In the Trace Score section of the Statistics tab of the ARO Data Analyzer, the score for Average Rate should be "(out of 62.5)" points, and the score for Energy Efficiency should be "(out of 187.5)" points.


##Documentation:
ARO Compilation and Build Guide.docx - Describes how to compile and build the ARO components.  
ARO Data Collector API Reference - To read this reference, open the file **index.html** in the folder ARODataCollector\docs\api  
ARO Data Analyzer API Reference - To read this reference, open the file **index.html** in the folder ARODataAnalyzer\docs\api


##Contents:
The ARO Open Source code package contains the following:


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
+  **res** - Resources for an Android project.  
+  **src** - Source code for the ARO Data Collector.
+  *ARODataCollector_V1.5.0.1.apk* - Android application package (APK) file for the ARO Data Collector.  
+  *AndroidManifest.xml* - Manifest file for an Android project.


**ARO Compilation and Build Guide.docx** - Describes how to compile and build the ARO components.

**README.md** - This file.


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
