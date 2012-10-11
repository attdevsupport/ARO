/*
 * Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.att.aro.bp;

import java.util.Collection;
import java.util.Collections;

/**
 * Default implementation of BestPracticeDisplayGroup
 */
public class BestPracticeDisplayGroupImpl implements BestPracticeDisplayGroup {
	
	private String headerName;
	private String description;
	private String referSectionName;
	private Collection<BestPracticeDisplay> bestPractices;

	/**
	 * Constructor that initializes all fields
	 * @param headerName
	 * @param description
	 * @param referSectionName
	 * @param bestPractices
	 */
	public BestPracticeDisplayGroupImpl(String headerName, String description,
			String referSectionName,
			Collection<BestPracticeDisplay> bestPractices) {
		this.headerName = headerName;
		this.description = description;
		this.referSectionName = referSectionName;
		this.bestPractices = bestPractices;
	}

	/**
	 * @see com.att.aro.bp.BestPracticeDisplayGroup#getHeaderName()
	 */
	@Override
	public String getHeaderName() {
		return headerName;
	}

	/**
	 * @see com.att.aro.bp.BestPracticeDisplayGroup#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * @see com.att.aro.bp.BestPracticeDisplayGroup#getReferSectionName()
	 */
	@Override
	public String getReferSectionName() {
		return referSectionName;
	}

	/**
	 * @see com.att.aro.bp.BestPracticeDisplayGroup#getBestPractices()
	 */
	@Override
	public Collection<BestPracticeDisplay> getBestPractices() {
		return Collections.unmodifiableCollection(bestPractices);
	}

}
