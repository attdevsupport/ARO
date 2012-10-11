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

/**
 * Represents a group of best practices.  Each best practice group is displayed
 * as a block of best practices on the best practices tab.
 */
public interface BestPracticeDisplayGroup {

	/**
	 * Returns the text to be displayed in the block header for the best
	 * practice group.
	 * @return header name text
	 */
	public String getHeaderName();

	/**
	 * Returns the description of the best practice group.  This description
	 * is displayed in the header of the best practice group block.
	 * @return best practice group description
	 */
	public String getDescription();

	/**
	 * Returns a short name of the best practice group that will be used to
	 * refer to the section in a sentence such as:  "Refer to the section
	 * on <referSectionName>".
	 * @return section refer name
	 */
	public String getReferSectionName();

	/**
	 * Returns the best practices that are included in this group.
	 * @return Collection of best practice specifications
	 */
	public Collection<BestPracticeDisplay> getBestPractices();

}
