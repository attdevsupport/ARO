// netmoncap.cpp : Defines the exported functions for the DLL application.
#include "objbase.h"
#include "time.h"
#include "ntddndis.h"
#include "NMApi.h"
#include "com_att_aro_pcap_NetmonAdapter.h"

#define NETMON_ERROR -100
#define NETMON_TRACE_FILE_LOAD_ERROR -101
#define PROCESS_NAME L"Conversation.ProcessName"

wchar_t* JavaToWSZ(JNIEnv* env, jstring string);
jstring WSZToJava(JNIEnv* env, wchar_t* wsz);
void ConvertFrameTimeToEpoch(UINT64 timestamp, ULONG& ulSeconds, ULONG& ulMicrosec);
void __stdcall ParserBuild(PVOID Context, ULONG StatusCode, LPCWSTR lpDescription, ULONG ErrorType);
void __stdcall SendFrameInfo(JNIEnv* env, jobject obj, ULONG ulMacType, ULONG seconds, ULONG microseconds, int ActFrameLength, BYTE* RawFrameBuf, wchar_t* appName);

wchar_t*
getProcessNameProp(HANDLE frameParser, ULONG ProcessNamePropID, int size)
{
    wchar_t* processName = new wchar_t[size];
    ULONG retlen;
    ULONG ret;
    NmPropertyValueType type;

    ret = NmGetPropertyValueById(frameParser, ProcessNamePropID, size, (PBYTE)processName, &retlen, &type);
    if(ret != ERROR_SUCCESS)
    {
        delete[] processName;
		if (ret == ERROR_INSUFFICIENT_BUFFER) {
			return getProcessNameProp(frameParser, ProcessNamePropID, (int) retlen);
		} else {
			return NULL;
		}
		return NULL;
    }
    else if(type == NmPropertyValueNone)
    {
        delete[] processName;
        return NULL;
    }
    else
    {
		return processName;
    }
}

/*
Desc: This method extracts the required information of .cap file.
Param: JNIEnv* env, jobject obj, jstring filename
Return: ULONG
*/
extern "C" JNIEXPORT jint JNICALL Java_com_att_aro_pcap_NetmonAdapter_parseTrace
  (JNIEnv *env, jobject obj, jstring filename){
	HINSTANCE LoadME = NULL;
	ULONG l_RetValue = NETMON_ERROR;
	HANDLE l_hwnd_CaptureFile = INVALID_HANDLE_VALUE;
	HANDLE l_hwnd_Parser = INVALID_HANDLE_VALUE;
	HANDLE l_hwnd_NplParser = INVALID_HANDLE_VALUE;
	HANDLE l_hwnd_ParserConfig = INVALID_HANDLE_VALUE;
	ULONG ProcessNamePropID;         // Global ID for Conversation.ProcessName.
	
	//Load and open the trace file
	wchar_t* szfilename = JavaToWSZ(env, filename);
	if(NULL == szfilename)
	{
		l_RetValue = NETMON_TRACE_FILE_LOAD_ERROR;
		return l_RetValue;
	}

	l_RetValue = NmOpenCaptureFile(szfilename, &l_hwnd_CaptureFile);
	delete[] szfilename;
	szfilename = NULL;

	if(l_RetValue == ERROR_SUCCESS)
	{
		// Initialize the parser engine and return a frame parser.			
		l_RetValue = NmLoadNplParser(NULL, NmAppendRegisteredNplSets, ParserBuild, 0, &l_hwnd_NplParser);
		if(ERROR_SUCCESS == l_RetValue)
		{
			//Create frame parser configuration
			l_RetValue = NmCreateFrameParserConfiguration(l_hwnd_NplParser, ParserBuild, 0, &l_hwnd_ParserConfig);
			if(ERROR_SUCCESS == l_RetValue)
			{

				// Conversations must be enabled before adding conversation properties.
				l_RetValue = NmConfigConversation(l_hwnd_ParserConfig, NmConversationOptionNone, TRUE);
				if(l_RetValue != ERROR_SUCCESS)
				{
					wprintf(L"Fail to enable conversation, error: %d\n", l_RetValue);
				}

				// Add properties.
				l_RetValue = NmAddProperty(l_hwnd_ParserConfig, PROCESS_NAME, &ProcessNamePropID);
				if(l_RetValue != ERROR_SUCCESS)
				{
					wprintf(L"Fail to add property %s, error: %d\n", PROCESS_NAME, l_RetValue);
				}

				//Create frame parser				
				l_RetValue = NmCreateFrameParser(l_hwnd_ParserConfig, &l_hwnd_Parser);
				if(ERROR_SUCCESS == l_RetValue)
				{
					ULONG l_ulFrameCount = 0;
					l_RetValue = NmGetFrameCount(l_hwnd_CaptureFile, &l_ulFrameCount);
					if(l_RetValue == ERROR_SUCCESS)
					{
						HANDLE l_hwndRawFrame = INVALID_HANDLE_VALUE;
						for(ULONG index = 0; index < l_ulFrameCount; index++)
						{
							HANDLE l_pParsedFrame = INVALID_HANDLE_VALUE;
							l_RetValue = NmGetFrame(l_hwnd_CaptureFile, index, &l_hwndRawFrame);
							if(l_RetValue == ERROR_SUCCESS)
							{
								ULONG RawFrameLength;
								l_RetValue = NmGetRawFrameLength(l_hwndRawFrame, &RawFrameLength);
							
								BYTE *RawFrameBuf = (BYTE *)malloc(RawFrameLength);
								ULONG ActFrameLength;
								l_RetValue = NmGetRawFrame(l_hwndRawFrame, RawFrameLength, RawFrameBuf, &ActFrameLength);

								// The last parameter is for API to return reassembled frame if enabled 
								// NULL means that API discards reassembled frame.
								l_RetValue = NmParseFrame(l_hwnd_Parser, l_hwndRawFrame, index, NmFieldFullNameRequired |
									NmContainingProtocolNameRequired |
									NmDataTypeNameRequired |
									NmFieldDisplayStringRequired |
									NmFrameConversationInfoRequired, &l_pParsedFrame, NULL);
								if(l_RetValue == ERROR_SUCCESS)
								{
									ULONG l_ulMacType = 0;
									l_RetValue = NmGetFrameMacType(l_pParsedFrame, &l_ulMacType);
									
									UINT64 timestamp;
									l_RetValue = NmGetFrameTimeStamp(l_pParsedFrame, &timestamp);
									
									// Release current parsed frame
									NmCloseHandle(l_pParsedFrame);
									
									ULONG l_ulSeconds, l_ulMicrosec;
									ConvertFrameTimeToEpoch(timestamp, l_ulSeconds, l_ulMicrosec);
									wchar_t* appName = getProcessNameProp(l_hwnd_Parser, ProcessNamePropID, 256);
									SendFrameInfo(env, obj, l_ulMacType, l_ulSeconds, l_ulMicrosec, ActFrameLength, RawFrameBuf, appName);
									delete[] appName;
								}							
								// Release current raw frame
								free(RawFrameBuf);
								NmCloseHandle(l_hwndRawFrame);
							}
						}
					}
					NmCloseHandle(l_hwnd_Parser);					
				}
				NmCloseHandle(l_hwnd_ParserConfig);
			}		
			NmCloseHandle(l_hwnd_NplParser);
		}
		NmCloseHandle(l_hwnd_CaptureFile);
	}
	 
	return ERROR_SUCCESS;
}

/*
Desc: This is parser compiler error callback function pointer.
Param: PVOID Context, ULONG StatusCode, LPCWSTR lpDescription, ULONG ErrorType
Return: void
*/
void __stdcall ParserBuild(PVOID Context, ULONG StatusCode, LPCWSTR lpDescription, ULONG ErrorType)
{
    wprintf(L"%s\n", lpDescription);
}

/*
Desc: This method sends the required information to ARO Analyzer application.
Param: JNIEnv* env, jobject obj, ULONG ulMacType, int seconds, int microseconds, int ActFrameLength, BYTE* RawFrameBuf
Return: void
*/
void __stdcall SendFrameInfo(JNIEnv* env, jobject obj, ULONG ulMacType, ULONG seconds, ULONG microseconds, int ActFrameLength, BYTE* RawFrameBuf, wchar_t* appName)
{
	jclass cls;
	jmethodID mid;
	jbyteArray dataArray;

	cls = env->GetObjectClass(obj);
	mid = env->GetMethodID(cls, "pcapHandler", "(IJJI[BLjava/lang/String;)V");
	if(mid != NULL) 
	{
		dataArray=env->NewByteArray((jsize)ActFrameLength);
		env->SetByteArrayRegion(dataArray, 0, (jsize)ActFrameLength, (const jbyte*)RawFrameBuf);
		env->CallVoidMethod(obj, mid, (jint)ulMacType, (jlong)seconds, (jlong)microseconds, (jint)ActFrameLength, dataArray, WSZToJava(env, appName));
		env->DeleteLocalRef(dataArray);
	}
}

/*
Desc: This method converts jstring to wchar_t format.
Param: JNIEnv* env, jstring string
Return: wchar_t*
*/
wchar_t* JavaToWSZ(JNIEnv* env, jstring string) 
{     
	if (string == NULL)         
		return NULL;     
	
	const char* raw = env->GetStringUTFChars(string, NULL);
	if (raw == NULL)         
		return NULL;     
	size_t len = mbstowcs(NULL, raw, INT_MAX);
	if (len < 0) {
		env->ReleaseStringUTFChars(string, raw);      
		return NULL;
	}
	wchar_t* wsz = new wchar_t[len+1];
	mbstowcs(wsz, raw, len + 1);
	env->ReleaseStringUTFChars(string, raw);      

	return wsz; 
}

jstring WSZToJava(JNIEnv* env, wchar_t* wsz) {
	if (wsz == NULL)
		return NULL;

	size_t len = wcstombs(NULL, wsz, INT_MAX);
	if (len < 0)
		return NULL;
	char* raw = new char[len + 1];
	wcstombs(raw, wsz, len + 1);
	jstring result = env->NewStringUTF(raw);
	delete[] raw;
	return result;
}

/*
Desc: This method converts frame time to epoch time format.
Param: UINT64 timestamp, ULONG& ulSeconds, ULONG& ulMicrosec
Return: void
*/
void ConvertFrameTimeToEpoch(UINT64 timestamp, ULONG& ulSeconds, ULONG& ulMicrosec)
{
	FILETIME ft;
	struct tm l_time;
	time_t t_of_day;

	ft.dwHighDateTime = timestamp >> 32;
	ft.dwLowDateTime = timestamp & 0xFFFFFFFFL;
	SYSTEMTIME l_SystemTime;
	FileTimeToSystemTime(&ft, &l_SystemTime);									
	l_time.tm_hour= l_SystemTime.wHour;
	l_time.tm_min = l_SystemTime.wMinute;
	l_time.tm_sec = l_SystemTime.wSecond;
	l_time.tm_year = l_SystemTime.wYear - 1900;
	l_time.tm_mon = l_SystemTime.wMonth - 1;
	l_time.tm_mday = l_SystemTime.wDay;
	l_time.tm_isdst = -1;
	t_of_day = mktime(&l_time);

	ulSeconds = (ULONG) t_of_day;
	ulMicrosec = (ULONG) l_SystemTime.wMilliseconds * 1000;
}