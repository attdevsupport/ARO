package com.att.aro.core.packetanalysis;

import java.util.List;

import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.Session;

public interface ICacheAnalysis {
	CacheAnalysis analyze(List<Session> sessionlist);
}
