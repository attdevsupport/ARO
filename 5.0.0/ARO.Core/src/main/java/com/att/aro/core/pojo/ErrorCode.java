package com.att.aro.core.pojo;

/**
 * Models errors.
 * <pre>
 *   data fields:
 *     int code             // An int to identify an ErrorCode
 *     String name          // A descriptive name for ErrorCode
 *     String description   // A useful description of ErrorCode
 * </pre>
 */
public class ErrorCode {
	
	/**
	 * An int to identify an ErrorCode
	 */
	private int code;
	
	/**
	 * A descriptive name for ErrorCode
	 */
	private String name;
	
	/**
	 * A useful description of ErrorCode
	 */
	private String description;

	/**
	 * 
	 * @return code
	 */
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	/**
	 * 
	 * @return name of ErrorCode
	 */
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/**
	 * 
	 * @return description of ErrorCode
	 */
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		sBuffer.append("code:");
		sBuffer.append(code);
		if (name != null) {
			sBuffer.append(", name:");
			sBuffer.append(name);
		}
		if (description != null) {
			sBuffer.append(", description:");
			sBuffer.append(description);
		}
		return sBuffer.toString();
	}
}
