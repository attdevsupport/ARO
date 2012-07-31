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
#define EVENT_SCREEN		1
#define EVENT_VOLUP		2
#define EVENT_VOLDOWN		3
#define EVENT_BALL		4
#define EVENT_POWER		5
#define EVENT_MENU		6
#define EVENT_HOME		7
#define EVENT_BACK		8
#define EVENT_SEARCH		9

#define ACTION_PRESS		1
#define ACTION_RELEASE		2

#define DEVICE_AGNOSTIC		1

struct device_key_info {
	char deviceName[40];
	char searchString[40];
	int deviceID;
} ;

#define MAX_DEVICES 20

struct device_key_info gDeviceKeyArray[MAX_DEVICES] ;

int giTotalDevices;
								
char gszDeviceLine[256];

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
extern int bearerChangedvalue;
extern int sleeptimeforbearerchange;

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

//QQQQQQ #define MAX_INPUT_FILE_ID 5
#define MAX_INPUT_FILE_ID 12
#define MAX_KEY_ENTRIES 15

KEY_ENTRY keyEntries[MAX_INPUT_FILE_ID+1][MAX_KEY_ENTRIES];
int nKeyEntries[MAX_INPUT_FILE_ID+1];

int inputEvents[MAX_INPUT_FILE_ID+1];
int maxEventPlusOne;
///////////////////////////////////////////////

extern char gszFileDir[256] ;
FILE * ofsEventsLog = NULL;

void WriteUserInputLog(char *pszLogInfo, char *pszLogInfo2) {
	if (strlen(gszFileDir) <= 3) {
		__android_log_print(ANDROID_LOG_DEBUG, "WriteEventLog:", "gszFileDir < 3");
		return ;
	}
	if (ofsEventsLog == NULL) {
		// Set up user_input_log_events log file name (used in user_input.c module)
		char szLogFileName[512];
		memset(szLogFileName, '\0', sizeof(szLogFileName));
		sprintf(szLogFileName, "/sdcard/ARO/%s/user_input_log_events", gszFileDir);
		ofsEventsLog = fopen(szLogFileName, "w");
		if (ofsEventsLog == NULL) {
			__android_log_print(ANDROID_LOG_DEBUG, "WriteEventLog:", "Cannot write to user_event_log file: %s", szLogFileName); 
			fprintf(stderr, "Cannot write to user event log file:%s\n", szLogFileName);
		} else {
			__android_log_print(ANDROID_LOG_DEBUG, "WriteEventLog:", "User event log file is: %s\n", szLogFileName);
		}
	}
	if (ofsEventsLog != NULL) {
		char DEBUG_TAG_CHAR = 'D';
		if ((pszLogInfo != NULL) && (pszLogInfo2 != NULL)) {
			fprintf(ofsEventsLog, "%c/%s %s\n", DEBUG_TAG_CHAR, pszLogInfo, pszLogInfo2);  
		} else if (pszLogInfo != NULL) {
			fprintf(ofsEventsLog, "%c/%s %s\n", DEBUG_TAG_CHAR, pszLogInfo) ; 
		} else {
			fprintf(ofsEventsLog, "%c/%s %s\n", DEBUG_TAG_CHAR);
		}
	} 
}

void CloseUserInputLog() {
	if (ofsEventsLog != NULL) {
	    fflush(ofsEventsLog);
	    fclose(ofsEventsLog);
	    ofsEventsLog = NULL ;
	}
}

void ReadDeviceKeyDB() {
	int device, input_file;
	KEY_ENTRY ke;	
	myDevice = -1;
	memset(nKeyEntries, 0, sizeof(nKeyEntries));
	#ifdef TEST_INPUT
	WriteUserInputLog("libpcap: ", "ReadDeviceKeyDB() at start");
	#endif
	__android_log_print(ANDROID_LOG_DEBUG, "libpcap: ReadDeviceKeyDb() ", "at start");
	FILE * ifs = fopen("/data/data/com.att.android.arodatacollector/key.db", "r");
	if (ifs == NULL) {
		__android_log_print(ANDROID_LOG_DEBUG, "libpcap: ReadDeviceKeyDb() ERROR ", "unable to open key db");
		printf("Cannot read file: key.db\n");
		exit(0);
	}

	#ifdef TEST_INPUT
	char aLine[256];
	memset(aLine, '\0', sizeof(aLine));
	#endif
	char szLine[256];
	int iDeviceIdx = 0 ;
	giTotalDevices = 0;

	__android_log_print(ANDROID_LOG_DEBUG, "libpcap: ReadDeviceKeyDb() ", "started reading key db");
	while (!feof(ifs)) {
	
		memset(szLine, '\0', sizeof(szLine));
		char *pszLine = fgets(szLine, (sizeof(szLine) - 1), ifs) ; 

		if ((pszLine != NULL) && (strlen(szLine) > 5)) {
			if (strncmp(szLine, "DEVICE", 6) == 0) {
				char* pszTok = strtok(szLine, ":") ;
				if (pszTok != NULL) {
					pszTok = strtok(NULL, ":");
					if ((pszTok != NULL) && (iDeviceIdx < MAX_DEVICES)){
						memset(gDeviceKeyArray[iDeviceIdx].deviceName, '\0', 
							(sizeof(gDeviceKeyArray[iDeviceIdx].deviceName)));
						strncpy(gDeviceKeyArray[iDeviceIdx].deviceName, pszTok, 
							(sizeof(gDeviceKeyArray[iDeviceIdx].deviceName) - 1));
						pszTok = strtok(NULL, ":");

						if (pszTok != NULL) {
							memset(gDeviceKeyArray[iDeviceIdx].searchString, '\0', 
								(sizeof(gDeviceKeyArray[iDeviceIdx].searchString)));
							strncpy(gDeviceKeyArray[iDeviceIdx].searchString, pszTok, 
								(sizeof(gDeviceKeyArray[iDeviceIdx].searchString) - 1));
							pszTok = strtok(NULL, ":");
							if (pszTok != NULL) {
								gDeviceKeyArray[iDeviceIdx].deviceID = atoi(pszTok);
							} 
							if (pszTok != NULL) {
								gDeviceKeyArray[iDeviceIdx].deviceID = atoi(pszTok);
								#ifdef TEST_INPUT
								sprintf(aLine, "Added to gDeviceKeyArray iDeviceIdx = %d deviceId = %d for deviceName: %s",
									iDeviceIdx, gDeviceKeyArray[iDeviceIdx].deviceID, 
									gDeviceKeyArray[iDeviceIdx].deviceName);
								WriteUserInputLog("libpcap: ReadDeviceKeyDB() ", aLine);
								#endif
							} else {
								#ifdef TEST_INPUT
								sprintf(aLine, "ERROR: not all info for gDeviceKeyArry iDeviceIdx = %d for deviceName: %s",
									iDeviceIdx, gDeviceKeyArray[iDeviceIdx].deviceName);
								WriteUserInputLog("libpcap: ReadDeviceKeyDB() ", aLine);
								#endif
							}
							if ((strlen(gszDeviceLine) > 0) &&
							    (strstr(gszDeviceLine, gDeviceKeyArray[iDeviceIdx].searchString) != NULL)) {
								myDevice = gDeviceKeyArray[iDeviceIdx].deviceID ;
								printf("Device Type: %s %d\n", gDeviceKeyArray[iDeviceIdx].deviceName);
								//TODO : Need to come up with generic solution for device transition wait
								if (myDevice == 9) {
									sleeptimeforbearerchange = 8;
								}
								#ifdef TEST_INPUT
								sprintf(aLine, "Device Type: %s: %d", 
									gDeviceKeyArray[iDeviceIdx].deviceName, myDevice);
								WriteUserInputLog("libpcap: MATCH! ReadDeviceKeyDB() ", aLine);
								#endif
							} else {
								#ifdef TEST_INPUT
								sprintf(aLine, "Device Type: %s: %d does not match", 
									gDeviceKeyArray[iDeviceIdx].deviceName, myDevice);
								WriteUserInputLog("libpcap: Does NOT match. ReadDeviceKeyDB() ", aLine);
								#endif
							}
						} else {
							#ifdef TEST_INPUT
							sprintf(aLine, "ERROR: line not complete %s iDeviceIdx = %d for deviceName: %s",
								szLine, iDeviceIdx, gDeviceKeyArray[iDeviceIdx].deviceName);
							WriteUserInputLog("libpcap: missing info in ReadDeviceKeyDB() ", aLine);
							#endif
						}
						iDeviceIdx++;
					}
				}
			} else {
				sscanf(szLine, "%d %d %d %d %d %d %d", 
					&device, &input_file, &ke.arg1, &ke.arg2, &ke.arg3, &ke.event, &ke.action);
				#ifdef TEST_INPUT
				sprintf(aLine, "line_values = %d %d %d %d %d %d %d", 
					device, input_file, ke.arg1, ke.arg2, ke.arg3, ke.event, ke.action);
				WriteUserInputLog("libpcap: ReadDeviceKeyDB() ", aLine);
				#endif
				if (device == -1) {
				    #ifdef TEST_INPUT
				    WriteUserInputLog("libpcap: ReadDeviceKeyDB() ", "device == -1, breaking");
				    #endif
				    break;
				}
				if ((device != myDevice) && (device != DEVICE_AGNOSTIC)){
				    #ifdef TEST_INPUT
				    sprintf(aLine, "myDevice = %d is not equal to device = %d ; or device_agnostic, continuing", myDevice, device);
				    WriteUserInputLog("libpcap: ReadDeviceKeyDB() ", aLine);
				    #endif
				    continue;
				} 
				#ifdef TEST_INPUT
				int iNKeyEntries = nKeyEntries[input_file] ;
				sprintf(aLine, "myDevice = %d should be equal to device = %d or be DEVICE_AGNOSTIC", myDevice, device);
				WriteUserInputLog("libpcap: ReadDeviceKeyDB()", aLine);
				int iInputFile = input_file;
				int inKeyEntriesForInputFile = nKeyEntries[input_file];
				sprintf(aLine, "for input_file %d nKeyEntries[input_file] is now %d:", 
					iInputFile, inKeyEntriesForInputFile);
				WriteUserInputLog("libpcap: ReadDeviceKeyDB()", aLine);
				sprintf(aLine, "for keyEntries[%d][%d] now will be assigned ke values: %d %d %d %d %d", 
					iInputFile, inKeyEntriesForInputFile, 
					ke.arg1, ke.arg2, ke.arg3, ke.event, ke.action);
				WriteUserInputLog("libpcap: ReadDeviceKeyDB()", aLine);
				#endif
				keyEntries[input_file][nKeyEntries[input_file]++] = ke;
			}
		}
	}
	fclose(ifs);
	giTotalDevices = iDeviceIdx;
	if (myDevice == -1) {
		if (strlen(gszDeviceLine) > 0) {
			char aLineInfo[256];
			memset(aLineInfo, '\0', sizeof(aLineInfo));
			sprintf(aLineInfo, 
				"Unknown device not mapped for [ro.product.device]: %s", 
				gszDeviceLine); 
			WriteUserInputLog("libpcap: ReadDeviceKeyDb() ", aLineInfo);
		}
		WriteUserInputLog("libpcap: ReadDeviceKeyDB() ", " -- will be using device agnostic ");
		__android_log_print(ANDROID_LOG_DEBUG, "libpcap: ReadDeviceKeyDb() ", 
				    " no device found -- will be using device agnostic");
		//exit(0);
	} 
	#ifdef TEST_INPUT
	WriteUserInputLog("libpcap: ", "ReadDeviceKeyDB() at end");
	#endif
	__android_log_print(ANDROID_LOG_DEBUG, "libpcap: ReadDeviceKeyDb() ", "completed reading key db");
}

void GetMyDeviceType() {
	#ifdef TEST_INPUT
	WriteUserInputLog("libpcap: ", "GetMyDeviceType() at start");
	#endif

	memset(gszDeviceLine, '\0', sizeof(gszDeviceLine));

	char szPropFileName[256];
	memset(szPropFileName, '\0', sizeof(szPropFileName));
	__android_log_print(ANDROID_LOG_DEBUG, "libpcap: GetMyDeviceType() ", "gszFileDir = %s", gszFileDir);
	sprintf(szPropFileName, "/sdcard/ARO/%s/prop", gszFileDir);
	char szCmd[512];
	memset(szCmd, '\0', sizeof(szCmd));
	sprintf(szCmd, "/system/bin/getprop > %s", szPropFileName);
	__android_log_print(ANDROID_LOG_DEBUG, "libpcap: GetMyDeviceType() ", "szCmd = %s", szCmd);
	system(szCmd);
	FILE * ifs = fopen(szPropFileName, "r");
	char szLine[256];
	if (ifs == NULL) {
		printf("Cannot determine device type\n");
		memset(szLine, '\0', sizeof(szLine));
		sprintf(szLine, "Could not open %s ; so could not determine device type", szPropFileName); 
		#ifdef TEST_INPUT
		WriteUserInputLog("libpcap: GetMyDeviceType() ", szLine);
		#endif
		__android_log_print(ANDROID_LOG_DEBUG, "libpcap: GetMyDeviceType() ", "Failed: %s", szLine);
		// exit(0);
		return ;
	} else {
		char buf[1024];
		while (!feof(ifs)) {
			if (fgets(buf, sizeof(buf), ifs) == NULL) {
			    break;
			}
			if (strstr(buf, "[ro.product.device]") != NULL) {
				strncpy(gszDeviceLine, buf, (sizeof(gszDeviceLine) - 1));
				break;
			}
		}
		if (ifs != NULL) {
		    fclose(ifs);
		}
	}
	if (strlen(gszDeviceLine) <= 0) {
		sprintf(szLine, 
			"[ro.product.device] entry not found in properties file: %s",
			szPropFileName);
		WriteUserInputLog("libpcap: GetMyDeviceType() ", szLine);
		__android_log_print(ANDROID_LOG_DEBUG, "libpcap: GetMyDeviceType() ", "Failed: %s", szLine);
	}
	#ifdef TEST_INPUT
	WriteUserInputLog("libpcap: GetMyDeviceType() ", "at end");
	#endif
}

int OpenInputDevice(int id) {	
	char deviceName[128];
	sprintf(deviceName, "/dev/input/event%d", id);
	printf("Opening input device %s... ", deviceName);

	int fd = open(deviceName, O_RDWR);
	if (fd == -1) {
		WriteUserInputLog("libpcap: Error for open in OpenInputDevice() ", deviceName);
		printf("Error: cannot open input device %s\n", deviceName);
		//exit(0);
	}
	#ifdef TEST_INPUT
	char aLine[256];
	memset(aLine, '\0', sizeof(aLine));
	sprintf(aLine, " OpenInputDevice() fd = %d", fd);
	WriteUserInputLog("libpcap: ", "OpenInputDevice() actually opened");
	WriteUserInputLog("libpcap: b", aLine);
	#endif
	printf("fd = %d\n", fd);	
	return fd;
}

int CloseInputDevice(int id) {	
	if (inputEvents[id] != -1) {
	    close(inputEvents[id]);
	}
}

//return 0 to stop
static int HandleEvents() {
	int i, j, k, m;
	struct timeval timeout; 
	timeout.tv_sec = 10;
	timeout.tv_usec = 0; 

	char aLine[256];
	memset(aLine, '\0', sizeof(aLine));
	
	sprintf(aLine, "at Start exitFlag = %d", exitFlag);
	if (exitFlag == 4) { 
		WriteUserInputLog("libpcap: HandleEvent() returning 0 due to exit flag, exitFlag now 3", aLine);
		exitFlag = 3; 
		return 0; 
	}

	FD_ZERO(&rfds);	
        int iMaxInputFileId = MAX_INPUT_FILE_ID;	
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {
		if (inputEvents[i] > 0) {
			FD_SET(inputEvents[i], &rfds);
		} 
	}
	int retval;
	retval = select(maxEventPlusOne, &rfds, NULL, NULL, &timeout);
	if (retval == 0) {
		WriteUserInputLog("libpcap: HandleEvents() retval from select is 0: ", 
				    "returning 1 and exiting HandleEvents()");
		return 1;
	} else if (retval == -1) {
		WriteUserInputLog("libpcap: HandleEvent() ERROR: select() fails!", "exiting app!");
		if(bearerChangedvalue > 0)
		{
			printf("Bearer Change Should capture again");
			__android_log_print(ANDROID_LOG_DEBUG, "Thread CaptureUserInput() ", "Bearer Change Should capture again=%d",bearerChangedvalue);
			HandleEvents();
		}
		else
		{
			printf("Error: Select() fails");
			exit(0);
		}
	}

	int iEventMatchesCount = 0;
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {
		#ifdef TEST_INPUT
		sprintf(aLine, "i=%d ; inputEvents[i]=%d", i, inputEvents[i]);
		WriteUserInputLog("libpcap HandleEvent top of outer for", aLine);
		#endif
		if (inputEvents[i] == -1) {
		    continue;
		}
		if (!(FD_ISSET(inputEvents[i], &rfds))) {
		    continue;
		}
		int rd = read(inputEvents[i], events, sizeof(struct input_event) * N_EVENTS);
		int nEvents = rd / sizeof(struct input_event);		
		m = nKeyEntries[i];
		#ifdef TEST_INPUT
		sprintf(aLine, "nEvents = %d ; m = %d", nEvents, m);
		WriteUserInputLog("libpcap HandleEvent top of outer for", aLine);
		#endif
		
		for (j=0; j<nEvents; j++) {
			int arg1 = events[j].type;
			int arg2 = events[j].code;
			int arg3 = events[j].value;
			double tm = GetTimestamp(&events[j].time);
			
			for (k=0; k<m; k++) {
				if (exitFlag == 4) { 
					#ifdef TEST_INPUT
					sprintf(aLine, "k=%d ; m=%d", k, m);
					WriteUserInputLog("libpcap HandleEvent exitFlag is 4; setting exitFlag=3; returning 0", aLine);
					#endif
					exitFlag = 3; 
					return 0; 
				}
				//NOTE: an "-1" entry for arg3 means anything positive
				if (keyEntries[i][k].arg1 == arg1 && 
				    keyEntries[i][k].arg2 == arg2 &&
						(keyEntries[i][k].arg3 == arg3 || 
						    (keyEntries[i][k].arg3 == -1 && arg3>0))) {
					#ifdef TEST_INPUT
					sprintf(aLine, "KEYINFO: m=%d i=%d j=%d k=%d ; event j arg1-2-3: %d %d %d ; kE[i][k].event=%d %s, kE[i][k].action=%d %s", 
                                                m, i, j, k, arg1, arg2, arg3, keyEntries[i][k].event, event_str[keyEntries[i][k].event], 
						keyEntries[i][k].action, action_str[keyEntries[i][k].action]);
					WriteUserInputLog("libpcap HandleEvent match! for: " , aLine);
					printf("%.6lf %s %s\n", 
						tm, 
						event_str[keyEntries[i][k].event], 
						action_str[keyEntries[i][k].action]
					);
					printf("\n exitFlag = %d",exitFlag);
					#endif
					if (exitFlag == 4) { 
						WriteUserInputLog("libpcap HandleEvent NO MATCH! exitFlag is 4, setting exitFlag to 3" , "returning 0");
						exitFlag = 3; 
						return 0; 
					}
					iEventMatchesCount++;
					fprintf(ofsEvents, "%.6lf %s %s\n", 
						tm, 
						event_str[keyEntries[i][k].event], 
						action_str[keyEntries[i][k].action]
					);
										
					if (keyEntries[i][k].event == EVENT_VOLDOWN) {
						if (keyEntries[i][k].action == ACTION_RELEASE) {
							sprintf(aLine, "exitFlag = %d", exitFlag);
							if (exitFlag == 3) {
								return 0 ;
							} else {
								;
							}
						} else {
							;
						}
					} else {
						;
						//exitFlag = 0;
					}
				} else {
					#ifdef TEST_INPUT
					sprintf(aLine, "NO_KY_INFO: m=%d i=%d j=%d k=%d ; event j arg1-2-3: %d %d %d ; NO MATCH; keep checking",
                                                m, i, j, k, arg1, arg2, arg3);
					WriteUserInputLog("libpcap HandleEvent no MATCH! ", aLine);
					#endif
				}
			}
		}
	}
	return 1;
}

void * CaptureUserInput(void * arg) { 
	int i;
	
	__android_log_print(ANDROID_LOG_DEBUG, "Thread CaptureUserInput() ", "pre_getMyDeviceType");
	GetMyDeviceType();
	__android_log_print(ANDROID_LOG_DEBUG, "Thread CaptureUserInput() ", "pre_ReadDeviceKeyDB");
	ReadDeviceKeyDB();
	
	__android_log_print(ANDROID_LOG_DEBUG, "Thread CaptureUserInput() ", "post_ReadDeviceKeyDB");
	printf("sizeof(struct input_event) = %d\n", sizeof(struct input_event));
	maxEventPlusOne = -1;
			
	char aLine[256];
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {		
		sprintf(aLine, "i = %d", i);
		if (nKeyEntries[i] == 0) {
			inputEvents[i] = -1;
		} else {
			inputEvents[i] = OpenInputDevice(i);
			if (inputEvents[i] > maxEventPlusOne) {
				maxEventPlusOne = inputEvents[i];
				sprintf(aLine, "NOT_EXPECTED: i = %d, inputEvents[i] (%d) > maxEventPlusOne = %d", 
					i, inputEvents[i], maxEventPlusOne);
				#ifdef TEST_INPUT
				WriteUserInputLog("libpcap: b CaptureUserInput() nKeyEntries[i] != 0 ", aLine);
				#endif
			} 
		}
	}
	maxEventPlusOne++;	
	
	while (1) {
		if  (!HandleEvents()) { 
		    #ifdef TEST_INPUT
		    WriteUserInputLog("libpcap: After HandleEvents returns 0 in CaptureUserInput() ", 
					"setting exitFlag = 3 and breaking from loop");
		    #endif
		    exitFlag = 3; 
		    break; 
		}
		if (exitFlag == 4) { 
		    #ifdef TEST_INPUT
		    WriteUserInputLog("libpcap: After HandleEvents exitFlag is 4 ", 
					"setting exitFlag = 3 and breaking from loop");
		    #endif
		    exitFlag = 3; 
		    break; 
		}
	}
	printf("Thread CaptureUserInput() exit.\n");
	__android_log_print(ANDROID_LOG_DEBUG, "Thread CaptureUserInput() exit", "EXIT");
	printf("sizeof(struct input_event) = %d\n", sizeof(struct input_event));
	
	for (i=0; i<=MAX_INPUT_FILE_ID; i++) {
		CloseInputDevice(i);
	}
	CloseUserInputLog();
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

