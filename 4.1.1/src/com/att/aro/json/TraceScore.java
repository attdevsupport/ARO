/**
 * 
 */
package com.att.aro.json;

/**
 * @author hy0910
 *
 */
public class TraceScore {
	
	private TraceScoreDetails[] causes;
	
	private TraceScoreDetails[] effects;

	/**
	 * @return the causes
	 */
	public TraceScoreDetails[] getCauses() {
		return causes;
	}

	/**
	 * @param causes the causes to set
	 */
	public void setCauses(TraceScoreDetails[] causes) {
		this.causes = causes;
	}

	/**
	 * @return the effects
	 */
	public TraceScoreDetails[] getEffects() {
		return effects;
	}

	/**
	 * @param effects the effects to set
	 */
	public void setEffects(TraceScoreDetails[] effects) {
		this.effects = effects;
	}
	

}
