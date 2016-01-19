/*
 * Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * piemodetest - an extremely simple app to simply report "PIE not enforced". This should be compiled in such a way as the binary will not be PIE (Position Independent Executable).
 * The binary should be launched with standard out and error piped to a file where the results can be found and interpreted.
 *
 * Purpose: Determine is SeLinux is requiring PIE executables or not.
 *
 * returns 0 in all cases
 */

#include <android/log.h>
#include <jni.h>
#include <errno.h>
#include <stdio.h>
#include <stdlib.h>

int Gargc;
char **Gargv;

int main(int argc, char **argv) {

	__android_log_print(ANDROID_LOG_DEBUG, "PIEMODE", "piemodetest:main(%i, %s)", argc, argv[argc - 1]);

	int i = 0;
	size_t n = 0;
	Gargc = argc;
	Gargv = (char**) malloc((argc + 1) * sizeof(char*));
	Gargv[argc] = NULL;

	for (i = 0; i < argc; ++i) {
		n = strlen(argv[i]) + 1;
		Gargv[i] = (char*) malloc(n);
		strcpy(Gargv[i], argv[i]);
		__android_log_print(ANDROID_LOG_DEBUG, "PIEMODE", "argv[%i]=%s", i, argv[i]);
	}

	printf("PIE not enforced\n");

	exit(0);
}
