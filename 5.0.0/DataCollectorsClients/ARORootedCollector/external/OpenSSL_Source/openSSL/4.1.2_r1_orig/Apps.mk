# Copyright 2006 The Android Open Source Project

LOCAL_PATH:= $(call my-dir)

local_src_files:= \
	apps/app_rand.c \
	apps/apps.c \
	apps/asn1pars.c \
	apps/ca.c \
	apps/ciphers.c \
	apps/crl.c \
	apps/crl2p7.c \
	apps/dgst.c \
	apps/dh.c \
	apps/dhparam.c \
	apps/dsa.c \
	apps/dsaparam.c \
	apps/ecparam.c \
	apps/ec.c \
	apps/enc.c \
	apps/engine.c \
	apps/errstr.c \
	apps/gendh.c \
	apps/gendsa.c \
	apps/genpkey.c \
	apps/genrsa.c \
	apps/nseq.c \
	apps/ocsp.c \
	apps/openssl.c \
	apps/passwd.c \
	apps/pkcs12.c \
	apps/pkcs7.c \
	apps/pkcs8.c \
	apps/pkey.c \
	apps/pkeyparam.c \
	apps/pkeyutl.c \
	apps/prime.c \
	apps/rand.c \
	apps/req.c \
	apps/rsa.c \
	apps/rsautl.c \
	apps/s_cb.c \
	apps/s_client.c \
	apps/s_server.c \
	apps/s_socket.c \
	apps/s_time.c \
	apps/sess_id.c \
	apps/smime.c \
	apps/speed.c \
	apps/spkac.c \
	apps/srp.c \
	apps/verify.c \
	apps/version.c \
	apps/x509.c

local_shared_libraries := \
	libssl \
	libcrypto

local_c_includes := \
	external/openssl \
	external/openssl/include

local_cflags := -DMONOLITH

include $(CLEAR_VARS)
LOCAL_MODULE:= openssl
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(local_src_files)
LOCAL_SHARED_LIBRARIES := $(local_shared_libraries)
LOCAL_C_INCLUDES := $(local_c_includes)
LOCAL_CFLAGS := $(local_cflags)
include $(LOCAL_PATH)/android-config.mk
include $(BUILD_EXECUTABLE)

include $(CLEAR_VARS)
LOCAL_MODULE:= openssl
LOCAL_MODULE_TAGS := optional
LOCAL_SRC_FILES := $(local_src_files)
LOCAL_SHARED_LIBRARIES := $(local_shared_libraries)
LOCAL_C_INCLUDES := $(local_c_includes)
LOCAL_CFLAGS := $(local_cflags)
include $(LOCAL_PATH)/android-config.mk
include $(BUILD_HOST_EXECUTABLE)
