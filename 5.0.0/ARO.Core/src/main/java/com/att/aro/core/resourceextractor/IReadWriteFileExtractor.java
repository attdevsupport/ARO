/**
 * 
 */
package com.att.aro.core.resourceextractor;


/**
 * @author Harikrishna
 *
 */
public interface IReadWriteFileExtractor {

	boolean extractFiles(String localTraceFolder, String filename, ClassLoader loader);
}
