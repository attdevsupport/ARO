#include "objbase.h"
#include "crypto_common.h"


BEGIN_NAMESPACE(CRYPTO)

void wpa_printf(int level, const char *fmt, ...)
{

}

void * os_zalloc(size_t size)
{
	void *ptr = os_malloc(size);
	if (ptr)
		os_memset(ptr, 0, size);
	return ptr;
}

void inc_byte_array(u8 *counter, size_t len)
{
	int pos = len - 1;
	while (pos >= 0) {
		counter[pos]++;
		if (counter[pos] != 0)
			break;
		pos--;
	}
}

END_NAMESPACE
