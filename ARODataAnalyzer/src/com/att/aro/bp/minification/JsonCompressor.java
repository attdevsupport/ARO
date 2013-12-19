package com.att.aro.bp.minification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonCompressor {
	
	private static final Logger LOGGER = Logger.getLogger(JsonCompressor.class.getName());
	private BufferedReader in ;
	public JsonCompressor(BufferedReader in){
		this.in = in;
	}
	/**
	 * reads the json and writes the compressed json
	 * @param  out
	 * */
	public void compress(BufferedWriter out) throws IOException {
		try{
			String jsonString = getJsonString();
			out.write(minifyJson(jsonString));
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					"Error from Json minifiaction: {0}", e.getMessage());
			throw (e);
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.flush();
					out.close();
				}
			} catch (IOException e) {
			LOGGER.log(Level.WARNING,
						"Error from Json minifiaction: {0}",
						e.getMessage());
				throw (e);
			}
		}
		
	}
	/**
	 * reads the json string from a file.
	 * */	
	private String getJsonString() {
		String strLine;
		StringBuilder st = new StringBuilder();
		try {
			while ((strLine = in.readLine()) != null) {
				st.append(strLine);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return st.toString();
	}
	/**
	 * compress the given json.
	 * @param  jsonString
	 * */
	private String minifyJson(String jsonString){
		String tokenizer = "\"|(/\\*)|(\\*/)|(//)|\\n|\\r";
		String tmp ,tmp2,leftString,rightString = "";
		int from = 0;
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile(tokenizer);
		Matcher matcher = pattern.matcher(jsonString);
		if (!matcher.find()){
			return jsonString;
		}else{
			matcher.reset();
		}
		while (matcher.find()) {
			leftString = jsonString.substring(0, matcher.start());
			rightString = jsonString.substring(matcher.end(), jsonString.length());
			tmp = jsonString.substring(matcher.start(), matcher.end());
			tmp2 = leftString.substring(from).replaceAll("(\\n|\\r|\\s)*", "");
			sb.append(tmp2);
			from = matcher.end();
			if (!tmp.substring(0, 1).matches("\\n|\\r|\\s")) {
				sb.append(tmp);
			}
		}
		sb.append(rightString);
		return sb.toString();		
		
	}

}
