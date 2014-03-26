# Makefile for libAROCrypt.jnilib
# You should set JAVA_HOME before running 'make'
# Or, you can manually change the following line, e.g., 'JAVA_DIR = /usr/java/latest'
JAVA_DIR = $(JAVA_HOME)

JNI_INCLUDE = $(JAVA_DIR)/include

# try to detect your OS using 'uname'
PLATFORM = $(shell "uname")

ifeq ($(PLATFORM), Linux)
    JNI_INCLUDE2 = $(JAVA_DIR)/include/linux
    COMPILE_OPTION = -shared -L.
    SUFFIX = .so
else
ifeq ($(PLATFORM), FreeBSD)
    JNI_INCLUDE2 = $(JAVA_DIR)/include/freebsd
    COMPILE_OPTION = -shared -L.
    SUFFIX = .so
else
ifeq ($(PLATFORM), SunOS)
    JNI_INCLUDE2 = $(JAVA_DIR)/include/solaris
    COMPILE_OPTION = -G
    SUFFIX = .so
else
ifeq ($(PLATFORM), Darwin)
    JNI_INCLUDE2 = /System/Library/Frameworks/JavaVM.framework/Headers 
    COMPILE_OPTION = -Wall -bundle -framework JavaVM
    SUFFIX = .jnilib
endif
endif
endif
endif

# Specify where header files are libcrypto.a are
OPENSSL_INCLUDE =.
OPENSSL_LIB_INCLUDE=./OpenSSL_Lib/libcrypto.a

#CC = cl
CC = gcc

all: libcrypto.a

libcrypto.a: AROCrypto.cpp crypto_openssl.cpp
		$(CC) $(COMPILE_OPTION) -I$(JNI_INCLUDE) -I$(JNI_INCLUDE2)\
		 -I$(OPENSSL_INCLUDE) $(OPENSSL_LIB_INCLUDE) AROCrypto.cpp crypto_openssl.cpp -o libAROCrypt$(SUFFIX)
        
clean:
		-rm libAROCrypt$(SUFFIX)
