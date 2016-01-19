/**
 * 
 */
package com.att.aro.core.packetanalysis.impl;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import com.att.aro.core.BaseTest;
import com.att.aro.core.packetanalysis.IParseHeaderLine;
import com.att.aro.core.packetanalysis.pojo.HttpDirection;
import com.att.aro.core.packetanalysis.pojo.HttpRequestResponseInfo;

/**
 * ParseHeaderLineTest
 * 
 * @author Barry Nelson
 *
 */
public class ParseHeaderLineTest extends BaseTest {

	ParseHeaderLineImpl parser;

	HttpRequestResponseInfo rrInfo = null;
	
	@Before
	public void setUp() {
		parser = (ParseHeaderLineImpl)context.getBean(IParseHeaderLine.class);
		MockitoAnnotations.initMocks(this);
		rrInfo = new HttpRequestResponseInfo();
	}
	
	@Test
	public void test() throws IOException {
		
		// contains 9 lines of header data
		String[] lines = {
				"POST /com.nelsoft.areeba/Services/Login/RequestLoginUser HTTP/1.1" + "\r\n"
				, "Accept: application/json" + "\r\n"
				, "Content-type: text/plain" + "\r\n"
				, "Content-Length: 229" + "\r\n"
				, "Host: 24.16.97.108:8080" + "\r\n"
				, "Connection: Keep-Alive" + "\r\n"
				, "User-Agent: Apache-HttpClient/UNAVAILABLE (java 1.4)" + "\r\n"
				, "\r\n"
				, "{\"accountID\":0,\"tokenID\":-5,\"userID\":-1,\"status\":\"\",\"serverHashKey\":\"\",\"deviceHash\":\"\",\"userPIN\":\"1234\",\"userName\":\"Barry\",\"deviceIMEI\":\"358239057521132 LGE Model Nexus 5 Version 4.4.2\",\"deviceIMSI\":\" \",\"versionNumber\":\"1.0.0.0\"}"
		};
		
		for (String line : lines){
			parser.parseHeaderLine(line, rrInfo);
		}
		
		String[] splits = rrInfo.getAllHeaders().split("\r\n");

		assertTrue((rrInfo.getAllHeaders().split("\r\n")).length == 9);
		
		for (int index=0 ; index < lines.length; index++){
			assertTrue(rrInfo.getAllHeaders().contains(lines[index]));
		}

		assertTrue(rrInfo.getHostName().equals("24.16.97.108"));		
		
		parser.parseHeaderLine(lines[0], rrInfo);
		parser.parseHeaderLine("Host: i2.cdn.turner.com\r\n", rrInfo);
		parser.parseHeaderLine("Cache-Control: max-age=10, public\r\n", rrInfo);
		assertTrue(rrInfo.getHostName().equals("i2.cdn.turner.com"));		
	}
	
	@Test
	public void testContentType() throws IOException {
		
		parser.parseHeaderLine("Content-Type: application/x-javascript; charset=utf-8\r\n", rrInfo);
		assertTrue(rrInfo.getCharset().equals("utf-8"));		
		
	}
	
	@Test
	public void testHostName() throws IOException {
		
		parser.parseHeaderLine("Host: test.site.com\r\n", rrInfo);
		assertTrue(rrInfo.getHostName().equals("test.site.com"));		
	}
	
	@Test
	public void testContentLength() throws IOException {
		
		rrInfo = new HttpRequestResponseInfo();
		
		// bad parse
		parser.parseHeaderLine("Content-Length: 2z9" + "\r\n", rrInfo);
		assertTrue(rrInfo.getContentLength() == 0);	
		
		// parse works properly
		parser.parseHeaderLine("Content-Length: 229" + "\r\n", rrInfo);
		assertTrue(rrInfo.getContentLength() == 229);	
		
		// value is already set, so no change made
		parser.parseHeaderLine("Content-Length: 52" + "\r\n", rrInfo);
		assertTrue(rrInfo.getContentLength() == 229);	
	}
	
	@Test
	public void testMisc() throws IOException {
		
		// chunked
		parser.parseHeaderLine("Transfer-Encoding: chunked\r\n", rrInfo);
		assertTrue(rrInfo.isChunked());
		
		parser.parseHeaderLine("Content-Encoding: gzip\r\n", rrInfo);
		assertTrue(rrInfo.getContentEncoding().equals("gzip"));
		
		parser.parseHeaderLine("Date: Thu, 11 Dec 2014 00:56:39 GMT\r\n", rrInfo);
		assertTrue(rrInfo.getDate().getTime() == 1418259399000L);
		
		
		parser.parseHeaderLine("Pragma: no-cache\r\n", rrInfo);
		assertTrue(rrInfo.isPragmaNoCache());
		assertTrue(rrInfo.isHasCacheHeaders());
	}
	
	@Test
	public void testREQUEST() throws IOException {
		
		rrInfo.setDirection(HttpDirection.REQUEST);
		parser.parseHeaderLine("Cache-Control: max-age=12, max-stale=43, public, min-fresh=512\r\n", rrInfo);
		assertTrue(rrInfo.isHasCacheHeaders());
		assertTrue(rrInfo.getMaxAge() == 12);
		assertTrue(rrInfo.getMaxStale() == 43);
		assertTrue(rrInfo.getMinFresh() == 512);
		
		parser.parseHeaderLine("Referer: http://www.htmlgoodies.com/tutorials/web_graphics/article.php/3922901/How-Can-I-Create-Images-for-Mobile-Devices.htm\r\n", rrInfo);
		assertTrue(rrInfo.getReferrer().getHost().equals("www.htmlgoodies.com"));
		
		parser.parseHeaderLine("If-Modified-Since: Mon, 11 Jun 2012 18:15:40 GMT\r\n", rrInfo);
		assertTrue(rrInfo.isIfModifiedSince());
		
		parser.parseHeaderLine("If-None-Match: \"b7650a81dd5ee17e5d5951ccb4dcde19:1339438540\"", rrInfo);
		assertTrue(rrInfo.isIfNoneMatch());
		

		parser.parseHeaderLine("Cache-Control: max-age=0, no-cache, no-store, private, must-revalidate, s-maxage=0\r\n", rrInfo);
		assertTrue(rrInfo.isNoCache());
		assertTrue(rrInfo.isNoStore());

		parser.parseHeaderLine("Cache-Control: only-if-cached\r\n", rrInfo);
		assertTrue(rrInfo.isOnlyIfCached());
		
	}
	
	@Test
	public void testRESPONSE() throws IOException {
		
		rrInfo.setDirection(HttpDirection.RESPONSE);
		
		parser.parseHeaderLine("Cache-Control: max-age=12, max-stale=43, public, min-fresh=512\r\n", rrInfo);

		assertTrue(rrInfo.isPublicCache());		
		assertTrue(rrInfo.getMaxAge() == 12);
	
		parser.parseHeaderLine("Cache-Control: max-age=12, max-stale=43, private, min-fresh=512\r\n", rrInfo);
		assertTrue(rrInfo.isPrivateCache());		
		assertTrue(rrInfo.getMaxAge() == 12);

		parser.parseHeaderLine("Cache-Control: public, must-revalidate, proxy-revalidate, max-age=3600, s-maxage=3\r\n", rrInfo);
		assertTrue(rrInfo.isPrivateCache());		
		assertTrue(rrInfo.isMustRevalidate());
		assertTrue(rrInfo.isProxyRevalidate());
		assertTrue(rrInfo.getsMaxAge() == 3);

		parser.parseHeaderLine("Etag: W/\"1406-1245114063000\"", rrInfo);
		assertTrue(rrInfo.getEtag().equals("1406-1245114063000"));

		parser.parseHeaderLine("Age: 59541\r\n", rrInfo);
		assertTrue(rrInfo.getAge() == 59541L);

		parser.parseHeaderLine("Expires: Tue, 25 Jun 2013 01:01:21 GMT\r\n", rrInfo);
		assertTrue(rrInfo.getExpires().getTime() == 1372122081000L);

		parser.parseHeaderLine("Last-Modified: Fri, 01 Mar 2013 00:41:30 GMT\r\n", rrInfo);
		assertTrue(rrInfo.getLastModified().getTime() == 1362098490000L);

		parser.parseHeaderLine("Content-Range: bytes 12-34/56\r\n", rrInfo);
		assertTrue(rrInfo.isRangeResponse());
	}
	
}
