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
