package com.att.android.arodatacollector.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TLSKey {
	private static final int MASTER_LEN = 48;
	private static final int TS_LEN = 8;
	
	byte[] timestamp = new byte[TS_LEN];
	byte[] master = new byte[MASTER_LEN];
	byte[] preMaster;
	private static final String TAG = "TLSKey";
	private int preMasterLen = 0;
	
	
	public TLSKey(String logcatStr){
		String transcodedStr = getTranscodedKey(logcatStr);
		final int keyLen = transcodedStr.length();
		preMasterLen = (keyLen - (TS_LEN + MASTER_LEN) * 2) / 2;
		
		AROLogger.d(TAG, "keyLen=" + keyLen + "; transcodedKey="+transcodedStr);
		int tsEndIndex_inc = (TS_LEN * 2) - 1;
		untranscode(transcodedStr, 0, tsEndIndex_inc, timestamp);
		
		int masterStartIndex_inc = tsEndIndex_inc + 1;
		int masterEndIndex_inc = masterStartIndex_inc + (MASTER_LEN * 2) - 1;
		untranscode(transcodedStr, masterStartIndex_inc,masterEndIndex_inc, master);
		
		int preMasterStartIndex_inc = masterEndIndex_inc + 1;
		preMaster = new byte[preMasterLen];
		untranscode(transcodedStr, preMasterStartIndex_inc, keyLen - 1, preMaster);
	}

	/** regexp for the tlsKey prefix. Note that the parentheses need to be escaped,
	 * and inside the parenthesis, there might be some white space before the number.
	 * The format is: F/_TLSKEY_(30914): HDGINJIGIMILNEEBIMO...
	 */
	private static final String TLSKeyLogPrefixRegExp = "F/_TLSKEY_\\(\\s*\\d+\\):\\s+";
	private static final String TLSKeyRegExp = "([a-z]+)";
	private static final int TLSKEY_GROUP = 1;
	private static final Pattern logcatPattern = Pattern.compile(TLSKeyLogPrefixRegExp + TLSKeyRegExp, Pattern.CASE_INSENSITIVE);
	private String getTranscodedKey(String logcatStr) {
		Matcher matcher = logcatPattern.matcher(logcatStr);
		if (matcher.find()){
			return matcher.group(TLSKEY_GROUP);
		}
		else {
			AROLogger.e(TAG, String.format("could not match regexp: %s with \nlogcatStr: %s", TLSKeyLogPrefixRegExp + TLSKeyRegExp, logcatStr));
		}
		return null;
	}

	/**
	 * When the key was dumped to logcat, it was transcoded to printable character in ssl/t1_enc.c; 
	 * so here we have to undo the transcoding.
	 * @param transcodedStr
	 * @param startIndex: inclusive start index
	 * @param endIndex: inclusive end index
	 * @return
	 */
	private boolean untranscode(String transcodedStr, int startIndex, int endIndex, byte[] result) {
		int strIndex = startIndex, keyIndex = 0;

		while (strIndex <= endIndex){
			char curChar = transcodedStr.charAt(strIndex);
			char nextChar = transcodedStr.charAt(strIndex + 1);
			
			//AROLogger.d(TAG, String.format("strIndex=%d, endIndex=%d, keyIndex=%d", strIndex, endIndex, keyIndex));
			
			if (isValidRange(curChar) && isValidRange(nextChar)){
				result[keyIndex] = (byte) ((curChar -'A') * 16 + (nextChar - 'A'));
				
				strIndex += 2; //advance 2 spots since we processed 2 chars
				keyIndex++;
			}
			else {
				AROLogger.e(TAG, String.format("char values out of range for indices [%d, %d]", strIndex, strIndex + 1));
				return false;
			}
			
		}
		
		return true;
	}

	private boolean isValidRange(char curChar) {
		return curChar >= 'A' && curChar <= 'P';
	}

	public byte[] getTimestamp() {
		return timestamp;
	}

	public byte[] getMaster() {
		return master;
	}

	public byte[] getPreMaster() {
		return preMaster;
	}

	public int getPreMasterLen() {
		return preMasterLen;
	}

}
