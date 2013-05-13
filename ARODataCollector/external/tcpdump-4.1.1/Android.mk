LOCAL_PATH:= $(call my-dir)
include $(CLEAR_VARS)

LOCAL_SRC_FILES:=\
	addrtoname.c\
	af.c\
	cpack.c\
	checksum.c\
	gmpls.c\
	gmt2local.c\
	ipproto.c\
	l2vpn.c\
	machdep.c\
	nlpid.c\
	oui.c\
	parsenfsfh.c\
	print-802_11.c\
	print-ap1394.c\
	print-ah.c\
	print-arcnet.c\
	print-aodv.c\
	print-arp.c\
	print-ascii.c\
	print-atalk.c\
	print-atm.c\
	print-beep.c\
	print-bfd.c\
	print-bgp.c\
	print-bootp.c\
	print-cdp.c\
	print-cfm.c\
	print-chdlc.c\
	print-cip.c\
	print-cnfp.c\
	print-dccp.c\
	print-decnet.c\
	print-domain.c\
	print-dvmrp.c\
     print-esp.c\
	print-enc.c\
	print-egp.c\
	print-eap.c\
	print-eigrp.c\
	print-ether.c\
	print-fddi.c\
	print-fr.c\
	print-frag6.c\
	print-gre.c\
	print-hsrp.c\
	print-icmp.c\
	print-igmp.c\
	print-igrp.c\
	print-ip.c\
	print-ipnet.c\
	print-ipcomp.c\
	print-ipfc.c\
	print-ipx.c\
	print-isakmp.c\
	print-isoclns.c\
	print-juniper.c\
	print-krb.c\
	print-l2tp.c\
	print-lane.c\
	print-ldp.c\
	print-llc.c\
	print-lldp.c\
	print-lmp.c\
	print-lspping.c\
	print-lwres.c\
	print-mobile.c\
	print-mpcp.c\
	print-mpls.c\
	print-msdp.c\
	print-nfs.c\
	print-ntp.c\
	print-null.c\
	print-olsr.c\
	print-ospf.c\
	print-pgm.c\
	print-pim.c\
	print-ppp.c\
	print-pppoe.c\
	print-pptp.c\
	print-radius.c\
	print-raw.c\
	print-rip.c\
	print-ripng.c\
	print-rrcp.c\
	print-rsvp.c\
	print-rt6.c\
	print-rx.c\
	print-sctp.c\
	print-sip.c\
	print-sl.c\
	print-sll.c\
	print-slow.c\
	print-snmp.c\
	print-stp.c\
	print-sunatm.c\
	print-sunrpc.c\
	print-symantec.c\
	print-syslog.c\
	print-smb.c \
	print-tcp.c\
	print-telnet.c\
	print-tftp.c\
	print-timed.c\
	print-token.c\
	print-udp.c\
	print-vjc.c\
	print-vrrp.c\
	print-wb.c\
	print-zephyr.c\
	setsignal.c\
	tcpdump.c\
	util.c\
	version.c\
	smbutil.c\
	signature.c\
	missing/strlcat.c\
	missing/strlcpy.c\
	print-dtp.c\
	print-udld.c\
	print-vtp.c\
	print-sflow.c\
	print-vqp.c\
	print-lwapp.c\
	print-bt.c\
	print-forces.c\
	print-usb.c\
	socket_server.c

LOCAL_CFLAGS:=-O2 -g -fpic
LOCAL_CFLAGS+=-DHAVE_CONFIG_H -D_U_="__attribute__((unused))" -Dlinux -D__GLIBC__ -D_GNU_SOURCE

LOCAL_C_INCLUDES := $(ANDROID_NDK_ROOT)/external/libpcap-1.1.1

LOCAL_C_INCLUDES += \
	$(LOCAL_PATH)/missing

LOCAL_STATIC_LIBRARIES += libpcap

LOCAL_LDLIBS := -lc -ldl -llog

#LOCAL_MODULE_PATH := $(TARGET_OUT_OPTIONAL_EXECUTABLES)

#LOCAL_MODULE_TAGS := eng

LOCAL_MODULE := tcpdump

#include $(BUILD_STATIC_LIBRARY)

include $(BUILD_EXECUTABLE)

include $(ANDROID_NDK_ROOT)/external/libpcap-1.1.1/Android.mk
