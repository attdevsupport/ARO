#ifndef _SOCKET_PROC_H_
#define _SOCKET_PROC_H_

#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <fcntl.h>
#include <dirent.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <stdlib.h>


typedef unsigned int DWORD;
typedef unsigned short WORD;
typedef unsigned char BYTE;

#define CAP_LINUX_STANDARD 1
#define CAP_LINUX_MMAP		 2

#define PACKET_UNKNOWN_APP				-1
#define PACKET_UNKNOWN_DIR				-2
#define PACKET_ARP								-3
#define PACKET_UNKNOWN_IP_PROT 		-4
#define PACKET_UNKNOWN_ETHER_PROT -5
#define PACKET_EOF								-127


void StartCapture(char * pcapFilename);
void TerminateCapture();
void Terminate();

//return appid
int UpdatePacket(struct timeval * pTS, const u_char *sp,const char *currentdevice,int dir, int bCooked);

#endif
