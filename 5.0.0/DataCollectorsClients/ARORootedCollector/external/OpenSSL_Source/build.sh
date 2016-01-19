#########################################################
#INSTRUCTIONS:
#	1. use cygwin and navigate to the directory containing this build file
#	2. execute: ./build.sh
#	Note: if error, check that NDK_HOME system var is set to point to the local ndk folder
#########################################################

buildVersions=(4.0.4 4.1.1)

#loop through and build
for version in ${buildVersions[@]}
do
	buildDir=openSSL/$version*_mod
	echo building $buildDir
	cd $buildDir
	pwd
	ndk-build clean
	ndk-build

	#copy .so files into assets/$version
	source=libs/armeabi*/*.so
	dest=../../../assets/$version
	mkdir -p $dest
	echo copying $source to $dest
	cp -f $source $dest
	
	#go back to the /external dir (for subsequent runs)
	cd ../../
done