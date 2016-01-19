package com.att.aro.console;

import org.springframework.context.ApplicationContext;

import com.att.aro.core.fileio.IFileManager;
import com.att.aro.core.pojo.ErrorCode;

/**
 * validate commands against arguments
 *
 */
public class Validator {
	public ErrorCode validate(Commands cmd, ApplicationContext context) {
		if (cmd.getStartcollector() != null) {
			String colname = cmd.getStartcollector();
			if (!"rooted_android".equals(colname) 
			 && !"vpn_android".equals(colname) 
			 && !"ios".equals(colname)) {
				return ErrorCodeRegistry.getUnsupportedCollector();
			}
			if (cmd.getOutput() == null) {
				return ErrorCodeRegistry.getOutputRequired();
			}
		}
		if (cmd.getAnalyze() != null) {
			//check something
			if (cmd.getFormat().equals("json") && cmd.getFormat().equals("html")) {
				return ErrorCodeRegistry.getUnsupportedFormat();
			}

			if (cmd.getOutput() == null) {
				return ErrorCodeRegistry.getOutputRequired();
			}
			IFileManager filemg = context.getBean(IFileManager.class);
			if (filemg.fileExist(cmd.getOutput())) {
				return ErrorCodeRegistry.getFileExist();
			}
		}
		if (cmd.getVideo() != null && !cmd.getVideo().equals("yes") && !cmd.getVideo().equals("no")) {
			return ErrorCodeRegistry.getInvalidVideoOption();
		}
		return null;
	}
}
