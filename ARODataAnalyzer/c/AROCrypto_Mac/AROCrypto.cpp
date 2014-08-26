#include "crypto_alg.h"
#include "crypto_openssl.h"
#include "com_att_aro_pcap_AROCryptoAdapter.h"
#include <string.h>


typedef unsigned char       BYTE;

#define ARO_CRYPTO_ERROR -1
#define CTX_TSI_SERVER 0
#define CTX_TSI_CLIENT 1
#define CTX_TSI_PENDING 2

struct crypto_cipher* tsiserver_ctx_server = NULL;
struct crypto_cipher* tsiserver_ctx_client = NULL;
struct crypto_cipher* tsiclient_ctx_server = NULL;
struct crypto_cipher* tsiclient_ctx_client = NULL;
struct crypto_cipher* tsipending_ctx_server = NULL;
struct crypto_cipher* tsipending_ctx_client = NULL;
struct crypto_hash * hmac = NULL;




extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptohashInitUpdateFinish1(
	JNIEnv *env, jobject obj, jint dir)

{
        int test = (int)dir;
	int ret = crypto_hash_finish1(test);
	return ret;
}

//This method should be called ONLY by tsiServer or tsiClient TLS_SESSION_INFO
extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptocipherdecrypt
	(JNIEnv *env, jobject obj, jint pCipher, jbyteArray enc, jbyteArray _plain, jint enclength, jint objectType)
{
	int ret = ARO_CRYPTO_ERROR;
	int i_pCipher = (int)pCipher;
	int i_enclength = (int)enclength;
	int i_objectType = (int)objectType;

	jbyte* encbuff = NULL;
	encbuff = env->GetByteArrayElements(enc, NULL);

	jbyte* _plainbuff = NULL;
	_plainbuff = env->GetByteArrayElements(_plain, NULL);

	if(!encbuff || !_plainbuff)
		return NULL;

	struct crypto_cipher* ctx = NULL;
	if(i_pCipher == CTX_TSI_CLIENT)
	{
		if(i_objectType == CTX_TSI_CLIENT)
			ctx = tsiclient_ctx_client;
		else
			ctx = tsiserver_ctx_client;
	}
	else
	{
		if(i_objectType == CTX_TSI_CLIENT)
			ctx = tsiclient_ctx_server;
		else
			ctx = tsiserver_ctx_server;
	}

	ret = crypto_cipher_decrypt(ctx, (BYTE*)encbuff, (BYTE*)_plainbuff, i_enclength);
	env->ReleaseByteArrayElements(enc, encbuff, 0);
	env->ReleaseByteArrayElements(_plain, _plainbuff, 0);
	
	return ret;
}



extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_ReadSSLKeys
(JNIEnv *env, jobject obj, jstring filename)
{
    int ret = ARO_CRYPTO_ERROR;
    const char *szfilename = env->GetStringUTFChars(filename, 0);
    if(NULL == szfilename)
        return ARO_CRYPTO_ERROR;
    
    double ts = 0.0;
    int preMasterLen = 0;
    int masterLen = 48;
    BYTE* preMaster = (BYTE*)malloc(256);
    BYTE* master = (BYTE*)malloc(48);
    FILE * ifs = fopen(szfilename, "rb");
    
    size_t r = ARO_CRYPTO_ERROR;
    if(ifs!=NULL)
    {
    while (!feof(ifs))
    {
        r = fread(&ts, 8, 1, ifs);
        r = fread(&preMasterLen, 4, 1, ifs);
        r = fread(preMaster, preMasterLen, 1, ifs);
        r = fread(master, masterLen, 1, ifs);
        
       jclass cls;
       jmethodID mid;
        jbyteArray preMasterArray;
        jbyteArray masterArray;
        cls = env->GetObjectClass(obj);
        mid = env->GetMethodID(cls, "sslKeyHandler", "(DI[B[B)V");
        if(mid != NULL)
        {
            preMasterArray=env->NewByteArray((jsize)preMasterLen);
            env->SetByteArrayRegion(preMasterArray, 0, (jsize)preMasterLen, (const jbyte*)preMaster);
            masterArray=env->NewByteArray((jsize)48);
            env->SetByteArrayRegion(masterArray, 0, (jsize)48, (const jbyte*)master);
            
            env->CallVoidMethod(obj, mid, (jdouble)ts, (jint)preMasterLen, preMasterArray, masterArray);
            env->DeleteLocalRef(preMasterArray);
            env->DeleteLocalRef(masterArray);
        }
    }
        fclose(ifs);
        ret = 0;
    }
    
    env->ReleaseStringUTFChars(filename, szfilename);
    return ret;
}
//
//This method should be called ONLY by tsiPending TLS_SESSION_INFO
extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptocipherinit
	(JNIEnv *env, jobject obj, jint alg, jbyteArray temp1, jbyteArray temp2, jint key_material, jint bClient)
{
	int ret = ARO_CRYPTO_ERROR;
	int i_alg = (int)alg;
	enum crypto_cipher_alg e_alg = (enum crypto_cipher_alg)i_alg;
	
	jbyte* temp1buff = NULL;
	temp1buff = env->GetByteArrayElements(temp1, NULL);

	jbyte* temp2buff = NULL;
	temp2buff = env->GetByteArrayElements(temp2, NULL);

	if(!temp1buff || !temp2buff)
		return ret;

	int i_key_material = (int)key_material;
	int i_bClient = (int)bClient;
	
	struct crypto_cipher* ctx = NULL;
	ctx = crypto_cipher_init(e_alg, (const BYTE*)temp1buff, (const BYTE*)temp2buff, i_key_material);
	env->ReleaseByteArrayElements(temp1, temp1buff, 0);
	env->ReleaseByteArrayElements(temp2, temp2buff, 0);
	if(ctx == NULL)
		return ret;
	
	if(i_bClient == 1)
		tsipending_ctx_client = ctx;		
	else
		tsipending_ctx_server = ctx;

	ret = 0;

	return ret;
}

extern "C" JNIEXPORT void JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptocipherdeinit
	(JNIEnv *env, jobject obj, jint objectType)
{
	int i_objectType = (int)objectType;
	if(i_objectType == CTX_TSI_SERVER)
	{
		if(tsiserver_ctx_client)
		{
			crypto_cipher_deinit(tsiserver_ctx_client);
			tsiserver_ctx_client = NULL;
		}
		if(tsiserver_ctx_server)
		{
			crypto_cipher_deinit(tsiserver_ctx_server);
			tsiserver_ctx_server = NULL;
		}
	}
	else if(i_objectType == CTX_TSI_CLIENT)
	{
		if(tsiclient_ctx_client)
		{
			crypto_cipher_deinit(tsiclient_ctx_client);
			tsiclient_ctx_client = NULL;
		}
		if(tsiclient_ctx_server)
		{
			crypto_cipher_deinit(tsiclient_ctx_server);
			tsiclient_ctx_server = NULL;
		}
	}
	else if(i_objectType == CTX_TSI_PENDING)
	{
		if(tsipending_ctx_client)
		{
			crypto_cipher_deinit(tsipending_ctx_client);
			tsipending_ctx_client = NULL;
		}
		if(tsipending_ctx_server)
		{
			crypto_cipher_deinit(tsipending_ctx_server);
			tsipending_ctx_server = NULL;
		}
	}	
}

extern "C" JNIEXPORT void JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_setcryptociphernull
	(JNIEnv *env, jobject obj, jint objectType, jint bClient)
{
	int i_objectType = (int)objectType;
	if(i_objectType == CTX_TSI_SERVER)
	{
		if(bClient == CTX_TSI_CLIENT)
			tsiserver_ctx_client = NULL;
		else
			tsiserver_ctx_server = NULL;
		
	}
	else if(i_objectType == CTX_TSI_CLIENT)
	{
		if(bClient == CTX_TSI_CLIENT)
			tsiclient_ctx_client = NULL;
		else
			tsiclient_ctx_server = NULL;
	}
	else if(i_objectType == CTX_TSI_PENDING)
	{
		if(bClient == CTX_TSI_CLIENT)
			tsipending_ctx_client = NULL;
		else
			tsipending_ctx_server = NULL;
	}
}

extern "C" JNIEXPORT void JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_copycryptocipher
	(JNIEnv *env, jobject obj, jint from, jint to)
{
	int i_from = (int)from;
	int i_to = (int)to;
	if(i_from == CTX_TSI_SERVER)
	{
		if(i_to == CTX_TSI_CLIENT)
		{
			tsiclient_ctx_server = tsiserver_ctx_server;
			tsiclient_ctx_client = tsiserver_ctx_client;
		}
		else
		{
			tsipending_ctx_server = tsiserver_ctx_server;
			tsipending_ctx_client = tsiserver_ctx_client;
		}
	}
	
	if(i_from == CTX_TSI_CLIENT)
	{
		if(i_to == CTX_TSI_SERVER)
		{
			tsiserver_ctx_server = tsiclient_ctx_server;
			tsiserver_ctx_client = tsiclient_ctx_client;
		}
		else
		{
			tsipending_ctx_server = tsiclient_ctx_server;
			tsipending_ctx_client = tsiclient_ctx_client;
		}
	}

	if(i_from == CTX_TSI_PENDING)
	{
		if(i_to == CTX_TSI_SERVER)
		{
			tsiserver_ctx_server = tsipending_ctx_server;
			tsiserver_ctx_client = tsipending_ctx_client;
		}
		else
		{
			tsiclient_ctx_server = tsipending_ctx_server;
			tsiclient_ctx_client = tsipending_ctx_client;
		}
	}	
}


//
