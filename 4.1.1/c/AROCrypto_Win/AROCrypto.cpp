// AROCrypto.cpp : Defines the exported functions for the DLL application.
#include "objbase.h"
#include "crypto_alg.h"
#include "crypto_openssl.h"
#include "com_att_aro_pcap_AROCryptoAdapter.h"

wchar_t* JavaToWSZ(JNIEnv* env, jstring string);
jstring WSZToJava(JNIEnv* env, wchar_t* wsz);

#define ARO_CRYPTO_ERROR -1
#define CTX_TSI_SERVER 0
#define CTX_TSI_CLIENT 1
#define CTX_TSI_PENDING 2

struct CRYPTO::crypto_cipher* tsiserver_ctx_server = NULL;
struct CRYPTO::crypto_cipher* tsiserver_ctx_client = NULL;
struct CRYPTO::crypto_cipher* tsiclient_ctx_server = NULL;
struct CRYPTO::crypto_cipher* tsiclient_ctx_client = NULL;
struct CRYPTO::crypto_cipher* tsipending_ctx_server = NULL;
struct CRYPTO::crypto_cipher* tsipending_ctx_client = NULL;

struct CRYPTO::crypto_hash * hmac = NULL;

extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptohashInitUpdateFinish(
	JNIEnv *env, jobject obj, jint dir, jint hash_alg, jbyteArray keyBlock, jint hash_size, jint recType, jint payloadLen, jbyteArray plain, jbyteArray seqNum)
{
	int ret = ARO_CRYPTO_ERROR;
	__try
	{
		int i_dir = (int)dir;
		int i_hash_alg = (int)hash_alg;
		enum CRYPTO::crypto_hash_alg e_alg = (enum CRYPTO::crypto_hash_alg)i_hash_alg;
		int i_hash_size = (int)hash_size;
		int i_recType = (int)recType;
		int i_payloadLen = (int)payloadLen;

		jbyte* keyBlockbuff = NULL;
		keyBlockbuff = env->GetByteArrayElements(keyBlock, NULL);
		jbyte* plainbuff = NULL;
		plainbuff = env->GetByteArrayElements(plain, NULL);
		jbyte* seqNumbuff = NULL;
		seqNumbuff = env->GetByteArrayElements(seqNum, NULL);

		switch (i_dir) {
			case 1: //UPLINK
				hmac = CRYPTO::crypto_hash_init(e_alg, (const BYTE*)keyBlockbuff, i_hash_size);
				break;
			case 2: //DOWNLINK
				hmac = CRYPTO::crypto_hash_init(e_alg, (const BYTE*)keyBlockbuff + i_hash_size, i_hash_size);
				break;
		}

		BYTE tlsTypeVersion[3] = {0x16, 0x03, 0x01};	
		tlsTypeVersion[0] = i_recType;

		BYTE _payloadLen[2];
		
		_payloadLen[1] = BYTE(i_payloadLen & 0xFF);
		_payloadLen[0] = BYTE(i_payloadLen >> 8);

		//HMAC: type + version + length + fragment(payload)
		
		crypto_hash_update(hmac, (const BYTE*)seqNumbuff, TLS_SEQ_NUM_LEN);
		crypto_hash_update(hmac, tlsTypeVersion, 3);
		crypto_hash_update(hmac, _payloadLen, 2);
		crypto_hash_update(hmac, (const BYTE*)plainbuff, i_payloadLen);
	
		BYTE hashResults[256];
		size_t hashLen = sizeof(hashResults);
		crypto_hash_finish(hmac, hashResults, &hashLen);
	
		if (hashLen != i_hash_size ||
			memcmp(hashResults, plainbuff + i_payloadLen, i_hash_size) != 0
		) {
			return ret;
		}

		env->ReleaseByteArrayElements(keyBlock, keyBlockbuff, 0);
		env->ReleaseByteArrayElements(plain, plainbuff, 0);
		env->ReleaseByteArrayElements(seqNum, seqNumbuff, 0);
		ret = 0;
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
		ret = ARO_CRYPTO_ERROR;
    }

	return ret;
}

//This method should be called ONLY by tsiServer or tsiClient TLS_SESSION_INFO
extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptocipherdecrypt
	(JNIEnv *env, jobject obj, jint pCipher, jbyteArray enc, jbyteArray _plain, jint enclength, jint objectType)
{
	int ret = ARO_CRYPTO_ERROR;
	__try
	{
		int i_pCipher = (int)pCipher;
		int i_enclength = (int)enclength;
		int i_objectType = (int)objectType;

		jbyte* encbuff = NULL;
		encbuff = env->GetByteArrayElements(enc, NULL);

		jbyte* _plainbuff = NULL;
		_plainbuff = env->GetByteArrayElements(_plain, NULL);

		if(!encbuff || !_plainbuff)
			return NULL;

		struct CRYPTO::crypto_cipher* ctx = NULL;
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

		ret = CRYPTO::crypto_cipher_decrypt(ctx, (BYTE*)encbuff, (BYTE*)_plainbuff, i_enclength);
		env->ReleaseByteArrayElements(enc, encbuff, 0);
		env->ReleaseByteArrayElements(_plain, _plainbuff, 0);
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
		ret = ARO_CRYPTO_ERROR;
    }

	return ret;
}

extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_ReadSSLKeys
(JNIEnv *env, jobject obj, jstring filename)
{
    int ret = ARO_CRYPTO_ERROR;
	__try
	{
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
				::Sleep(100);
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
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
		ret = ARO_CRYPTO_ERROR;
    }
    return ret;
}

//This method should be called ONLY by tsiPending TLS_SESSION_INFO
extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptocipherinit
	(JNIEnv *env, jobject obj, jint alg, jbyteArray temp1, jbyteArray temp2, jint key_material, jint bClient)
{
	int ret = ARO_CRYPTO_ERROR;
	__try
	{
		int i_alg = (int)alg;
		enum CRYPTO::crypto_cipher_alg e_alg = (enum CRYPTO::crypto_cipher_alg)i_alg;
	
		jbyte* temp1buff = NULL;
		temp1buff = env->GetByteArrayElements(temp1, NULL);

		jbyte* temp2buff = NULL;
		temp2buff = env->GetByteArrayElements(temp2, NULL);

		if(!temp1buff || !temp2buff)
			return ret;

		int i_key_material = (int)key_material;
		int i_bClient = (int)bClient;
	
		struct CRYPTO::crypto_cipher* ctx = NULL;
		ctx = CRYPTO::crypto_cipher_init(e_alg, (const BYTE*)temp1buff, (const BYTE*)temp2buff, i_key_material);
		env->ReleaseByteArrayElements(temp1, temp1buff, 0);
		env->ReleaseByteArrayElements(temp2, temp2buff, 0);
		if(ctx == NULL)
			return ret;
	
		if(i_bClient == 1)
			tsipending_ctx_client = ctx;		
		else
			tsipending_ctx_server = ctx;

		ret = 0;
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
		ret = ARO_CRYPTO_ERROR;
    }

	return ret;
}

extern "C" JNIEXPORT void JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_cryptocipherdeinit
	(JNIEnv *env, jobject obj, jint objectType)
{
	__try
	{
		int i_objectType = (int)objectType;
		if(i_objectType == CTX_TSI_SERVER)
		{
			if(tsiserver_ctx_client)
			{
				CRYPTO::crypto_cipher_deinit(tsiserver_ctx_client);
				tsiserver_ctx_client = NULL;
			}
			if(tsiserver_ctx_server)
			{
				CRYPTO::crypto_cipher_deinit(tsiserver_ctx_server);
				tsiserver_ctx_server = NULL;
			}
		}
		else if(i_objectType == CTX_TSI_CLIENT)
		{
			if(tsiclient_ctx_client)
			{
				CRYPTO::crypto_cipher_deinit(tsiclient_ctx_client);
				tsiclient_ctx_client = NULL;
			}
			if(tsiclient_ctx_server)
			{
				CRYPTO::crypto_cipher_deinit(tsiclient_ctx_server);
				tsiclient_ctx_server = NULL;
			}
		}
		else if(i_objectType == CTX_TSI_PENDING)
		{
			if(tsipending_ctx_client)
			{
				CRYPTO::crypto_cipher_deinit(tsipending_ctx_client);
				tsipending_ctx_client = NULL;
			}
			if(tsipending_ctx_server)
			{
				CRYPTO::crypto_cipher_deinit(tsipending_ctx_server);
				tsipending_ctx_server = NULL;
			}
		}
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_setcryptociphernull
	(JNIEnv *env, jobject obj, jint objectType, jint bClient)
{
	__try
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
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
    }
}

extern "C" JNIEXPORT void JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_copycryptocipher
	(JNIEnv *env, jobject obj, jint from, jint to)
{
	__try
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
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
    }
}

extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_AROCryptoAdapter_tlsprf
	(JNIEnv *env, jobject obj, jbyteArray preMaster, jint preMasterLen, jstring label, jbyteArray random, jint randomLen, jbyteArray master, jint masterSecretLen)
{
	int ret = ARO_CRYPTO_ERROR;
	__try
	{
		jbyte* preMasterbuff = NULL;
		preMasterbuff = env->GetByteArrayElements(preMaster, NULL);

		jbyte* randombuff = NULL;
		randombuff = env->GetByteArrayElements(random, NULL);

		jbyte* masterbuff = NULL;
		masterbuff = env->GetByteArrayElements(master, NULL);

		if(!preMasterbuff || !randombuff || !masterbuff)
			return ARO_CRYPTO_ERROR;

		wchar_t* szlabel = JavaToWSZ(env, label);
		if(NULL == szlabel)
			return ARO_CRYPTO_ERROR;

		int strSize = lstrlenW(szlabel);
		char cszlabel[32] = "";
		for(int i=0; i<strSize; i++)
		{
			cszlabel[i] = (char)szlabel[i];
		}
		cszlabel[strSize] = '\0';

		ret = CRYPTO::tls_prf((const BYTE*)preMasterbuff, preMasterLen, cszlabel, (const BYTE*)randombuff, randomLen, (BYTE*)masterbuff, masterSecretLen);
	
		env->ReleaseByteArrayElements(preMaster, preMasterbuff, 0);
		env->ReleaseByteArrayElements(random, randombuff, 0);
		env->ReleaseByteArrayElements(master, masterbuff, 0);
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
		ret = ARO_CRYPTO_ERROR;
    }

	return ret;
}

/*
Desc: This method converts jstring to wchar_t format.
Param: JNIEnv* env, jstring string
Return: wchar_t*
*/
wchar_t* JavaToWSZ(JNIEnv* env, jstring string) 
{   
	wchar_t* wsz = NULL;
	__try
	{
		if (string == NULL)         
			return NULL;     
	
		const char* raw = env->GetStringUTFChars(string, NULL);
		if (raw == NULL)         
			return NULL;     
		
		size_t convertedChars = 0;
		mbstowcs_s(&convertedChars, NULL, 0, raw, INT_MAX);

		if (convertedChars < 0) {
			env->ReleaseStringUTFChars(string, raw);      
			return NULL;
		}
		
		size_t len = 0;
		wsz = new wchar_t[convertedChars + 1];
		mbstowcs_s(&len, wsz, convertedChars + 1, raw, convertedChars + 1);
		env->ReleaseStringUTFChars(string, raw);
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
    }

	return wsz; 
}

jstring WSZToJava(JNIEnv* env, wchar_t* wsz) {
	jstring result = NULL;
	__try
	{
		if (wsz == NULL)
			return NULL;

		size_t convertedChars = 0;
		wcstombs_s(&convertedChars, NULL, 0, wsz, INT_MAX);
		if (convertedChars < 0)
			return NULL;
		
		size_t len = 0;
		char* raw = new char[convertedChars + 1];
		wcstombs_s(&len, raw, convertedChars + 1, wsz, convertedChars + 1);
		result = env->NewStringUTF(raw);
		delete[] raw;
	}
	__except(EXCEPTION_EXECUTE_HANDLER)
    {
    }

	return result;
}