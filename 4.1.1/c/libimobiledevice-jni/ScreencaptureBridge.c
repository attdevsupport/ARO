#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <errno.h>
#include <time.h>

#include "ScreencaptureBridge.h"
#include "ScreencaptureBridgeHelper.h"

#include <libimobiledevice/libimobiledevice.h>
#include <libimobiledevice/lockdown.h>
#include <libimobiledevice/screenshotr.h>

idevice_t device = NULL;
lockdownd_client_t lckd = NULL;
screenshotr_client_t shotr = NULL;
lockdownd_service_descriptor_t service = NULL;
int result = -1;
int i;
const char *udid = NULL;



/*
 * Class:     com_att_aro_libimobiledevice_Screencapture
 * Method:    startService
 * Signature: ()V
 */
JNIEXPORT jstring JNICALL Java_com_att_aro_libimobiledevice_ScreencaptureImpl_startService
  (JNIEnv * env, jobject obj){
	const char * str;// = "SUCCESS";

	if (IDEVICE_E_SUCCESS != idevice_new(&device, udid)) {
		str = "No device found, is it plugged in?\n";
		return outPut(env, str);
	}
	if(!device){
		str = "No device mounted";
		return outPut(env, str);
	}
	if (LOCKDOWN_E_SUCCESS != lockdownd_client_new_with_handshake(device, &lckd, NULL)) {
		idevice_free(device);
		str = "Failed to acquire lock service.";
		return outPut(env, str);
	}

	lockdownd_start_service(lckd, "com.apple.mobile.screenshotr", &service);
	lockdownd_client_free(lckd);

	if (service && service->port > 0) {
		if (screenshotr_client_new(device, service, &shotr) != SCREENSHOTR_E_SUCCESS) {
			str = "Could not connect to screenshotr service!";
		}else{
			str = "SUCCESS";
		}
	}else{
		str = "Could not start screenshotr service! Try running Instruments tool from XCode on this device and see if it work, then try this again.";
	}
	return outPut(env, str);
}
jstring outPut(JNIEnv * env, const char * str){
	jstring jString = (*env)->NewStringUTF( env, str );
	return jString;
}

/*
 * Class:     com_att_aro_libimobiledevice_Screencapture
 * Method:    captureScreen
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_com_att_aro_libimobiledevice_ScreencaptureImpl_captureScreen
  (JNIEnv * env, jobject obj){

	char *imgdata = NULL;
	uint64_t imgsize = 0;
	if (screenshotr_take_screenshot(shotr, &imgdata, &imgsize) != SCREENSHOTR_E_SUCCESS) {

		//printf("Could not get screenshot!\n");
		return NULL;
	}

	jbyteArray dataArr = (*env)->NewByteArray(env,imgsize);

	//void SetByteArrayRegion(JNIEnv *env, jbyteArray array, jsize start, jsize len, jbyte *buf);
	(*env)->SetByteArrayRegion(env, dataArr, 0, imgsize, (jbyte*)imgdata);
	free(imgdata);
	return dataArr;
}

/*
 * Class:     com_att_aro_libimobiledevice_Screencapture
 * Method:    stopService
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_att_aro_libimobiledevice_ScreencaptureImpl_stopService
  (JNIEnv * env, jobject obj){

	if(shotr){
		screenshotr_client_free(shotr);
	}
	if (service){
		lockdownd_service_descriptor_free(service);
	}
	if(device){
		idevice_free(device);
	}

}
