#include "socket_proc.h"

#include <time.h>
#include <sys/timeb.h>
#include <sys/stat.h>

#define MAX_PIDS 8192
#define MAX_CONNS 512
#define MAX_PROCS 512
#define MAX_APP   512

#define PROT_TCP 6
#define PROT_UDP 17

#define TIMEOUT 30	//in sec

extern double pcapTime;
extern double userTime;
extern int exitFlag;
int bearerChangedvalue;

extern FILE * ofsAppID;
extern FILE * ofsAppName;

FILE * ofsEvents;
FILE * ofsCPU;
FILE * ofsTime;

extern int captureMode;

/************** App Info (stored on sdcard) ****************/

/************** App Info (stored on sdcard) ****************/
typedef struct _APP_INFO APP_INFO;
struct _APP_INFO {
	char name[256];
	int hash;	
	int appid;
	APP_INFO * pNext;
};

#define APP_BINS 256
APP_INFO * ppApps[APP_BINS];
int nApps;

/************** connection info ****************/
struct _CONN_INFO {
	DWORD localIP;
	DWORD remoteIP;	
	int localPort;
	int remotePort;	
	int protocol;	
	DWORD iNode;	
	
	int pid;
	int appid;
	
	time_t tv_sec;
};	
typedef struct _CONN_INFO CONN_INFO;
CONN_INFO conns[MAX_CONNS];
int nConns;
int nConnsWithPID;
int nNewConns;
int byteCounter;

/************** process info ****************/

struct _PROC_NAME {
	int pid;
	char name[256];
};
typedef struct _PROC_NAME PROC_NAME;
PROC_NAME procs[MAX_PROCS];
int nProcs;

/************** PIDs ****************/

int PIDs[MAX_PIDS];
int nPIDs;

#define	INOBUCKS	128		/* inode hash bucket count -- must be a power of two */
#define INOHASH(inode)	((DWORD)((inode * 31415) >> 3) & (INOBUCKS - 1))

#define IPHASH(ip)			((WORD)((ip & 0xFFFF) + (ip & 0xFFFF0000) >> 16))

//mapping from INOHASH(inode) to CONN_INFO. iNode2connid[x][0] is the number of conns in that bin
BYTE iNode2connid[INOBUCKS][MAX_CONNS];

//mapping from local port to CONN_INFO, used by tcpdump
//possible values: -1: entry not used, -2: multiple entries - need to lookup 'conns' talbe, 0-MAX_CONNS-1:conn id
short port2connid[65536];
int nPort2connidSet;
int port2connidSetList[MAX_CONNS];

//mapping from remoteIP (hashed) to CONN_INFO, used by tcpdump
short ip2connid[65536];
int nIP2connidSet;
int ip2connidSetList[MAX_CONNS];

char filebuf[65536];
char buf[4096];
        
char gszFileDir[256] = "";

int GetStringHash(const char * str) {
	int r = 0;
	for (;*str !=0; str++) {
		r += *str;
	}
	return r;
}

DWORD Hex2Dec(const char * str, int nDigs) {
	int i;
	DWORD r = 0;
	for (i=0; i<nDigs; i++, str++) {
		if (*str == 0) return r;
		r <<= 4;
		if (*str >= '0' && *str <= '9') 
			r+= *str-'0';
		else if (*str >= 'A' && *str <= 'F')
			r+= *str-'A'+10;
		else
			fprintf(stderr, "I don't understand the hex\n");
	}
	return r;
}

char * GetIPStr(DWORD ip, char * buf) {
	sprintf(buf, "%u.%u.%u.%u",
		ip & 0xFF,
		(ip & 0xFF00) >> 8,
		(ip & 0xFF0000) >> 16,
		(ip & 0xFF000000) >> 24
	);
	return buf;
}


const char * GetProcName(int pid) {	
	int i;

	
	for (i=0; i<nProcs; i++) {
		if (procs[i].pid == pid) return procs[i].name;
	}
	
	//read /proc/pid/cmdline
	sprintf(buf, "/proc/%d/cmdline", pid);
	FILE * ifs = fopen(buf, "r");
	if (ifs == NULL) {
		return NULL;
	}
	setvbuf(ifs, filebuf, _IOFBF, sizeof(filebuf));	
		
	procs[nProcs].pid = pid;
	fgets(procs[nProcs].name, sizeof(procs[nProcs].name), ifs);	
	fclose(ifs);
	
	if (procs[nProcs].name[0] < 32) 
		return NULL;
	else {			
		return procs[nProcs++].name;
	}
}

void DumpConnection(CONN_INFO * pCI) {
	char buf1[64];
	char buf2[64];
	fprintf(stderr, "%s:%d <=> %s:%d %s [iNode=%d PID=%d APPID=%d]\n",		
		GetIPStr(pCI->localIP, buf1),
		pCI->localPort,
		GetIPStr(pCI->remoteIP, buf2),
		pCI->remotePort,
		pCI->protocol == PROT_TCP ? "TCP" : "UDP",
		pCI->iNode,
		pCI->pid,
		pCI->appid
	);
}

void DumpConnectionTable() {
	int i;
	printf("========================================\n");
	for (i=0; i<nConns; i++) {
		DumpConnection(&conns[i]);
	}
	printf("========================================\n");
}

void GetSocketInfo(char * str, int len, DWORD * pIP, int * pPort) {
	int i;
	for (i=0; i<len; i++) if (str[i] == ':') break;
	if (str[i]!=':' || (i!=8 && i!=32)) {
		fprintf(stderr, "I don't understad the address %s\n", str);
		*pIP = 0;
		*pPort = 0;
		return;
	}
	
	str[i] = 0;
	*pPort = Hex2Dec(str+i+1, 32);
	*pIP =  (Hex2Dec(str+i-2, 2)) |
				 ((Hex2Dec(str+i-4, 2)) << 8 ) |
				 ((Hex2Dec(str+i-6, 2)) << 16) |
				 ((Hex2Dec(str+i-8, 2)) << 24);
				 
	if (*pPort<0 || *pPort>65535) {
		fprintf(stderr, "Invalid port nmber %d\n", *pPort);
		exit(0);
	}
}

void ReadConnections(const char * filename, int protocol, time_t curSec) {
	FILE * ifs = fopen(filename, "r");
	if (ifs == NULL) {
		fprintf(stderr, "Cannot find %s", filename);
		return;
	}
	
	setvbuf(ifs, filebuf, _IOFBF, sizeof(filebuf));
	
	fgets(buf, sizeof(buf), ifs);	//read the header line
	while (!feof(ifs)) {
		if (fgets(buf, sizeof(buf), ifs) == NULL) break;
		
		//Parse line:
		//field 0
		//field 1: local addr  xxxx:xx (hex)
		//field 2: remote addr xxxx:xx (hex)
		//field 9: iNode (dec)		
		int i0, i=0, j=-1;
		int len = (int)strlen(buf);
		CONN_INFO * pConn = &conns[nConns];
		pConn->protocol = protocol;
		pConn->iNode = -1;
		pConn->pid = -1;
		pConn->appid = -1;
		pConn->tv_sec = curSec;
		
		while (1) {
			//read field j
			while (i<len && buf[i]<=32) i++;
			if (i>=len) break;
			
			j++;
			i0 = i;
			while (i<len && buf[i]>32) i++;
			buf[i++] = 0;
						
			if (j==1) {
				GetSocketInfo(buf+i0, i-i0-1, &pConn->localIP, &pConn->localPort);
			} else if (j==2) {
				GetSocketInfo(buf+i0, i-i0-1, &pConn->remoteIP, &pConn->remotePort);
			} else if (j==9) {
				pConn->iNode = atoi(buf+i0);
				break;
			}
		}
		
		if (j!=9) {
			fprintf(stderr, "I see a broken line with %d fields for %s\n", j, filename);
		} else {
			int iNodeHash = INOHASH(pConn->iNode);
			iNode2connid[iNodeHash][++iNode2connid[iNodeHash][0]] = nConns++;
			nNewConns++;
		}
	}	

	fclose(ifs);
}

void Init() {	
	nNewConns = 0;
	nProcs = 0;
	nPIDs = 0;
	nConnsWithPID = 0;	
	
	int i;
	for (i=0; i<INOBUCKS; i++) {
		iNode2connid[i][0] = 0;
	}
	
	for (i=0; i<nPort2connidSet; i++) {		
		port2connid[port2connidSetList[i]] = -1;
	}	
	nPort2connidSet = 0;
	
	for (i=0; i<nIP2connidSet; i++) {
		ip2connid[ip2connidSetList[i]] = -1;
	}
	nIP2connidSet = 0;
}

void ReadProcFD(const char * dir, int pid) {
	static char fdname[1024];
	DIR * d = opendir(dir);
	struct dirent * dirp;
	int len;
	
	if (d) {
		while ((dirp = readdir(d)) != NULL) {
			sprintf(fdname, "/proc/%d/fd/%s", pid, dirp->d_name);			
			len = readlink(fdname, buf, sizeof(buf));
			if (len > 9) {
				if (buf[0]=='s' && buf[1]=='o' && buf[2]=='c' && buf[3]=='k' && buf[4]=='e' && buf[5]=='t' && buf[6]==':' && buf[7]=='[' && buf[len-1]==']') {					
					buf[len-1] = 0;
					DWORD iNode = atoi(buf + 8);
					int h = INOHASH(iNode);
					int i;
					for (i=1; i<=iNode2connid[h][0]; i++) {
						int connID = iNode2connid[h][i];
						if (conns[connID].iNode == iNode) {
							if (conns[connID].pid == -1) {							
								conns[connID].pid = pid;
								nConnsWithPID++;
							}
							goto NEXT_FD;
						}
					}
					//fprintf(stderr, "Cannot find socket with iNode=%d\n", iNode);			
				}
			}
			
			NEXT_FD:
			;
		}
	}
	closedir(d);
}

int GetProcessID(const char * str) {
	int len = strlen(str);
	int i, r=0;
	for (i=0; i<len; i++) {
		if (str[i]<'0' || str[i]>'9') return -1;
		r*=10;
		r+=str[i]-'0';
	}
	return r;
}

void AttachProcessInfo() {
	DIR * d = opendir("/proc");
	struct dirent * dirp;
	int pid;
	if (d) {
		while ((dirp = readdir(d)) != NULL) {
			pid = GetProcessID(dirp->d_name);
			if (pid != -1) {								
				PIDs[nPIDs++] = pid;				
			}
		}
	} else {
		fprintf(stderr, "Cannot access /proc\n");
	}
	closedir(d);
	
	int i;
	for (i=nPIDs-1;i>=0;i--) {
		sprintf(buf, "/proc/%d/fd", PIDs[i]);				
		ReadProcFD(buf, PIDs[i]);
		if (nConnsWithPID == nNewConns) break;
	}	
}

void UpdatePort2Connid() {
	int i;
	
	if (nPort2connidSet != 0) {
		fprintf(stderr, "nPort2connidSet is not zero\n");	
		exit(0);	
	}
	
	for (i=0; i<nConns; i++) {
			if (conns[i].appid == -1) {
				fprintf(stderr, "Invalid connection entry with appid=-1\n");
				exit(0);
			}
				
			int localPort = conns[i].localPort;			
			if (port2connid[localPort] == -1) {
				port2connid[localPort] = i;
				port2connidSetList[nPort2connidSet++] = localPort;
			} else {				
				port2connid[localPort] = -2;
			}		
	}
}

void UpdateIP2Connid() {
	int i;
	
	if (nIP2connidSet != 0) {
		fprintf(stderr, "nIP2connidSet is not zero\n");
		exit(0);
	}
	
	for (i=0; i<nConns; i++) {
		if (conns[i].appid == -1) {
			fprintf(stderr, "Invalid connection entry with appid=-1\n");
			exit(0);
		}
			
		int hRemoteIP = IPHASH(conns[i].remoteIP);
		if (ip2connid[hRemoteIP] == -1) {
			ip2connid[hRemoteIP] = i;
			ip2connidSetList[nIP2connidSet++] = hRemoteIP;
		} else {
			ip2connid[hRemoteIP] = -2;
		}
	}
}

int GetAppID(const char * procName) {
	int h = GetStringHash(procName);	
	int bin = h % APP_BINS;
	
	APP_INFO * pAppInfo = ppApps[bin];
	while (pAppInfo != NULL) {
		if (h == pAppInfo->hash && !strcmp(pAppInfo->name, procName))
			return pAppInfo->appid;
		else
			pAppInfo = pAppInfo->pNext;
	}
	
	//new entry
	pAppInfo = ppApps[bin];
	
	ppApps[bin] = (APP_INFO *)malloc(sizeof(APP_INFO));
	strcpy(ppApps[bin]->name, procName);
	ppApps[bin]->hash = h;
	ppApps[bin]->appid = nApps++;
	ppApps[bin]->pNext = pAppInfo;
	
	fprintf(ofsAppName, "%s\n", procName);
	fflush(ofsAppName);	
	
	return ppApps[bin]->appid;
}

void UpdateNames() {
	int i;
	for (i=0; i<nConns; i++) {
		if (conns[i].appid != -1) continue;
			
		const char * name = GetProcName(conns[i].pid);
		if (name != NULL) {
			conns[i].appid = GetAppID(name);
		}
	}
}

void CleanConnectionInfo(time_t curSec) {
	//remove duplicated connention entries, time-out connection entries, connection entries with appid=-1
	
	//printf("Before clean nConns = %d\n", nConns);
	//DumpConnectionTable();
	
	int i,j;
	for (i=0; i<nConns; i++) {
		if (conns[i].appid == -1) continue;
		if (curSec >= conns[i].tv_sec + TIMEOUT) {
			conns[i].appid = -1;
			continue;
		}
		
		for (j=0; j<i; j++) if (conns[j].appid != -1) {
			if (conns[j].localIP    == conns[i].localIP &&
					conns[j].remoteIP   == conns[i].remoteIP &&
					conns[j].localPort  == conns[i].localPort &&
					conns[j].remotePort == conns[i].remotePort &&
					conns[j].protocol   == conns[i].protocol) 
			{
					if (conns[i].tv_sec > conns[j].tv_sec) {
						conns[j].appid = -1;
					} else {
						conns[i].appid = -1;
					}
					break;
			}
		}
	}
	
	int nConns2 = 0;
	for (i=0; i<nConns; i++) if (conns[i].appid != -1) {
		if (i != nConns2) {
			memcpy(conns + nConns2, conns + i, sizeof(CONN_INFO));
		}
		nConns2++;
	}
	
	nConns = nConns2;
	
	//printf("After clean nConns = %d\n", nConns);
	//DumpConnectionTable();
}

void UpdateProcessSocketInfo(time_t curSec) {	
	Init();

	ReadConnections("/proc/net/tcp",  PROT_TCP, curSec);
	ReadConnections("/proc/net/tcp6", PROT_TCP, curSec);
	ReadConnections("/proc/net/udp",  PROT_UDP, curSec);
	ReadConnections("/proc/net/udp6", PROT_UDP, curSec);

	AttachProcessInfo();
	UpdateNames();	
	
	CleanConnectionInfo(curSec);
	UpdatePort2Connid();
	UpdateIP2Connid();
}

CONN_INFO * SearchForConn(int localPort, int remotePort, DWORD remoteIP, int protocol) {
	int i;
	CONN_INFO * pConn = NULL;
	
	for (i=0; i<nConns; i++) {
		if (conns[i].appid == -1) {
			fprintf(stderr, "Invalid connection entry with appid=-1\n");
			exit(0);
		}
		
		if (/*conns[i].localPort == localPort &&*/ conns[i].remotePort == remotePort && conns[i].remoteIP == remoteIP && conns[i].protocol == protocol)
			return &conns[i];
		
		if (/*conns[i].localPort == localPort && conns[i].remotePort == remotePort &&*/ conns[i].remoteIP == remoteIP && conns[i].protocol == protocol)
			pConn = &conns[i];
		
	}
	return pConn;
}

int UpdatePacket_Core(int localPort, DWORD localIP, int remotePort, DWORD remoteIP, int protocol, time_t curSec) {
	int cid1, cid2;
	
	cid1 = port2connid[localPort];
	if (cid1 >= 0 && curSec < conns[cid1].tv_sec + TIMEOUT) {		
		conns[cid1].tv_sec = curSec;
		return conns[cid1].appid;
	}

	cid2 = ip2connid[IPHASH(remoteIP)];
	if (cid2 >=0 && curSec < conns[cid2].tv_sec + TIMEOUT) {
		conns[cid2].tv_sec = curSec;
		return conns[cid2].appid;
	}
		
	if (cid1 == -2 || cid2 == -2) {		
		CONN_INFO * pCI = SearchForConn(localPort, remotePort, remoteIP, protocol);
		if (pCI!=NULL && curSec < pCI->tv_sec + TIMEOUT) {
			pCI->tv_sec = curSec;
			return pCI->appid;
		}
	}
	
	UpdateProcessSocketInfo(curSec);
		
	cid1 = port2connid[localPort];
	if (cid1 >= 0) {		
		conns[cid1].tv_sec = curSec;
		return conns[cid1].appid;
	}
	
	cid2 = ip2connid[IPHASH(remoteIP)];
	if (cid2 >=0) {
		conns[cid2].tv_sec = curSec;
		return conns[cid2].appid;
	}
		
	if (cid1 == -2 || cid2 == -2) {
		CONN_INFO * pCI = SearchForConn(localPort, remotePort, remoteIP, protocol);
		if (pCI!=NULL) {
			pCI->tv_sec = curSec;
			return pCI->appid;
		}
	}
	
	return -1;
}

int UpdatePacket(struct timeval * pTS, const u_char *sp,const char *currentInterface,int dir, int bCooked) {
	
	if (captureMode != CAP_LINUX_MMAP && captureMode != CAP_LINUX_STANDARD) {
		printf("Unknown capture mode\n");
		exit(0);
	}
	int ethernetHeaderByte=14;
	int base = 0;
	if (bCooked) base = 2;	
	if (strcmp(currentInterface, "vsnet0") == 0) { // This is for LG Thrill inetrface which does not contains headers in packets
		ethernetHeaderByte = 0;
		base = 0;
		if (sp[0] != 0x45)
			return PACKET_UNKNOWN_ETHER_PROT;

	} else {
		int etherProtocol = (int) (*((WORD *) (sp + base + 12)));
		if (etherProtocol != 0x0008) {
			if (etherProtocol == 0x0608)
				return PACKET_ARP;
			else
				return PACKET_UNKNOWN_ETHER_PROT;
		}
	}	
	
	int protocol = (int)(*(sp+base+ethernetHeaderByte+9));
	if (protocol != PROT_TCP && protocol != PROT_UDP) return PACKET_UNKNOWN_IP_PROT;	
	
	DWORD srcIP = *((DWORD *)(sp+base+ethernetHeaderByte+12));
	DWORD dstIP = *((DWORD *)(sp+base+ethernetHeaderByte+16));
	
	int offset = 4 * ((*(sp+base+ethernetHeaderByte)) & 0x0F);
	int srcPort, dstPort;
	
	srcPort = (int)(*((WORD *)(sp+base+ethernetHeaderByte+offset)));
	dstPort = (int)(*((WORD *)(sp+base+ethernetHeaderByte+offset+2)));		
	srcPort = ((srcPort & 0xFF00) >> 8) | ((srcPort & 0x00FF) << 8);
	dstPort = ((srcPort & 0xFF00) >> 8) | ((srcPort & 0x00FF) << 8);			
	
	
	int appid;

	if (dir == 1)	//uplink
		appid = UpdatePacket_Core(srcPort, srcIP, dstPort, dstIP, protocol, pTS->tv_sec);
	else if (dir == 2) //downlink
		appid = UpdatePacket_Core(dstPort, dstIP, srcPort, srcIP, protocol, pTS->tv_sec);
	else
		return PACKET_UNKNOWN_DIR;

		
	return appid;
}

void SetCaptureDir(char * pcapFileDir) {
	// Note: pcapFileDir sets the global ARO file directory
	//       for this capture
	
	memset(gszFileDir, '\0', sizeof(gszFileDir));
	sprintf(gszFileDir, "%s", pcapFileDir);
	
}


void StartCapture(char * pcapFilename) {	
	// Note: pcapFilename recieves full path to tcpdump file name at end of
	// fuction; Also, SetCaptureDir() must be called before StartCapture() 
	// in main in tcpdump.c 

	exitFlag = 0;

	//Sync time
	struct timeb t;
	ftime(&t);	
	pcapTime =  t.time + t.millitm / (double) 1000.0f;	
	FILE * ifs = fopen("/proc/uptime", "r");
	if (ifs == NULL) {
		fprintf(stderr, "Failed to sync time\n");
		exit(0);
	}	
	fscanf(ifs, "%lf", &userTime);
	printf("Time synchronized: Network time = %.3lf User time = %.3lf\n", pcapTime, userTime);	
	fclose(ifs);	
	nConns = 0;
	
	//Get names	

	//put time in the format of "date	+%Y-%m-%d-%H-%M-%S" into buf
	putenv("TZ=EST5EDT");
	tzset(); 	
	time_t ts = time(NULL);
	struct tm *pTM = localtime(&ts);
	
	sprintf(buf, "%04d-%02d-%02d-%02d-%02d-%02d", 
		pTM->tm_year + 1900,	
		pTM->tm_mon + 1,
		pTM->tm_mday,
		pTM->tm_hour,
		pTM->tm_min,
		pTM->tm_sec
	);

	//We don't initialize the trace files during bearer change
	//BearerChangedvalue =0 Bearer not changed
	//Bearer change
	if(bearerChangedvalue ==0 )
	{
	//sync time
		sprintf(pcapFilename, "/sdcard/ARO/%s/time", gszFileDir);
	
		printf("Synchronized timestamp file: %s\n", pcapFilename);
		ofsTime = fopen(pcapFilename, "w");	
		if (ofsTime == NULL) {
			
			fprintf(stderr, "Cannot write %s\n", pcapFilename);
			exit(0);
		}
		fprintf(ofsTime, "%s\n%.3lf\n%u\n", "Synchronized timestamps", pcapTime, (DWORD)(userTime * 1000.0f));
	
		//event file
		sprintf(pcapFilename, "/sdcard/ARO/%s/processed_events", gszFileDir);
		printf("User event file: %s\n", pcapFilename);
		ofsEvents = fopen(pcapFilename, "w");	
		if (ofsEvents == NULL) {
			
			fprintf(stderr, "Cannot write %s\n", pcapFilename);
			exit(0);
		}
		//appid file
		sprintf(pcapFilename, "/sdcard/ARO/%s/appid", gszFileDir);
		printf("App ID file: %s\n", pcapFilename);
		ofsAppID = fopen(pcapFilename, "w");	
		if (ofsAppID == NULL) {
			
			fprintf(stderr, "Cannot write %s\n", pcapFilename);
			exit(0);
		}	
		
		//appname file
		sprintf(pcapFilename, "/sdcard/ARO/%s/appname", gszFileDir);
		printf("App name file: %s\n", pcapFilename);
		ofsAppName = fopen(pcapFilename, "w");	
		if (ofsAppName == NULL) {
			
			fprintf(stderr, "Cannot write %s\n", pcapFilename);
			exit(0);
		}		
		
		//cpu file
		sprintf(pcapFilename, "/sdcard/ARO/%s/cpu", gszFileDir);
		printf("CPU usage file name: %s\n", pcapFilename);
		ofsCPU = fopen(pcapFilename, "w");
		if (ofsCPU == NULL) {
			
			fprintf(stderr, "Cannot write %s\n", ofsCPU);
			exit(0);
		}
	//pcap filename (the last filename to fill the buffer)
	sprintf(pcapFilename, "/sdcard/ARO/%s/traffic.cap", gszFileDir);
	printf("Pcap file: %s\n", pcapFilename);	
	
	//Init memory for socket-proc
	memset(port2connid, 0xff, sizeof(port2connid));		
	nPort2connidSet = 0;	
	memset(ip2connid, 0xff, sizeof(ip2connid));
	nIP2connidSet = 0;
	//Init appid-related data structures
	nApps = 0;
	memset(ppApps, 0, sizeof(ppApps));
	
	}
	else
	{
		sprintf(pcapFilename, "/sdcard/ARO/%s/traffic%d.cap", gszFileDir,bearerChangedvalue);
		
		printf("Pcap file: %s\n", pcapFilename);	
		
	}
	
}

void TerminateCapture() {
	
	//Dump appid and names
	
	int i;
//	for (i=0; i<nApps; i++) {
//		fprintf(ofsAppName, "%s\n", apps[i].name);
//	}	

//Sync time
	struct timeb t;
	ftime(&t);	
	pcapTime =  t.time + t.millitm / (double) 1000.0f;	
	FILE * ifs = fopen("/proc/uptime", "r");
	if (ifs == NULL) {
		fprintf(stderr, "Failed to sync time\n");
		exit(0);
	}	
	fscanf(ifs, "%lf", &userTime);
	
	printf("TerminateCapture()- Time synchronized: Network time = %.3lf User time = %.3lf\n", pcapTime, userTime);	
	fclose(ifs);	

	fprintf(ofsTime, "%.3lf\n", pcapTime);
	fclose(ofsTime);

	fprintf(ofsAppName, ".");	
	fprintf(ofsAppID, "%d\n", PACKET_EOF);
	fprintf(ofsCPU, "-1 -1\n");
	
	fclose(ofsAppID);
	fclose(ofsAppName);
	fclose(ofsEvents);
	fclose(ofsCPU);
	
	
}
