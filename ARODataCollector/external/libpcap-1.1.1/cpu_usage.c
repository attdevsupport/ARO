#include "cpu_usage.h"
#include <stdio.h>
#include <stdlib.h>

void * CaptureCPUUsage(void * arg) {
	
	printf("CaptureCPUUsage() should not be called\n");
	exit(0);
	
	return 0;
}
