/* Tcpdump server socket connection on specified pot number to start and stop the tcpdump */

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <sys/types.h> 
#include <sys/socket.h>
#include <netinet/in.h>
#include <android/log.h> 

#include "socket_server.h"

extern int exitFlag;

void socket_error(const char *msg) {
	perror(msg);
	exit(1);
}

void* start_server_socket(void *arg) {
	//TCPDUMP server port no
	int server_port = 50999;
	int sockfd, newsockfd, portno, pid;
	socklen_t clilen;
	struct sockaddr_in serv_addr, cli_addr;

	sockfd = socket(AF_INET, SOCK_STREAM, 0);
	if (sockfd < 0) {
		socket_error("Error in opening tcpdump socket");
		__android_log_print(ANDROID_LOG_INFO, "TcpdumpSocket","error in opening tcpdump socket");
	}
	bzero((char *) &serv_addr, sizeof(serv_addr));
	portno = server_port;
	__android_log_print(ANDROID_LOG_INFO, "TcpdumpSocket","portno = %d", portno);
	serv_addr.sin_family = PF_INET;
	serv_addr.sin_addr.s_addr = htonl(INADDR_ANY);
	serv_addr.sin_port = htons(portno);
	if (bind(sockfd, (struct sockaddr *) &serv_addr, sizeof(serv_addr)) < 0) {
		socket_error("Error in binding to tcdump socket");
		__android_log_print(ANDROID_LOG_INFO, "TcpdumpSocket","Error in binding to tcpdump socket");
	}
	__android_log_print(ANDROID_LOG_DEBUG, "TcpdumpSocket","socket listening");
	listen(sockfd, 5);
	clilen = sizeof(cli_addr);
	while (1) {
		printf(" inside tcpdump server socket \n");
		newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
		if (newsockfd < 0) {
			__android_log_print(ANDROID_LOG_INFO, "TcpdumpSocket","Error in accept");
			socket_error("Error in accept");
		} else {
			__android_log_print(ANDROID_LOG_INFO, "TcpdumpSocket","dostuff");
			dostuff(newsockfd);
		}
		break;
	}
	close(sockfd);
	return 0;
}
/* Separate instance of dostuff function for each connection.  It handles all communication once connnection has been established */

void dostuff(int sock) {
	int n;
	char buffer[256];

	bzero(buffer, 256);
	exitFlag = 3;
	n = read(sock, buffer, 255);
	if (n < 0) {
		socket_error("ERROR reading from socket");
		__android_log_print(ANDROID_LOG_INFO, "ERROR: reading from socket","TCPDUMP STOP MESSAGE");
	}
	__android_log_print(ANDROID_LOG_DEBUG, "TcpdumpSocket","TCPDUMP STOP MESSAGE");
	n = write(sock, "Message Received", 18);
	if (n < 0) {
		socket_error("Error writing message to socket");
		__android_log_print(ANDROID_LOG_INFO, "ERROR: writing message to socket","TCPDUMP STOP MESSAGE");
	}
	exitFlag = 4;
	printf("\n exitFlag Value= %d ", exitFlag);
	__android_log_print(ANDROID_LOG_DEBUG, "TcpdumpSocket", "Stop Recived");
	__android_log_print(ANDROID_LOG_INFO, "TcpdumpSocket", "Stop Recieved: exitFlag Value = %d", exitFlag);

}
