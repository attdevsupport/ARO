#include<jni.h>
#include<pcap.h>
#include <errno.h>

#include "com_att_aro_pcap_PCapAdapter.h"

struct user_data {
	JNIEnv *env;
	int datalink;
	jobject obj;
	jmethodID handler;
};

void jpcap_handler(u_char *, const struct pcap_pkthdr *, const u_char *);
int readPcap(pcap_t *p, int cnt, pcap_handler callback, u_char *user);

/*
 JNI method used to ping the pcap library by doing a device search.  This will
 verify that winpcap is installed on Windows.
 */
JNIEXPORT void JNICALL Java_com_att_aro_pcap_PCapAdapter_ping
(JNIEnv * env, jclass cls)
{
	/* Retrieve the device list */
	pcap_if_t *alldevs;
	char errbuf[PCAP_ERRBUF_SIZE];
	pcap_findalldevs(&alldevs, errbuf);

	/* Free the device list */
	pcap_freealldevs(alldevs);
}

/*
 Loop Packets
 */
JNIEXPORT jstring JNICALL Java_com_att_aro_pcap_PCapAdapter_loopPacket(JNIEnv *env, jobject obj, jstring filename) {
	char *file;
	pcap_t *pcds;
	char errbuf[PCAP_ERRBUF_SIZE];
	struct user_data user;
	jclass cls;
	jmethodID mid;

	// Open specified pcap file
	file = (char *) (*env)->GetStringUTFChars(env, filename, 0);
	pcds = pcap_open_offline(file, errbuf);
	(*env)->ReleaseStringUTFChars(env, filename, file);
	if (pcds == NULL){
		return (*env)->NewStringUTF(env, errbuf);
	}


	cls = (*env)->GetObjectClass(env, obj);
	mid = (*env)->GetMethodID(env, cls, "pcapHandler", "(IJJI[B)V");
	if (mid == NULL) {
		return (*env)->NewStringUTF(env, "Callback method not found");
	}

	// Populate user data structure passed to jpcap_handler
	user.datalink = pcap_datalink(pcds);
	user.env = env;
	user.obj = obj;
	user.handler = mid;

	// Run pcap loop
//	int p_loop_result = pcap_loop(pcds, -1, jpcap_handler, (u_char *) (&user));
	int p_loop_result = readPcap(pcds, -1, jpcap_handler, (u_char *) (&user));

	// Clean up
	pcap_close(pcds);
	return NULL;
}

/*
 * read entire pcap file until end of file is reached
 * ignores unreadable packets such as Null/Loopback
 */
int readPcap(pcap_t *pcds, int cnt, pcap_handler callback, u_char *user) {

	struct pcap_pkthdr *pcap_packet_header; // The header that pcap gives us
	const u_char *pkt_data;
	int result = 0;

	while ((cnt == -1)||(cnt-- > 0)) {
		errno = 0;
		result = pcap_next_ex(pcds, &pcap_packet_header, &pkt_data);
		if (result != -2) { // test for end of file
			if (result != -1) { // skip unreadable packets
				jpcap_handler(user, pcap_packet_header, pkt_data);
			}
		} else {
			cnt = 0;
		}
	}
	return 1;
}

/*
 * report the packet back to Java caller
 */
void jpcap_handler(u_char *args, const struct pcap_pkthdr *header, const u_char *data) {

	jbyteArray dataArray;

	struct user_data *user = (struct user_data *) args;
	JNIEnv *env = user->env;

	dataArray = (*env)->NewByteArray(env, (jsize) header->caplen);
	(*env)->SetByteArrayRegion(env, dataArray, 0, (jsize) header->caplen, data);
	(*env)->CallVoidMethod(env, user->obj, user->handler, (jint) user->datalink, (jlong) header->ts.tv_sec, (jlong) header->ts.tv_usec, (jint) header->len, dataArray);
	(*env)->DeleteLocalRef(env, dataArray);

}

