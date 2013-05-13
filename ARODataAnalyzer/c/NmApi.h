#pragma once
#ifndef __NM_API_HEADER__
#define __NM_API_HEADER__

#ifndef MAC_ADDRESS_SIZE
#define MAC_ADDRESS_SIZE 6
#endif

#ifndef NM_STATUS_API_VERSION_MISMATCHED
#define NM_STATUS_API_VERSION_MISMATCHED 0xE111000CL
#endif

const USHORT NMAPI_MAC_ADDRESS_SIZE = MAC_ADDRESS_SIZE;
const USHORT NMAPI_GUID_SIZE = 16;

///
/// <summary><c>NmCaptureFileMode</c>Network Monitor capture file expansion mode</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmCaptureFileMode : UINT32
{
    NmCaptureFileWrapAround,
    NmCaptureFileChain,
    NmCaptureFileLastFlag

} NmCaptureFileMode;

///
/// <summary><c>NmCaptureCallbackExitMode</c>Capture callback function exit mode</summary> 
/// <remarks>
///     NmCaptureStopAndDiscard - NmStopCapture/NmPauseCapture returns immediately user's capture callback function will not be called after
///                               NmStopCapture/NmPauseCapture returns
/// </remarks>
typedef enum _NmCaptureCallbackExitMode : UINT32
{
    NmDiscardRemainFrames = 1,
    NmReturnRemainFrames = 2,

} NmCaptureCallbackExitMode;

///
/// <summary><c>NmAdapterOpState</c>Network Monitor driver adapter operational states</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmAdapterOpState
{
    NmAdapterStateNone,
    NmAdapterStateBound,
    NmAdapterStateStopped,
    NmAdapterStateCapturing,
    NmAdapterStatePaused,

} NmAdapterOpState;

///
/// <summary><c>NmAdapterCaptureMode</c>Network Monitor driver adapter capture mode</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmAdapterCaptureMode
{
    NmLocalOnly,
    NmPromiscuous

} NmAdapterCaptureMode;

///
/// <summary><c>NM_FRAME_CALLBACK</c>Callback function definition for frame capturing</summary> 
/// <remarks>
/// The user function must use __stdcall calling convention.
/// </remarks>
typedef VOID (CALLBACK *NM_FRAME_CALLBACK)(HANDLE hCaptureEngine,
                                           ULONG  ulAdapterIndex,
                                           PVOID  pCallerContext,
                                           HANDLE hFrame);

///
/// <summary><c>NM_NPL_PARSER_CALLBACK</c>Callback function definition for parser compile, frame parsing process status</summary> 
/// <remarks>
/// The user function must use __stdcall calling convention.
/// </remarks>
typedef VOID (CALLBACK *NM_NPL_PARSER_CALLBACK)(PVOID pCallerContext, 
                                                ULONG dwStatusCode, 
                                                LPCWSTR lpDescription, 
                                                ULONG ulType);

///
/// <summary><c>NmCallbackMsgType</c>Status levels of the call back message</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmCallbackMsgType
{
    NmApiCallBackMsgTypeNone,
    NmApiCallBackMsgTypeError,
    NmApiCallBackMsgTypeWarning,
    NmApiCallBackMsgTypeInformation,
    NmApiCallBackMsgTypeLast

} NmCallbackMsgType;

///
/// <summary><c>NmNplParserLoadingOption</c>NPL loading option</summary> 
/// <remarks>
/// By default the NmLoadNplOptionNone is used.  Only the user specified NPL path(s) are loaded.
/// If both NmAppendRegisteredNplSets and a NPL path are specified, the resulting NPL parser will include
/// Both and the specified NPL path(s) are prefixed.
/// </remarks>
typedef enum _NmNplParserLoadingOption
{
    NmLoadNplOptionNone,
    NmAppendRegisteredNplSets

} NmNplParserLoadingOption;

///
/// <summary><c>NmFrameParserOptimizeOption</c>Frame parser optimization options</summary> 
/// <remarks>
/// Options used when create frame parser.
/// </remarks>
typedef enum _NmFrameParserOptimizeOption
{
    ///
    /// Create frame parser without optimization according to the added filter
    ///
    NmParserOptimizeNone = 0,
    ///
    /// Create frame parser optimized based on added filters, fields and properties
    ///
    NmParserOptimizeFull = 1,

    NmParserOptimizeLast

} NmFrameParserOptimizeOption;

///
/// <summary><c>NmFrameParsingOption</c>Frame parser parsing options</summary> 
/// <remarks>
/// Options used by NmParseFrame function.
/// </remarks>
typedef enum _NmFrameParsingOption : UINT32
{
    NmParsingOptionNone = 0,
    ///
    /// Provide full path name of the current field if specified
    ///
    NmFieldFullNameRequired = 1,
    ///
    /// Provide the name of the protocol that contains the current field if specified
    ///
    NmContainingProtocolNameRequired = 2,
    ///
    /// Provide data type name of the current field
    ///
    NmDataTypeNameRequired = 4,
    ///
    /// Use caller specified frame number
    ///
    NmUseFrameNumberParameter = 8,
    ///
    /// Provide the display string of the field
    ///
    NmFieldDisplayStringRequired = 16,
    ///
    /// Provide the frame conversation information
    ///
    NmFrameConversationInfoRequired = 32,

    NmParsingOptionLast

} NmFrameParsingOption;

///
/// <summary><c>FRAME_PARSING_OPTION_MAX</c>The maximum value NmFrameParsingOption can be</summary> 
/// <remarks>
/// If there are n flags defined, The NmParsingOptionLast is 2^n - 1.  If n = 5, the constant is 63.
/// </remarks>
const UINT32 FRAME_PARSING_OPTION_MAX = ((NmParsingOptionLast - 1) * 2 - 1);

///
/// <summary><c>NmConversationOption</c>Frame parser conversation configuration options</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmConversationConfigOption : UINT32
{
    NmConversationOptionNone,
    NmConversationOptionLast

} NmConversationOption;

///
/// <summary><c>NmReassemblyOption</c>Frame parser reassembly configuration options</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmReassemblyConfigOption : UINT32
{
    NmReassemblyOptionNone,
    NmReassemblyOptionLast

} NmReassemblyOption;
                          
///
/// <summary><c>NmFrameFragmentationType</c>Fragmentation types returned in parsed frames</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmFrameFragmentationType
{
    FragmentTypeNone,
    FragmentTypeStart,
    FragmentTypeMiddle,
    FragmentTypeEnd

} NmFrameFragmentationType;

/// <summary><c>NmParsedFieldProperty</c>The name string properties in parsed field</summary> 
/// <remarks>
/// </remarks>
typedef enum _NmParsedFieldNames
{
    NmFieldNamePath,
    NmFieldDataTypeName,
    NmFieldContainingProtocolName,
    NmFieldDisplayString

} NmParsedFieldNames;

///
/// <summary><c>NmMvsKeyType</c>Key types of the multi-value storage property</summary> 
/// <remarks>
/// The NmMvsKeyTypeNumber, NmMvsKeyTypeByteArray, NmMvsKeyTypeByteArray are the key types for retrieving the
/// Multi-value storage properties.
/// 
/// The NmMvsKeyTypeArrayIndex type is used as index value when retrieve property in the group property array.
/// </remarks>
typedef enum _NmMvsKeyType
{
    NmMvsKeyTypeNone,
    NmMvsKeyTypeNumber,
    NmMvsKeyTypeString,
    NmMvsKeyTypeByteArray,
    NmMvsKeyTypeArrayIndex,
    NmMvsKeyTypeLast

} NmMvsKeyType;

///
/// <summary>
/// <c>NmPropertyScope</c>
/// Scopes of properties.  It is reported in the property info.
/// </summary> 
/// <remarks>
/// </remarks>
///
typedef enum _NmPropertyScope
{
    NmPropertyScopeNone = 0,
    NmPropertyScopeConversation = 1,
    NmPropertyScopeGlobal = 2,
    NmPropertyScopeFrame = 4

} NmPropertyScope;

///
/// <summary>
/// <c>NmPropertyContainerType</c>
/// The property aggregation form.  
/// The regular form is a single value of NmPropertyValueType that can be addressed by just property name.
/// The multi-value storage is a set of properties that share the same name but different keys.
/// The array is a set of properties that stored in array and retrieved by name and index.
/// An array property is in the regular form if there is only one element.  The NmGetPropertyInfo may return
/// Container type NmPropertyContainerTypeValue if the key is not added along in NmAddProperty.
/// </summary> 
/// <remarks>
/// </remarks>
///
typedef enum _NmPropertyContainerType
{
    NmPropertyContainerTypeNone = 0,
    NmPropertyContainerTypeValue,
    NmPropertyContainerTypeMvs,
    NmPropertyContainerTypeArray

} NmPropertyContainerType;

///
/// <summary>
/// <c>NmPropertyValueType</c>
/// Type of the property value.
/// </summary> 
/// <remarks>
/// Number value is in signed or unsigned integer format
/// String value is in wide char format
/// Byte Blob is in byte array
///
/// The value type of properties, in the same multi value storage property addressed 
/// By different keys or in the same property group by different indexes,
/// Can be different.
/// </remarks>
///
typedef enum _NmPropertyValueType
{
    NmPropertyValueNone,
    NmPropertyValueSignedNumber,
    NmPropertyValueUnsignedNumber,
    NmPropertyValueString,
    NmPropertyValueByteBlob

} NmPropertyValueType, *PNmPropertyValueType;

///
/// <summary><c>NM_CAPTURE_STATISTICS</c></summary> 
/// <remarks>
/// The statistics is per adapter in API.  To get the totals for the whole engine, caller can
/// sum up all capturing adapters.
/// The EngineDropCount represents dropped frames caused by capture engine in user mode for each adapter.
/// </remarks>
typedef struct _NM_CAPTURE_STATISTICS
{
    USHORT Size;
    /// The frame drop count in driver
    UINT64 DriverDropCount;
    /// The count of frames that are filtered by driver.
    UINT64 DriverFilteredCount;
    /// The count of frames that have be seen in driver.
    UINT64 DriverSeenCount;
    /// The frame drop count in capture engine.
    UINT64 EngineDropCount;

}  NM_CAPTURE_STATISTICS,*PNM_CAPTURE_STATISTICS;

///
/// <summary><c>NM_PROPERTY_STORAGE_KEY</c></summary> 
/// <remarks>
/// NM_PROPERTY_STORAGE_KEY is used with property name or id obtained from NmAddProperty.
/// KeyNumber, KeyString and KeyBuffer are for Multi-Value storage.  The KeyLength is required for KeyBuffer.
/// ArrayIndex is for specifying the array index of the property group specified by NmMvsKeyTypeArrayIndex.
/// </remarks>
typedef struct _NM_PROPERTY_STORAGE_KEY
{
    USHORT          Size;
    ULONG           KeyLength;
    NmMvsKeyType    KeyType;
    union
    {
        /// The numeric key for the property in the multi value storage
        UINT64      KeyNumber;
        /// The wide string key for the property in the multi value storage.  It must be null terminated.
        LPWSTR      KeyString;
        /// The key in the form of byte array for the property in the multi value storage
        PBYTE       KeyBuffer;
        /// The index for the property in the property group
        UINT64      ArrayIndex;
    } Key;

} NM_PROPERTY_STORAGE_KEY, *PNM_PROPERTY_STORAGE_KEY;

///
/// Contains runtime information for instantiated properties
///
typedef struct _NM_PROPERTY_INFO
{
    /// For version control
    USHORT Size;
    /// Property Scope
    NmPropertyScope Scope;
    /// Property container type, e.g., MVS, Array.
    NmPropertyContainerType ContainerType;
    /// The element count of the name excluding the terminator.
    /// When no name buffer is provided, i.e., Name parameter is NULL, NameSize is only used as output
    /// Parameter that returns the actual name length.
    /// When the name buffer is provided, the NameSize are both input and output parameters.
    /// The NameSize acts as a input parameter specifying the buffer length.  The name's actual length
    /// Is also returned by NmGetPropertyInfo.
    USHORT NameSize;
    /// Property string added by NmAddProperty.  If a buffer is assigned when call NmGetPropertyInfo, 
    /// The NameSize must specify the buffer length (in element count.)  NmGetPropertyInfo returns the 
    /// Property Name in the buffer if the buffer has enough space.  Or buffer over flow error is returned.
    LPWSTR Name;
    /// The data type of the property.  If the value type is string, the terminator is not included.
    NmPropertyValueType ValueType;
    /// The size of the value in BYTE.  If the value type is string, the terminator is excluded.
    ULONG ValueSize;
    /// number of items in Array.  The regular an multi-value storage properties have only one item
    ULONG ItemCount;

} NM_PROPERTY_INFO, *PNM_PROPERTY_INFO;

/// <summary><c>NM_FRAGMENTATION_INFO</c>Fragmentation information returned in parsed frames</summary> 
/// <remarks>
/// </remarks>
typedef struct _NM_FRAGMENTATION_INFO
{
    USHORT  Size;
    WCHAR   FragmentedProtocolName[MAX_PATH];
    WCHAR   PayloadProtocolName[MAX_PATH];
    NmFrameFragmentationType FragmentType;

} NM_FRAGMENTATION_INFO, *PNM_FRAGMENTATION_INFO;

///
/// <summary><c>NM_NIC_ADAPTER_INFO</c> Contain adapter information.</summary> 
/// <remarks>
/// </remarks>
///
typedef struct  _NM_NIC_ADAPTER_INFO
{
    USHORT                  Size;
    UCHAR                   PermanentAddr[MAC_ADDRESS_SIZE];
    UCHAR                   CurrentAddr[MAC_ADDRESS_SIZE];
    NDIS_MEDIUM             MediumType;
    NDIS_PHYSICAL_MEDIUM    PhysicalMediumType;
    WCHAR                   ConnectionName[MAX_PATH];
    WCHAR                   FriendlyName[MAX_PATH];
    WCHAR                   Guid[MAX_PATH];

    ///
    /// Network adapter operational state. Indicates if the network adapter is bound, capturing, pause or stopped
    ///
    NmAdapterOpState        OpState;
    ///
    /// Indicates if the network adapter is enabled or disabled. It only can be enabled if it is bound to the Network Monitor driver
    ///
    BOOL                    Enabled;
    BOOL                    PModeEnabled;

    ///
    /// Frame indication callback is assigned per adapter
    ///
    NM_FRAME_CALLBACK       CallBackFunction;

} NM_NIC_ADAPTER_INFO, *PNM_NIC_ADAPTER_INFO;

///
/// <summary><c>NM_API_CONFIGURATION</c>Contain all configurable API parameters</summary> 
/// <remarks>
/// </remarks>
///
typedef struct _NM_API_CONFIGURATION
{
    ///
    /// Caller sets the member 'Size' when pass in allocated buffer 
    ///
    USHORT  Size;

    ///
    /// Configurable limits that overwrite default API settings 
    ///
    ULONG RawFrameHandleCountLimit;
    ULONG ParsedFrameHandleCountLimit;
    ULONG CaptureEngineHandleCountLimit;
    ULONG NplParserHandleCountLimit;
    ULONG FrameParserConfigHandleCountLimit;
    ULONG FrameParserHandleCountLimit;
    ULONG CaptureFileHandleCountLimit;

    ///
    /// API threading mode for COM initialization.  Only support COINIT_MULTITHREADED and COINIT_APARTMENTTHREADED
    ///
    COINIT  ThreadingMode;

    ///
    /// Configurable default feature/behavior parameters
    ///
    NmConversationOption      ConversationOption;
    NmReassemblyOption        ReassemblyOption;
    NmCaptureFileMode         CaptureFileMode;
    NmFrameParsingOption      FrameParsingOption;
    NmCaptureCallbackExitMode CaptureCallbackExitMode;

    ///
    /// Hard limits the API enforce (not configurable)
    ///
    ULONG MaxCaptureFileSize;
    ULONG MinCaptureFileSize;
    
    /// Maximum number of handles per handle type  
    ULONG MaxApiHandleLimit;

} NM_API_CONFIGURATION, *PNM_API_CONFIGURATION;

///
/// <summary><c>NM_PROTOCOL_SEQUENCE_CONFIG</c>Data structure for API user to specify NPL properties and fields
///                                            For frame order correction support.
/// </summary> 
/// <remarks>
/// </remarks>
///
typedef struct _NM_PROTOCOL_SEQUENCE_CONFIG
{
    ///
    /// API verifies the member 'Size' against the size of its version.  They must match.
    ///
    USHORT Size;

    ///
    /// The names of the properties containing the values to form the key 
    /// to identify the group of the frames to get in order.  If multiple names are used,
    /// They are separated by semicolons.  The string must be NULL terminated.
    ///
    LPWSTR GroupKeyString;

    ///
    /// The name of the property containing the frame's sequence number.
    ///
    LPWSTR SequencePropertyString;

    ///
    /// The name of the property containing the frame's next sequence number.
    ///
    LPWSTR NextSequencePropertyString;

} NM_PROTOCOL_SEQUENCE_CONFIG, *PNM_PROTOCOL_SEQUENCE_CONFIG;

///
/// <summary><c>NM_OPEN_CAP_PARAMETER</c>Data structure for calling NmOpCaptureFileInOrder</summary>
/// 
/// <remarks>
/// </remarks>
///
typedef struct _NM_ORDER_PARSER_PARAMETER
{
    ///
    /// API verifies the member 'Size' against the size of its version.  They must match.
    ///
    USHORT Size;

    ///
    /// The frame parser used for handling out of order frames.  It must be built from a frame parser
    /// Configuration that has sequence information specified by NM_PROTOCOL_SEQUENCE_CONFIG.
    ///
    HANDLE FrameParser;

    ///
    /// For future option flags.
    ///
    ULONG Option;

} NM_ORDER_PARSER_PARAMETER, *PNM_ORDER_PARSER_PARAMETER;

///
/// <summary><c>NM_PARSED_DATA_FIELD</c> Return structure contains the parsed field.</summary> 
/// <remarks>
/// Returned to caller from NmGetParsedFieldInfo function
/// </remarks>
///
typedef struct _NM_PARSED_FIELD_INFO
{
    ///
    /// API verifies the member 'Size' against the size of its version.  They must match.
    ///
    USHORT   Size;
    USHORT  FieldIndent;
    ///
    /// The size of the string that holds the full path of the data field if the NmFrameParseOptions 
    /// NmFieldFullNameRequired is set, e.g., Frame.Ethernet.IPv4.SourceAddress;  Otherwise it is the size
    /// of the current field name only
    ///
    USHORT  NamePathLength;
    ///
    /// The size of the string that contains the name of the NPL data type if the NmFrameParseOptions 
    /// NmDataTypeNameRequired is set, e.g., L"UINT16";  Otherwise it is zero.
    ///
    USHORT  NplDataTypeNameLength;
    ///
    /// The size of the string that contains the protocol containing the field if the NmFrameParseOptions 
    /// NmContainingProtocolNameRequired is set;  Otherwise it is zero
    ///
    USHORT  ProtocolNameLength;
    ///
    /// The size of the display string of the field if the NmFrameParseOptions 
    /// NmFieldDisplayStringRequired is set;  Otherwise it is zero
    ///
    USHORT  DisplayStringLength;
    ///
    /// Offset in current protocol
    ///
    ULONG   ProtocolBitOffset;
    ///
    /// Field offset in frame
    ///
    ULONG   FrameBitOffset;
    ///
    /// Length of the field in bits
    ///
    ULONG   FieldBitLength;
    ///
    /// The variant type defined as in VARENUM
    ///
    USHORT  ValueType;
    ///
    /// The size of the buffer required to hold the field value represented in VARIANT struct including
    /// The length of the content if the VARIANT contains a pointer to ARRAY or string.
    ///
    USHORT  ValueBufferLength;

} NM_PARSED_FIELD_INFO, *PNM_PARSED_FIELD_INFO;

///
/// <summary><c>NM_PARSED_FIELD_INFO_EX</c> Return structure contains the parsed field.information</summary> 
/// <remarks>
/// Returned to caller from NmGetParsedFieldInfoEx function
/// </remarks>
///
typedef struct _NM_PARSED_FIELD_INFO_EX
{
    ///
    /// API verifies the member 'Size' against the size of its version.  They must match.
    ///
    USHORT   Size;
    USHORT  FieldIndent;
    ///
    /// The size of the string that holds the full path of the data field if the NmFrameParseOptions 
    /// NmFieldFullNameRequired is set, e.g., Frame.Ethernet.IPv4.SourceAddress;  Otherwise it is the size
    /// of the current field name only
    ///
    USHORT  NamePathLength;
    ///
    /// The size of the string that contains the name of the NPL data type if the NmFrameParseOptions 
    /// NmDataTypeNameRequired is set, e.g., L"UINT16";  Otherwise it is zero.
    ///
    USHORT  NplDataTypeNameLength;
    ///
    /// The size of the string that contains the protocol containing the field if the NmFrameParseOptions 
    /// NmContainingProtocolNameRequired is set;  Otherwise it is zero
    ///
    USHORT  ProtocolNameLength;
    ///
    /// The size of the display string of the field if the NmFrameParseOptions 
    /// NmFieldDisplayStringRequired is set;  Otherwise it is zero
    ///
    USHORT  DisplayStringLength;
    ///
    /// Offset in current protocol
    ///
    ULONG   ProtocolBitOffset;
    ///
    /// Field offset in frame
    ///
    ULONG   FrameBitOffset;
    ///
    /// Length of the field in bits
    ///
    ULONG   FieldBitLength;
    ///
    /// The variant type defined as in VARENUM
    ///
    USHORT  ValueType;
    ///
    /// The size of the buffer required to hold the field value represented in VARIANT struct including
    /// The length of the content if the VARIANT contains a pointer to ARRAY or string.
    ///
    ULONG  ValueBufferLength;

} NM_PARSED_FIELD_INFO_EX, *PNM_PARSED_FIELD_INFO_EX;

///
/// <summary><c>NM_TIME</c>Data structure for NmGetFrameTimeStampEx and NmBuildRawFrameFromBufferEx function</summary>
/// 
/// <remarks>
/// </remarks>
///
typedef struct _NM_TIME
{
    ///
    /// Size of structure that is set by the caller.
    ///
    DWORD NmTimeStampSize;
    ///
    /// UTC Time Stamp.
    ///
    FILETIME TimeStamp;
    ///
    /// TRUE if the time is originally UTC. FALSE if the time was local and converted to UTC.
    ///
    BOOL IsUTC;
    ///
    /// TRUE if the time zone information exists.
    ///
    BOOL HasTZI;
    ///
    /// Time zone information for the frame.
    /// If TRUE == isUTC and TRUE == hasTZI, TZI contains correct time zone information
    /// If TRUE == isUTC and FALSE == hasTZI, TZI does not contain time zone information
    /// if FALSE == isUTC and FALSE == hasTZI, Time stamp is originally local time and converted to UTC using LocalFileTimeToFileTime(). TZI does not contain time zone information.
    /// If FALSE == isUTC and TRUE == hasTZI, At the moment, there is no scenario to be this case.
    TIME_ZONE_INFORMATION TZI;
} NM_TIME, *PNM_TIME;

//////////////////////////////////////////////
///
///NmApi function declarations
///
//////////////////////////////////////////////
/// <summary><c>NmGetApiVersion</c>Query current version</summary> 
/// <remarks>
/// The API version matches Network Monitor engine version.
/// </remarks>
/// <example> This sample shows how to call the NmGetApiVersion method.
/// <code>
///     USHORT majorNumber = 0;
///     USHORT minorNumber = 0;
///     USHORT BuildNumber = 0;
///     USHORT RevisionNumber = 0;
///     NmGetApiVersion(&majorNumber, &minorNumber, &BuildNumber, &RevisionNumber);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="Major">[out] Major version number</param>
/// <param name="Minor">[out] Minor version number</param>
/// <param name="Build">[out] Build number</param>
/// <param name="Revision">[out] Revision number</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>Return nothing</returns>
extern "C" VOID WINAPI NmGetApiVersion(__out PUSHORT Major, __out PUSHORT Minor, __out PUSHORT Build, __out PUSHORT Revision);

/// <summary><c>NmGetApiConfiguration</c>Return current API configuration parameters</summary> 
/// <remarks>
/// Caller provides the non-null pointer of the NM_API_CONFIGURATION struct. 
/// </remarks>
/// <example> This sample shows how to call the NmGetApiConfiguration method.
/// <code>
///     NM_API_CONFIGURATION apiConfiguration;
///     apiConfiguration.Size = sizeof(NM_API_CONFIGURATION);
///     NmGetApiConfiguration(&apiConfiguration);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pConfiguration">[out] Struct pointer for API to fill</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     NM_API_VERSION_MISMATCHED: The version of NM_API_CONFIGURATION struct is different
///     ERROR_BAD_ARGUMENTS: pConfiguration is NULL.
/// </returns>
extern "C" ULONG WINAPI NmGetApiConfiguration(__in PNM_API_CONFIGURATION pConfiguration);

/// <summary><c>NmApiInitialize</c>Overwrite default configuration.</summary> 
/// <remarks>
/// Caller needs to provide storage for NM_API_CONFIGURATION struct.
/// </remarks>
/// <example> This sample shows how to call the NmApiInitialize method.
/// <code>
///     ULONG status = ERROR_SUCCESS;
///     NM_API_CONFIGURATION apiConfig;
///     apiConfig.Size = sizeof(NM_API_CONFIGURATION);
///     // specify all configuration parameters before call 
///     status = NmApiInitialize(&apiConfig);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ApiConfig">[in] Caller specified API configuration parameter struct</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     NM_API_VERSION_MISMATCHED: The version of NM_API_CONFIGURATION struct is different
///     ERROR_INVALID_STATE: Cannot change API configuration
/// </returns>
extern "C" ULONG WINAPI NmApiInitialize(__in_opt PNM_API_CONFIGURATION ApiConfig);

/// <summary><c>NmApiClose</c>Release API resources</summary> 
/// <remarks>
/// Should be called when done with the API
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
/// </returns>
extern "C" ULONG WINAPI NmApiClose();

/// <summary><c>NmOpenCaptureEngine</c>Open a capture engine</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="phCaptureEngine">[out] The returned handle to the capture engine object on success</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_INVALID_STATE:     The operation is not available.
///     ERROR_NOT_ENOUGH_MEMORY: Fail to allocate memory for the object.
/// </returns>
extern "C" ULONG WINAPI NmOpenCaptureEngine(__out PHANDLE phCaptureEngine);

/// <summary><c>NmGetAdapterCount</c>Return number of the adapters that the capture engine can access</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The capture engine under query</param>
/// <param name="pulCount">[out] The returned count of adapters</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found capture engine specified
/// </returns>
extern "C" ULONG WINAPI NmGetAdapterCount(__in HANDLE hCaptureEngine , __out PULONG pulCount);

/// <summary><c>NmGetAdapter</c>Get adapter information from the capture engine</summary> 
/// <remarks>
/// Caller can use name, GUID etc. to select adapter to use.  The adapter index should be within the 
/// Range returned by NmGetAdapterCount method.  Caller needs to provide the storage of the
/// NM_FRAME_CALLBACK struct.
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The handle of the capture engine object</param>
/// <param name="ulIndex">[in] The index number of the adapter to retrieve</param>
/// <param name="pNMAdapterInfo">[out] The returned adapter information struct</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: Either the capture engine or the adapter indicated by index is not found.
///     NM_API_VERSION_MISMATCHED: The version of NM_NIC_ADAPTER_INFO struct is different
/// </returns>
extern "C" ULONG WINAPI NmGetAdapter(__in HANDLE hCaptureEngine, __in ULONG ulIndex, __out PNM_NIC_ADAPTER_INFO pNMAdapterInfo);

/// <summary><c>NmConfigAdapter</c>Configure the adapter with the frame indication callback and the caller context.</summary> 
/// <remarks>
/// The current callback function and context will overwrite the previous ones.  The adapter index number
/// Must be in the range returned from NmGetAdapterCount method.
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The handle to the target capture engine</param>
/// <param name="ulIndex">[in] The index number of the target adapter</param>
/// <param name="CallbackFunction">[in] The frame indication callback function pointer to set</param>
/// <param name="pCallerContext">[in] The caller context pointer</param>
/// <param name="ExitMode">[in] The callback function exit mode</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_NOT_FOUND: Either the capture engine or the adapter is not found.
/// </returns>
extern "C" ULONG WINAPI NmConfigAdapter(__in HANDLE hCaptureEngine,
                                        __in ULONG ulIndex, 
                                        __in NM_FRAME_CALLBACK CallbackFunction, 
                                        __in PVOID pCallerContext, 
                                        __in NmCaptureCallbackExitMode ExitMode = NmDiscardRemainFrames);

/// <summary><c>NmStartCapture</c>Start capture on the specified capture engine and adapter</summary> 
/// <remarks>
/// Capture mode can be PMODE and LocalOnly.
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The handle to the target capture engine</param>
/// <param name="ulAdapterIndex">[in] The index number of the target adapter</param>
/// <param name="CaptureMode">[in] The capture mode, PMODE or LOCAL_ONLY</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_NOT_FOUND: not found capture engine or adapter specified
///     ERROR_INVALID_STATE: Cannot pause at current state
/// </returns>
extern "C" ULONG WINAPI NmStartCapture(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex, __in NmAdapterCaptureMode CaptureMode);

/// <summary><c>NmPauseCapture</c>Pause the capture</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The handle to the target capture engine</param>
/// <param name="ulAdapterIndex">[in] The index number of the target adapter</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_INVALID_STATE: Cannot pause at current state
///     ERROR_NOT_FOUND: not found capture engine or adapter specified
/// </returns>
extern "C" ULONG WINAPI NmPauseCapture(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex);

/// <summary><c>NmResumeCapture</c>Resume the capture that is previously paused</summary> 
/// <remarks>
/// Cannot resume after NmStopCapture is called.  The frame indication callback is no longer invoked 
/// Until NmResumeCapture method is called
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The handle to the target capture engine</param>
/// <param name="ulAdapterIndex">[in] The index number of the target adapter</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_INVALID_STATE: Cannot resume at current state
///     ERROR_NOT_FOUND: not found capture engine or adapter specified
/// </returns>
extern "C" ULONG WINAPI NmResumeCapture(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex);

/// <summary><c>NmStopCapture</c>Stop capture on given capture engine and adapter</summary> 
/// <remarks>
/// The frame indication callback is no longer invoked.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The handle to the target capture engine</param>
/// <param name="ulAdapterIndex">[in] The index number of the target adapter</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_INVALID_STATE: Cannot stop at current state
///     ERROR_NOT_FOUND: not found capture engine or adapter specified
/// </returns>
extern "C" ULONG WINAPI NmStopCapture(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex);

//////////////////////////////////////////////////////
/// Parsing functions
//////////////////////////////////////////////////////

/// <summary><c>NmLoadNplParser</c>Load NPL scripts and create NPL parser</summary> 
/// <remarks>
/// Callback function is invoked for compile error/warning/info.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pFileName">[in] The start parser script file name</param>
/// <param name="ulFlags">[in] Option flags</param>
/// <param name="CallbackFunction">[in] The parser compiler error callback function pointer</param>
/// <param name="pCallerContext">[in] The caller context pointer that will be passed back to the callback function</param>
/// <param name="phNplParser">[Out] The returned handle to the NPL parser object</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: Fail to create NPL parser object
/// </returns>
extern "C" ULONG WINAPI NmLoadNplParser(__in LPCWSTR pFileName,
                                        __in NmNplParserLoadingOption ulFlags,
                                        __in NM_NPL_PARSER_CALLBACK CallbackFunction,
                                        __in PVOID pCallerContext,
                                        __out PHANDLE phNplParser);

/// <summary><c>NmCreateFrameParserConfiguration</c>Create frame parser configuration that contains the filter and field configuration</summary> 
/// <remarks>
/// All the frame parser features including conversation and reassembly must be added in the configuration before creating the frame parser.
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hNplParser">[in] The handle of the NPL parser used for the frame parser</param>
/// <param name="CallbackFunction">[in] The compiler error callback function pointer</param>
/// <param name="pCallerContext">[in] The caller context pointer that will be passed back to the callback function</param>
/// <param name="phFrameParserConfiguration">[out] The returned handle of the frame parser configuration object</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: Fail to create frame parser configuration object.
///     ERROR_NOT_FOUND: not found specified NPL parser
///     
/// </returns>
extern "C" ULONG WINAPI NmCreateFrameParserConfiguration(__in HANDLE hNplParser,
                                                         __in NM_NPL_PARSER_CALLBACK CallbackFunction,
                                                         __in PVOID pCallerContext,
                                                         __out PHANDLE phFrameParserConfiguration);

/// <summary><c>NmAddFilter</c>Add filter for optimizing frame parser</summary> 
/// <remarks>
/// The filter id is used to evaluate the state of the filter on a parsed frame.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the target frame parser configuration object</param>
/// <param name="pFilterString">[in] The text of the filter</param>
/// <param name="pulFilterId">[out] The returned filter index in the frame parser</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
/// </returns>
extern "C" ULONG WINAPI NmAddFilter(__in HANDLE hFrameParserConfiguration, __in LPCWSTR pFilterString, __out PULONG pulFilterId);

/// <summary><c>NmAddField</c>Add field for optimizing frame parser</summary> 
/// <remarks>
/// All the fields are enumerated in the parsed frame if no field is added.  The field id is used to retrieve the field 
/// In the parsed frame.  Caller needs to provide unique fully qualified field name, e.g., TCP.Option.Ack.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the target frame parser configuration object</param>
/// <param name="pFilterString">[in] The text of the field</param>
/// <param name="pulFilterId">[out] The returned field index in the frame parser</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
/// </returns>
extern "C" ULONG WINAPI NmAddField(__in HANDLE hFrameParserConfiguration, __in LPCWSTR pFqfnString, __out PULONG pulFieldId);

/// <summary><c>NmAddProperty</c>Add a property to the configuration.</summary> 
/// <remarks>
/// The property name should have scope prefix such as Conversation, Global, etc.  If not specified, 
/// The frame property is the default scope.
/// The multi-value storage property name can contain the key, e.g., Property.myMvs$[key] as in NPL parser.  The key can be number, string or other property name.
/// The array property name can contain the index, e.g., Global.myArray[3] as in NPL parser.
/// </remarks>
/// <example> This sample shows how to call the NmAddProperty method.
/// <code>
///     HANDLE hFrameParserConfiguration;
///     ULONG myPropID;
///     NmAddProperty(hFrameParserConfiguration, L"Property.TCPPayloadLength", &myPropID);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] Frame Parser Configuration Handle</param>
/// <param name="pPropertyString">[in] Fully qualified name of the property.</param>
/// <param name="pulPropertyId">[out] Returned ID used to reference the property.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
///     ERROR_INVALID_PARAMETER: The specified property name is invalid
/// </returns>
extern "C" ULONG WINAPI NmAddProperty(__in HANDLE hFrameParserConfiguration, __in LPCWSTR pPropertyString, __out PULONG pulPropertyId);

/// <summary><c>NmAddSequenceOrderConfig</c>Add protocol sequence order configurations</summary> 
/// <remarks>
/// The protocol sequence configuration includes the properties that define the related protocols' sequence control data such
/// As TCP sequence number.
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfig">[in] Caller provided sequence configuration data</param>
/// <param name="pSeqConfig">[in] Caller provided sequence configuration data</param>
/// <param name="pulConfigId">[out] The retrieval ID of the configuration added to the frame parser configuration</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: Fail to allocate memory to store the configuration.
/// </returns>
extern "C" ULONG WINAPI NmAddSequenceOrderConfig(__in HANDLE hFrameParserConfig,
                                                 __in PNM_PROTOCOL_SEQUENCE_CONFIG pSeqConfig,
                                                 __out PULONG pulConfigId);

/// <summary><c>NmConfigReassembly</c>Enable or disable reassembly</summary> 
/// <remarks>
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the target frame parser configuration object</param>
/// <param name="Option">[in] Reassembly options</param>
/// <param name="bEnable">[in] Action to take, enable or disable</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
/// </returns>
extern "C" ULONG WINAPI NmConfigReassembly(__in HANDLE hFrameParserConfiguration, __in NmReassemblyOption Option, __in BOOL bEnable);

/// <summary><c>NmConfigConversation</c>Configure conversation options</summary> 
/// <remarks>
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the target frame parser configuration object</param>
/// <param name="Option">[in] conversation configuration options</param>
/// <param name="bEnable">[in] Action to take, enable or disable</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
/// </returns>
extern "C" ULONG WINAPI NmConfigConversation(__in HANDLE hFrameParserConfiguration, __in NmConversationOption Option, __in BOOL bEnable);

/// <summary><c>NmConfigStartDataType</c>Configure start data type</summary> 
/// <remarks>
/// By default, the frame parser starts parsing a frame from the Network Monitor built-in protocol "Frame".
/// This function lets the caller set the data type to start at.  This is useful for parsing an arbitrary
/// Data buffer with a frame parser starting from the data type that is configured with this function.
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the target frame parser configuration object</param>
/// <param name="pStartDataTypeName">[in] The name of the data type that the created frame parser starts with</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
///     ERROR_INSUFFICIENT_BUFFER: The given start type name is longer than 260 characters.
/// </returns>
extern "C" ULONG WINAPI NmConfigStartDataType(__in HANDLE hFrameParserConfiguration, __in LPCWSTR pStartDataTypeName);

/// <summary><c>NmGetStartDataType</c>Return the start data type of the given frame parser configuration</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the target frame parser configuration object</param>
/// <param name="ulBufferLength">[in] Caller buffer length</param>
/// <param name="pStartDataTypeName">[out] Caller buffer to hold the name of the data type that the created frame parser starts with</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
///     ERROR_INSUFFICIENT_BUFFER: The truncated name is in provided buffer
/// </returns>
extern "C" ULONG WINAPI NmGetStartDataType(__in HANDLE hFrameParserConfiguration,
                                           __in ULONG ulBufferELength,
                                           __out_ecount(ulBufferELength) LPWSTR pStartDataTypeName);

/// <summary><c>NmCreateFrameParser</c>Create frame parser from given configuration</summary> 
/// <remarks>
/// The optimization option is set to NmParserOptimizeNone by default that no optimization is applied.
/// The existing native applications do not need to recompile.  The new application can take advantage of this flag to 
/// Force optimization in the scenario where no field is added.  Without this option, the caller can only get a non-optimized 
/// Parser so that all the fields are included in the parsed frame.  With this option, an optimized frame parser can be
/// generated to serve the dedicated filtering scenarios.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParserConfiguration">[in] The handle of the source frame parser configuration object</param>
/// <param name="phFrameParser">[out] The returned handle of the frame parser</param>
/// <param name="OptimizeOption">[in] The optimization flag for creating the frame parser</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser configuration
/// </returns>
extern "C" ULONG WINAPI NmCreateFrameParser(__in HANDLE hFrameParserConfiguration,
                                            __out PHANDLE phFrameParser,
                                            __in NmFrameParserOptimizeOption OptimizeOption = NmParserOptimizeNone);

/// <summary><c>NmParseFrame</c>Parse the raw Network Monitor frame and return it in parsed format</summary> 
/// <remarks>
/// The parsed frame contains the frame information, filter state and enumeration of fields.  When reassembly is
/// Enabled, the last fragment of the payload completing the reassembly process and insert the reassembled raw frame.
/// The ulFrameNumber parameter is for conversation or global properties if using frame number as the key.  If the same
/// Frame number is used for different frames, the properties' values may be overwritten by the last instance.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] The handle of the frame parser used to parse the given raw frame object</param>
/// <param name="hRawFrame">[in] The handle of the target raw frame to parser</param>
/// <param name="ulFrameNumber">[in] The frame number should be used in parsing process if enabled by option flag</param>
/// <param name="ulOption">[in] See flag definition NmFrameParsingOption</param>
/// <param name="phParsedFrame">[out] The handle to the result parsed frame object</param>
/// <param name="InsertedRawFrameCount">[out] the handle of the inserted raw frame object as the result of parsing, e.g., reassembly</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser or raw frame
/// </returns>
extern "C" ULONG WINAPI NmParseFrame(__in HANDLE hFrameParser,
                                     __in HANDLE hRawFrame,
                                     __in ULONG ulFrameNumber,
                                     __in ULONG ulOption,
                                     __out PHANDLE phParsedFrame,
                                     __out PHANDLE phInsertedRawFrame);

/// <summary><c>NmParseBuffer</c>Parse the given data buffer and return it in parsed format</summary> 
/// <remarks>
/// The data buffer contains the byte array that can be a raw frame, part of raw frame or any arbitrary data.
/// The parsed frame contains the fabricated frame information. The filter state and enumeration of field are supported.
/// The inter frame reassembly is not supported since it requires multiple frames and conversation that are 
/// Not available in buffer mode.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] The handle of the frame parser used to parse the Given frame object</param>
/// <param name="pDataBuffer">[in] The pointer to the target data buffer</param>
/// <param name="ulBufferLength">[in] length of the data buffer in previous parameter</param>
/// <param name="ulFrameNumber">[in] The frame number should be used in parsing process if enabled by option flag</param>
/// <param name="ulOption">[in] See flag definition NmFrameParsingOption</param>
/// <param name="phParsedFrame">[out] The handle to the result parsed frame object</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser or raw frame
/// </returns>
extern "C" ULONG WINAPI NmParseBuffer(__in HANDLE hFrameParser,
                                      __in_ecount(ulBufferLength) PBYTE pDataBuffer,
                                      __in ULONG ulBufferLength,
                                      __in ULONG ulFrameNumber,
                                      __in ULONG ulOption,
                                      __out PHANDLE phParsedFrame);

/// <summary><c>NmBuildRawFrameFromBuffer</c>Build a raw frame using a given data buffer</summary> 
/// <remarks>
/// The data buffer is transformed to a raw frame object.  The media type, time stamp are optional.  Their default
/// Values are zero.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pDataBuffer">[in] The pointer to the target data buffer</param>
/// <param name="ulBufferLength">[in] length of the data buffer in previous parameter</param>
/// <param name="ulMedia">[in] Media type of the target raw frame</param>
/// <param name="ullTimeStamp">[in] Capture time stamp of the target raw frame</param>
/// <param name="phRawFrame">[out] The handle to the result parsed frame object</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: No space to build the new frame
/// </returns>
extern "C" ULONG WINAPI NmBuildRawFrameFromBuffer(
    __in_ecount(ulBufferLength) PBYTE pDataBuffer,
    __in ULONG ulBufferLength,
    __in ULONG ulMedia,
    __in UINT64 ullTimeStamp,
    __out PHANDLE phRawFrame);

/// <summary><c>NmBuildRawFrameFromBufferEx</c>Build a raw frame using a given data buffer</summary> 
/// <remarks>
/// Same as NmBuildRawFrameFromBuffer but stores time zone information
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pDataBuffer">[in] The pointer to the target data buffer</param>
/// <param name="ulBufferLength">[in] length of the data buffer in previous parameter</param>
/// <param name="ulMedia">[in] Media type of the target raw frame</param>
/// <param name="pTime">[in] Capture time information of the target raw frame</param>
/// <param name="phRawFrame">[out] The handle to the result parsed frame object</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: No space to build the new frame
/// </returns>
extern "C" ULONG WINAPI NmBuildRawFrameFromBufferEx(
    __in_ecount(ulBufferLength) PBYTE pDataBuffer,
    __in ULONG ulBufferLength,
    __in ULONG ulMedia,
    __in PNM_TIME pTime,
    __out PHANDLE phRawFrame);

/// <summary><c>NmGetFrameFragmentInfo</c>Return fragment information of the given parsed frame</summary> 
/// <remarks>
/// Raw frame does not aware of its fragment type. Only parsing the frame can tell when reassembly is enabled.
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="pFragmentationInfo">[out] Caller provided struct pointer</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame
///     ERROR_INSUFFICIENT_BUFFER: If the protocol name length is longer than the buffer in PNmReassemblyInfo struct
/// </returns>
extern "C" ULONG WINAPI NmGetFrameFragmentInfo(__in HANDLE hParsedFrame, __out PNM_FRAGMENTATION_INFO pFragmentationInfo);

/// <summary><c>NmGetFilterCount</c>Return configured filter count in the given frame parser</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] frame parser under inspection</param>
/// <param name="pulFilterCount">[out] number of filters of the given frame parser.  It is zero if return code is not success</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser
/// </returns>
extern "C" ULONG WINAPI NmGetFilterCount(__in HANDLE hFrameParser, __out PULONG pulFilterCount);

/// <summary><c>NmEvaluateFilter</c>Return the state of specified filter in given parsed frame</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame to evaluate</param>
/// <param name="ulFilterId">[in] The identify number of the filter</param>
/// <param name="pbPassFilter">[out] The filter evaluation result.  TRUE means pass; FALSE means not pass</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame
/// </returns>
extern "C" ULONG WINAPI NmEvaluateFilter(__in HANDLE hParsedFrame, __in ULONG ulFilterId, __out PBOOL pbPassFilter);

/// <summary><c>NmGetFieldCount</c>Return number of fields enumerated in the given parsed frame</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target frame</param>
/// <param name="pulFieldCount">[out] The number of fields returned in parsed frame</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame
/// </returns>
extern "C" ULONG WINAPI NmGetFieldCount(__in HANDLE hParsedFrame, __out PULONG pulFieldCount);

/// <summary><c>NmGetParsedFieldInfo</c>Return the field information of a parsed frame specified by field index number</summary> 
/// <remarks>
/// The pointer to field is valid until the parsed frame containing the field is closed.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field to get</param>
/// <param name="ulOption">[in] The retrieve flag</param>
/// <param name="pParsedFieldInfo">[out] The pointer to the parsed field buffer</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
///     ERROR_ARITHMETIC_OVERFLOW: The field length is greater than 65535
///     NM_STATUS_API_VERSION_MISMATCHED: The pParsedFieldInfo.Size is not initialized or it is different from the version of the API version.
/// </returns>
extern "C" ULONG WINAPI NmGetParsedFieldInfo(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __in ULONG ulOption, __out PNM_PARSED_FIELD_INFO pParsedFieldInfo);

/// <summary><c>NmGetParsedFieldInfoEx</c>Return the field information of a parsed frame specified by field index number</summary> 
/// <remarks>
/// The pointer to field is valid until the parsed frame containing the field is closed.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field to get</param>
/// <param name="ulOption">[in] The retrieve flag</param>
/// <param name="pParsedFieldInfo">[out] The pointer to the parsed field buffer</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
///     NM_STATUS_API_VERSION_MISMATCHED: The pParsedFieldInfo.Size is not initialized or it is different from the version of the API version.
/// </returns>
extern "C" ULONG WINAPI NmGetParsedFieldInfoEx(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __in ULONG ulOption, __out PNM_PARSED_FIELD_INFO_EX pParsedFieldInfoEx);

/// <summary><c>NmGetFieldName</c>Return the names of the parsed field specified by field id</summary> 
/// <remarks>
/// ulBufferLength is element count.
/// </remarks>
/// <example> Description
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field to get</param>
/// <param name="RequestedName">[in] The enum to select intended name property</param>
/// <param name="ulBufferLength">[in] The length of caller provided buffer length</param>
/// <param name="pBuffer">[out] The caller provided buffer</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
///     ERROR_INSUFFICIENT_BUFFER: If ulBufferLength is shorted than the name length
/// </returns>
extern "C" ULONG WINAPI NmGetFieldName(__in HANDLE hParsedFrame,
                                       __in ULONG ulFieldId,
                                       __in NmParsedFieldNames RequestedName,
                                       __in ULONG ulBufferElementCount,
                                       __out_ecount(ulBufferElementCount)LPWSTR pNameBuffer);

/// <summary><c>NmGetFieldOffsetAndSize</c>Return the offset and size of the field specified by field id</summary> 
/// <remarks>
/// The returned field size is in unit of bit
/// </remarks>
/// <example> Description
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="pulFieldOffset">[out] The pointer to the returned field offset</param>
/// <param name="pulFieldSize">[out] The pointer to the returned field size</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
/// </returns>
extern "C" ULONG WINAPI NmGetFieldOffsetAndSize(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __out PULONG pulFieldOffset, __out PULONG pulFieldSize);

/// <summary><c>NmGetFieldValueNumber8Bit</c>Return number type field value</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="pubNumber">[out] The value of the requested field</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
/// </returns>
extern "C" ULONG WINAPI NmGetFieldValueNumber8Bit(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __out PUINT8 pubNumber);

/// <summary><c>NmGetFieldValueNumber16Bit</c>Return number type field value</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="puiNumber">[out] The value of the requested field</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
/// </returns>
extern "C" ULONG WINAPI NmGetFieldValueNumber16Bit(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __out PUINT16 puiNumber);

/// <summary><c>NmGetFieldValueNumber32Bit</c>Return number type field value</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="pulNumber">[out] The value of the requested field</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
/// </returns>
extern "C" ULONG WINAPI NmGetFieldValueNumber32Bit(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __out PUINT32 pulNumber);

/// <summary><c>NmGetFieldValueNumber64Bit</c>Return number type field value</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="pullNumber">[out] The value of the requested field</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
/// </returns>
extern "C" ULONG WINAPI NmGetFieldValueNumber64Bit(__in HANDLE hParsedFrame, __in ULONG ulFieldId, __out PUINT64 pullNumber);

/// <summary><c>NmGetFieldValueByteArray</c>Return byte array type field value</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="ulLength">[in] The size of the array in byte</param>
/// <param name="pByteBuffer">[out] The value of the requested field</param>
/// <param name="pulReturnLength">[out] The number of bytes returned in the buffer</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
///     ERROR_INSUFFICIENT_BUFFER: Not enough space in buffer, data is not copied
///     ERROR_RESOURCE_NOT_AVAILABLE: The field is a container so the content is not available
/// </returns>
extern "C" ULONG WINAPI NmGetFieldValueByteArray(__in HANDLE hParsedFrame,
                                                 __in ULONG ulFieldId,
                                                 __in ULONG ulLength,
                                                 __out_ecount_part(ulLength, *pulReturnLength) PBYTE pByteBuffer,
                                                 __out PULONG pulReturnLength);

/// <summary><c>NmGetFieldValueString</c>Return string array type field value</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="ulElementLength">[in] The size of string buffer in element unit</param>
/// <param name="pString">[out] The value of the requested field</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
///     ERROR_INSUFFICIENT_BUFFER: Not enough space in buffer, partial sting is copied.
/// </returns>
extern "C" ULONG WINAPI NmGetFieldValueString(__in HANDLE hParsedFrame,
                                              __in ULONG ulFieldId,
                                              __in ULONG ulElementLength,
                                              __out_ecount(ulElementLength) LPWSTR pString);

/// <summary><c>NmGetFieldInBuffer</c>Get the field in user provided buffer </summary> 
/// <remarks>
/// Only the content up to the buffer length is copied.  Caller may call NmGetFieldOffsetAndSize to get the size 
/// Before calling this function with proper buffer length.  Or call this function twice if first time failed with 
/// ERROR_INSUFFICIENT_BUFFER status.  Allocate new buffer length according to the return length.
///
/// All fields with values can be retrieved.  The return data is in the same order as it on wire.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] The handle of the target parsed frame</param>
/// <param name="ulFieldId">[in] The identify number of the field</param>
/// <param name="ulBufferLength">[in] The length of caller provided buffer</param>
/// <param name="pFieldBuffer">[out] caller provided buffer</param>
/// <param name="pulReturnLength">[out] actual number of byte copied</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified parsed frame or field
///     ERROR_INSUFFICIENT_BUFFER: Not enough space in buffer, data is not copied.
/// </returns>
extern "C" ULONG WINAPI NmGetFieldInBuffer(__in HANDLE hParsedFrame,
                                           __in ULONG ulFieldId,
                                           __in ULONG ulBufferLength,
                                           __out_ecount_part(ulBufferLength, *pulReturnLength) PBYTE pFieldBuffer,
                                           __out PULONG pulReturnLength);

/// <summary><c>NmGetRequestedPropertyCount</c>Get the number of properties added to the parser.</summary> 
/// <remarks>
/// None.
/// </remarks>
/// <example> This sample shows how to call the NmGetRequestedPropertyCount method.
/// <code>
///     HANDLE hFrameParser;
///     ULONG propertyCount = 0;
///     NmGetRequestedPropertyCount(hFrameParser, &propertyCount);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] Frame Parser Handle</param>
/// <param name="pulCount">[out] Count of properties added to this frame configuration.</param>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle, wrong scope or NULL pointer
///     ERROR_NOT_FOUND: not found specified frame parser
/// </returns>
extern "C" ULONG WINAPI NmGetRequestedPropertyCount(__in HANDLE hFrameParser, __out PULONG pulCount);

/// <summary><c>NmGetPropertyInfo</c>Return info structure for a specific property by ID.</summary> 
/// <remarks>
/// When the property container type is multi-value storage, the value type and size may be unknown if the property name added does not contain the key.
/// Since the size is unknown, the caller may need to call the retrieval function twice with the proper buffer of required size returned by the 
/// Retrieval function which first returned ERROR_INSUFFICIENT_BUFFER.  The same is true for array properties when the index in not included in the property string.
///
/// If the property container type is unknown, the property is not available for retrieval.
/// </remarks>
/// <example> This sample shows how to call the NmGetParsedPropertyInfo method.
/// <code>
///     HANDLE hFrameParser;
///     ULONG PropertyId;
///     PNM_PROPERTY_INFO PropertyInfo;
///
///     PropertyInfo.Size = sizeof(NmPropertyInfo);
///     NmGetPropertyInfo(hFrameParser, PropertyId, &PropertyInfo);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] Frame Parser Configuration Handle</param>
/// <param name="ulPropertyId">[in] ID of the property returned from NmAddProperty</param>
/// <param name="pInfo">[out] Count of properties added to this frame configuration.</param>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified property
///     NM_STATUS_API_VERSION_MISMATCHED: NM_PARSED_PROPERTY_INFO version mismatch checked by struct size.
/// </returns>
extern "C" ULONG WINAPI NmGetPropertyInfo(__in HANDLE hFrameParser, __in ULONG ulPropertyId, __out PNM_PROPERTY_INFO pInfo);

/// <summary><c>NmGetPropertyValueById</c>Return property value by ID.</summary> 
/// <remarks>
/// The Key for multi-value storage properties or Index for array properties must not provide both the property name and key index array.
/// The key type must match the type used in NPL parser.
/// If no key is added, set ulKeyCount to zero.
/// </remarks>
/// <example> This sample shows how to call the NmGetPropertyValueById.
/// <code>
///  1. REGULAR PROPERTY VALUE RETRIEVAL EXAMPLE:
///
///     HANDLE hFrameParser;
///     ULONG myPropertyID = 1;
///     WCHAR retPropertyValue[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueById( myFrameParser,
///                             myPropertyID,
///                             MAX_PATH, 
///                             retPropertyValue, 
///                             &returnLength,
///                             &propertyType);
///
///  2. ARRAY PROPERTY VALUE RETRIEVAL EXAMPLE:
///
///     HANDLE hFrameParser;
///     ULONG myNameTableID = 2;
///     PNM_PROPERTY_STORAGE_KEY myKey;
///     myKey.Size = Sizeof(PNM_PROPERTY_STORAGE_KEY);
///     myKey.KeyType = NmMvsKeyTypeArrayIndex;
///     myKey.ArrayIndex = 5;
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueById( myFrameParser,
///                             myNameTableID,
///                             MAX_PATH, 
///                             retName, 
///                             &returnLength,
///                             &propertyType,
///                             1,
///                             myKey);
///
///  3. MVS PROPERTY RETRIEVAL EXAMPLE:
///
///     HANDLE hFrameParser;
///     ULONG myNameTableID = 3;
///     PNM_PROPERTY_STORAGE_KEY myKey;
///     myKey.Size = Sizeof(PNM_PROPERTY_STORAGE_KEY);
///     myKey.KeyType = NmMvsKeyTypeString;
///     myKey.KeyString = L"192.168.1.1";
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueById( myFrameParser,
///                             myNameTableID,
///                             MAX_PATH, 
///                             retName, 
///                             &returnLength,
///                             &propertyType,
///                             1,
///                             myKey);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] Frame Parser Handle</param>
/// <param name="ulPropertyId">[in] ID of the property returned from NmAddProperty</param>
/// <param name="ulBufferSize">[in] Size of the buffer supplied in byte count.</param>
/// <param name="pBuffer">[out] Buffer for returned data.</param>
/// <param name="pulReturnLength">[out] Size of the data returned.</param>
/// <param name="pType">[out] Value type of the returned MVS property.</param>
/// <param name="ulKeyCount">[in] Number of keys provided</param>
/// <param name="pKeyArray">[in] key Array to look up in MVS and property group </param>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified property
///     ERROR_INSUFFICIENT_BUFFER: Not enough space in buffer, data is not copied.  The required length is returned.
/// </returns>
extern "C" ULONG WINAPI NmGetPropertyValueById(__in HANDLE hFrameParser, 
                                               __in ULONG ulPropertyId, 
                                               __in ULONG ulBufferSize, 
                                               __out_ecount_part(ulBufferSize, *pulReturnLength) PBYTE pBuffer, 
                                               __out PULONG pulReturnLength, 
                                               __out PNmPropertyValueType pType, 
                                               __in ULONG ulKeyCount = 0, 
                                               __in PNM_PROPERTY_STORAGE_KEY pKeyArray = NULL);

/// <summary><c>NmGetPropertyValueByName</c>Return property value by Name.</summary> 
/// <remarks>
/// The property is not necessarily added to the frame parser configuration if a non-optimized frame parser is used.  In this case, the property id is not available and the
/// The property name can be used.  The full qualified name must be used as to add the property to the frame parser configuration.
/// The key type for multi-value storage must match the type used in NPL parser.
/// </remarks>
/// <example> This sample shows how to call the NmGetPropertyValueByName.
/// <code>
///  1. REGULAR PROPERTY VALUE RETRIEVAL EXAMPLE:
///
///     HANDLE hFrameParser;
///     LPWSTR myPropertyName = L"Global.IamReguler";
///     WCHAR retPropertyValue[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueByName( myFrameParser,
///                               myPropertyName,
///                               MAX_PATH, 
///                               retPropertyValue, 
///                               &returnLength,
///                               &propertyType);
///
///  2. GROUP PROPERTY VALUE RETRIEVAL EXAMPLE:
///
///     HANDLE hFrameParser;
///     ULONG myGroupName = L"Conversation.IamPropertyGroup";
///     PNM_PROPERTY_STORAGE_KEY myKey;
///     myKey.Size = Sizeof(PNM_PROPERTY_STORAGE_KEY);
///     myKey.KeyType = NmMvsKeyTypeArrayIndex;
///     myKey.ArrayIndex = 5;
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueByName( myFrameParser,
///                               myGroupName,
///                               MAX_PATH, 
///                               retName, 
///                               &returnLength,
///                               &propertyType,
///                               1,
///                               myKey);
///
///  3. MVS PROPERTY RETRIEVAL EXAMPLE:
///
///     HANDLE hFrameParser;
///     ULONG myName = L"Property.IamMvs";
///     PNM_PROPERTY_STORAGE_KEY myKey;
///     myKey.Size = Sizeof(PNM_PROPERTY_STORAGE_KEY);
///     myKey.KeyType = NmMvsKeyTypeNumber;
///     myKey.KeyNumber = 2048;
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueByName( myFrameParser,
///                               myName,
///                               MAX_PATH, 
///                               retName, 
///                               &returnLength,
///                               &propertyType,
///                               1,
///                               myKey);
///
///  4. MVS PROPERTY RETRIEVAL EXAMPLE 2 (KEY SPECIFIED IN NAME STRING):
///
///     HANDLE hFrameParser;
///     ULONG myName = L"Property.IamMvs$[1000, "\MyId\"]";
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     NmPropertyValueType propertyType;
///     NmGetPropertyValueByName( myFrameParser,
///                               myName,
///                               MAX_PATH, 
///                               retName, 
///                               &returnLength,
///                               &propertyType);
///
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrameParser">[in] Frame Parser Handle</param>
/// <param name="pPropertyName">[in] full qualified name of the property </param>
/// <param name="ulBufferSize">[in] Size of the buffer supplied in byte count.</param>
/// <param name="pBuffer">[out] Buffer for returned data.</param>
/// <param name="pulReturnLength">[out] Size of the data returned.</param>
/// <param name="pType">[out] Value type of the returned MVS property.</param>
/// <param name="ulKeyCount">[in] Number of keys provided</param>
/// <param name="pKeyArray">[in] key Array to look up in MVS and property group </param>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified property
///     ERROR_INSUFFICIENT_BUFFER: Not enough space in buffer, data is not copied.  The required length is returned.
/// </returns>
extern "C" ULONG WINAPI NmGetPropertyValueByName(__in HANDLE hFrameParser, 
                                                 __in LPCWSTR pPropertyName, 
                                                 __in ULONG ulBufferSize, 
                                                 __out_ecount_part(ulBufferSize, *pulReturnLength) PBYTE pBuffer, 
                                                 __out PULONG pulReturnLength, 
                                                 __out PNmPropertyValueType pType, 
                                                 __in ULONG ulKeyCount = 0, 
                                                 __in PNM_PROPERTY_STORAGE_KEY pKeyArray = NULL);

/// <summary><c>NmGetRawFrameLength</c>Return length of the raw frame</summary> 
/// <remarks>
/// 
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrame">[in] The handle of the target raw frame</param>
/// <param name="pulLength">[out] Frame length</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified raw frame
/// </returns>
extern "C" ULONG WINAPI NmGetRawFrameLength(__in HANDLE hFrame, __out PULONG pulLength);

/// <summary><c>NmGetRawFrame</c>Copy raw frame to the buffer</summary> 
/// <remarks>
/// The frame buffer is valid until the raw frame is closed.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hRawFrame">[in] The handle of the target raw frame</param>
/// <param name="ulLength">[in] Caller Frame buffer length in byte</param>
/// <param name="pFrameBuffer">[out] caller frame data buffer</param>
/// <param name="pulReturnLength">[out] actual number of byte copied</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified raw frame
///     ERROR_INSUFFICIENT_BUFFER: Not enough space in buffer, partial data is copied.
/// </returns>
extern "C" ULONG WINAPI NmGetRawFrame(__in HANDLE hRawFrame,
                                      __in ULONG ulLength,
                                      __out_ecount_part(ulLength, *pulReturnLength) PBYTE pFrameBuffer,
                                      __out PULONG pulReturnLength);

/// <summary><c>NmGetPartialRawFrame</c>Return partial frame data in caller provided buffer</summary> 
/// <remarks>
/// Use caller provided offset and buffer length to copy the frame data.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrame">[in] The handle of the target raw frame</param>
/// <param name="ulFrameOffset">[in] Start offset to copy</param>
/// <param name="ulBufferLength">[in] Caller buffer length, the Number of bytes to copy</param>
/// <param name="pFrameBuffer">[out] Caller provided buffer</param>
/// <param name="pulReturnLength">[out] actual number of byte copied</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified raw frame
/// </returns>
extern "C" ULONG WINAPI NmGetPartialRawFrame(__in HANDLE hRawFrame,
                                             __in ULONG ulFrameOffset,
                                             __in ULONG ulBufferLength,
                                             __out_ecount_part(ulBufferLength, *pulReturnLength) PBYTE pFrameBuffer,
                                             __out PULONG pulReturnLength);

/// <summary><c>NmGetFrameMacType</c>Return MAC type of the frame</summary> 
/// <remarks>
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrame"> [in] The handle of a parsed or a raw frame object</param>
/// <param name="pulMacType"> [out] The pointer to the MAC type of the frame</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_INVALID_PARAMETER: hFrame is not a parsed or a raw frame handle.
///     ERROR_NOT_FOUND: not found specified frame
/// </returns>
extern "C" ULONG WINAPI NmGetFrameMacType(__in HANDLE hFrame, __out PULONG pulMacType);

/// <summary><c>NmGetFrameTimeStamp</c>Return the local time stamp of the frame</summary> 
/// <remarks>
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrame"> [in] The handle of a parsed or a raw frame object</param>
/// <param name="pTimeStamp"> [out] The pointer to the local time stamp of the frame.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_INVALID_PARAMETER: hFrame is not a parsed or a raw frame handle.
///     ERROR_NOT_FOUND: not found specified frame
/// </returns>
extern "C" ULONG WINAPI NmGetFrameTimeStamp(__in HANDLE hFrame, __out PUINT64 pTimeStamp);

/// <summary><c>NmGetFrameTimeStampEx</c>Return the extended time information of the capture</summary> 
/// <remarks>
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrame"> [in] The handle of a parsed or a raw frame object</param>
/// <param name="pTime"> [out] The pointer to the NM_TIME of the frame</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_INVALID_PARAMETER: hFrame is not a parsed or a raw frame handle.
///     ERROR_NOT_FOUND: not found specified frame
/// </returns>
extern "C" ULONG WINAPI NmGetFrameTimeStampEx(__in HANDLE hFrame, __out PNM_TIME pTime);

/// <summary><c>NmGetFrameCommentInfo</c>Return the frame comment title and description</summary>
/// <remarks>
/// If the buffers passed in are NULL, the buffer length arguments will indicate the required lengths.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hFrame"> [in] The handle of the raw frame object></param>
/// <param name="pulTitleBufferLength"> [inout] The pointer to the actual byte length that corresponds to the title buffer</param>
/// <param name="pTitleBuffer"> [out] Caller supplied buffer to hold the comment title</param>
/// <param name="pulDescriptionBufferLength"> [inout] The pointer to the actual byte length that corresponds to the description buffer</param>
/// <param name="pDescriptionBuffer"> [out] Caller supplied buffer to hold the comment description</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: Specified parsed frame not found
///     ERROR_INSUFFICIENT_BUFFER: If either of the supplied buffers is shorter than the content to retrieve.
///     ERROR_EMPTY: Frame comment information was not found
/// </returns>
extern "C" ULONG WINAPI NmGetFrameCommentInfo(__in HANDLE hFrame, 
                                              __inout PULONG pulTitleBufferLength, 
                                              __out_ecount(*pulTitleBufferLength) PBYTE pTitleBuffer, 
                                              __inout PULONG pulDescriptionBufferLength,
                                              __out_ecount(*pulDescriptionBufferLength) PBYTE pDescriptionBuffer);


//////////////////////////////////////////////////////
/// Capture file functions
/////////////////////////////////////////////////////
/// <summary><c>NmCreateCaptureFile</c> Create a new Network Monitor capture file for adding frames.</summary> 
/// <remarks>
/// This is the capture file to write to. Close it by calling NmCloseObjHandle method.
/// The file can be opened in 2 modes specified by Flags, wrap around (default) and chain capture.
/// </remarks>
/// <example> Description
/// <code>
///     
///     
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pFileName">[in] The name of the file to create</param>
/// <param name="ulSize">[in] The maximum size of the file in byte.  The hard limit is 500 MByte</param>
/// <param name="ulFlags">[in] Specify the file modes, wrap-round or chain capture</param>
/// <param name="phCaptureFile">[out] The returned handle to the capture file object if successful</param>
/// <param name="pulReturnSize">[out] The actual size of the returned file in byte.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: not enough memory to build required objects.
/// </returns>
extern "C" ULONG WINAPI NmCreateCaptureFile(__in LPCWSTR pFileName,
                                            __in ULONG ulSize,
                                            __in ULONG ulFlags,
                                            __out PHANDLE phCaptureFile,
                                            __out PULONG pulReturnSize);

/// <summary><c>NmOpenCaptureFile</c> Open a Network Monitor capture file to read</summary> 
/// <remarks>
/// The file is read only. Close capture file by calling NmCloseObjHandle method.
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pFileName">[in] The name of the file to open</param>
/// <param name="phCaptureFile">[out] The returned handle of the capture file object if successful</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointer
///     ERROR_NOT_FOUND: not found specified file
///     ERROR_NOT_ENOUGH_MEMORY: not enough memory to build required objects.
/// </returns>
extern "C" ULONG WINAPI NmOpenCaptureFile(__in LPCWSTR pFileName, __out PHANDLE phCaptureFile);

/// <summary><c>NmOpenCaptureFileInOrder</c> Open a Network Monitor capture file to read.  The out of sequence frames are put in order </summary> 
/// <remarks>
/// The file is read only. Close capture file by calling NmCloseObjHandle method.
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pFileName">[in] The name of the file to open</param>
/// <param name="pOrderParser">[in] The frame parser configured with sequence parameters to handle out of order frames</param>
/// <param name="phCaptureFile">[out] The returned handle of the capture file object if successful</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointer
///     ERROR_NOT_FOUND: not found specified file
///     ERROR_INVALID_PARAMETER: frame parser does not have sequence configuration.
///     ERROR_NOT_ENOUGH_MEMORY: not enough memory to build required objects.
///     NM_STATUS_API_VERSION_MISMATCHED: PNM_ORDER_PARSER_PARAMETER version does not match.
/// </returns>
extern "C" ULONG WINAPI NmOpenCaptureFileInOrder(__in LPCWSTR pFileName, __in PNM_ORDER_PARSER_PARAMETER pOrderParser, __out PHANDLE phCaptureFile);

/// <summary><c>NmAddFrame</c> Add a frame to the specified capture file.</summary> 
/// <remarks>
/// The target capture file must be opened with NmCreateCaptureFile method
/// </remarks>
/// <example> 
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureFile">[in] The destination capture file for the frame</param>
/// <param name="hFrame">[in] The frame to add</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle
///     ERROR_NOT_FOUND: not found specified file or frame
/// </returns>
extern "C" ULONG WINAPI NmAddFrame(__in HANDLE hCaptureFile, __in HANDLE hFrame);

/// <summary><c>NmGetFrameCount</c> Get frame count in the specified capture file</summary> 
/// <remarks>
/// 
/// </remarks>
/// <exception>None</exception>
/// <param name="hCaptureFile">[in] The target capture file under query</param>
/// <param name="pFrameCount">[out] Return frame count</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified capture file
/// </returns>
extern "C" ULONG WINAPI NmGetFrameCount(__in HANDLE hCaptureFile, __out PULONG pFrameCount);

/// <summary><c>NmGetFrame</c> Get frame by number from the specified capture file.</summary> 
/// <remarks>
/// The frame number is the index number in the capture file.
/// </remarks>
/// <exception>None</exception>
/// <param name="hCaptureFile">[in] Handle to the capture file</param>
/// <param name="ulFrameNumber">[in] Frame number in the capture file to retrieve</param>
/// <param name="phFrame">[out] The returned handle to the raw frame object if successful</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: Frame handle is valid
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_FOUND: not found specified capture file or frame
/// </returns>
extern "C" ULONG WINAPI NmGetFrame(__in HANDLE hCaptureFile, __in ULONG ulFrameNumber, __out PHANDLE phFrame);

/// <summary><c>NmCloseHandle</c> Release the reference to the object by handle</summary> 
/// <remarks>
/// Callers need to close all the object handles returned from API after finish using them.
/// </remarks>
/// <exception>None</exception>
/// <param name="hObjectHandle"> Handle to the object to release </param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>Void</returns>
extern "C" VOID WINAPI NmCloseHandle(__in HANDLE hObjectHandle);

//////////////////////////////////////////////
///
/// Conversation Info
///

/// <summary><c>NmGetTopConversation</c>Return the top level conversation and protocol name.</summary> 
/// <remarks>
/// The frame parsed used to parse the frame must have conversation configured as TRUE.
///
/// The protocol name length is returned to caller.  So if the provided buffer is not enough, caller
/// Can call again with the proper sized buffer accordingly.
/// </remarks>
/// <example> This sample shows how to call the NmGetTopConversation method.
/// <code>
///     HANDLE hParsedFrame;
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     NmGetTopConversation(hParsedFrame, 
///                          MAX_PATH, 
///                          retName, 
///                          &returnLength,
///                          &conversationId);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] Parsed Frame</param>
/// <param name="ulBufferESize">[in] Size of the for protocol name in WCHAR.</param>
/// <param name="pProtocolName">[out] Buffer for protocol name.</param>
/// <param name="pulProtocolNameLength">[out] Not include terminator in WCHAR.</param>
/// <param name="pulConversationID">[out] ID of the TOP Level Conversation</param>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_INSUFFICIENT_BUFFER: Insufficient buffer space
///     ERROR_NOT_FOUND: not found specified parsed frame or the conversation.
/// </returns>
extern "C" ULONG WINAPI NmGetTopConversation(__in HANDLE hParsedFrame,
                                             __in ULONG ulBufferESize,
                                             __out_ecount_part(ulBufferESize, *pulProtocolNameLength) LPWSTR pProtocolName,
                                             __out PULONG pulProtocolNameLength,
                                             __out PULONG pulConversationID);

/// <summary><c>NmGetParentConversation</c>Return parent conversation information of the given conversation.</summary> 
/// <remarks>
/// The frame parsed used to parse the frame must have conversation configured as TRUE.
///
/// The parent protocol name length is returned to caller.  So if the provided buffer is not enough, caller
/// Can call again with the proper sized buffer.
/// </remarks>
/// <example> This sample shows how to call the NmGetParentConversation method.
/// <code>
///     HANDLE myParsedFrame;
///     ULONG protocolId = 24;
///     WCHAR retName[MAX_PATH];
///     ULONG returnLength;
///     ULONG parentConvID;
///     NmGetParentConversation(myParsedFrame,
///                             protocolId,
///                             MAX_PATH, 
///                             retName, 
///                             &returnLength,
///                             &parentConvID);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hParsedFrame">[in] Parsed Frame</param>
/// <param name="ulConversationID">[in] ID of the Conversation you want the parent of.</param>
/// <param name="ulBufferESize">[in] Buffer size for the Parent protocol name in WCHAR count.</param>
/// <param name="pParentProtocolName">[out] Buffer for the Parent Protocol Name. </param>
/// <param name="pulParentProtocolNameLength">[out] Returned Length of Parent Protocol Name in WCHAR.</param>
/// <param name="pulParentConversationID">[out] Size of the for protocol name.</param>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_INSUFFICIENT_BUFFER: Insufficient buffer space
///     ERROR_NOT_FOUND: not found specified frame parser or the conversation.
/// </returns>
extern "C" ULONG WINAPI NmGetParentConversation(__in HANDLE hParsedFrame,
                                                __in ULONG ulConversationId,
                                                __in ULONG ulBufferESize,
                                                __out_ecount_part(ulBufferESize, *pulParentProtocolNameLength) LPWSTR pParentProtocolNameBuffer,
                                                __out PULONG pulParentProtocolNameLength, 
                                                __out PULONG pulParentConversationID);

/// <summary><c>NmNplProfileAttribute</c></summary> 
/// <remarks>
/// The NmNplProfileAttribute enumeration is used to select which string the profile should return when
/// using the SetNplProfileAttribute and GetNplProfileAttribute methods.
/// </remarks>
typedef enum _NmNplProfileAttribute
{
    NmNplProfileAttributeName,
    NmNplProfileAttributeGuid,
    NmNplProfileAttributeDescription,
    NmNplProfileAttributeIncludePath,
    NmNplProfileAttributeDirectory,
    NmNplProfileAttributePackageName,
    NmNplProfileAttributePackageVersion,
    NmNplProfileAttributePackageGuid,
    NmNplProfileAttributeDependencies,
    NmNplProfileAttributeTypeDescription,
} NmNplProfileAttribute;

/// <summary><c>NmLoadWithNplProfile</c>Create frame parser using the provided Guid for an NPL profile</summary> 
/// <remarks>
/// The ulFlags is reserved for future options.
/// </remarks>
/// <example> 
/// <code>
/// /*
/// * Callback for Parser load information and errors
/// */
///void MyParserErrorCallback(PVOID pCallerContext,
///                        ULONG dwStatusCode,
///                        LPCWSTR lpDescription,
///                        ULONG ulMsgType)
///{
///     // Determine the type of message this callback refers to
///     switch(ulMsgType)
///     {
///     case NmApiCallBackMsgTypeError:
///         fwprintf(stdout, L" Error:  ");    
///         break;
///     case NmApiCallBackMsgTypeWarning:
///         fwprintf(stdout, L" Warning:");    
///         break;
///     case NmApiCallBackMsgTypeInformation:
///         fwprintf(stdout, L" Info:   ");    
///         break;
///     default:
///         fwprintf(stdout, L" Unknown:");    
///         break;
///     }
///     fwprintf(stdout, L" %s - status = 0x%x\n", lpDescription, dwStatusCode);
///}
//
///void AttemptToCompile()
///{
///     HANDLE nplParser;
///     ULONG status = ERROR_SUCCESS;
///     status = NmLoadWithProfile(L"AD161723-4281-4d33-804E-5E43EE61D163", 0L, MyParserErrorCallback, NULL, &nplParser);
///     if (status != ERROR_SUCCESS) 
///     {
///         fwprintf(stdout, L"Could not load parser\n");
///         return;
///     }
///}
/// </code>
/// </example>
/// <exception>None</exception>
/// <param name="pProfileGuid">[in] The GUID of the profile to use.</param>
/// <param name="ulFlags">[in] Option flags</param>
/// <param name="CallbackFunction">[in] The compiler error callback function pointer</param>
/// <param name="pCallerContext">[in] The caller context pointer that will be passed back to the callback function</param>
/// <param name="phNplParser">[Out] The returned handle to the NPL parser object</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: Successfully compiled NPL
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer
///     ERROR_NOT_ENOUGH_MEMORY: Fail to create frame parser configuration object.
///     ERROR_NOT_FOUND: The given profile GUID does not exist.
///     ERROR_NO_MATCH: the provided GUID does not exist.
/// </returns>
extern "C" ULONG WINAPI NmLoadWithNplProfile(
    __in LPCWSTR pProfileGuid, 
    __in NmNplParserLoadingOption ulFlags,
    __in NM_NPL_PARSER_CALLBACK CallbackFunction, 
    __in PVOID pCallerContext, 
    __out PHANDLE phNplParser );

/// <summary><c>NmCreateNplProfile</c>adds a profile given using given parameters.</summary> 
/// <remarks>
/// This method allows developers to create profiles much like when the UI builds a profile.
/// The simplest case does not provide the optional argument TemplateID.  In this case, a new profile is 
/// created using the paths that have been provided.  No sparser is generated, and no directories are formed.
/// The include path must be provided in this case.
///
/// The more complex case provides a TemplateID that corresponds to an existing profile.  The include path for
/// the existing profile will be duplicated for the new profile (which is why the provided IncludePath is
/// ignored in this case).  Profiles created in this way will have a directory created using the GUID of the 
/// profile if successful. When these profiles are deleted, all files present in the created directory will 
/// also be removed.  Template profiles gain an additional search path to the user's local APPDATA as well
/// as an explicit include my_sparser.npl in the generated sparser.npl.
/// </remarks>
/// <example>
/// <code>
///
/// // Building a blank profile.
/// ULONG status = NmCreateNplProfile( L"ProfileTest00-1", L"This profile is created, verified, and deleted.", 
///                                    L"c:\\somePath", L"MyGuid" );
///
/// // Building a profile from a template using the GUID of a previously existing profile.
/// ULONG status = NmCreateNplProfile( L"ProfileTest00-2", L"This profile is created from another profile.", 
///                                    NULL, L"MyGuid2", L"AD161723-4281-4d33-804E-5E43EE61D163" );
///
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="Name">[in] The name of the profile. </param>
/// <param name="Description">[in] The description of the profile. </param>
/// <param name="IncludePath">[in] A semicolon delimited list of the paths to search in when building parsers.
///  This argument is not used when a templateID is provided.</param>
/// <param name="Guid">[in] The GUID for the profile.</param>
/// <param name="TemplateID">[in] Optional. Defaults to NULL. If provided, the newly created profile will be 
///  built as a template of the given profile. </param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: Successfully added the profile.
///     ERROR_ALREADY_ASSIGNED: The provided GUID is already present.
///     ERROR_BAD_ARGUMENTS: The include path should be provided.
///     ERROR_NO_MATCH: The provided TemplateID does not exist.
/// </returns>
extern "C" ULONG WINAPI NmCreateNplProfile( 
    __in LPCWSTR Name, 
    __in LPCWSTR Description,
    __in LPCWSTR IncludePath,
    __in LPCWSTR Guid,
    __in LPCWSTR templateID = NULL );

/// <summary><c>NmGetNplProfileAttribute</c>Retrieves the profile attribute using the index of the profile.</summary> 
/// <remarks>
/// This method can be used to retrieve an attribute value as a string for a given profile.  If the given buffer is too small,
/// the method will populate as much as it can, and then change the value of ulBufferLenth to the required number of bytes to 
/// retrieve the entire string.
/// </remarks>
/// <example>
/// <code>
///     WCHAR myBuffer[1024];
///     ULONG myBufferSize = 1024;
///     ULONG status = NmGetNplProfileAttribute( 1, NmNplProfileAttributePackageGuid, &myBufferSize, myBuffer );
///     fwprintf( stdout, L"  [%d] PackageGuid: %s\n", status, myBuffer );
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pProfileGuid">[in] the 1-based index of the profile</param>
/// <param name="attribute">[in] the attribute enum that corresponds to the string we are looking for</param>
/// <param name="ulBufferELength">[inout] The length of the buffer we would like to fill</param>
/// <param name="pAttributeBuffer">[out] The buffer to place the string in.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: Successfully retrieved the attribute.
///     ERROR_NO_MATCH: The provided GUID does not match an existing profile's GUID.
///     ERROR_INSUFFICIENT_BUFFER: The provided buffer is too short.  The ulBufferELenth will be updated.
/// </returns>
extern "C" ULONG WINAPI NmGetNplProfileAttribute(
    __in ULONG ulIndex, 
    __in NmNplProfileAttribute attribute,
    __inout PULONG ulBufferELength, 
    __out_ecount(*ulBufferELength) LPWSTR pAttributeBuffer );

/// <summary><c>NmGetNplProfileAttributeByGuid</c>Retrieves the profile attribute using the Guid of the profile.</summary> 
/// <remarks>
/// This method can be used to retrieve an attribute value as a string for a given profile.  If the given buffer is too small,
/// the method will populate as much as it can, and then change the value of ulBufferLenth to the required number of bytes to 
/// retrieve the entire string.
/// </remarks>
/// <example>
/// <code>
///     WCHAR myBuffer[1024];
///     ULONG myBufferSize = 1024;
///     ULONG status = NmGetNplProfileAttributeByGuid( guid, NmNplProfileAttributePackageGuid, &myBufferSize, myBuffer );
///     fwprintf( stdout, L"  [%d] PackageGuid: %s\n", status, myBuffer );
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pProfileGuid">[in] the guid of the profile</param>
/// <param name="attribute">[in] the attribute enum that corresponds to the string we are looking for</param>
/// <param name="ulBufferELength">[inout] The length of the buffer we would like to fill</param>
/// <param name="pAttributeBuffer">[out] The buffer to place the string in.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: Successfully retrieved the attribute.
///     ERROR_NO_MATCH: The provided GUID does not match an existing profile's GUID.
///     ERROR_INSUFFICIENT_BUFFER: The provided buffer is too short.  The ulBufferELenth will be updated.
/// </returns>
extern "C" ULONG WINAPI NmGetNplProfileAttributeByGuid(
    __in LPCWSTR pProfileGuid, 
    __in NmNplProfileAttribute attribute,
    __inout PULONG ulBufferELength, 
    __out_ecount(*ulBufferELength) LPWSTR pAttributeBuffer );

/// <summary><c>NmSetNplProfileAttribute</c>Sets the profile's attribute using the index of the profile.</summary> 
/// <remarks>
/// This method can be used to set an attribute on a profile.  There are only three attributes that can be 
/// modified on an existing profile; the Name, the Description, and the Include Path.  This method accepts the index of
/// profile to modify.
/// </remarks>
/// <example>
/// <code>
/// // Load the first profile.
/// ULONG status = NmSetNplProfileAttribute( 0, NmNplProfileAttributeDescription, L"TestValue" );
/// fwprintf( stdout, L"Attempted to set to 'TestValue' and received a status code of %d\n", status );
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulIndex">[in] the index of the profile to use..</param>
/// <param name="attribute">[in] the enumeration item that corresponds to the desired attribute.</param>
/// <param name="pAttributeBuffer">[in] The buffer to populate the attribute from.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: The attribute has been successfully updated.
///     ERROR_NO_MATCH: The provided index is not found in the Parser Profile Manager.
///     ERROR_INVALID_ACCESS: The profile is read only and cannot be modified.
///     ERROR_BAD_ARGUMENTS: The enumeration item was not Description, Name, or IncludePath.
/// </returns>
extern "C" ULONG WINAPI NmSetNplProfileAttribute(
    __in ULONG ulIndex, 
    __in NmNplProfileAttribute attribute,
    __in LPWSTR pAttributeBuffer );

/// <summary><c>NmSetNplProfileAttributeByGuid</c>Sets the profile's attribute using the GUID of the profile.</summary> 
/// <remarks>
/// This method can be used to set an attribute on a profile.  There are only three attributes that can be 
/// modified on an existing profile; the Name, the Description, and the Include Path.
/// </remarks>
/// <example>
/// <code>
/// ULONG status = NmSetNplProfileAttribute( L"AD161723-4281-4d33-804E-5E43EE61D163", NmNplProfileAttributeDescription, L"TestValue" );
/// fwprintf( stdout, L"Attempted to set to 'TestValue' and received a status code of %d\n", status );
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulIndex">[in] the GUID of the profile to update.</param>
/// <param name="attribute">[in] the enumeration item that corresponds to the desired attribute.</param>
/// <param name="pAttributeBuffer">[in] The buffer to populate the attribute from.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: The attribute has been successfully updated.
///     ERROR_NO_MATCH: The provided GUID is not found in the Parser Profile Manager.
///     ERROR_INVALID_ACCESS: The profile is read only and cannot be modified.
///     ERROR_BAD_ARGUMENTS: The enumeration item was not Description, Name, or IncludePath.
/// </returns>
extern "C" ULONG WINAPI NmSetNplProfileAttributeByGuid( __in LPCWSTR pProfileGuid, 
                                                        __in NmNplProfileAttribute attribute,
                                                        __in LPWSTR pAttributeBuffer );

/// <summary><c>NmGetNplProfileCount</c>retrieves the number of profiles available</summary> 
/// <remarks>
/// The number of profiles includes the User Defined Profiles and the Installed Profiles.
/// This number will always be greater than zero.  If no profiles are present, the parser engine
/// will construct the pure capture profile when the ParserProfileManager is initialized which 
/// will be done when this method is called.
/// </remarks>
/// <example>
/// <code>
/// ULONG myProfileCount = 0;
/// ULONG status = NmGetNplProfileCount( &myProfileCount );
/// </code>
/// </example>
/// <exception>None</exception>
/// <param name="pulCount">[out] Number of profiles in the ParserProfileManager.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: Method will always return success.
/// </returns>
extern "C" ULONG WINAPI NmGetNplProfileCount( __out PULONG pulCount );


/// <summary><c>NmGetActiveNplProfileGuid</c>retrieves the GUID for the currently active profile</summary> 
/// <remarks>
/// If the provided buffer is not long enough to contain the active NPL Profile's Id, the method will return
/// ERROR_INSUFFICIENT_BUFFER and the ulBufferELength will be assigned to the necessary size to read the buffer.
/// </remarks>
/// <example>
/// <code>
/// WCHAR myGuid[50] = {0};
/// ULONG myGuidLength = 50;
///  ULONG status = NmGetActiveNplProfileGuid( &myGuidLength, myGuid );
///  switch (status)
///  {
///  case ERROR_SUCCESS:
///      // We have successfully retrieved the GUID in our buffer.
///      break;
///  case ERROR_INSUFFICIENT_BUFFER:
///      // We have partially stored the GUID in our buffer.
///      // The value of myGuidLength is the larger size that we need to allocate for the buffer.
///      break;
///  case ERROR_NO_MATCH:
///      // No profile has been set as active.
///      break;
///  }
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulBufferELength">[in] The length of the provided buffer.</param>
/// <param name="pProfileGuid">[out] The buffer to place the GUID of the active profile into. </param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: The GUID has been retrieved.
///     ERROR_NO_MATCH: There is no active Npl Profile assigned.
///     ERROR_INSUFFICIENT_BUFFER: the provided buffer is not long enough to contain the GUID.
/// </returns>
extern "C" ULONG WINAPI NmGetActiveNplProfileGuid( __inout PULONG ulBufferELength, 
                                                   __out_ecount(*ulBufferELength) LPWSTR pProfileGuid );

/// <summary><c>NmSetActiveNplProfile</c>Sets the active profile to the given ID</summary> 
/// <remarks>
/// Method will attempt to compile the provided profile. If compilation is not successful, then ERROR_CAN_NOT_COMPLETE will be
/// returned. In this case, users should use NmLoadWithNplProfile to determine what errors were encountered.  When successful,
/// the profile is set as active for all Microsoft Network Monitor clients that share the HKCU key. (netmon.exe/nmcap.exe/etc)
/// </remarks>
/// <example>
/// <code>
///   LPWSTR MicrosoftFasterNPLProfileID = L"AD161723-4281-4d33-804E-5E43EE61D163";
///   ULONG status = NmSetActiveNplProfile( MicrosoftFasterNPLProfileID );
///   if (ERROR_SUCCESS == status) // Successfully set the profile.
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pProfileGuid">[in] </param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: The profile has been set as active.
///     ERROR_NO_MATCH: The GUID is not present.
///     ERROR_CAN_NOT_COMPLETE: The provided profile cannot successfully compile.
/// </returns>
extern "C" ULONG WINAPI NmSetActiveNplProfile( __in LPCWSTR pProfileGuid );

/// <summary><c>NmDeleteNplProfile</c></summary> 
/// <remarks>
/// Method will delete a profile if the profile can be deleted. Some profiles cannot be deleted.
/// Profiles that cannot be deleted include: 
///            Installed Profiles from Microsoft/3rd party packages.
///            The currently active profile.
/// </remarks>
/// <example>
/// <code>
///   LPWSTR MicrosoftFasterNPLProfileID = L"AD161723-4281-4d33-804E-5E43EE61D163";
///   ULONG status = NmDeleteNplProfile( MicrosoftFasterNPLProfileID );
///   if (ERROR_INVALID_ACCESS == status) // Attempted to delete an installed profile.
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="pProfileGuid">[in] </param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS: The profile has been successfully deleted.
///     ERROR_NO_MATCH: The provided GUID is not available for deletion.
///     ERROR_ALREADY_ASSIGNED: The provided GUID could not be deleted because it is currently active.
///     ERROR_INVALID_ACCESS: The profile is read only.
/// </returns>
extern "C" ULONG WINAPI NmDeleteNplProfile(__in LPCWSTR pProfileGuid);

//////////////////////////////////////////////
///
/// Driver capture filter
///
/// OVERVIEW: You can specify a driver filter per adapter, per process.  The driver filtering consists of
/// a set of filters where each filter contain a set blocks.  Each block contains a set of OLP,
/// (Offset, Length, Pattern), expressions which also contain an operand.  The operation, AND or OR, is fixed 
/// for each block of OLP expressions.  And the operation between blocks is the opposite of the operation 
/// between each OLP.  So if you set the block operation to AND, the OLP operation is OR.  The operation 
/// between filters can be AND or OR.  The order of the operations for filters occurs in the order you add 
/// them.  The order of operations for blocks and OLP expressions depends on the index you specify when you 
/// add them.
///
/// OLP expressions, blocks and filters can also be "short circuited" using the NmOlpActionFlags.  This allows
/// you to optimize an expression when you know the evaluation in the driver will not be sufficient to
/// completely evaluate the intended filter operation accurately.  For instance, consider a filter where you are 
/// looking for a TCP port. In some cases the port offset can change when IPv4 options exist.  This short circuit
/// provides a way to return the frame early rather than evaluating the rest of the expression.
/// 
/// You must add a driver filter before the capture engine is started.  Driver filtering can affect your
/// capturing performance as a larger delay in the driver doing these evaluations may cause you to drop
/// frames.  You can use NmGetLiveCaptureFrameCounts to monitor dropped frames and other statistics.
///

///
/// <summary><c>NmOlpActionFlags</c></summary> 
/// <remarks>
/// These flags are used when the OLP expressions, blocks and filters are created.  For filter creation, these
/// flags are combined with NmFilterOptionFlags in the options parameter.  These flags override the 
/// normal AND/OR operations that would occur for the conditions, blocks and filters following the current
/// entity; OLP expression, block or filter.
///
/// For example, if block type is AND and the NmOlpActionFlagsCopyOnFalse is specified at block creation, 
/// the frames that fail the block are copied to user mode although they would have been dropped in the 
/// normal AND operation.  These flags "short circuit" the rest of the evaluation.  If this evaluation is 
/// true then the driver continues to evaluate the remaining blocks if they exist, or copies this frame to
/// user mode if this is the only block.  This behavior is identical for all three levels: condition, block 
/// and filter.
///
/// The action flags are exclusive, i.e., they cannot be set at the same time that could be done by mistake 
/// In NmCreateOlpFilter where the two flags are combined.
/// </remarks>
typedef enum _NmOlpActionFlags
{
    NmOlpActionFlagsNone,
    NmOlpActionFlagsCopyOnFalse,
    NmOlpActionFlagsDropOnFalse,
    NmOlpActionFlagsLast

} NmOlpActionFlags;

///
/// <summary><c>NmFilterOptionFlags</c></summary> 
/// <remarks>
/// Filter options are used for filter creation only.
///
/// NmFilterOptionFlagsBlockTypeAnd specifies the logical operation among all blocks contained in the filter 
/// and the opposite logical operation is performed among all the OLP conditions in each block.
/// 
/// NmFilterOptionFlagsAndToNext specifies the logical operation between this filter and the next. Note that 
/// multiple filters are evaluated in the order that they are added.  If filter A is added first followed by 
/// filter B, A is evaluated first.  By default, when this flag is not set, A is OR'd with B.
/// 
/// A is AND'd with B if NmFilterOptionFlagsAndToNext is set in filter A during creation.  The frames are dropped 
/// if A fails; B will be evaluated if A passes.
///
/// If there are filters A, B and C, and A has NmFilterOptionFlagsAndToNext set, the logic is A AND B OR C, i.e., it 
/// Only affect the operation between the two adjacent filters, given they are added in order of A, B and C.
///
/// NmFilterOptionFlagsBlockTypeAnd and NmFilterOptionFlagsAndToNext are not exclusive to each other.  They can 
/// also be combined with NmOlpActionFlags during filter creation.  NmOlpActionFlags override NmFilterOptionFlags
/// when the action criteria is met.  The NmOlpActionFlags are in the lower WORD of the combined flags.
///
/// </remarks>
typedef enum _NmFilterOptionFlags
{
    NmFilterOptionFlagsNone,
    /// If set, the blocks contained in the filter are AND together.
    NmFilterOptionFlagsBlockTypeAnd = 0x00010000,
    /// If set, the current filter is AND to the next filter if exist; otherwise 
    /// It is OR to the next filter
    NmFilterOptionFlagsAndToNext = 0x00020000,
    NmFilterOptionFlagsLast

} NmFilterOptionFlags;

///
/// <summary><c>NmFilterMatchMode</c>Filter match mode for TRUE</summary> 
/// <remarks>
/// NmFilterMatchModeEqual:     return TRUE if pattern matches the frame data.
/// NmFilterMatchModeNotEqual:  return TRUE if pattern does not match the frame data.
/// NmFilterMatchModeGreater:   return TRUE if pattern is greater than the frame data.
/// NmFilterMatchModeLesser:    return TRUE if pattern is less than the frame data.
/// </remarks>
typedef enum _NmFilterMatchMode
{
    NmFilterMatchModeNone,
    NmFilterMatchModeEqual,
    NmFilterMatchModeNotEqual,
    NmFilterMatchModeGreater,
    NmFilterMatchModeLesser,
    NmFilterMatchModeLast

} NmFilterMatchMode;

/// <summary><c>NmCreateOlp</c>Create OLP for the capture filter</summary> 
/// <remarks>
/// Build an OLP expression and return the OLP ID which is unique in processes scope.
///
/// The OLP pattern must be aligned with and match that of the data in the frame you are evaluating.
/// For example, if the bit offset is 34, the bit length is 17, and the pattern is 0b10101010101010101, the
/// representation in the given buffer should be 0b00101010,10101010,10100000 or 0x2AAAA0 (3 bytes).  In other
/// words, the data you'd see in the frame will also match 0x2AAAA0 for the range of bits in question.
///                                                  -------------------
/// Concerning the two leading zeros and five zeros on the back, the bits in front or rear of the pattern is not 
/// required to be set to zero.
/// 
/// Given bit offset BO an bit length BL, the byte array length should be:
/// (((BO % 8) + BL)/8 + (((BO + BL) % 8) == 0)? 0:1)
/// The byte array length in above example: 
/// (((34 % 8) + 17)/8 + (((34 + 17) % 8) == 0)? 0:1) is 3
/// 
/// If the byte array passed in is shorter, the behavior is unpredictable or most likely an access violation.
///
/// </remarks>
/// <example>
/// <code>
/// Here is an example of TCP syn flag OLP that allows only TCP syn packets.  This assumes that the underlying 
/// protocols match Frame.Ethernet.IPv4.TCP for the frame you evaluate.
///
/// ULONG BitOffset = 446; 
/// ULONG BitLength = 1; 
/// BYTE  TcpFlagSyn = 0x02; // Syn flag bit is the 7th from the MSB (0b00000010).
/// ULONG OlpId;
/// ULONG status = NmCreateOlp(BitOffset, BitLength, &TcpFlagSyn, NmFilterMatchModeEqual, NmOlpActionFlagsNone, &OlpId);
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="BitOffset">[in] The bit offset of the OLP</param>
/// <param name="BitLength">[in] The bit length of the OLP</param>
/// <param name="pPattern">[in] The pattern of the OLP.</param>
/// <param name="OpMode">[in] The comparison operator type, e.g., EQ, NOT EQ, GREATER, LESS, etc.</param>
/// <param name="ulOptions">[in] The option configuration flags defined by NmOlpActionFlags</param>
/// <param name="pulOlpId">[out] The process unique ID for the OLP.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointers or zero BitLength.
///     ERROR_ARITHMETIC_OVERFLOW: If more than 4G OLP entities are created, the OLP id overflows.
///     ERROR_NOT_ENOUGH_MEMORY: Fail to allocate memory for the OLP entity object.
/// </returns>
extern "C" ULONG WINAPI NmCreateOlp(__in ULONG BitOffset, __in ULONG BitLength, __in PBYTE pPattern, __in NmFilterMatchMode OpMode, __in NmOlpActionFlags ulOptions, __out PULONG pulOlpId);

/// <summary><c>NmCreateOlpBlock</c>Create OLP block for the capture filter </summary> 
/// <remarks>
/// Create an OLP block and return its ID which is unique in processes scope.  The ulConditionCount specifies 
/// the maximum number of the OLP conditions the block can hold.  The ulConditionCount must be non-zero.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulConditionCount">[in] The number of conditions the block can hold</param>
/// <param name="ulOptions">[in] Optional NmOlpActionFlags flags </param>
/// <param name="pulOlpBlockId">[out] The unique ID of the OLP block</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointer or ulConditionCount is zero.
///     ERROR_ARITHMETIC_OVERFLOW: If more than 4G OLP entities are created, the OLP id overflows.
///     ERROR_NOT_ENOUGH_MEMORY: Fail to allocate memory for the OLP entity object.
/// </returns>
extern "C" ULONG WINAPI NmCreateOlpBlock(__in ULONG ulConditionCount, __in NmOlpActionFlags ulOptions, __out PULONG pulOlpBlockId);

/// <summary><c>NmAddOlpToBlock</c>Add the OLP to the OLP block </summary> 
/// <remarks>
/// Add the OLP condition to the OLP block.  The ulOlpBlockId must reference an OLP block entity that was
/// created by NmCreateOlpBlock.  The ulOlpId must reference to an OLP condition entity created by NmCreateOlp.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulOlpBlockId">[in] The target OLP block ID created by NmCreateOlpBlock</param>
/// <param name="ulIndex">[in] The zero based index position of the OLP in the target block</param>
/// <param name="ulOlpId">[in] The ID of the OLP to add returned from NmCreateOlpl</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: The specified index is invalid; or the OLP types referenced by the IDs are wrong.
///     ERROR_NOT_FOUND: The specified block or OLP is not found.
/// </returns>
extern "C" ULONG WINAPI NmAddOlpToBlock(__in ULONG ulOlpBlockId, __in ULONG ulIndex, __in ULONG ulOlpId);

/// <summary><c>NmCreateOlpFilter</c>Create the OLP driver capture filter</summary> 
/// <remarks>
/// Create an OLP filter entity and return its ID that is unique in processes scope.  
/// The ulBlockCount specifies the maximum number of the OLP blocks the 
/// filter created can hold.  The ulBlockCount must be non-zero.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulBlockCount">[in] The number of OLP blocks the filter can hold</param>
/// <param name="ulOptions">[in] The filter configuration flags.  NmFilterOptionFlags and NmOlpActionFlags can be combined</param>
/// <param name="pulOlpFilterId">[out] The unique ID of the new OLP filter</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: NULL pointer, ulBlockCount is zero or the ulOptions is invalid.
///     ERROR_ARITHMETIC_OVERFLOW: If more than 4G OLP entities are created, the OLP id overflows.
///     ERROR_NOT_ENOUGH_MEMORY: Fail to allocate memory for the OLP entity object.
/// </returns>
extern "C" ULONG WINAPI NmCreateOlpFilter(__in ULONG ulBlockCount, __in ULONG ulOptions, __out PULONG pulOlpFilterId);

/// <summary><c>NmAddOlpBlockToFilter</c>Add the OLP block to the OLP filter</summary> 
/// <remarks>
/// Add the OLP block to the OLP filter.  The ulOlpFilterId must reference to a OLP filter entity that is created by
/// NmCreateOlpFilter.  The ulOlpBlockId must reference to a OLP block entity created by NmCreateOlpBlock.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulOlpFilterId">[in] The ID of the target OLP filter created by NmCreateOlpFilter.</param>
/// <param name="ulIndex">[in] The index position of the OLP block in the target OLP filter</param>
/// <param name="ulOlpBlockId">[in] The ID of the OLP block to add created by NmCreateOlpBlock.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BAD_ARGUMENTS: The specified index is invalid; or the OLP types referenced by the ids are wrong.
///     ERROR_NOT_FOUND: The specified filter or block is not found.
/// </returns>
extern "C" ULONG WINAPI NmAddOlpBlockToFilter(__in ULONG ulOlpFilterId, __in ULONG ulIndex, __in ULONG ulOlpBlockId);

/// <summary><c>NmDeleteOlpEntity</c>Delete an OLP entity specified by its ID</summary> 
/// <remarks>
/// Delete the specified OLP entity, (filter, or block, or OLP expression), from API OLP entity pool so the deleted entity
/// can no longer used to construct new driver filters. The OLP entity here is not the actual driver capture Filter but 
/// the layered logical objects that can be added to the driver using NmAddDriverCaptureFilter, or deleted from the driver 
/// using NmDeleteDriverCaptureFilter.
///
/// Do not delete an OLP entity before the filter containing it has been added to the driver, otherwise 
/// the filter will not be built correctly.
///
/// All OLP entities (OLP condition, block and filter) can be deleted after the filter is added to the driver.
///
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="ulEntityId">[in] The unique ID of the OLP entity: OLP expression, block or filter.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_NOT_FOUND: The specified ulEntityId is invalid.
/// </returns>
extern "C" ULONG WINAPI NmDeleteOlpEntity(__in ULONG ulEntityId);

/// <summary><c>NmAddDriverCaptureFilter</c>Add capture filter to the adapter </summary> 
/// <remarks>
/// Adds the capture filter created with NmCreateOlpFilter to the driver. This function must be called when  
/// the adapter is not actively capturing.
///
/// There is a limit to the number of OLP conditions that can be added to the driver.  This limit is per 
/// adapter for each API process. The default limit is 16.  If the limit of OLP conditions are exceeded, 
/// ERROR_INVALID_PARAMETER is returned.  The limit can be changed by modifying the DWORD in the registry, 
/// HKLM\SYSTEM\CurrentControlSet\Services\nm3\OlpFilterConditionMaxCount. 
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The capture engine handle returned from NmOpenCaptureEngine.</param>
/// <param name="ulAdapterIndex">[in] The adapter the filter was added to.</param>
/// <param name="ulFilterId">[in] The filter id returned from NmCreateOlpFilter and used to reference the OLP Filter.</param>
/// <param name="ulOption">[in] The option defined for filter configuration.  Reserved.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_INVALID_PARAMETER: Invalid handle, or driver parameter validation failed, or filter is invalid (no OLP inside).
///     ERROR_NOT_ENOUGH_MEMORY: No memory to construct filter.
///     ERROR_NO_SYSTEM_RESOURCES: Too many OLP in driver or not enough resource for the operation.
///     ERROR_BUSY: The driver is not in the proper state for this operation.
///     ERROR_OBJECT_ALREADY_EXISTS: The specified filter already exists in driver.
///     ERROR_NOT_FOUND: not found specified capture engine, adapter or filter
/// </returns>
extern "C" ULONG WINAPI NmAddDriverCaptureFilter(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex, __in ULONG ulFilterId, __in ULONG ulOption);

/// <summary><c>NmDeleteDriverCaptureFilter</c>Remove capture filter from the driver </summary> 
/// <remarks>
/// Removes the driver capture filter specified by filter ID created by NmCreateOlpFilter.
/// The filter will no longer available afterwards.  All entities are also cleaned up and
/// no longer available.
///
/// This function must be called when the adapter is not actively capturing.
///
/// All driver capture filters are deleted from driver when the application terminates.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The capture engine handle returned by NmOpenCaptureEngine.</param>
/// <param name="ulAdapterIndex">[in] The adapter the filter is to be removed from</param>
/// <param name="ulFilterId">[in] The capture filter id to delete, created by NmCreateOlpFilter.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_BUSY: The driver is not in the proper state for this operation.
///     ERROR_NOT_FOUND: not found specified capture engine, adapter or filter.
/// </returns>
extern "C" ULONG WINAPI NmDeleteDriverCaptureFilter(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex, __in ULONG ulFilterId);

/// <summary><c>NmGetLiveCaptureFrameCounts</c>Return the frame counters of the adapter</summary> 
/// <remarks>
///     The counters in PNM_CAPTURE_STATISTICS are reported for the specified adapter.  If there are multiple capture engines capture on the
///     same adapter, they share the same counter set.
/// </remarks>
/// <example>
/// <code>
/// </code>
/// </example>
///
/// <exception>None</exception>
/// <param name="hCaptureEngine">[in] The capture engine handle returned by NmOpenCaptureEngine.</param>
/// <param name="ulAdapterIndex">[in] The adapter index.</param>
/// <param name="pCaptureStatistics">[out] The capture statistics of the specified adapter.</param>
/// <permission cref="System.Security.PermissionSet">Everyone can access this method.</permission>
/// <returns>
///     ERROR_SUCCESS:
///     ERROR_NOT_FOUND: not found specified adapter.
///     ERROR_BAD_ARGUMENTS: Invalid handle or NULL pointer.
/// </returns>
extern "C" ULONG WINAPI NmGetLiveCaptureFrameCounts(__in HANDLE hCaptureEngine, __in ULONG ulAdapterIndex, __out PNM_CAPTURE_STATISTICS pCaptureStatistics);

#endif /// __NM_API_HEADER__
