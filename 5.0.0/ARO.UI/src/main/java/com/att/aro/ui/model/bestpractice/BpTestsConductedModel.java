/**
 * Copyright 2016 AT&T
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
package com.att.aro.ui.model.bestpractice;

import java.util.ArrayList;
import java.util.Collection;

public class BpTestsConductedModel {

	private Collection<BPTest> bpTestList;

	public BpTestsConductedModel() {
		bpTestList = new ArrayList<BPTest>();
	}

	public Collection<BPTest> getBpTest() {
		return bpTestList;
	}

	public void setBpTest(Collection<BPTest> bpTestList) {
		this.bpTestList = bpTestList;
	}

	public void addBpTest(BPTest bpTest) {
		this.bpTestList.add(bpTest);
	}

}
