#include "stdio.h"
#pragma once

#define BEGIN_NAMESPACE(X) namespace X {
#define END_NAMESPACE }


BEGIN_NAMESPACE(CRYPTO)

#define  OPT_CAST(x)
#define XMALLOC  os_malloc
#define XFREE    os_free
#define XREALLOC os_realloc

#define os_strlen strlen
#define os_memset memset
#define os_memcpy memcpy
#define os_memmove memmove
#define os_memcmp memcmp
#define os_malloc malloc
#define os_free free
#define os_realloc realloc
#define os_snprintf _snprintf

void * os_zalloc(size_t size);
typedef unsigned char u8;
typedef unsigned short u16;
typedef unsigned int u32;

#ifndef MIN
#define MIN(x, y) (((x) < (y)) ? (x) : (y))
#endif

#define MSG_DEBUG 1
#define MSG_MSGDUMP 2

void wpa_printf(int level, const char *fmt, ...);
void * os_zalloc(size_t size);
void inc_byte_array(u8 *counter, size_t len);

END_NAMESPACE
