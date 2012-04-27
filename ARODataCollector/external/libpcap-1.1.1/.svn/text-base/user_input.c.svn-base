#include "user_input.h"

//#define TEST_INPUT

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include <sys/time.h>
#include <sys/timeb.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/timeb.h>
#include <fcntl.h>
#include <linux/input.h>
#include <android/log.h>
//user input events and their string names
#define EVENT_SCREEN 	1
#define EVENT_VOLUP 	2
#define EVENT_VOLDOWN 3
#define EVENT_BALL		4
#define EVENT_POWER		5
#define EVENT_MENU		6
#define EVENT_HOME		7
#define EVENT_BACK		8
#define EVENT_SEARCH	9

#define ACTION_PRESS		1
#define ACTION_RELEASE 	2

#define DEVICE_NEXUSONE		1
#define DEVICE_CAPTIVATE 	2
#define DEVICE_INSPIRE 		3
#define DEVICE_ATRIX 		4
#define DEVICE_INFUSE		5
#define DEVICE_GENERIC		6  // emulator
#define DEVICE_LGTHRILL		7
#define DEVICE_HTCARIA		8
#define DEVICE_ATRIX2		9

const char * event_str[] = {
	"",
	"screen",
	"key volup",
	"key voldown",
	"key ball",
	"key power",
	"key menu",
	"key home",
	"key back",
	"key search"
};

const char * action_str[] = {
	"",
	"press",
	"release"
};

#define N_EVENTS 128
struct input_event events[N_EVENTS];

fd_set rfds;

int myDevice;
double userEventTime;
extern FILE * ofsEvents;
extern double pcapTime;
extern double userTime;
extern int exitFlag;

double GetTimestamp(struct timeval * pT) {
	//return pT->tv_sec + pT->tv_usec / (double)1000000.0f;
	struct timeb t;
	ftime(&t);	
	userEventTime =  t.time + t.millitm / (double) 1000.0f;
	return userEventTime;
}

///////////////////////////////////////////////
typedef struct {
	int arg1;
	int arg2;
	int arg3;
	int event;
	int action;
} KEY_ENTRY;

#define MAX_INPUT_FILE_ID 5
#define MAX_KEY_ENTRIES 10

KEY_ENTRY keyEntries[MAX_INPUT_FILE_ID+1][MAX_KEY_ENTRIES];
int nKeyEntries[MAX_INPUT_FILE_ID+1];

int inputEvents[MAX_INPUT_FILE_ID+1];
int maxEventPlusOne;
///////////////////////////////////////////////

void ReadDeviceKeyDB() {
	int device, input_file;
	KEY_ENTRY ke;	
	memset(nKeyEntries, 0, sizeof(nKeyEntries));
		
	FILE * ifs = fopen("/data/data/com.att.android.arodatacollector/key.db", "r");
	if (ifs == NULL) {
		printf("Cannot read file: key.db\n");
		exit(0);
	}
	
	while (1) {
		fscanf(ifs, "%d %d %d %d %d %d %d", &device, &input_file, &ke.arg1, &ke.arg2, &ke.arg3, &ke.event, &ke.action);
		
		if (device == -1) break;
		if (device != myDevice) continue;
			
		keyEntries[input_file][nKeyEntries[input_file]++] = ke;
	}
	fclose(ifs);
}

void GetMyDeviceType() {
	system("/system/bin/getprop > /sdcard/prop");
	FILE * ifs = fopen("/sdcard/prop", "r");
	if (ifs == NULL) {
		printf("Cannot determine device type\n");
		exit(0);
	}
	
	myDevice = -1;
	
	char buf[1024];
	while (!feof(ifs)) {
		if (fgets(buf, sizeof(buf), ifs) == NULL) break;
		if (strstr(buf, "[ro.product.device]") != NULL) {
			if (strstr(buf, "[passion]") != NULL) {
				myDevice = DEVICE_NEXUSONE;
				printf("Device type: Nexus One\n");
			} else if (strstr(buf, "[ace]") != NULL) {
				myDevice = DEVICE_INSPIRE;
				printf("Device type: HTC Inspire\n");
			} else if (strstr(buf, "[olympus]") != NULL) {
				myDevice = DEVICE_ATRIX;
				printf("Device type: Moto Atrix\n");
			} else if (strstr(buf, "[SGH-I897]") != NULL) {				
				myDevice = DEVICE_CAPTIVATE;
				printf("Device type: Samsung Captivate\n");
			} else if (strstr(buf, "[SGH-I997]") != NULL) {				
				myDevice = DEVICE_INFUSE;
				printf("Device type: SamSung Infuse\n");
			} else if (strstr(buf, "[liberty]") != NULL) {				
				myDevice = DEVICE_HTCARIA;
				printf("Device type: HTC Aria\n");
			} else if (strstr(buf, "[generic]") != NULL) {				
				myDevice = DEVICE_GENERIC;
				printf("Device type: Emulator\n");
			} else if (strstr(buf, "[p925]") != NULL) {	
				myDevice = DEVICE_LGTHRILL;
				printf("Device type: LG-p925\n");
			}else if (strstr(buf, "[edison]") != NULL) {	
				myDevice = DEVICE_ATRIX2;
				printf("Device type: Moto Atrix2\n");
			}  else {
				printf("Unknown device type (Device Not Mapped): %s\n", buf);
//				exit(0);
			}
		}
	}	
	
	fclose(ifs);
	
	if (myDevice == -1) {
		printf("Unknown device type: %s\n", buf);
		//exit(0);
	}		
}

int OpenInputDevice(int id) {	
	char deviceName[128];
	sprintf(deviceName, "/dev/input/event%d", id);
	printf("Opening input device %s... ", deviceName);
	
	int fd = open(deviceName, O_RDWR);
	if (fd == -1) {
		printf("Error: cannot open input device %s\n");
		//exit(0);
	}
	
	printf("fd = %d\n", fd);	
	return fd;
}

int CloseInputDevice(int id) {	
	if (inputEvents[id] != -1) close(inputEvents[id]);
}

//return 0 to stop
static int HandleEvents() {
	int i, j, k, m;
	struct timeval timeout; 
	timeout.tv_sec = 10;
	timeout.tv_usec = 0; 

	if (exitFlag == 4) { exitFlag = 3; return 0; }

	FD_ZERO(&rfds);	
	
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {
		if (inputEvents[i] > 0) {
			FD_SET(inputEvents[i], &rfds);
		}
	}
	int retval;
	retval = select(maxEventPlusOne, &rfds, NULL, 			NULL, &timeout);				

	if (retval == 0) {
		return 1;
	} else if (retval == -1) {
		printf("Error: Select() fails\n");		
		exit(0);
	}

	

	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {
		if (inputEvents[i] == -1) continue;
		if (!(FD_ISSET(inputEvents[i], &rfds))) continue;		
		int rd = read(inputEvents[i], events, sizeof(struct input_event) * N_EVENTS);
		int nEvents = rd / sizeof(struct input_event);		
		m = nKeyEntries[i];
		
		for (j=0; j<nEvents; j++) {
			int arg1 = events[j].type;
			int arg2 = events[j].code;
			int arg3 = events[j].value;
			double tm = GetTimestamp(&events[j].time);
			
			for (k=0; k<m; k++) {

				if (exitFlag == 4) { exitFlag = 3; return 0; }

				//an "-1" entry for arg3 means anything positive
				if (keyEntries[i][k].arg1 == arg1 && keyEntries[i][k].arg2 == arg2 &&
						(keyEntries[i][k].arg3 == arg3 || (keyEntries[i][k].arg3 == -1 && arg3>0))
				) {
					
					#ifdef TEST_INPUT
					printf("%.6lf %s %s\n", 
						tm, 
						event_str[keyEntries[i][k].event], 
						action_str[keyEntries[i][k].action]
					);
					printf("\n exitFlag = %d ",exitFlag);					
					#endif
					if (exitFlag == 4) { exitFlag = 3; return 0; }

					fprintf(ofsEvents, "%.6lf %s %s\n", 
						tm, 
						event_str[keyEntries[i][k].event], 
						action_str[keyEntries[i][k].action]
					);
										
					if (keyEntries[i][k].event == EVENT_VOLDOWN) {
						if (keyEntries[i][k].action == ACTION_RELEASE) {
							if (exitFlag == 3) return 0;
						} else {
							;
						}
					} else {
						//exitFlag = 0;
					}

				}
			}
		}
	}
		
	return 1;
}

void * CaptureUserInput(void * arg) { 
	int i;
	
	GetMyDeviceType();
	ReadDeviceKeyDB();
	
	printf("sizeof(struct input_event) = %d\n", sizeof(struct input_event));
	maxEventPlusOne = -1;
			
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {		
		if (nKeyEntries[i] == 0) {
			inputEvents[i] = -1;
		} else {
			inputEvents[i] = OpenInputDevice(i);
			if (inputEvents[i] > maxEventPlusOne) maxEventPlusOne = inputEvents[i];
		}
	}
	maxEventPlusOne++;	
	
	while (1) {
	if(!HandleEvents()){ exitFlag = 3; break; }
	if (exitFlag == 4) { exitFlag = 3; break; }
	}
	printf("Thread CaptureUserInput() exit.\n");
	__android_log_print(ANDROID_LOG_DEBUG, "Thread CaptureUserInput() exit", "EXIT");
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {
		CloseInputDevice(i);
	}
	return 0;
}

/*
#Device type: nexus one=1, captivate=2, inspire=3, atrix=4, Infuse = 5, HTC Aria =6
#Event Screen=1 volume up=2 volume down=3 ball=4 power=5 menu=6 home=7 back=8 search=9
#Action press=1 release=2
#Device	Input_File Arg1	Arg2 Arg3	Event Action
1		2			1	330		1	1 		1
1	    2	        1	330	    0	1       2
1		4			1	115		1	2 		1
1		4			1	115		0	2 		2
1		4	1	114	1	3 1
1		4	1	114	0	3 2
1		4	1	116	1	5 1
1		4	1	116	0	5 2
1		5	1	272	1	4 1
1		5	1	272	0	4 2


2	3	3	48	40	1 1
2	3	3	48	0	1 2
2	1	1	42	1	2 1
2	1	1	42	0	2 2
2	1	1	58	1	3 1
2	1	1	58	0	3 2
2	1	1	26	1	5 1
2	1	1	26	0	5 2
2	2	1	158	1	6 1
2	2	1	158	0	6 2
2	2	1	139	1	7 1
2	2	1	139	0	7 2
2	2	1	28	1	8 1
2	2	1	28	0	8 2
2	2	1	107	1	9 1
2	2	1	107	0	9 2
3	3	3	48 -1	1 1
3	3	3	48	0	1 2
3	4	1	115	1	2 1
3	4	1	115	0	2 2
3	4	1	114	1	3 1
3	4	1	114	0	3 2
3	4	1	116	1	5 1
3	4	1	116	0	5 2
4	3	3	48	1	1 1
4	3	3	48	0	1 2
4	1	1	115	1	2 1
4	1	1	115	0	2 2
4	1	1	114	1	3 1
4	1	1	114	0	3 2
4	0	1	107	1	5 1
4	0	1	107	0	5 2
-1 -1 -1 -1 -1 -1 -1
*/
