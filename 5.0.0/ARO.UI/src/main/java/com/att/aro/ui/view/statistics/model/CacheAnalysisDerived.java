/**
 * 
 */
package com.att.aro.ui.view.statistics.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.att.aro.core.packetanalysis.pojo.CacheAnalysis;
import com.att.aro.core.packetanalysis.pojo.CacheEntry;
import com.att.aro.core.packetanalysis.pojo.Diagnosis;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.Statistic;
import com.att.aro.ui.exception.AROUIPanelException;

/**
 * This is most likely a temporary extension of the data model for derived CacheAnalysis
 * getters.  It's currently here only because some of the attributes were not in the model
 * and/or they could not be found at the time (learning the model).  If the functionality is
 * not in the model, it may go there.  If the attribute(s) happens to be found, they will
 * go away here.
 * 
 * TODO:  Validate that we really don't have this information in the model.
 * TODO:  Re-factor to avoid going through collections each time if this stays!!  Inefficient!
 * 
 * @author Nathan F Syfrig
 *
 */
public class CacheAnalysisDerived {
	private final double pctCacheableResponses;
	private final double pctCacheableBytes;
	private final double pctNonCacheableResponses;
	private final double pctNonCacheableBytes;
	private final double pctExpiredResponses;
	private final double pctExpiredBytes;
	private final double pctNotExpiredResponses;
	private final double pctNotExpiredBytes;
	private final double pctCacheMissResponses;
	private final double pctCacheMissBytes;
	private final double pctNotCacheableResponses;
	private final double pctNotCacheableBytes;
	private final double pctCacheHitNotExpiredDupResponses;
	private final double pctCacheHitNotExpiredDupBytes;
	private final double pctCacheHitRespChangedResponses;
	private final double pctCacheHitRespChangedBytes;
	private final double pctCacheHitExpiredDupClientResponses;
	private final double pctCacheHitExpiredDupClientBytes;
	private final double pctCacheHitExpiredDupServerResponses;
	private final double pctCacheHitExpiredDupServerBytes;
	private final double pctCacheHitDup304Responses;
	private final double pctCacheHitDup304Bytes;
	private final double pctPartialHitExpiredDupClientResponses;
	private final double pctPartialHitExpiredDupClientBytes;
	private final double pctPartialHitExpiredDupServerResponses;
	private final double pctPartialHitExpiredDupServerBytes;
	private final double pctPartialHitNotExpiredDupResponses;
	private final double pctPartialHitNotExpiredDupBytes;

	private final Diagnosis[] countsAttributes = {
			Diagnosis.CACHING_DIAG_NOT_CACHABLE,

			// The following 4 collectively identifies a cached entry
			Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER,
			Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT,
			Diagnosis.CACHING_DIAG_OBJ_CHANGED,
			Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304,

			Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP,

			Diagnosis.CACHING_DIAG_CACHE_MISSED,
			Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT,
			Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER,
			Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT
	};

	private final Diagnosis[] invalidDiagnosis = {
			Diagnosis.CACHING_DIAG_INVALID_RESPONSE,
			Diagnosis.CACHING_DIAG_INVALID_REQUEST,
			Diagnosis.CACHING_DIAG_REQUEST_NOT_FOUND,
			Diagnosis.CACHING_DIAG_INVALID_REQUEST,
			Diagnosis.CACHING_DIAG_INVALID_OBJ_NAME
	};

	// Repurposing an illegal diagnosis bucket to house the totals (it's a hack, but...)
	private final Diagnosis repurposedTotalBucket = Diagnosis.CACHING_DIAG_REQUEST_NOT_FOUND;


	private class DiagnosisCounts {
		int matchedResponses = 0;
		long matchedBytes = 0;
		int unmatchedResponses = 0;
		long unmatchedBytes = 0;

		public DiagnosisCounts() {}

		public int getMatchedResponses() {
			return matchedResponses;
		}
		public long getMatchedBytes() {
			return matchedBytes;
		}
		public int getUnmatchedResponses() {
			return unmatchedResponses;
		}
		public long getUnmatchedBytes() {
			return unmatchedBytes;
		}

		public void incrementMatchedResponsesBytes(CacheEntry entry) {
			++matchedResponses;
			matchedBytes += entry.getRawBytes();
		}
		public void incrementUnmatchedResponsesBytes(CacheEntry entry) {
			++unmatchedResponses;
			unmatchedBytes += entry.getRawBytes();
		}

		public DiagnosisCounts addCounts(DiagnosisCounts add) {
			matchedResponses += add.matchedResponses;
			matchedBytes += add.matchedBytes;
			unmatchedResponses += add.unmatchedResponses;
			unmatchedBytes += add.unmatchedBytes;

			return this;
		}


		@Override
		public String toString() {
			return "DiagnosisCounts [matchedResponses=" + matchedResponses
					+ ", matchedBytes=" + matchedBytes
					+ ", unmatchedResponses=" + unmatchedResponses
					+ ", unmatchedBytes=" + unmatchedBytes + "]";
		}
	}


	private boolean isValidResult(Diagnosis diagnosisCheck) {
		boolean validResult = true;
		for (Diagnosis currentDiagnosis : invalidDiagnosis) {
			if (currentDiagnosis == diagnosisCheck) {
				validResult = false;
				break;
			}
		}
		return validResult;
	}

	/**
	 * This gets all the counts we need in a single pass through the diagnostic results
	 * 
	 * @param diagnosticResults
	 * @param diagnosis The list of diagnostic count buckets that are part of the return map
	 * @return A map of the counts associated with a diagnosis attributes in diagnosis
	 */
	private Map<Diagnosis, DiagnosisCounts> getResponseBytesCounts(
			List<CacheEntry> diagnosticResults, Diagnosis[] diagnosis) {
		Map<Diagnosis, DiagnosisCounts> diagnosisPercentsMap =
				new HashMap<Diagnosis, DiagnosisCounts>();

		DiagnosisCounts totalBucket = new DiagnosisCounts();
		diagnosisPercentsMap.put(repurposedTotalBucket, totalBucket);

		// Create the count buckets for matched and unmatched attributes
		Set<Diagnosis> duplicateCheck = new HashSet<Diagnosis>();
		for (Diagnosis currentDiagnosis : diagnosis) {
			if (duplicateCheck.contains(currentDiagnosis)) {
				throw new AROUIPanelException("Cannot have duplicate diagnosis (" +
						currentDiagnosis.name() + ")");
			}
			diagnosisPercentsMap.put(currentDiagnosis, new DiagnosisCounts());
			duplicateCheck.add(currentDiagnosis);
		}
		duplicateCheck.clear();
		duplicateCheck = null;

		// Now go through the diagnosis results, updating counts as necessary
		for (CacheEntry diagnosticResult : diagnosticResults) {
			Diagnosis currentResultDiagnosis = diagnosticResult.getDiagnosis();
			// Only look if basic validity passed
			if (isValidResult(currentResultDiagnosis)) {
				for (Diagnosis currentDiagnosis : diagnosis) {
					if (currentResultDiagnosis == currentDiagnosis) {
						diagnosisPercentsMap.get(currentDiagnosis).
							incrementMatchedResponsesBytes(diagnosticResult);
					}
					else {
						diagnosisPercentsMap.get(currentDiagnosis).
							incrementUnmatchedResponsesBytes(diagnosticResult);
					}
				}
				totalBucket.incrementMatchedResponsesBytes(diagnosticResult);
			}
		}
		return diagnosisPercentsMap;
	}


	public CacheAnalysisDerived(CacheAnalysis cacheAnalysis, Statistic statistic,
			List<Session> sessions) {
		Map<Diagnosis, DiagnosisCounts> counts = getResponseBytesCounts(
				cacheAnalysis.getDiagnosisResults(), countsAttributes);
		int totalResponses = counts.get(repurposedTotalBucket).getMatchedResponses();
		long totalBytes = counts.get(repurposedTotalBucket).getMatchedBytes();
		pctCacheableResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_CACHABLE).getUnmatchedResponses(), totalResponses);
		pctCacheableBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_CACHABLE).getUnmatchedBytes(), totalBytes);

		pctNonCacheableResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_CACHABLE).getMatchedResponses(), totalResponses);
		pctNonCacheableBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_CACHABLE).getMatchedBytes(), totalBytes);
		DiagnosisCounts expired = new DiagnosisCounts()
			.addCounts(counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER))
			.addCounts(counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT))
			.addCounts(counts.get(Diagnosis.CACHING_DIAG_OBJ_CHANGED))
			.addCounts(counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304));
		DiagnosisCounts notExpired = counts.get(Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP);
		int totalExpiredResponses = expired.getMatchedResponses() +
				notExpired.getMatchedResponses();
		long totalExpiredBytes = expired.getMatchedBytes() + notExpired.getMatchedBytes();
		pctExpiredResponses = expired.getMatchedResponses();
		pctExpiredBytes = expired.getMatchedBytes();
		pctNotExpiredResponses = percent(notExpired.getMatchedResponses(),
				totalExpiredResponses);
		pctNotExpiredBytes = percent(notExpired.getMatchedBytes(), totalExpiredBytes);

		int totalCacheResponses =
			counts.get(Diagnosis.CACHING_DIAG_CACHE_MISSED).getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_NOT_CACHABLE).getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT).getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER).getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304).getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT)
				.getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER)
				.getMatchedResponses() +
			counts.get(Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT).getMatchedResponses();
		long totalCacheBytes =
			counts.get(Diagnosis.CACHING_DIAG_CACHE_MISSED).getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_NOT_CACHABLE).getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT).getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER).getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304).getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT)
				.getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER)
				.getMatchedBytes() +
			counts.get(Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT).getMatchedBytes();
		pctCacheMissResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_CACHE_MISSED).getMatchedResponses(), totalCacheResponses);
		pctCacheMissBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_CACHE_MISSED).getMatchedBytes(), totalCacheBytes);
		pctNotCacheableResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_CACHABLE).getMatchedResponses(), totalCacheResponses);
		pctNotCacheableBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_CACHABLE).getMatchedBytes(), totalCacheBytes);
		pctCacheHitNotExpiredDupResponses = percent(notExpired.getMatchedResponses(),
				totalCacheResponses);
		pctCacheHitNotExpiredDupBytes = percent(notExpired.getMatchedBytes(), totalCacheBytes);
		pctCacheHitRespChangedResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_CHANGED).getMatchedResponses(), totalCacheResponses);
		pctCacheHitRespChangedBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_CHANGED).getMatchedBytes(), totalCacheBytes);
		pctCacheHitExpiredDupClientResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT).getMatchedResponses(),
					totalCacheResponses);
		pctCacheHitExpiredDupClientBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_CLIENT).getMatchedBytes(),
				totalCacheBytes);
		pctCacheHitExpiredDupServerResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER).getMatchedResponses(),
				totalCacheResponses);
		pctCacheHitExpiredDupServerBytes= percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_SERVER).getMatchedBytes(),
				totalCacheBytes);
		pctCacheHitDup304Responses = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304).getMatchedResponses(),
				totalCacheResponses);
		pctCacheHitDup304Bytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_304).getMatchedBytes(),
				totalCacheBytes);
		pctPartialHitExpiredDupClientResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT)
					.getMatchedResponses(), totalCacheResponses);
		pctPartialHitExpiredDupClientBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_CLIENT)
					.getMatchedBytes(), totalCacheBytes);
		pctPartialHitExpiredDupServerResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER)
				.getMatchedResponses(), totalCacheResponses);
		pctPartialHitExpiredDupServerBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_OBJ_NOT_CHANGED_DUP_PARTIALHIT_SERVER)
				.getMatchedBytes(), totalCacheBytes);
		pctPartialHitNotExpiredDupResponses = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT)
				.getMatchedResponses(), totalCacheResponses);
		pctPartialHitNotExpiredDupBytes = percent(counts.get(
				Diagnosis.CACHING_DIAG_NOT_EXPIRED_DUP_PARTIALHIT)
				.getMatchedBytes(), totalCacheBytes);
	}


	public double getPctCacheableResponses() {
		return pctCacheableResponses;
	}
	public double getPctCacheableBytes() {
		return pctCacheableBytes;
	}
	public double getPctNonCacheableResponses() {
		return pctNonCacheableResponses;
	}
	public double getPctNonCacheableBytes() {
		return pctNonCacheableBytes;
	}
	public double getPctExpiredResponses() {
		return pctExpiredResponses;
	}
	public double getPctExpiredBytes() {
		return pctExpiredBytes;
	}
	public double getPctNotExpiredResponses() {
		return pctNotExpiredResponses;
	}
	public double getPctNotExpiredBytes() {
		return pctNotExpiredBytes;
	}
	public double getPctCacheMissResponses() {
		return pctCacheMissResponses;
	}
	public double getPctCacheMissBytes() {
		return pctCacheMissBytes;
	}
	public double getPctNotCacheableResponses() {
		return pctNotCacheableResponses;
	}
	public double getPctNotCacheableBytes() {
		return pctNotCacheableBytes;
	}
	public double getPctCacheHitNotExpiredDupResponses() {
		return pctCacheHitNotExpiredDupResponses;
	}
	public double getPctCacheHitNotExpiredDupBytes() {
		return pctCacheHitNotExpiredDupBytes;
	}
	public double getPctCacheHitRespChangedResponses() {
		return pctCacheHitRespChangedResponses;
	}
	public double getPctCacheHitRespChangedBytes() {
		return pctCacheHitRespChangedBytes;
	}
	public double getPctCacheHitExpiredDupServerResponses() {
		return pctCacheHitExpiredDupServerResponses;
	}
	public double getPctCacheHitExpiredDupServerBytes() {
		return pctCacheHitExpiredDupServerBytes;
	}
	public double getPctCacheHitDup304Responses() {
		return pctCacheHitDup304Responses;
	}
	public double getPctCacheHitDup304Bytes() {
		return pctCacheHitDup304Bytes;
	}
	public double getPctPartialHitExpiredDupClientResponses() {
		return pctPartialHitExpiredDupClientResponses;
	}
	public double getPctPartialHitExpiredDupClientBytes() {
		return pctPartialHitExpiredDupClientBytes;
	}
	public double getPctPartialHitExpiredDupServerResponses() {
		return pctPartialHitExpiredDupServerResponses;
	}
	public double getPctPartialHitExpiredDupServerBytes() {
		return pctPartialHitExpiredDupServerBytes;
	}
	public double getPctPartialHitNotExpiredDupResponses() {
		return pctPartialHitNotExpiredDupResponses;
	}
	public double getPctPartialHitNotExpiredDupBytes() {
		return pctPartialHitNotExpiredDupBytes;
	}
	public double getPctCacheHitExpiredDupClientResponses() {
		return pctCacheHitExpiredDupClientResponses;
	}
	public double getPctCacheHitExpiredDupClientBytes() {
		return pctCacheHitExpiredDupClientBytes;
	}


	private double percent(double numerator, double denominator) {
		return denominator != 0.0 ? (numerator / denominator) * 100.00 : 0.0;
	}


	@Override
	public String toString() {
		return "CacheAnalysisDerived [pctCacheableResponses="
				+ pctCacheableResponses + ", pctCacheableBytes="
				+ pctCacheableBytes + ", pctNonCacheableResponses="
				+ pctNonCacheableResponses + ", pctNonCacheableBytes="
				+ pctNonCacheableBytes + ", pctExpiredResponses="
				+ pctExpiredResponses + ", pctExpiredBytes=" + pctExpiredBytes
				+ ", pctNotExpiredResponses=" + pctNotExpiredResponses
				+ ", pctNotExpiredBytes=" + pctNotExpiredBytes
				+ ", pctCacheMissResponses=" + pctCacheMissResponses
				+ ", pctCacheMissBytes=" + pctCacheMissBytes
				+ ", pctNotCacheableResponses=" + pctNotCacheableResponses
				+ ", pctNotCacheableBytes=" + pctNotCacheableBytes
				+ ", pctCacheHitNotExpiredDupResponses="
				+ pctCacheHitNotExpiredDupResponses
				+ ", pctCacheHitNotExpiredDupBytes="
				+ pctCacheHitNotExpiredDupBytes
				+ ", pctCacheHitRespChangedResponses="
				+ pctCacheHitRespChangedResponses
				+ ", pctCacheHitRespChangedBytes="
				+ pctCacheHitRespChangedBytes
				+ ", pctCacheHitExpiredDupClientResponses="
				+ pctCacheHitExpiredDupClientResponses
				+ ", pctCacheHitExpiredDupClientBytes="
				+ pctCacheHitExpiredDupClientBytes
				+ ", pctCacheHitExpiredDupServerResponses="
				+ pctCacheHitExpiredDupServerResponses
				+ ", pctCacheHitExpiredDupServerBytes="
				+ pctCacheHitExpiredDupServerBytes
				+ ", pctCacheHitDup304Responses=" + pctCacheHitDup304Responses
				+ ", pctCacheHitDup304Bytes=" + pctCacheHitDup304Bytes
				+ ", pctPartialHitExpiredDupClientResponses="
				+ pctPartialHitExpiredDupClientResponses
				+ ", pctPartialHitExpiredDupClientBytes="
				+ pctPartialHitExpiredDupClientBytes
				+ ", pctPartialHitExpiredDupServerResponses="
				+ pctPartialHitExpiredDupServerResponses
				+ ", pctPartialHitExpiredDupServerBytes="
				+ pctPartialHitExpiredDupServerBytes
				+ ", pctPartialHitNotExpiredDupResponses="
				+ pctPartialHitNotExpiredDupResponses
				+ ", pctPartialHitNotExpiredDupBytes="
				+ pctPartialHitNotExpiredDupBytes + ", countsAttributes="
				+ Arrays.toString(countsAttributes) + ", invalidDiagnosis="
				+ Arrays.toString(invalidDiagnosis)
				+ ", repurposedTotalBucket=" + repurposedTotalBucket + "]";
	}
}
