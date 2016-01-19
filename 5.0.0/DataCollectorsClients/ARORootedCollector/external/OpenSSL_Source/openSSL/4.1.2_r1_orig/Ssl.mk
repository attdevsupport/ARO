local_c_includes := \
	external/openssl \
	external/openssl/include \
	external/openssl/crypto

local_src_files:= \
	ssl/bio_ssl.c \
	ssl/d1_both.c \
	ssl/d1_enc.c \
	ssl/d1_lib.c \
	ssl/d1_pkt.c \
	ssl/d1_srtp.c \
	ssl/kssl.c \
	ssl/s23_clnt.c \
	ssl/s23_lib.c \
	ssl/s23_meth.c \
	ssl/s23_pkt.c \
	ssl/s23_srvr.c \
	ssl/s2_clnt.c \
	ssl/s2_enc.c \
	ssl/s2_lib.c \
	ssl/s2_meth.c \
	ssl/s2_pkt.c \
	ssl/s2_srvr.c \
	ssl/s3_both.c \
	ssl/s3_clnt.c \
	ssl/s3_enc.c \
	ssl/s3_lib.c \
	ssl/s3_meth.c \
	ssl/s3_pkt.c \
	ssl/s3_srvr.c \
	ssl/ssl_algs.c \
	ssl/ssl_asn1.c \
	ssl/ssl_cert.c \
	ssl/ssl_ciph.c \
	ssl/ssl_err.c \
	ssl/ssl_err2.c \
	ssl/ssl_lib.c \
	ssl/ssl_rsa.c \
	ssl/ssl_sess.c \
	ssl/ssl_stat.c \
	ssl/ssl_txt.c \
	ssl/t1_clnt.c \
	ssl/t1_enc.c \
	ssl/t1_lib.c \
	ssl/t1_meth.c \
	ssl/t1_reneg.c \
	ssl/t1_srvr.c \
	ssl/tls_srp.c

local_c_includes += $(log_c_includes)

#######################################
# target static library
include $(CLEAR_VARS)
include $(LOCAL_PATH)/android-config.mk

ifeq ($(TARGET_ARCH),arm)
LOCAL_NDK_VERSION := 5
LOCAL_SDK_VERSION := 9
endif
LOCAL_SRC_FILES += $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_SHARED_LIBRARIES = $(log_shared_libraries)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= libssl_static
include $(BUILD_STATIC_LIBRARY)

#######################################
# target shared library
include $(CLEAR_VARS)
include $(LOCAL_PATH)/android-config.mk

ifeq ($(TARGET_ARCH),arm)
LOCAL_NDK_VERSION := 5
LOCAL_SDK_VERSION := 9
endif
LOCAL_SRC_FILES += $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_SHARED_LIBRARIES += libcrypto $(log_shared_libraries)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= libssl
include $(BUILD_SHARED_LIBRARY)

#######################################
# host shared library
include $(CLEAR_VARS)
include $(LOCAL_PATH)/android-config.mk
LOCAL_SRC_FILES += $(local_src_files)
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_SHARED_LIBRARIES += libcrypto $(log_shared_libraries)
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE:= libssl
include $(BUILD_HOST_SHARED_LIBRARY)

#######################################
# ssltest
include $(CLEAR_VARS)
include $(LOCAL_PATH)/android-config.mk
LOCAL_SRC_FILES:= ssl/ssltest.c
LOCAL_C_INCLUDES += $(local_c_includes)
LOCAL_SHARED_LIBRARIES := libssl libcrypto $(log_shared_libraries)
LOCAL_MODULE:= ssltest
LOCAL_MODULE_TAGS := optional
include $(BUILD_EXECUTABLE)
