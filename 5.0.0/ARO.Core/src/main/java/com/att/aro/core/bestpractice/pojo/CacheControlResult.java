/*
 *  Copyright 2014 AT&T
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
package com.att.aro.core.bestpractice.pojo;

public class CacheControlResult extends AbstractBestPracticeResult{

	private int hitNotExpiredDupCount = 0;
	private int hitExpired304Count = 0;
	private String exportAllCacheConNExpDesc;
	private String exportAllCacheCon304Desc;
	
	public int getHitNotExpiredDupCount() {
		return hitNotExpiredDupCount;
	}

	public void setHitNotExpiredDupCount(int hitNotExpiredDupCount) {
		this.hitNotExpiredDupCount = hitNotExpiredDupCount;
	}

	public int getHitExpired304Count() {
		return hitExpired304Count;
	}

	public void setHitExpired304Count(int hitExpired304Count) {
		this.hitExpired304Count = hitExpired304Count;
	}

	public String getExportAllCacheConNExpDesc() {
		return exportAllCacheConNExpDesc;
	}

	public void setExportAllCacheConNExpDesc(String exportAllCacheConNExpDesc) {
		this.exportAllCacheConNExpDesc = exportAllCacheConNExpDesc;
	}

	public String getExportAllCacheCon304Desc() {
		return exportAllCacheCon304Desc;
	}

	public void setExportAllCacheCon304Desc(String exportAllCacheCon304Desc) {
		this.exportAllCacheCon304Desc = exportAllCacheCon304Desc;
	}

	@Override
	public BestPracticeType getBestPracticeType() {
		return BestPracticeType.USING_CACHE;//cache control-> using cache(content expired) ref. bestpractice.properties
	}

}
