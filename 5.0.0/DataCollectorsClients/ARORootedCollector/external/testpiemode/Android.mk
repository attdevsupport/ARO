LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:= piemodetest.c

LOCAL_CFLAGS  := -O2 -g -fpic
LOCAL_CFLAGS  += -DHAVE_CONFIG_H -D_U_="__attribute__((unused))" -Dlinux -D__GLIBC__ -D_GNU_SOURCE
#LOCAL_CFLAGS  += -fPIE
#LOCAL_LDFLAGS += -fPIE -pie

LOCAL_LDLIBS := -lc -ldl -llog

LOCAL_MODULE := piemodetest

include $(BUILD_EXECUTABLE)
