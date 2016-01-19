/*
 *  Copyright 2015 AT&T
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
package com.att.aro.ui.view.menu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Harikrishna Yaramachu
 *
 */


/**
 * Contains sampled trace analysis data, and provides methods for calculating percentile 
 * rankings on that data, for energy, signaling, and throughput.
 * 
 * @author Harikrishna Yaramachu
 */
public class ApplicationSampling {

	/**
	 * Bean class to contain the application information. These data's are used
	 * to do the benchmarking.
	 */
	private static class SampleApp {
		private double kbps;
		private double jpkb;
		private double promoRatio;

		public SampleApp(String name, double kbps, double jpkb,
				double promoRatio) {
			this.kbps = kbps;
			this.jpkb = jpkb;
			this.promoRatio = promoRatio;
		}
	}

	private static List<SampleApp> applicationSampling = new ArrayList<SampleApp>();

	private List<Double> throughputSamples;
	private List<Double> jpkbSamples;
	private List<Double> promoRatioSamples;

	static {
		applicationSampling.add(new SampleApp("APP001.xlsx", 5.318428898,
				0.656, 0.05));
		applicationSampling.add(new SampleApp("APP002.xlsx", 3.543694554,
				0.951, 0.04));
		applicationSampling.add(new SampleApp("APP003.xlsx", 4.662700855,
				0.738, 0.05));
		applicationSampling.add(new SampleApp("APP004.xlsx", 4.662700855,
				0.738, 0.05));
		applicationSampling.add(new SampleApp("APP005.xlsx", 29.26691259,
				0.139, 0.06));
		applicationSampling.add(new SampleApp("APP006.xlsx", 29.75980373,
				0.142, 0.09));
		applicationSampling.add(new SampleApp("APP007.xlsx", 4.534057885,
				0.429, 0.05));
		applicationSampling.add(new SampleApp("APP008.xlsx", 55.02585018,
				0.034, 0.05));
		applicationSampling.add(new SampleApp("APP009.xlsx", 200.4057815,
				0.019, 0.04));
		applicationSampling.add(new SampleApp("APP010.xlsx", 3.207616361,
				0.615, 0.06));
		applicationSampling.add(new SampleApp("APP011.xlsx", 10.04671948,
				0.343, 0.09));
		applicationSampling.add(new SampleApp("APP012.xlsx", 29.26691259,
				0.139, 0.06));
		applicationSampling.add(new SampleApp("APP013.xlsx", 29.75980373,
				0.142, 0.09));
		applicationSampling.add(new SampleApp("APP014.xlsx", 2.879706071,
				0.807, 0.06));
		applicationSampling.add(new SampleApp("APP015.xlsx", 8.454391912,
				0.385, 0.09));
		applicationSampling.add(new SampleApp("APP016.xlsx", 7.188047398,
				0.452, 0.08));
		applicationSampling.add(new SampleApp("APP017.xlsx", 9.165134328,
				0.202, 0.06));
		applicationSampling.add(new SampleApp("APP018.xlsx", 15.37382601,
				0.113, 0.05));
		applicationSampling.add(new SampleApp("APP019.xlsx", 23.94973516,
				0.172, 0.09));
		applicationSampling.add(new SampleApp("APP020.xlsx", 1.903620899,
				0.712, 0.04));
		applicationSampling.add(new SampleApp("APP021.xlsx", 12.73601786,
				0.214, 0.06));
		applicationSampling.add(new SampleApp("APP022.xlsx", 0.668458477,
				0.769, 0.02));
		applicationSampling.add(new SampleApp("APP023.xlsx", 18.66748201,
				0.132, 0.05));
		applicationSampling.add(new SampleApp("APP024.xlsx", 5.822380952,
				0.626, 0.09));
		applicationSampling.add(new SampleApp("APP025.xlsx", 5.822380952,
				0.626, 0.09));
		applicationSampling.add(new SampleApp("APP026.xlsx", 98.66420208,
				0.034, 0.06));
		applicationSampling.add(new SampleApp("APP027.xlsx", 0.313155653, 1.01,
				0.01));
		applicationSampling.add(new SampleApp("APP028.xlsx", 85.70640997, 0.03,
				0.02));
		applicationSampling.add(new SampleApp("APP029.xlsx", 52.89869862,
				0.074, 0.02));
		applicationSampling.add(new SampleApp("APP030.xlsx", 99.24736557,
				0.029, 0.04));
		applicationSampling.add(new SampleApp("APP031.xlsx", 18.14704279,
				0.242, 0.06));
		applicationSampling.add(new SampleApp("APP032.xlsx", 102.385119, 0.008,
				0.02));
		applicationSampling.add(new SampleApp("APP033.xlsx", 100.7423301,
				0.022, 0.04));
		applicationSampling.add(new SampleApp("APP034.xlsx", 0.238590612,
				3.569, 0.03));
		applicationSampling.add(new SampleApp("APP035.xlsx", 53.9737792, 0.081,
				0.05));
		applicationSampling.add(new SampleApp("APP036.xlsx", 38.17802815,
				0.041, 0.04));
		applicationSampling.add(new SampleApp("APP037.xlsx", 5.287584098,
				0.276, 0.04));
		applicationSampling.add(new SampleApp("APP038.xlsx", 15.1301021, 0.119,
				0.06));
		applicationSampling.add(new SampleApp("APP039.xlsx", 1.641724296,
				0.897, 0.04));
		applicationSampling.add(new SampleApp("APP040.xlsx", 84.9008123, 0.032,
				0.04));
		applicationSampling.add(new SampleApp("APP041.xlsx", 5.53178027, 0.22,
				0.04));
		applicationSampling.add(new SampleApp("APP042.xlsx", 74.95768605,
				0.035, 0.01));
		applicationSampling.add(new SampleApp("APP043.xlsx", 53.42516628,
				0.059, 0.04));
		applicationSampling.add(new SampleApp("APP044.xlsx", 80.06503231,
				0.037, 0.02));
		applicationSampling.add(new SampleApp("APP045.xlsx", 2.350747963,
				0.454, 0.03));
		applicationSampling.add(new SampleApp("APP046.xlsx", 6.838243723,
				0.331, 0.06));
		applicationSampling.add(new SampleApp("APP047.xlsx", 18.65907347,
				0.082, 0.01));
		applicationSampling.add(new SampleApp("APP048.xlsx", 18.65907347,
				0.082, 0.01));
		applicationSampling.add(new SampleApp("APP049.xlsx", 37.11392073,
				0.074, 0.01));
		applicationSampling.add(new SampleApp("APP050.xlsx", 23.73336443,
				0.127, 0.08));
		applicationSampling.add(new SampleApp("APP051.xlsx", 222.811551, 0.015,
				0.05));
		applicationSampling.add(new SampleApp("APP052.xlsx", 47.14401257,
				0.114, 0.01));
		applicationSampling.add(new SampleApp("APP053.xlsb", 43.19401905,
				0.122, 0.01));
		applicationSampling.add(new SampleApp("APP054.xlsx", 95.57071815,
				0.056, 0.02));
		applicationSampling.add(new SampleApp("APP055.xlsx", 65.43079101,
				0.042, 0.04));
		applicationSampling.add(new SampleApp("APP056.xlsx", 0.101778375,
				10.481, 0.04));
		applicationSampling.add(new SampleApp("APP057.xlsx", 19.95741812, 0.12,
				0.05));
		applicationSampling.add(new SampleApp("APP058.xlsx", 38.48450704,
				0.128, 0.04));
		applicationSampling.add(new SampleApp("APP059.xlsx", 10.59658361, 0.27,
				0.09));
		applicationSampling.add(new SampleApp("APP060.xlsx", 12.03696373,
				0.161, 0.05));
		applicationSampling.add(new SampleApp("APP061.xlsx", 0.292540428,
				2.757, 0.03));
		applicationSampling.add(new SampleApp("APP062.xlsx", 141.0133092,
				0.032, 0.08));
		applicationSampling.add(new SampleApp("APP063.xlsx", 48.56675904,
				0.075, 0.07));
		applicationSampling.add(new SampleApp("APP064.xlsx", 2.39059952, 1.688,
				0.13));
		applicationSampling.add(new SampleApp("APP065.xlsx", 5.8716916, 0.547,
				0.09));
		applicationSampling.add(new SampleApp("APP066.xlsx", 39.82274296,
				0.141, 0));
		applicationSampling.add(new SampleApp("APP067.xlsx", 107.9761089,
				0.039, 0.07));
		applicationSampling.add(new SampleApp("APP068.xlsx", 16.93134489,
				0.237, 0.07));
		applicationSampling.add(new SampleApp("APP069.xlsx", 9.026643038,
				0.175, 0.05));
		applicationSampling.add(new SampleApp("APP070.xlsx", 234.6989682,
				0.022, 0.02));
		applicationSampling.add(new SampleApp("APP071.xlsx", 134.0070359,
				0.029, 0.04));
		applicationSampling.add(new SampleApp("APP072.xlsx", 30.89917246,
				0.108, 0.09));
		applicationSampling.add(new SampleApp("APP073.xlsx", 155.1695779,
				0.022, 0.04));
		applicationSampling.add(new SampleApp("APP074.xlsx", 10.89976628,
				0.447, 0.16));
		applicationSampling.add(new SampleApp("APP075.xlsx", 3.037396201,
				0.936, 0.07));
		applicationSampling.add(new SampleApp("APP076.xlsx", 1.804415347,
				1.559, 0.09));
		applicationSampling.add(new SampleApp("APP077.xlsx", 1.52297206, 1.975,
				0.11));
		applicationSampling.add(new SampleApp("APP078.xlsx", 7.167181373,
				0.346, 0.04));
		applicationSampling.add(new SampleApp("APP079.xlsx", 5.428763281,
				0.624, 0.11));
		applicationSampling.add(new SampleApp("APP080.xlsx", 9.677147067,
				0.377, 0.09));
		applicationSampling.add(new SampleApp("APP081.xlsx", 6.152019173,
				0.466, 0.06));
		applicationSampling.add(new SampleApp("APP082.xlsx", 11.68292589,
				0.286, 0.07));
		applicationSampling.add(new SampleApp("APP083.xlsx", 2.190667833, 1.36,
				0.11));
		applicationSampling.add(new SampleApp("APP084.xlsx", 13.45953749,
				0.279, 0.08));
		applicationSampling.add(new SampleApp("APP085.xlsx", 7.18297311, 0.458,
				0.09));
		applicationSampling.add(new SampleApp("APP086.xlsx", 28.41380125, 0.14,
				0.06));
		applicationSampling.add(new SampleApp("APP087.xlsx", 69.99632814,
				0.061, 0.05));
		applicationSampling.add(new SampleApp("APP088.xlsx", 38.02442693,
				0.149, 0));
		applicationSampling.add(new SampleApp("APP089.xlsx", 39.18730159,
				0.128, 0.01));
		applicationSampling.add(new SampleApp("APP090.xlsx", 11.73340407,
				0.298, 0.07));
		applicationSampling.add(new SampleApp("APP091.xlsx", 27.2203256, 0.154,
				0.05));
		applicationSampling.add(new SampleApp("APP092.xlsx", 9.076062544, 0.37,
				0.07));
		applicationSampling.add(new SampleApp("APP093.xlsx", 12.48129794,
				0.177, 0.09));
		applicationSampling.add(new SampleApp("APP094.xlsx", 22.77093016, 0.18,
				0.09));
		applicationSampling.add(new SampleApp("APP095.xlsx", 2.14818724, 0.609,
				0.04));
		applicationSampling.add(new SampleApp("APP096.xlsx", 3.171723074,
				0.478, 0.05));
		applicationSampling.add(new SampleApp("APP097.xlsx", 17.81147826,
				0.133, 0.06));
		applicationSampling.add(new SampleApp("APP098.xlsx", 57.95071457,
				0.054, 0.08));
		applicationSampling.add(new SampleApp("APP099.xlsx", 31.48077395, 0.07,
				0.05));
		applicationSampling.add(new SampleApp("APP100.xlsx", 3.582338569,
				0.958, 0.14));
		applicationSampling.add(new SampleApp("APP101.xlsx", 1.96453817, 1.309,
				0.07));
		applicationSampling.add(new SampleApp("APP102.xlsx", 0.09639626, 2.161,
				0.01));
		applicationSampling.add(new SampleApp("APP103.xlsx", 7.369398048,
				0.332, 0.07));
		applicationSampling.add(new SampleApp("APP104.xlsx", 1.700519552, 0.54,
				0.02));
		applicationSampling.add(new SampleApp("APP105.xlsx", 13.33784578,
				0.172, 0.06));
		applicationSampling.add(new SampleApp("APP106.xlsx", 5.606420063,
				0.315, 0.05));
	}

	private static ApplicationSampling instance = new ApplicationSampling();

	/**
	 * Returns the singleton instance of the ApplicationSampling class.
	 * 
	 * @return The singleton instance of the ApplicationSampling class.
	 */
	public static ApplicationSampling getInstance() {
		return instance;
	}

	/**
	 * Private constructor. Use getInstance()
	 */
	private ApplicationSampling() {
		this.throughputSamples = new ArrayList<Double>(
				applicationSampling.size());
		this.jpkbSamples = new ArrayList<Double>(applicationSampling.size());
		this.promoRatioSamples = new ArrayList<Double>(
				applicationSampling.size());
		for (SampleApp sample : applicationSampling) {
			this.throughputSamples.add(sample.kbps);
			this.jpkbSamples.add(sample.jpkb);
			this.promoRatioSamples.add(sample.promoRatio);
		}
		Collections.sort(this.throughputSamples);
		// Use sort in reverse because lower is better
		Collections.sort(this.jpkbSamples, Collections.reverseOrder());
		Collections.sort(this.promoRatioSamples, Collections.reverseOrder());
	}

	/**
	 * Calculates the throughput percentile based upon the sampled trace analysis data.
	 * 
	 * @param throughput The throughput value to be compared to the total throughput.
	 * 
	 * @return A double that is the throughput value in percentage format.
	 */
	public double getThroughputPercentile(double throughput) {
		int index = 0;
		while (index < this.throughputSamples.size()
				&& throughput > this.throughputSamples.get(index)) {
			++index;
		}
		return 100.0 * index / this.throughputSamples.size();
	}

	/**
	 * Calculates the joules/kilobyte percentile based upon the sampled trace analysis data.
	 * 
	 * @param jpkb The joules/kilobyte value to be compared to the total joules/kilobyte 
	 * in the sample.
	 * 
	 * @return A double that is the joules/kilobyte value in percentage format.
	 */
	public double getJpkbPercentile(double jpkb) {
		return getRatio(this.jpkbSamples, jpkb);
	}

	/**
	 * Calculates the signaling overhead percentile (the promotion ratio) based upon the 
	 * sampled trace analysis data.
	 * 
	 * @param promoRatio The promotion ratio value to be compared to the total packet 
	 * duration in the sample.
	 * 
	 * @return A double that is the promotion ratio in percentage format.
	 */
	public double getPromoRatioPercentile(double promoRatio) {
		return getRatio(this.promoRatioSamples, promoRatio);
	}

	/**
	 * Common method to calculate the jpkb percentile and promo ratio percentile
	 * 
	 * @param sampleDataList
	 *            List of doble values
	 * @param sampleData
	 *            value to compare.
	 * @return ratio for provided values.
	 */
	private double getRatio(List<Double> sampleDataList, double sampleData) {
		int index = 0;
		// Use less than because lower is better
		while (index < sampleDataList.size()
				&& sampleData < sampleDataList.get(index)) {
			++index;
		}
		return 100.0 * index / sampleDataList.size();
	}
}

