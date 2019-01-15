package com.mockrunner.test.httpserver;

import org.junit.Test;

import com.github.kristofa.test.http.HttpResponse;
import com.github.kristofa.test.http.file.HttpResponseFileReaderImpl;
import com.mockrunner.httpserver.CustFileHttpResponseProxy;

import junit.framework.Assert;

public class CustFileHttpResponseProxyTest {

	private final static String TEST_FILE_DIRECTORY = "target/test-classes/";

	@Test
	public void testCustFileHttpResponseProxy_CustFileName() {
		String respFileName = TEST_FILE_DIRECTORY + "CustFileHttpResponseProxyTest_response_00001.txt";
		String respEntityFileName = TEST_FILE_DIRECTORY + "CustFileHttpResponseProxyTest_response_entity_00001.txt";

		CustFileHttpResponseProxy proxy = new CustFileHttpResponseProxy(respFileName, respEntityFileName,
				new HttpResponseFileReaderImpl());
		HttpResponse response = proxy.consume();
		Assert.assertEquals(response.getHttpCode(), 200);
		Assert.assertEquals(response.getContentType(), "application/json");
		Assert.assertEquals(new String(response.getContent()), "OK");
	}

	@Test
	public void testCustFileHttpResponseProxy_ResponseFileNotFound() {
		try {
			String respFileName = TEST_FILE_DIRECTORY + "CustFileHtsstpResponseProxyTest_response_1.txt";
			String respEntityFileName = TEST_FILE_DIRECTORY + "CustFilxxeHttpResponseProxyTest_response_entity_1.txt";

			CustFileHttpResponseProxy proxy = new CustFileHttpResponseProxy(respFileName, respEntityFileName,
					new HttpResponseFileReaderImpl());
			HttpResponse response = proxy.consume();
			Assert.fail("no handle the file not exists exception");
		} catch (IllegalStateException e) {
		}
	}

	@Test
	public void testCustFileHttpResponseProxy_ResponseEntityFileNotFound() {
		String respFileName = TEST_FILE_DIRECTORY + "CustFileHttpResponseProxyTest_response_00001.txt";
		String respEntityFileName = TEST_FILE_DIRECTORY + "CustFilxxeHttpResponseProxyTest_response_entity_00001.txt";

		CustFileHttpResponseProxy proxy = new CustFileHttpResponseProxy(respFileName, respEntityFileName,
				new HttpResponseFileReaderImpl());
		HttpResponse response = proxy.consume();
		Assert.assertEquals(response.getHttpCode(), 200);
		Assert.assertEquals(response.getContentType(), "application/json");
		Assert.assertNull(response.getContent());
	}

}
