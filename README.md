
#Application Resource Optimizer (ARO)

#NOTE: Application Resource Optimizer (ARO) code is now rebranded and lives on as [Video Optimizer] (https://developer.att.com/application-resource-optimizer/support/faqs#rebranding-aro-to-video-optimizer); the latest open source version of Video Optimizer is now [available here for download] (https://github.com/attdevsupport/VideoOptimzer).

All works distributed in this package are covered by the Apache 2.0 License unless otherwise stated.

> Copyright 2016 AT&T Intellectual Property

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
ARO Analyzer 5.0.0  

**Note:**
Before you start ARO 5.0.0, make sure you meet the following system requirements for your operating system.

    - At least 1GB of RAM
    - Java 7 or above (should have the latest update of Java 1.7 or above)
    - At least 1GB of RAMFor Android developers, Android SDK Platform-tools version 23.0.1 or later
    - For those using the ARO Collector on iOS, use xcode 7 or above
    - WinPcap

**What's new in Release 5.0.0?**    

+  **New ARO 5.0.0 containing APIs, ARO Command Line Interface (CLI) and Examples**  

	- 	ARO APIs: To facilitate the integration of ARO with developer tools, automated testing environments and Enterprise build environments, we've exposed APIs for collecting and analyzing traces.
	- 	ARO CLI: The ARO Command Line Interface provides access to the primary functions of AT&T ARO through console commands, allowing you to integrate ARO data collection and analysis into your testing and build tools.

  
+  **ARO Rooted Collector and VPN Collector Support for Android 5.0 through 6.0**  
The ARO 5.0.0 collector supports data collection from devices running Android 5.0 through Android 6.0  
  

+  **ARO iOS Collector Support for iOS 9**  
The ARO iOS collector supports data collection from devices running iOS 9.

+  **ARO is compatible with MAC 10.11 and Windows 10**  

+  **Updates to ARO Best Practices**  
The File Compression test has been enhanced so that more types of files are tested for compression, and the number of tests has been streamlined by removing the Content Pre-fetching and Inefficient Connections' Offloading to Wi-Fi when Possible tests.

**What are the known issues in Release 5.0.0?** 
+  Intermittent issue - When using ARO Data Collector apk, if you notice collection was successful, but the trace folder is empty, then the trace folder can be retrieved manually by using the following ADB shell command (trace name is the name of the trace folder name): adb pull /sdcard/ARO/tracename
+  When collecting a trace using the VPN collector, if the user manually quits the VPN on the device, ARO Analyzer will not receive the trace back to the local computer. User will only get a trace with only video file in the trace folder. To avoid this issue, do not stop the VPN collector on the device manually and instead stop the trace by using the stop collector from ARO Analyzer Menu.

##Contents:
The ARO Open Source code package contains the following:

##5.0.0

 **ARO.Core** - ARO main project

 **ARO.Console** - ARO user commend line interface project

 **ARO.UI** - ARO GUI interface project

 **ARO.Parent** - ARO Project for ordering all dependency projects 

 **DataCollectors** ARO plug in project for the bridge of core and devices

##4.1.1
**ARODataAnalyzer** - Main folder for the ARO Data Analyzer open source code.  
+  **bin** - Contains the Jpcap dlls.  
+  **c** - Contains C code used to build the Jpcap dlls in the bin folder.  
+  **docs** - API reference for the ARO Data Analyzer. (Open docs\api\index.html)  
+  **lib** - .jar files for ARO and its dependencies (ddmlib, jcommon, jfreechart, and jmf).  
+  **src** - Source code for the ARO Data Analyzer.  
+  *build.xml* - Used to build the .jar file for the ARO Data Analyzer.  


##ClientCollectors
**ARODataCollector** - Main folder for the ARO Data Analyzer open source code.  
+  **docs** - API reference for the ARO Data Collector (Open docs\api\index.html)  
+  **external** - Source code for external dependencies: tcpdump and libpcap.  
+  **libs** - .jar file for FlurryAgent. 
+  **res** - Resources for an Android project.  
+  **src** - Source code for the ARO Data Collector.  
+  *AndroidManifest.xml* - Manifest file for an Android project.

**ARODataCollector_OpenSource_v3.1.1.10.apk.zip** - A zip package containing a compiled open source version of the ARO Data Collector.  

##Documentation
**ARO Compilation and Build Guide.pdf** - Describes how to compile and build the ARO 4.1.1 components.  
**Adding Custom Best Practices in Open Source ARO.pdf** - Describes how to add custom Best Practices to the ARO Data Analyzer.  

**README.md** - This file.


##Running the pre-built Open Source ARO 5.0.0
To launch the pre-built version of ARO Analyzer that is included in this open source download, do the following:

**On Mac/Windows:**  
1. Download the zip file for this repository, and un-zip the files to the desired location. (e.g: Desktop)  
2. Confirm that there are four folders in the unzipped files: switch to folder "5.0.0" and inside the folder there are five folders inside.   
3. Open terminal and change directory to the ARO.Parent folder in the ARO location. (e.g: cd /Users/ARO-master/5.0.0/ARO.Parent)  
4. Run maven pom file , ARO.Parent project will help to build all of the ARO module projects orderly.

Note: please check the ARO.Parent pom file module project location, change the modules path to the desired location if needed.

**Build project Independently**
Users can build the single project with each project pom file.
+  **ARO.Core** - Running ARO.Core pom file with maven tool, use "mvn install" to publish ARO.Core library  to your local repository.
+  **DataCollectors** - Each of the collectors is spring plug in, we suggest running "mvn install" phase to publish to the user local repository for ARO.Console and ARO.UI dependency 
+  **ARO.Console** - After run the pom file with maven tool, users need to switch to the lib folder and run jar file. We have script under main/resources folder(aro/aro.bat) leverage the comment line interface.
+  **ARO.UI** - After running the pom file with maven tool, users need to switch to the lib folder. Open the terminal and execution AROUI.jar file.

  
##Running the pre-built Open Source ARO Analyzer 4.1.1:  
  
To launch the pre-built version of ARO Analyzer that is included in this open source download, do the following:

**On Mac:**  
1. Download the zip file for this repository, and un-zip the files to the desired location. (e.g: Desktop)  
2. Confirm that there is one main folder in the unzipped files: ARO, and two sub-folders inside ARO: bin and lib.  
3. Open terminal and change directory to the bin folder in the ARO location. (e.g: cd Desktop/ARO/bin)  
4. Inside the bin directory, type: ./aro  
  

**On Windows:**  
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
