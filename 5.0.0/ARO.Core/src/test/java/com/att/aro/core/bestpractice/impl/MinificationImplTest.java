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
package com.att.aro.core.bestpractice.impl;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.att.aro.core.BaseTest;
import com.att.aro.core.bestpractice.pojo.AbstractBestPracticeResult;
import com.att.aro.core.packetanalysis.IHttpRequestResponseHelper;
import com.att.aro.core.packetanalysis.pojo.AnalysisFilter;
import com.att.aro.core.packetanalysis.pojo.Burst;
import com.att.aro.core.packetanalysis.pojo.BurstCollectionAnalysisData;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;
import com.att.aro.core.packetanalysis.pojo.PacketAnalyzerResult;
import com.att.aro.core.packetanalysis.pojo.Session;
import com.att.aro.core.packetanalysis.pojo.TimeRange;
import com.att.aro.core.packetanalysis.pojo.TraceDirectoryResult;
import com.att.aro.core.packetanalysis.pojo.TraceResultType;
import com.att.aro.core.packetreader.pojo.Packet;

public class MinificationImplTest extends BaseTest{
	
	MinificationImpl MinificationImpl;
	Packet packet;
	PacketAnalyzerResult tracedata;

	
	private TraceDirectoryResult dirdata;
	private Burst burst01;
	private TimeRange timeRange;
	private AnalysisFilter analysisFilter;
	private BurstCollectionAnalysisData burstCollectionAnalysisData;
	
	AbstractBestPracticeResult result = null;

	@Before
	public void setup(){
		burst01 = Mockito.mock(Burst.class);
		tracedata = Mockito.mock(PacketAnalyzerResult.class);
		timeRange = Mockito.mock(TimeRange.class);
		dirdata = Mockito.mock(TraceDirectoryResult.class);
		analysisFilter = Mockito.mock(AnalysisFilter.class);
		burstCollectionAnalysisData = Mockito.mock(BurstCollectionAnalysisData.class);		

	}

	/**
	 * tests with empty session
	 */
	@Test   
	public void runTest_1(){
		

		List<Session> sessionlist;
		Session session_1;
		
		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		MinificationImpl = (MinificationImpl)context.getBean("minify");
		result = MinificationImpl.runTest(tracedata);

		assertEquals("Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.", result.getAboutText());
		assertEquals("Minify CSS, JS, JSON and HTML", result.getDetailTitle());
		assertEquals("http://developer.att.com/ARO/BestPractices/Minification", result.getLearnMoreUrl());
		assertEquals("File Download: Minify CSS, JS, JSON and HTML", result.getOverviewTitle());
		assertEquals("Your trace passes.", result.getResultText());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("PASS", result.getResultType().toString());

	}

	/**
	 * tests html compression
	 */
	@Test   
	public void runTest_2(){
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;
	//	Session session_2;

		session_1 = mock(Session.class);
	//	session_2 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);
		//sessionlist.add(session_2);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		List<HttpRequestResponseInfo> reqList_2 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);
		
		Mockito.when((TraceDirectoryResult)tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
	//	Mockito.when(session_2.getRequestResponseInfo()).thenReturn(reqList_2);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);
	//	Mockito.when(req_1.getContentType()).thenReturn("text/javascript");
		
		MinificationImpl = (MinificationImpl)context.getBean("minify");


		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(rr_2.getContentType()).thenReturn("text/html"); // "application/ecmascript" "application/json" "application/javascript" "text/javascript" "message/http"
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
		//	String mystring = "<html><head>    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=ISO-8859-1\">    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1, maximum-scale=1.0, user-scalable=0\">    <meta charset=\"utf-8\">    <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">        <title>      jcpenney | Protocol�� Roman 4-pc. Luggage Set    </title>    <link href=\"/jcp/common/themes/css/foundation.min.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/common/themes/css/foundation-icons.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/common/themes/css/fonts.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/common/themes/css/style.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/company-branding/css/company-branding.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/footer-social/css/footer-social.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/dfpm/css/dfpm.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/footer-group/css/footer-group.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/app/footer/css/footer.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/footer-signup/css/footer-signup.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/terms-conditions/css/terms-conditions.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/search/css/search.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/cart/css/cart.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/app/header/css/header.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/acct-nav/css/acct-nav.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/app/index-bag/css/index-bag.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/promo-reward/css/promo-reward.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/total-calc/css/total-calc.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/order-total/css/order-total.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/h-bag-item/css/h-bag-item.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/bread-crumb/css/bread-crumb.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/top-banner/css/top-banner.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <!-- PRODUCT CSS -->    <link href=\"/jcp/assets/clr-pckr/css/clr-pckr.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/size-chrt/css/size-chrt.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/add-bag/css/add-bag.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-des/css/prd-des.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-warranty/css/prd-warranty.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-ad/css/prd-ad.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-img/css/prd-img.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-spl-promo/css/prd-spl-promo.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-truck-dlv/css/prd-truck-dlv.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-suggestion/css/prd-suggestion.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-socialnet/css/prd-socialnet.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/price-tag/css/price-tag.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/price/css/price.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-calculate/css/prd-calculate.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <!-- START v-Data -->    <link href=\"/jcp/assets/prd-gift-card/css/prd-gift-card.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/prd-v-data/css/JcpVdata.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <!--END v-Data -->    <link href=\"/jcp/assets/warranty-modal/css/warranty-modal.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <link href=\"/jcp/assets/confirm-popup/css/confirm-popup.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <!-- Prd Suggesstion -->    <link href=\"/jcp/common/themes/css/slick.css?v=R2014_6.6.0_B1.1\" rel=\"stylesheet\">    <script src=\"/jcp/common/js/all-min.js?v=R2014_6.6.0_B1.1\">    </script>    <script src=\"/jcp/common/js/typeahead.bundle-min.js?v=R2014_6.6.0_B1.1\">    </script>    <script src=\"/jcp/share/angular/angular.min.js?v=R2014_6.6.0_B1.1\">          </script>    <script src=\"/jcp/share/angular/angular-cookies.min.js?v=R2014_6.6.0_B1.1\">    </script>    <script src=\"/jcp/share/angular/angular-sanitize.min.js?v=R2014_6.6.0_B1.1\">          </script>    <script src=\"/jcp/share/framework/Framework.js?v=R2014_6.6.0_B1.1\">          </script>    <script src=\"/jcp/common/js/apputil.js?v=R2014_6.6.0_B1.1\">    </script>                                                    	    <script src=\"/jcp/share/adaptive/jcp.min.js?v=R2014_6.6.0_B1.1\">  </script>            <script src=\"/jcp/common/js/controllers/product-controller.js?v=R2014_6.6.0_B1.1\">      </script>  <script src=\"/jcp/common/js/slick.min.js?v=R2014_6.6.0_B1.1\">  </script>  <!-- JS -->    <script type=\"text/javascript\">    jcpDLjcp = {    };    jcpDLjcp.common = {    };    jcpDLjcp.common.shipToCountry = '';    jcpDLjcp.common.shipToCurrencyCode = '';    jcpDLjcp.common.vid = '';  </script>  <script type=\"text/javascript\" src=\"//nexus.ensighten.com/jcpenney/Bootstrap.js\" id=\"tagManagmentSource\">  </script>  <meta name=\"apple-itunes-app\" content=\"app-id=925338276\">  </head>  <body>    <div class=\"off-canvas-wrap\" ng-cloak=\"\">      <div class=\"inner-wrap\">                <!-- smart banner -->        <meta name=\"apple-itunes-app\" content=\"app-id=925338276\">                                                                                                                                                <main class=\"main-section\">    <div class=\"row\">      <div class=\"small-12 columns\">                                                                                            <div>        <div ng-controller=\"productController\" ng-init=\"initializeData({&quot;type&quot;:&quot;REGULAR&quot;,&quot;videoUrl&quot;:null,&quot;overSizedItemSurcharge&quot;:0.0,&quot;channelOffering&quot;:&quot;Omnichannel&quot;,&quot;knowledgeAssistants&quot;:[{&quot;kaId&quot;:&quot;40300044&quot;,&quot;title&quot;:&quot;travel buying guide&quot;,&quot;assetReferenceURL&quot;:null,&quot;editor&quot;:&quot;&lt;p&gt;&lt;img name=\\&quot;travel_guide\\&quot; src=\\&quot;http://www.jcpenney.com/dotcom/images/travel_buying_guide1.jpg\\&quot; style=\\&quot;width: 620px; height: 1548px;\\&quot; usemap=\\&quot;#imgmap2014521154139\\&quot; /&gt;&lt;map id=\\&quot;imgmap2014521154139\\&quot; name=\\&quot;imgmap2014521154139\\&quot;&gt;&lt;area _fcksavedurl=\\&quot;luggage\\&quot; alt=\\&quot;luggage\\&quot; coords=\\&quot;8,4,98,30\\&quot; onclick=\\&quot;document.images.travel_guide.src='/mobile/images/travel_buying_guide1.jpg';\\&quot; shape=\\&quot;rectangle\\&quot; style=\\&quot;display: block; cursor: pointer\\&quot; title=\\&quot;luggage\\&quot; /&gt; &lt;area _fcksavedurl=\\&quot;materials\\&quot; alt=\\&quot;materials\\&quot; coords=\\&quot;105,7,259,29\\&quot; onclick=\\&quot;document.images.travel_guide.src='/mobile/images/travel_buying_guide2.jpg';\\&quot; shape=\\&quot;rectangle\\&quot; style=\\&quot;display: block; cursor: pointer\\&quot; title=\\&quot;materials\\&quot; /&gt; &lt;area _fcksavedurl=\\&quot;tips\\&quot; alt=\\&quot;tips\\&quot; coords=\\&quot;266,2,344,29\\&quot; onclick=\\&quot;document.images.travel_guide.src='/mobile/images/travel_buying_guide3.jpg';\\&quot; shape=\\&quot;rectangle\\&quot; style=\\&quot;display: block; cursor: pointer\\&quot; title=\\&quot;tips\\&quot; /&gt; &lt;area _fcksavedurl=\\&quot;brands\\&quot; alt=\\&quot;brands\\&quot; coords=\\&quot;350,2,411,29\\&quot; onclick=\\&quot;document.images.travel_guide.src='/mobile/images/travel_buying_guide4.jpg';\\&quot; shape=\\&quot;rectangle\\&quot; style=\\&quot;display: block; cursor: pointer\\&quot; title=\\&quot;brands\\&quot; /&gt;&lt;/map&gt;&lt;/p&gt;\\r\\n&quot;,&quot;pdfAssetReference&quot;:false,&quot;startDate&quot;:&quot;May 21, 2014 12:00:00 AM&quot;,&quot;endDate&quot;:&quot;Dec 31, 2030 12:00:00 AM&quot;,&quot;dhtml&quot;:null,&quot;type&quot;:null}],&quot;itemId&quot;:&quot;04731900018&quot;,&quot;productUrls&quot;:[],&quot;description&quot;:&quot;&lt;p&gt;Whether you travel every week or once a year, this four-piece luggage set with a soft-sided design is the perfect set for any of your trips.&lt;/p&gt;\\r\\n&lt;div style=\\&quot;page-break-after: always;\\&quot;&gt;\\r\\n\\t&lt;span style=\\&quot;display: none;\\&quot;&gt;&amp;nbsp;&lt;/span&gt;&lt;/div&gt;\\r\\n&lt;ul&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\tuprights expand by 2&amp;quot;&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\t1 interior compartment on uprights&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\t2 exterior compartments on uprights&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\t1 exterior compartment on tote bag&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\tretractable handle on uprights&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\tnylon zippers on tote and travel kit&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\t2 inline wheels on uprights&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\tall bags are water resistant&lt;/li&gt;\\r\\n&lt;/ul&gt;\\r\\n&lt;p&gt;Polyester with polyester lining. Wipe clean. Imported.&lt;/p&gt;\\r\\n&lt;ul&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\t25&amp;quot; upright: 17x7x25&amp;quot;H; weighs 7.2 pounds&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\t21&amp;quot; carry-on upright: 13&amp;frac12;x5&amp;frac34;x20&amp;frac12;&amp;quot;H; weighs 6 pounds&amp;nbsp;&amp;nbsp;&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\ttote bag: 12x5&amp;frac12;x16&amp;quot;H; weighs 1 pound&amp;nbsp;&lt;/li&gt;\\r\\n\\t&lt;li&gt;\\r\\n\\t\\ttravel kit: 5x4&amp;frac12;x9&amp;frac12;&amp;quot;H; weighs 0.2 pounds&amp;nbsp;&lt;/li&gt;\\r\\n&lt;/ul&gt;\\r\\n&quot;,&quot;shortDescription&quot;:&quot;&lt;p&gt;Whether you travel every week or once a year, this four-piece luggage set with a soft-sided design is the perfect set for any of your trips.&lt;/p&gt;\\r\\n&quot;,&quot;recommendationsURL&quot;:&quot;http://m.jcpenney.com:80/v2/recommendations/product?id=pp5004510046&quot;,&quot;isTruckDeliveryRequired&quot;:false,&quot;messages&quot;:[],&quot;promotions&quot;:null,&quot;vDataOption&quot;:null,&quot;serviceAgreements&quot;:[],&quot;warranties&quot;:[],&quot;isFurnitureProduct&quot;:false,&quot;options&quot;:[{&quot;name&quot;:&quot;PRODUCT_OPTION&quot;,&quot;displayText&quot;:&quot;PRODUCT&quot;,&quot;optionValues&quot;:[{&quot;url&quot;:&quot;http://m.jcpenney.com:80/v2/products/pp5004510046?PRODUCT_OPTION=4-pc.%20Luggage%20Set&quot;,&quot;selected&quot;:false,&quot;available&quot;:true,&quot;value&quot;:&quot;4-pc. Luggage Set&quot;,&quot;image&quot;:null,&quot;inventoryMsg&quot;:null}]},{&quot;name&quot;:&quot;SKU_OPTION_COLOR&quot;,&quot;displayText&quot;:&quot;COLOR&quot;,&quot;optionValues&quot;:[{&quot;url&quot;:&quot;http://m.jcpenney.com:80/v2/products/pp5004510046?COLOR=Black&quot;,&quot;selected&quot;:false,&quot;available&quot;:true,&quot;value&quot;:&quot;Black&quot;,&quot;image&quot;:{&quot;url&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP1028201420511009C.tif?hei=380&amp;amp;wid=380&amp;op_usm=.4,.8,0,0&amp;resmode=sharp2&quot;,&quot;swatchUrl&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP0819201417055394S&quot;,&quot;type&quot;:&quot;SWATCH&quot;,&quot;altText&quot;:&quot;Black&quot;},&quot;inventoryMsg&quot;:null},{&quot;url&quot;:&quot;http://m.jcpenney.com:80/v2/products/pp5004510046?COLOR=Blue&quot;,&quot;selected&quot;:false,&quot;available&quot;:true,&quot;value&quot;:&quot;Blue&quot;,&quot;image&quot;:{&quot;url&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP1028201420511158C.tif?hei=380&amp;amp;wid=380&amp;op_usm=.4,.8,0,0&amp;resmode=sharp2&quot;,&quot;swatchUrl&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP0819201417055494S&quot;,&quot;type&quot;:&quot;SWATCH&quot;,&quot;altText&quot;:&quot;Blue&quot;},&quot;inventoryMsg&quot;:null},{&quot;url&quot;:&quot;http://m.jcpenney.com:80/v2/products/pp5004510046?COLOR=Red&quot;,&quot;selected&quot;:false,&quot;available&quot;:true,&quot;value&quot;:&quot;Red&quot;,&quot;image&quot;:{&quot;url&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP0819201417055544M.tif?hei=380&amp;amp;wid=380&amp;op_usm=.4,.8,0,0&amp;resmode=sharp2&quot;,&quot;swatchUrl&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP0819201417055594S&quot;,&quot;type&quot;:&quot;SWATCH&quot;,&quot;altText&quot;:&quot;Red&quot;},&quot;inventoryMsg&quot;:null}]}],&quot;inventory&quot;:null,&quot;stores&quot;:[],&quot;pages&quot;:[],&quot;comparativePrice&quot;:null,&quot;estDeliveryMsg&quot;:null,&quot;isWhiteGlovedDelivery&quot;:false,&quot;parentEnsembleId&quot;:null,&quot;relatedItems&quot;:null,&quot;vDataType&quot;:null,&quot;id&quot;:&quot;pp5004510046&quot;,&quot;webId&quot;:&quot;0473190&quot;,&quot;name&quot;:&quot;Protocol�� Roman 4-pc. Luggage Set&quot;,&quot;url&quot;:null,&quot;isNew&quot;:true,&quot;isPreOrder&quot;:false,&quot;rating&quot;:0.0,&quot;reviewCount&quot;:0,&quot;marketingLabel&quot;:null,&quot;brandLogoUrl&quot;:&quot;http://m.jcpenney.com:80/mobile/images/PP_logo_protocol.jpg&quot;,&quot;images&quot;:[{&quot;url&quot;:&quot;http://s7d9.scene7.com/is/image/JCPenney/DP1028201420511009C.tif&quot;,&quot;swatchUrl&quot;:null,&quot;type&quot;:&quot;PRIMARY&quot;,&quot;altText&quot;:&quot;Protocol�� Roman 4-pc. Luggage Set&quot;}],&quot;prices&quot;:[{&quot;max&quot;:160.0,&quot;min&quot;:160.0,&quot;type&quot;:&quot;ORIGINAL&quot;},{&quot;max&quot;:39.99,&quot;min&quot;:39.99,&quot;type&quot;:&quot;SALE&quot;}],&quot;reviewsURL&quot;:&quot;http://m.jcpenney.com:80/v2/products/pp5004510046/reviews&quot;})\">        <!---->                                                  <div>            <section id=\"size-chrt\" ng-controller=\"sizeChrtController\" ng-init=\"initialize('pp5004510046')\">              <section>                                                                                                                                <div id=\"knowassist1\" class=\"reveal-modal size-chart-scroll\" data-reveal=\"\">                  <a class=\"close-reveal-modal\">                    x                  </a>                  <p>                    <img name=\"travel_guide\" src=\"http://www.jcpenney.com/dotcom/images/travel_buying_guide1.jpg\" width=\"620\" height=\"1548\" style=\"width: 620px; height: 1548px;\" usemap=\"#imgmap2014521154139\">                    <map id=\"imgmap2014521154139\" name=\"imgmap2014521154139\">                      <area _fcksavedurl=\"luggage\" alt=\"luggage\" coords=\"8,4,98,30\" onclick=\"document.images.travel_guide.src='/mobile/images/travel_buying_guide1.jpg';\" shape=\"rectangle\" style=\"display: block; cursor: pointer\" title=\"luggage\">                                            <area _fcksavedurl=\"materials\" alt=\"materials\" coords=\"105,7,259,29\" onclick=\"document.images.travel_guide.src='/mobile/images/travel_buying_guide2.jpg';\" shape=\"rectangle\" style=\"display: block; cursor: pointer\" title=\"materials\">                                            <area _fcksavedurl=\"tips\" alt=\"tips\" coords=\"266,2,344,29\" onclick=\"document.images.travel_guide.src='/mobile/images/travel_buying_guide3.jpg';\" shape=\"rectangle\" style=\"display: block; cursor: pointer\" title=\"tips\">                                            <area _fcksavedurl=\"brands\" alt=\"brands\" coords=\"350,2,411,29\" onclick=\"document.images.travel_guide.src='/mobile/images/travel_buying_guide4.jpg';\" shape=\"rectangle\" style=\"display: block; cursor: pointer\" title=\"brands\">                    </map>                  </p>                                  </div>                                                                                              </section>            </section>          </div>                                                                                                                                                <!-- Removing stub data for now, once we have the api available make it dynamic -->    <!--<section><div id=\"get-details\" class=\"small reveal-modal\" data-reveal=\"\"><div class=\"row\"><div class=\"small-12 columns\"><h5>Get Details</h5><div class=\"get-details\"><b>furniture and mattress promotional financing offer details:</b><br/><br/><p><b>NO INTEREST IF PAID IN FULL WITHIN 12 MONTHS.</b>Offer applies to furniture and mattress purchases of $1,000 or more (after discounts) made on either a JCPenney Credit Card or a JCPenney Mastercard (excludes Baby Furniture, Puerto Rico store purchases and deposits on special orders). Interest will be charged to your account from the purchase date if the promotional purchases is not paid in full within 12 months. Discounts or other promotional offers that reduce the purchase amount may result in the minimum qualifying purchase amount requirement not being satisfied. Under the promotion, no interest will be assessed on the promotional purchase if you pay the promotional purchase amount in full within 12 months. If you do not, interest will be assessed on the promotional purchase from the date of purchase. Minimum monthly payments required. Making required minimum monthly payments may not pay off purchase by end of promotional period. Regular account terms apply to non-promotional purchases and, after promotion ends, to promotional purchases. For new JCPenney Credit Card accounts: Standard purchase APR is 26.99%. Minimum interest charge will be no less than $2.00. Existing cardholders should see their credit card agreement for their applicable terms. Promotional purchases of delayed delivery merchandise will be charged to account when merchandise is available for delivery. Subject to credit approval. PLEASE PRINT THIS INFORMATION FOR YOUR RECORDS.</p></div></div></div><a class=\"close-reveal-modal\"></a></div></section>-->                    <section class=\"jcp-ensemble-descript\" id=\"pp5004510046\" ng-controller=\"prdDesController\">            <dl class=\"accordion\" data-accordion=\"\">        <dd class=\"active\">          <a test-event=\"product-desc\" test-assert=\"product-desc\" href=\"#panel2pp5004510046\" class=\"title\">                        Product Description                        <div class=\"jcp-webid\">                            Web Id              0473190                          </div>          </a>                    <div id=\"panel2pp5004510046\" class=\"content active\">            <div>              <p>                Whether you travel every week or once a year, this four-piece luggage set with a soft-sided design is the perfect set for any of your trips.              </p>              <div style=\"page-break-after: always;\">                <span style=\"display: none;\">                  &nbsp;                </span>              </div>              <ul>                <li>                  uprights expand by 2\"                </li>                <li>                  1 interior compartment on uprights                </li>                <li>                  2 exterior compartments on uprights                </li>                <li>                  1 exterior compartment on tote bag                </li>                <li>                  retractable handle on uprights                </li>                <li>                  nylon zippers on tote and travel kit                </li>                <li>                  2 inline wheels on uprights                </li>                <li>                  all bags are water resistant                </li>              </ul>              <p>                Polyester with polyester lining. Wipe clean. Imported.              </p>              <ul>                <li>                  25\" upright: 17x7x25\"H; weighs 7.2 pounds                </li>                <li>                  21\" carry-on upright: 13��x5��x20��\"H; weighs 6 pounds&nbsp;&nbsp;                </li>                <li>                  tote bag: 12x5��x16\"H; weighs 1 pound&nbsp;                </li>                <li>                  travel kit: 5x4��x9��\"H; weighs 0.2 pounds&nbsp;                </li>              </ul>                          </div>          </div>                  </dd>        <!-- -->                <dd id=\"cusreview\">          <a href=\"#reviews\" class=\"title\" test-event=\"cust-review\" test-assert=\"cust-review\">            Customer Reviews                      </a>          <div id=\"reviews\" class=\"content clearfix review_content row\">            <div>                            <div class=\"pdp-review\" ng-repeat=\"review in reviews | limitTo:reviewMaxLength\">                <div class=\"row columns\">                  <div class=\"medium-4 columns\">                    <div class=\"star-rating\">                      <div data-rating=\"{{review.totalRating}}\">                      </div>                    </div>                  </div>                  <div class=\"medium-8 columns\">                    <span class=\"date\">                      {{review.submissionDate}}                    </span>                    <p class=\"jcp-review-name m0\">                      <span>                        {{review.title}}                      </span>                    </p>                    <p class=\"jcp-review-content m0\">                      {{review.text}}                    </p>                  </div>                </div>              </div>              <!--------------------------- JCP-PRD-REV-RAT START --------------------------->              <div data-reveal=\"\" class=\"small reveal-modal p10 jcp-position\" id=\"seereview\">                <div class=\"small-12  jcp-review-item \">                  <a href=\"#\">                    Review                  </a>                </div>                <div class=\"small-12 columns p0 \">                  <div class=\"divcenter product_name\">                    <h3 class=\"colr_headtxt\">                      Protocol�� Roman 4-pc. Luggage Set                    </h3>                    <p>                      Web Id                      0473190                    </p>                  </div>                  <div class=\"rating_review p0 row \">                    <div class=\"star-rating m0 p0\">                      <div data-rating=\"0.0\">                      </div>                    </div>                    <div class=\"review_txt\">                      <a href=\"#\">                        (0)                      </a>                      |                       <a id=\"write_review\" href=\"http://jcpenney.ugc.bazaarvoice.com/1573mobile/pp5004510046/writereview.htm?submissionurl=http%3A%2F%2Fm.jcpenney.com%3A80%2Fjcp%2FsubmissionPage.jsp&amp;return=http%3A%2F%2Fm.jcpenney.com%3A80%2Fjcp%2Fproduct.jsp%3FppId%3Dpp5004510046%26searchTerm%3DProtocol%25C3%2582%25C2%25AE%2BRoman%2B4-pc.%2BLuggage%2BSe%26catId%3DSearchResults%26_dyncharset%3DUTF-8%26colorizedImg%3DDP0819201417055544M.tif%26cm_mmc%3DAffiliates-_-g%2FkEfAT435U-_-1-_-10%26utm_medium%3Daffiliate%26utm_source%3Dg%2FkEfAT435U%26utm_campaign%3D1%26utm_content%3D10%26cvosrc%3Daffiliate.g%2FkEfAT435U.1_10%26siteID%3Dg%252FkEfAT435U-GCyjD6pb%252F%252AoghN49mHkJxQ%26r%3Dtrue\" onclick=\"cmCreateConversionEventTag('Write a review','1','Product Review',null,'');\">                        write a review                      </a>                    </div>                  </div>                </div>                <div class=\"medium-5 \">                  <div class=\"row sort collapse filter-primary-container\">                    <div class=\"small-12 medium-8 columns jcp-filter-drop\">                      <select class=\"shorter\" ng-options=\"option.value as option.name for option in sortOptions\" ng-model=\"sortType\" ng-change=\"sortReview()\">                      </select>                    </div>                  </div>                </div>                <div class=\"jcp-reviewall-scroll\">                  <div ng-repeat=\"review in reviews\">                    <div class=\"row columns p0\">                      <div class=\"medium-4 columns p0\">                        <div class=\"star-rating columns p0\">                          <div data-rating=\"{{review.totalRating}}\">                          </div>                        </div>                      </div>                      <div class=\"medium-8 columns p0\">                        <span class=\"date\">                          {{review.submissionDate}}                        </span>                        <p class=\"jcp-review-name m0\">                          <span>                            {{review.title}}                          </span>                        </p>                        <p class=\"jcp-review-content m0\">                          {{review.text}}                        </p>                      </div>                    </div>                  </div>                </div>                <a class=\"button expand {{showMore}}\" href=\"#\" ng-click=\"showMoreReview()\">                  Show more                </a>                <a class=\"close-reveal-modal\">                  ��                </a>              </div>            </div>            <div class=\"small-12 columns jcp-review-btm\" ng-if=\"(reviews.length&gt;reviewMaxLength)\">  <a data-reveal-id=\"seereview\" href=\"#\" class=\"jcp-readall\">    Read all reviews  </a>            </div>            <div class=\"medium-12 large-6 columns large-pull-6 jcp-review-top\">                                                        <a class=\"button small expand secondary\" id=\"write_review\" href=\"http://jcpenney.ugc.bazaarvoice.com/1573mobile/pp5004510046/writereview.htm?submissionurl=http%3A%2F%2Fm.jcpenney.com%3A80%2Fjcp%2FsubmissionPage.jsp&amp;return=http%3A%2F%2Fm.jcpenney.com%3A80%2Fjcp%2Fproduct.jsp%3FppId%3Dpp5004510046%26searchTerm%3DProtocol%25C3%2582%25C2%25AE%2BRoman%2B4-pc.%2BLuggage%2BSe%26catId%3DSearchResults%26_dyncharset%3DUTF-8%26colorizedImg%3DDP0819201417055544M.tif%26cm_mmc%3DAffiliates-_-g%2FkEfAT435U-_-1-_-10%26utm_medium%3Daffiliate%26utm_source%3Dg%2FkEfAT435U%26utm_campaign%3D1%26utm_content%3D10%26cvosrc%3Daffiliate.g%2FkEfAT435U.1_10%26siteID%3Dg%252FkEfAT435U-GCyjD6pb%252F%252AoghN49mHkJxQ%26r%3Dtrue\" onclick=\"cmCreateConversionEventTag('Write a review','1','Product Review',null,'');\">                Be the first to write a review              </a>                                        </div>          </div>        </dd>              </dl>    </section>        <div id=\"terms-conditions\" class=\"small reveal-modal\" data-reveal=\"\">      <div class=\"row\" test-assert=\"terms-popup\" test-event=\"terms-popup\">        <div class=\"small-12 columns\">          <h5>            Terms and Conditions          </h5>          <div class=\"terms\">            <p>              Yes, I want to receive promotional text alerts from jcpenney at the mobile number I provided, including coupons, advertisements, events, polls, give-aways, sweepstakes and contests, downloads and information alerts from jcpenney. I understand that I am not required to provide my consent as a condition of purchasing any goods or services.            </p>            <p>              Message and data rates may apply from your mobile provider. You will receive a confirmation text message that you must reply to with the requested keyword to complete registration.            </p>            <p>              By entering and submitting enrollment information above, you agree to receive automated promotional text alerts from jcpenney including up to 8 SMS messages per month (plus additional in-store messaging if WiFi is activated). You further agree that jcpenney may use location information (such as GPS data) from your mobile phone when you are in or near a jcpenney store and send you additional automated promotional text alerts based on location. Prices mentioned in the promotional text alerts may vary in Alaska and Puerto Rico. You also affirm that you are 18 years of age or older, are authorized to agree to receive promotional text alerts on this phone number and are responsible for any mobile message or data rates incurred. By replying the requested keyword to complete registration, persons 15 to 17 years of age are confirming parental consent to enroll their number in promotional text alerts except where restricted by state law. To opt out, reply               <strong>                STOP              </strong>              to any message or text               <strong>                STOP              </strong>              to 527365 (JCP365). Please note that texting               <strong>                STOP              </strong>              to 527365 will always result in an opt-out confirmation text to be sent to your phone. You will not receive any messages from jcp promotional text alerts thereafter, unless you explicitly text               <strong>                STOP              </strong>              ,               <strong>                HELP              </strong>              ,               <strong>                JOIN              </strong>              or other advertised keywords connected to the program. For help, reply               <strong>                HELP              </strong>              to any message or text               <strong>                HELP              </strong>              to 527365 (JCP365) or call               <a href=\"tel://1.800.322.1189\">                1.800.322.1189              </a>              or email               <a href=\"mailto:mobile@jcpenney.com\">                mobile@jcpenney.com              </a>              .            </p>            <p>              List of supported carriers as of July 2013. Carriers subject to change. AT&amp;T, T-Mobile��, Verizon Wireless, U.S. Cellular, Sprint, ACS Wireless, Alltel, Bluegrass Cellular, Boost, Cellcom, Cellular One, Cincinnati Bell Wireless, Cricket, Cross, Element Mobile, EpicTouch, GCI Communications, Hawkeye, Inland Cellular, Keystone Wireless (Immix/PC Management), Maximum (AKA Max/Benton/Albany), Metro PCS, Nex-Tech Wireless, nTelos, Peoples Wireless, Pioneer, Plateau, Revol Wireless, Thumb Cellular, Union Wireless, United Wireless, Virgin Mobile.            </p>          </div>          <a test-event=\"terms-okbtn\" test-assert=\"terms-okbtn\" href=\"#\" class=\"button medium expand close-reveal-modal\" style=\"position:relative;\">            OK          </a>                  </div>      </div>    </div>          </div>  <div class=\"row collapse share-product\">                                    <section id=\"prd-Socialnet\">      <div class=\"large-3 medium-4 columns p0\" test-event=\"share-prod\" test-assert=\"share-prod\">        <label>          Share        </label>        <ul class=\"social-icons\">          <li test-event=\"share-fb\" test-assert=\"share-fb\">            <a href=\"http://m.jcpenney.com/mobile/jsp/browse/socialMediaTargetURL.jsp?ppId=pp5004510046&amp;key=facebook&amp;srcPage=\" target=\"new\">              <svg width=\"36\" height=\"36\" alt=\"Facebook\">                <image test-event=\"fb-img\" test-assert=\"fb-img\" width=\"36\" height=\"36\" xlink:href=\"/jcp/common/themes/images/social-icons/facebook.svg\" src=\"/jcp/common/themes/images/social-icons/facebook.svg\"></image>              </svg>            </a>          </li>          <li test-event=\"share-twitter\" test-assert=\"share-twitter\">            <a href=\"http://m.jcpenney.com/mobile/jsp/browse/socialMediaTargetURL.jsp?ppId=pp5004510046&amp;key=twitternongr&amp;srcPage=\" target=\"new\">              <svg width=\"36\" height=\"36\" alt=\"Twitter\">                <image test-event=\"twitter-img\" test-assert=\"twitter-img\" width=\"36\" height=\"36\" xlink:href=\"/jcp/common/themes/images/social-icons/twitter.svg\" src=\"/jcp/common/themes/images/social-icons/twitter.svg\"></image>              </svg>            </a>          </li>          <li test-event=\"share-pinterest\" test-assert=\"share-pinterest\">            <a href=\"http://www.pinterest.com/pin/create/button/?url=http://www.jcpenney.com/prod.jump?ppId=pp5004510046&amp;description=&lt;p&gt;Whether you travel every week or once a year, this four-piece luggage set with a soft-sided design is the perfect set for any of your trips.&lt;/p&gt;&lt;div style=&quot;page-break-after: always;&quot;&gt;&lt;span style=&quot;display: none;&quot;&gt;&amp;nbsp;&lt;/span&gt;&lt;/div&gt;&lt;ul&gt;&lt;li&gt;uprights expand by 2&amp;quot;&lt;/li&gt;&lt;li&gt;1 interior compartment on uprights&lt;/li&gt;&lt;li&gt;2 exterior compartments on uprights&lt;/li&gt;&lt;li&gt;1 exterior compartment on tote bag&lt;/li&gt;&lt;li&gt;retractable handle on uprights&lt;/li&gt;&lt;li&gt;nylon zippers on tote and travel kit&lt;/li&gt;&lt;li&gt;2 inline wheels on uprights&lt;/li&gt;&lt;li&gt;all bags are water resistant&lt;/li&gt;&lt;/ul&gt;&lt;p&gt;Polyester with polyester lining. Wipe clean. Imported.&lt;/p&gt;&lt;ul&gt;&lt;li&gt;25&amp;quot; upright: 17x7x25&amp;quot;H; weighs 7.2 pounds&lt;/li&gt;&lt;li&gt;21&amp;quot; carry-on upright: 13&amp;frac12;x5&amp;frac34;x20&amp;frac12;&amp;quot;H; weighs 6 pounds&amp;nbsp;&amp;nbsp;&lt;/li&gt;&lt;li&gt;tote bag: 12x5&amp;frac12;x16&amp;quot;H; weighs 1 pound&amp;nbsp;&lt;/li&gt;&lt;li&gt;travel kit: 5x4&amp;frac12;x9&amp;frac12;&amp;quot;H; weighs 0.2 pounds&amp;nbsp;&lt;/li&gt;&lt;/ul&gt;&amp;media=http://s7d9.scene7.com/is/image/JCPenney/DP1028201420511009C?hei=380&amp;wid=380&amp;op_usm=.4,.8,0,0&amp;resmode=sharp2\" target=\"new\">  <svg width=\"36\" height=\"36\" alt=\"Pinterest\">    <image test-event=\"pinterest-img\" test-assert=\"pinterest-img\" width=\"36\" height=\"36\" xlink:href=\"/jcp/common/themes/images/social-icons/pinterest.svg\" src=\"/jcp/common/themes/images/social-icons/pinterest.svg\"></image>  </svg>            </a>          </li>          <li test-event=\"share-gplus\" test-assert=\"share-gplus\">            <a href=\"https://plus.google.com/share?url=http://www.jcpenney.com/prod.jump?ppId=pp5004510046&amp;_dyncharset=UTF-8&amp;colorizedImg=http://s7d9.scene7.com/is/image/JCPenney/DP1028201420511009C\" target=\"new\">              <svg width=\"36\" height=\"36\" alt=\"Google+\">                <image test-event=\"gplus-img\" test-assert=\"gplus-img\" width=\"36\" height=\"36\" xlink:href=\"/jcp/common/themes/images/social-icons/google-plus.svg\" src=\"/jcp/common/themes/images/social-icons/google-plus.svg\"></image>              </svg>            </a>          </li>          <li test-event=\"share-mail\" test-assert=\"share-mail\">            <a href=\"/mobile/jsp/browse/shareProductEmail.jsp?r=true&amp;grview=&amp;startindex=&amp;lotId=&amp;catidback=&amp;subcatid=&amp;srcPage=&amp;ppId=pp5004510046\" target=\"_self\">              <svg width=\"36\" height=\"36\" alt=\"Email\">                <image test-event=\"mail-img\" test-assert=\"mail-img\" width=\"36\" height=\"36\" xlink:href=\"/jcp/common/themes/images/social-icons/email.svg\" src=\"/jcp/common/themes/images/social-icons/email.svg\"></image>              </svg>            </a>          </li>        </ul>      </div>    </section>      </div>    </div>    <div class=\"small-12 columns\">    <section id=\"jcp-sugges\">      <!-- start: suggested products -->      <div class=\"suggestion-prd all-hot product-suggesion-container row\" ng-controller=\"prdSuggestionController\">        <div class=\"small-12 medium-12 columns p0\">          <h5 class=\"product-suggesion-heading\" test-event=\"strategy-msg\" test-assert=\"strategy-msg\">            {{strategyMsg}}          </h5>          <div class=\"row rec-hldr\">            <div class=\"rec-wrapper slick_suggesstion product-result\">              <div class=\"rec-product\" ng-repeat=\"product in suggesprd\" prdsuggessionend=\"\">                <div class=\"carousel-content-container\">                  <div class=\"product\">                    <div class=\"thumb\">                      <a href=\"javascript:void(0)\" ng-repeat=\"image in product.images | filter:{ type: 'PRIMARY'}\" ng-click=\"selectedProduct(product.id)\">                                                <img test-event=\"suggestion-prod\" test-assert=\"suggestion-prod\" ng-src=\"{{image.url}}?wid=180&amp;hei=180&amp;op_usm=.4,.8,0,0&amp;resmode=sharp2\" alt=\"{{image.altText}}\">                      </a>                    </div>                  </div>                  <div class=\"price\">                    <h6 ng-click=\"selectedProduct(product.id)\" class=\"title\">                      {{product.name}}                    </h6>                    <p class=\"sale-price\" ng-if=\"product.salePrice.length &gt;0\">  Sale {{product.salePrice}}                    </p>                    <p class=\"sale-price\" ng-if=\"product.clearancePrice.length &gt;0\">  Clearance {{product.clearancePrice}}                    </p>                    <p class=\"old-price\" ng-if=\"product.originalPrice.length &gt;0\">  Original {{product.originalPrice}}                    </p>                  </div>                  <div class=\"rating-and-reviews\" ng-if=\"product.rating &gt;0\">  <div class=\"star-rating customrating5\">    <div data-rating=\"{{product.rating}}\">    </div>  </div>  <div class=\"reviews-count\">    <span title=\"reviews\">      ({{product.reviewCount}})    </span>  </div>                  </div>                </div>              </div>            </div>          </div>        </div>        <!-- end: suggested products -->      </div></section>    </div>  </div>  </div>  </main>          <a class=\"exit-off-canvas\">  </a>  <footer class=\"sub line\">        <section id=\"jcp-footer-link\">      <div class=\"row\">        <div class=\"large-10 large-centered columns\">          <ul class=\"inline\">            <li>              <a onclick=\"window.location.href='http://www.jcpenney.com/?stop_mobi=yes'\" href=\"#\" test-event=\"footer-about\" test-assert=\"footer-about\">                <h6>                  Full Site                </h6>              </a>            </li>            <li>              <h6>                <a onclick=\"window.location.href='/mobile/jsp/customerservice/serviceContent.jsp?r=true&amp;pageId=pg40027900018&amp;isFooter=true&amp;title=privacy%20policy#'\" href=\"#\" test-event=\"copyright\" test-assert=\"copyright\">                  Copyright &amp; Legal Notice                </a>              </h6>            </li>            <li>              <a onclick=\"window.location.href='/mobile/jsp/customerservice/serviceContent.jsp?r=true&amp;pageId=pg40027900018&amp;isFooter=true&amp;title=privacy%20policy'\" href=\"#\" test-event=\"privacy-policy\" test-assert=\"privacy-policy\">                <h6>                  privacy policy                </h6>              </a>            </li>            <li>              <a onclick=\"window.location.href='/mobile/jsp/customerservice/serviceContent.jsp?r=true&amp;pageId=pg40027900020&amp;isFooter=true&amp;title=your%20California%20privacy%20rights'\" href=\"#\" test-event=\"privacy-rights\" test-assert=\"privacy-rights\">                <h6>                  your California Privacy Rights                </h6>              </a>            </li>            <li>              <a href=\"\" id=\"prodtxt\" test-event=\"product-recalls\" test-assert=\"product-recalls\" onclick=\"window.location.href='http://www.jcpenney.net/Customers/Product-Recalls.aspx'\">                <h6>                  product recalls                </h6>              </a>            </li>            <li>              <a href=\"\" test-event=\"site-map\" test-assert=\"site-map\" onclick=\"window.location.href='http://www.jcpenney.com/dotcom/sitemap/index.jsp'\">                <h6>                  site map                </h6>              </a>            </li>            <li>              <a href=\"\" test-event=\"adchoices\" test-assert=\"adchoices\" onclick=\"window.location.href='http://www.aboutads.info/choices/'\">                <h6>                  AdChoices                   <img src=\"/jcp/common/themes/images/adchoices-icon.png\" alt=\"Adchoices icon\" title=\"Adchoices icon\">                </h6>              </a>            </li>          </ul>          <div class=\"columns text-center copyright\" test-event=\"all-rights\" test-assert=\"all-rights\">            <h6>              ��JCP Media�� Inc��� 2014��� All Rights Reserved            </h6>          </div>        </div>      </div>    </section>      </footer>              <input type=\"hidden\" id=\"cmCatId\" value=\"\">        <script type=\"text/javascript\">    /* Fixed defect - 2294.Including cordova js is overriding the default browser navigator object andeluminate.js is calling navigator.javaEnabled() which is blocking the coremetrics tag firing.*/    navigator.javaEnabled=function(){      return false;    }      function renderCoreMertic(jcp_t,jcp_c,jcp_m,cm_vc) {        cmSetClientID(\"90024642\",false,\"www88.jcpenney.com\",\"jcpenney.com\");        cmSetupOther({\"cm_FormPageID\":true}                    );        if (jcp_c != '' && jcp_c.charAt(jcp_c.length-1) == '/') {          jcp_c = jcp_c.substr(0, jcp_c.length-1);        }        var mmcParamArray = getMarketingAttributeFromCookie();              }  </script>  <script type=\"text/javascript\">    var cmJCP_C= '';    var cmJCP_T= '';    var cmJCP_MMC= 'Affiliates-_-g/kEfAT435U-_-1-_-10';    cmJCP_MMC = cmJCP_MMC.replace(new RegExp(\"-_-\", 'g'),\"|\");    var cm_vc= '';    renderCoreMertic(cmJCP_C,cmJCP_T,cmJCP_MMC,cm_vc);  </script>      </div></div><script src=\"/jcp/common/js/common.js\"></script></body></html>";
			String aSession = "GET /b?s=792600146&_R=&_L=m%06refresh-announcement%01l%06refresh-banner-dismiss%02refresh-banner-line1%02refresh-banner-line1%02refresh-banner-whats-new-cta%08m%06global-nav%01l%06Logo-main%02Signup-main%02Explore-main%02Explore-recent_photos%02Explore-the_commons%02Explore-getty_collection%02Explore-galleries%02Explore-world_map%02Explore-app_garden%02Explore-camera_finder%02Explore-flickr_blog%02Upload-main%02Account-sign_in%08m%06photo-container%01l%06action-menu-click%02%23%02%23comments%02share-menu-click%02prev_button%02lightbox%02next_button%08m%06comments%01l%06%2Fphotos%2F%2F%02%2Fhtml.gne%3Ftighten%3D0%26type%3Dcomment&t=1370377426&_P=2.9.4%05A_pn%03%2Fphoto.gne%04A_sid%03QhIPpAes0Fti%04_w%03www.flickr.com%2Fphotos%2F92457242%40N04%2F8404052962%2F%04A_%031 HTTP/1.1\r\nHost: geo.yahoo.com\r\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/536.29.13 (KHTML, like Gecko) Version/6.0.4 Safari/536.29.13\r\nAccept: */*\r\nReferer: http://www.flickr.com/photos/92457242@N04/8404052962/\r\nAccept"
			+ "-Language: en-us\r\nAccept-Encoding: gzip, deflate\r\nCookie: ucs=bnas=0; B=actnnip8iv3a6&b=3&s=4l\r\nConnection: keep-alive\r\n\r\n"
			+ "                                                                                                                                                                               "
			+ "HTTP/1.1 200 OK\r\nDate: Tue, 04 Jun 2013 20:23:44 GMT\r\nP3P: policyref=\"http://info.yahoo.com/w3c/p3p.xml\", CP=\"CAO DSP COR CUR ADM DEV TAI PSA PSD IVAi IVDi CONi TELo OTPi OUR DELi SAMi OTRi UNRi PUBi IND PHY ONL UNI PUR FIN COM NAV INT DEM CNT STA POL HEA PRE LOC GOV\"\r\nCache-Control: no-cache, no-store, private\r\nPragma: no-cache\r\nContent-Length: 43\r\nConnection: close\r\nContent-Type: image/gif\r\n\r\nGIF89a  ��  ������   !��    ,       D ;";

			Mockito.when(reqhelper.isJavaScript("text/html")).thenReturn(false);
			Mockito.when(reqhelper.isHtml("text/html")).thenReturn(true);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = MinificationImpl.runTest(tracedata);

		assertEquals("Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.", result.getAboutText());
		assertEquals("Minify CSS, JS, JSON and HTML", result.getDetailTitle());
		assertEquals("http://developer.att.com/ARO/BestPractices/Minification", result.getLearnMoreUrl());
		assertEquals("File Download: Minify CSS, JS, JSON and HTML", result.getOverviewTitle());
		//assertEquals("ARO detected 1 files that could be shrunk through minification, resulting in 0 kB savings.", result.getResultText());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());


	
	}

	/**
	 * tests css compression
	 */
	@Test   
	public void runTest_3(){
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		List<HttpRequestResponseInfo> reqList_2 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);
		
		Mockito.when((TraceDirectoryResult)tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);
		
		MinificationImpl = (MinificationImpl)context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentType()).thenReturn("text/css");
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = "GET /style.css HTTP/1.1\nHost: searchinsidevideo.com\nUser-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_3) AppleWebKit/536.29.13 (KHTML, like Gecko) Version/6.0.4 Safari/536.29.13"
					+ "\nAccept: text/css,*/*;q=0.1"
					+ "\nReferer: http://searchinsidevideo.com/\nAccept-Language: en-us\nAccept-Encoding: gzip, deflate\nConnection: keep-alive\n\nHTTP/1.1 200 OK\nDate: Tue, 04 Jun 2013 20:24:38 GMT\nServer: Apache\nLast-Modified: Fri, 28 Dec 2012 06:49:57 GMT\nETag: \"161827b-122c-4d1e41252af40\"\nAccept-Ranges: bytes\nContent-Length: 4652\nConnection: close\nContent-Type: text/css\n\n@charset \"UTF-8\";\r\n\r\nbody,div,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,\r\nform,fieldset,input,textarea,p,blockquote,th,td{\r\npadding:0;\r\nmargin:0;\r\n}\r\n\r\ntable{\r\nborder-collapse: collapse;\r\nborder-spacing:0;\r\n}\r\nimg{\r\nborder:0;\r\nline-height:0;\r\n}\r\nol,ul{\r\nlist-style:none;\r\n}\r\n\r\nbody{\r\nfont:12px/1.5 \"������������\",\"Meiryo\",arial,\"������������������ Pro W3\",\"Hiragino Kaku Gothic Pro\",Osaka,\"������ ���������������\",\"MS PGothic\",Sans-Serif;\r\ncolor:#333;\r\n}\r\n\r\n\r\na:link,a:visited{color:#438918;text-decoration:none;}\r\na:hover{color:#367f93;}\r\na:active, a:focus {outline:0;}\r\nimg{border:0;}\r\n\r\n\r\n\r\n/*******************************\r\n���������������\r\n*******************************/\r\n#header, #mainNav, #wrapper,#footer ul{\r\nmargin:0 auto;\r\nwidth:880px;\r\nclear:both;\r\n}\r\n\r\n#sidebar{\r\nfloat:left;\r\nwidth:233px;\r\npadding:22px 0 50px;\r\n}\r\n\r\n#main{\r\nfloat:right;\r\nwidth:627px;\r\npadding:22px 0 50px;\r\n}\r\n\r\n\r\n/*******************************\r\n/* ������������\r\n*******************************/\r\n#headerWrap{\r\nheight:147px;\r\nbackground:#fff url(images/wall.jpg) repeat-x 0 0;\r\n}\r\n\r\n#header{\r\nposition:relative;\r\nheight:147px;\r\n}\r\n\r\n#header h1,#header h2,#header p{\r\nposition:absolute;\r\ntop:31px;\r\nfont-size:10px;\r\nfont-weight:normal;\r\nline-height:22px;\r\n}\r\n\r\n/* ��������������� */\r\n#header h1{\r\ntop:2px;\r\nleft:0;\r\ncolor:#555;\r\n}\r\n\r\n/* ������ */\r\n#header h2{\r\nleft:0;\r\n}\r\n\r\n/* ������ */\r\n#header p{\r\nright:0;\r\n}\r\n\r\n\r\n/************************************\r\n/* ������������������������������\r\n************************************/\r\nul#mainNav{\r\nposition:absolute;\r\ntop:102px;\r\nheight:45px;\r\nbackground:url(images/mainNavBg.png) no-repeat 0 0;\r\n}\r\n\r\nul#mainNav li{\r\ntext-indent: -5000px;\r\nfloat:left;\r\n}\r\n\r\nul#mainNav a{\r\ndisplay: block;\r\nwidth: 176px;\r\nheight: 45px;\r\nbackground:url(images/mainNav1.jpg) no-repeat 0 0;\r\n}\r\n\r\nul#mainNav li.current_page_item a,ul#mainNav li.current-menu-item a,ul#mainNav li a:hover{background-position:0 -45px;}\r\n\r\nul#mainNav li.menu-item-2 a{background-image:url(images/mainNav2.jpg);}\r\nul#mainNav li.menu-item-3 a{background-image:url(images/mainNav3.jpg);}\r\nul#mainNav li.menu-item-4 a{background-image:url(images/mainNav4.jpg);}\r\nul#mainNav li.menu-item-5 a{background-image:url(images/mainNav5.jpg);}	\r\n\r\n\r\n\r\n/*******************************\r\n/* ���������\r\n*******************************/\r\n#mainImg{margin-bottom:20px;}\r\n\r\nh3.heading{\r\nclear:both;\r\npadding-left:30px;\r\nline-height:34px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\nbackground:url(images/headingBg.png) no-repeat 0 0;\r\n}\r\n\r\n.article{\r\nborder:0;\r\nmargin:0 0 20px 0;\r\npadding: 0 10px 0 10px;\r\nbackground:none;\r\nborder:1px solid #dcdcdc;\r\n}\r\n\r\n.article_cell{\r\nclear:both;\r\npadding:20px 0 25px;\r\nborder-bottom:1px dashed #dcdcdc;\r\n}\r\n\r\n.main{\r\npadding:20px 10px 20px 10px;\r\nmargin:0 0 20px 0;\r\nborder:0;\r\nbackground:none;\r\nborder:1px solid #dcdcdc;\r\n}\r\n\r\n.last{border-bottom:none;}\r\n\r\n.main h4{\r\nmargin:0 0 10px 10px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\n}\r\n\r\n.article h4{\r\nmargin:0 0 10px 10px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\n}\r\n\r\n.alignleft{\r\nfloat:left;\r\npadding:0 15px 15px 10px;\r\n}\r\n\r\n.alignright{\r\nfloat:right;\r\npadding: 0 10px 15px 15px;\r\n}\r\n\r\n.aligncenter{\r\npadding: 20px 0 20px 0;\r\ntext-align: center;\r\n}\r\n\r\n.main p{\r\npadding:0 10px 0 10px;\r\n}\r\n\r\n.article p{\r\npadding:0 10px 0 10px;\r\n}\r\n\r\n.picture{\r\nwidth:193px;\r\nfloat:left;\r\ntext-align:center;\r\nbackground-color:#ffffff;\r\nborder-top:1px solid #eaeaea;\r\nborder-right:1px solid #ddd;\r\nborder-bottom:1px solid #ccc;\r\nborder-left:1px solid #eaeaea;\r\npadding:5px 0 5px 0;\r\nmargin:5px 0 0 5px;\r\n}\r\n\r\n.picture:hover{\r\nbackground-color:#fffaef;\r\n}\r\n\r\n\r\n/*******************************\r\n/* ���������������\r\n*******************************/\r\n#sidebar h3{\r\nclear:both;\r\npadding-left:30px;\r\nline-height:34px;\r\nfont-size:16px;\r\nfont-weight:normal;\r\ncolor:#438918;\r\nbackground:url(images/side_headingBg.png) no-repeat 0 0;\r\n}\r\n\r\nul.info{\r\noverflow:hidden;\r\npadding:0 0 10px 17px;\r\nborder:1px solid #dcdcdc;\r\nmargin-bottom:20px;\r\n}\r\n\r\nul.info li{\r\nline-height:0;\r\npadding:10px 0;\r\nmargin-right:15px;\r\nborder-bottom:1px dashed #dcdcdc;\r\n}\r\n\r\nul.info a:link,ul.info a:visited{\r\ndisplay: block;\r\npadding-left:12px;\r\nline-height:normal;\r\ntext-decoration:none;\r\ncolor:#313131;\r\nbackground:url(images/linkArrow.gif) no-repeat 0 50%;\r\n}\r\n\r\nul.info a:hover, ul.info li.current_page_item a, ul.info li.current-menu-item a{color:#438918;}\r\n\r\nul.info li.last{border-bottom:none;}\r\n\r\n#sidebar p{margin-bottom:20px;}\r\n\r\n/*******************************\r\n/* ������������\r\n*******************************/\r\n#footer{\r\nclear:both;\r\nbackground:#a2ae52;\r\n}\r\n\r\n#footer ul{\r\npadding:25px 0;\r\ntext-align:center;\r\n}\r\n\r\n#footer li{\r\ndisplay: inline;\r\npadding: 5px 16px;\r\nborder-left:1px dotted #e2f0d9;\r\n}\r\n\r\n#footer li a{\r\ntext-decoration:none;\r\ncolor:#fff;\r\n}\r\n\r\n#footer li a:hover{color:#e2f0d9;}\r\n\r\np#copy{\r\npadding:10px 0 37px;\r\ntext-align:center;\r\ncolor:#fff;\r\nfont-size:10px;\r\n}"
			+ "";

			Mockito.when(reqhelper.isJavaScript("text/javascript")).thenReturn(false);
			Mockito.when(reqhelper.isCss("text/css")).thenReturn(true);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = MinificationImpl.runTest(tracedata);

//		System.out.println(result.getAboutText());
//		System.out.println(result.getDetailTitle());
//		System.out.println(result.getLearnMoreUrl());
//		System.out.println(result.getOverviewTitle());
//		System.out.println(result.getResultText());
//		System.out.println(result.getBestPracticeType());
//		System.out.println(result.getResultType().toString());

		assertEquals("Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.", result.getAboutText());
		assertEquals("Minify CSS, JS, JSON and HTML", result.getDetailTitle());
		assertEquals("http://developer.att.com/ARO/BestPractices/Minification", result.getLearnMoreUrl());
		assertEquals("File Download: Minify CSS, JS, JSON and HTML", result.getOverviewTitle());
		//assertEquals("ARO detected 1 files that could be shrunk through minification, resulting in 1 kB savings.", result.getResultText());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());

	}

	/**
	 * tests Javascript compression
	 */
	@Test   
	public void runTest_4(){
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		List<HttpRequestResponseInfo> reqList_2 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);
		
		Mockito.when((TraceDirectoryResult)tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);
		
		MinificationImpl = (MinificationImpl)context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentType()).thenReturn("text/javascript");
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = ""
					+ "/*! jQuery v1.7.1 jquery.com | jquery.org/license */"
			+ ""
			+ "(function(a, b) {\r\n	function myFunction(p1, p2) {\r\n	    return p1 * p2;\r\n	}\r\n})(window);"
			+ "";

			Mockito.when(reqhelper.isJavaScript("text/javascript")).thenReturn(true);
			Mockito.when(reqhelper.isCss("text/css")).thenReturn(false);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = MinificationImpl.runTest(tracedata);

//		System.out.println(result.getAboutText());
//		System.out.println(result.getDetailTitle());
//		System.out.println(result.getLearnMoreUrl());
//		System.out.println(result.getOverviewTitle());
//		System.out.println(result.getResultText());
//		System.out.println(result.getBestPracticeType());
//		System.out.println(result.getResultType().toString());

		assertEquals("Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.", result.getAboutText());
		assertEquals("Minify CSS, JS, JSON and HTML", result.getDetailTitle());
		assertEquals("http://developer.att.com/ARO/BestPractices/Minification", result.getLearnMoreUrl());
		assertEquals("File Download: Minify CSS, JS, JSON and HTML", result.getOverviewTitle());
		//assertEquals("ARO detected 1 files that could be shrunk through minification, resulting in 0 kB savings.", result.getResultText());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());

	}

	/**
	 * tests JSON compression
	 */
	@Test   
	public void runTest_5(){
		List<Session> sessionlist;
		Session session_1;
		HttpRequestResponseInfo req_1;
		HttpRequestResponseInfo rr_2;

		session_1 = mock(Session.class);
		sessionlist = new ArrayList<Session>();
		sessionlist.add(session_1);

		req_1 = mock(HttpRequestResponseInfo.class);
		rr_2 = mock(HttpRequestResponseInfo.class);
		List<HttpRequestResponseInfo> reqList_1 = new ArrayList<HttpRequestResponseInfo>();
		List<HttpRequestResponseInfo> reqList_2 = new ArrayList<HttpRequestResponseInfo>();
		reqList_1.add(req_1);
		reqList_1.add(rr_2);
		
		Mockito.when((TraceDirectoryResult)tracedata.getTraceresult()).thenReturn(dirdata);
		Mockito.when(dirdata.getTraceResultType()).thenReturn(TraceResultType.TRACE_DIRECTORY);
		Mockito.when(session_1.getRequestResponseInfo()).thenReturn(reqList_1);
		Mockito.when(tracedata.getSessionlist()).thenReturn(sessionlist);
		Mockito.when(req_1.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentLength()).thenReturn(5);
		
		MinificationImpl = (MinificationImpl)context.getBean("minify");

		Mockito.when(rr_2.getDirection()).thenReturn(HttpDirection.RESPONSE);
		Mockito.when(req_1.getContentType()).thenReturn("application/json");
		Mockito.when(rr_2.getAssocReqResp()).thenReturn(req_1);
		Mockito.when(rr_2.getContentLength()).thenReturn(5);
		Mockito.when(req_1.getAssocReqResp()).thenReturn(rr_2);
		Mockito.when(rr_2.getObjName()).thenReturn("/images/travel_buying_guide1.jpg");
		IHttpRequestResponseHelper reqhelper = mock(IHttpRequestResponseHelper.class);
		MinificationImpl.setHttpRequestResponseHelper(reqhelper);
		try {
			String aSession = 
					"[\n    {\n        \"name\": \"Dow Jones Industrial Average\",\n        \"display_name\": \"Dow Jones Industrial Average\",\n        \"symbol\": \"^DJI\",\n        \"price\": \"15,291.36\",\n        \"change\": \"-96.22\",\n        \"per_change\": \"-0.63%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^DJI/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    },\n    {\n        \"name\": \"S&P 500\",\n        \"display_name\": \"S&P 500\",\n        \"symbol\": \"^GSPC\",\n        \"price\": \"1,651.56\",\n        \"change\": \"-17.60\",\n        \"per_change\": \"-1.05%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^GSPC/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    },\n    {\n        \"name\": \"NASDAQ Composite\",\n        \"display_name\": \"NASDAQ Composite\",\n        \"symbol\": \"^IXIC\",\n        \"price\": \"3,451.57\",\n        \"change\": \"-50.55\",\n        \"per_change\": \"-1.44%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^IXIC/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    },\n    {\n        \"name\": \"FTSE 100\",\n        \"display_name\": \"FTSE 100\",\n        \"symbol\": \"^FTSE\",\n        \"price\": \"6,840.27\",\n        \"change\": \"36.40\",\n        \"per_change\": \"+0.53%\",\n        \"chart_uri\": \"http://chart.finance.yahoo.com/instrument/1.0/^FTSE/chart;range=1d/image;size=170x65?region=US&lang=en-US\"\n    }\n]"
					;

			Mockito.when(reqhelper.isJSON("application/json")).thenReturn(true);
			Mockito.when(reqhelper.isCss("text/css")).thenReturn(false);
			Mockito.when(reqhelper.getContentString(rr_2, session_1)).thenReturn(aSession);
			Mockito.when(reqhelper.getContentString(req_1, session_1)).thenReturn(aSession);
			Mockito.when(req_1.getObjName()).thenReturn("/en/top100-css-websites.html");
			Mockito.when(session_1.getDomainName()).thenReturn("www.google.com");
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		result = MinificationImpl.runTest(tracedata);

//		System.out.println(result.getAboutText());
//		System.out.println(result.getDetailTitle());
//		System.out.println(result.getLearnMoreUrl());
//		System.out.println(result.getOverviewTitle());
//		System.out.println(result.getResultText());
//		System.out.println(result.getBestPracticeType());
//		System.out.println(result.getResultType().toString());

		assertEquals("Many text files contain excess whitespace to allow for better human coding. Run these files through a minifier to remove the whitespace in order to reduce file size.", result.getAboutText());
		assertEquals("Minify CSS, JS, JSON and HTML", result.getDetailTitle());
		assertEquals("http://developer.att.com/ARO/BestPractices/Minification", result.getLearnMoreUrl());
		assertEquals("File Download: Minify CSS, JS, JSON and HTML", result.getOverviewTitle());
		//assertEquals("ARO detected 1 files that could be shrunk through minification, resulting in 0 kB savings.", result.getResultText());
		assertEquals("MINIFICATION", result.getBestPracticeType().toString());
		assertEquals("FAIL", result.getResultType().toString());

	}


}
