# creates {project}/libs/{app_abi}/tcpdump
# after compile move it to {project}/raw/tcpdump
# remember to ndk-build the jni/Application.mk 

APP_ABI := armeabi
APP_PIE := false
APP_BUILD_SCRIPT := Android.mk
APP_PLATFORM := android-7