package com.mockrunner.test.httpserver;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import com.github.kristofa.test.http.ForwardHttpRequestBuilder;
import com.github.kristofa.test.http.FullHttpRequest;
import com.github.kristofa.test.http.FullHttpRequestImpl;
import com.github.kristofa.test.http.HttpMessageHeader;
import com.github.kristofa.test.http.HttpRequestResponseLogger;
import com.github.kristofa.test.http.HttpRequestResponseLoggerFactory;
import com.github.kristofa.test.http.HttpResponseImpl;
import com.github.kristofa.test.http.Method;
import com.github.kristofa.test.http.MockHttpServer;
import com.github.kristofa.test.http.SimpleHttpResponseProvider;
import com.mockrunner.httpserver.PartialMockingHttpServer;

public class PartialMockingHttpServerTest {

	private final static int PROXY_PORT = 51234;
	private final String PROXY_URL = "http://localhost:" + PROXY_PORT;
	private final static int PORT = 51233;

	private PartialMockingHttpServer proxy;
	private MockHttpServer server;
	private HttpClient client;
	private HttpRequestResponseLoggerFactory mockLoggerFactory;
	private HttpRequestResponseLogger mockLogger;
	private SimpleHttpResponseProvider responseProvider;
	private SimpleHttpResponseProvider proxyResponseProvider;

	@Before
	public void setup() throws Exception {

		final ForwardHttpRequestBuilder forwardHttpRequestBuilder = new ForwardHttpRequestBuilder() {

			@Override
			public FullHttpRequest getForwardRequest(final FullHttpRequest request) {
				final FullHttpRequestImpl forwardRequest = new FullHttpRequestImpl(request);
				forwardRequest.port(PORT);
				forwardRequest.domain("localhost");
				return forwardRequest;
			}
		};

		mockLoggerFactory = mock(HttpRequestResponseLoggerFactory.class);
		mockLogger = mock(HttpRequestResponseLogger.class);
		when(mockLoggerFactory.getHttpRequestResponseLogger()).thenReturn(mockLogger);

		proxyResponseProvider = new SimpleHttpResponseProvider();
		proxy = new PartialMockingHttpServer(PROXY_PORT, Arrays.asList(forwardHttpRequestBuilder), mockLoggerFactory,
				proxyResponseProvider);
		proxy.start();

		responseProvider = new SimpleHttpResponseProvider();
		server = new MockHttpServer(PORT, responseProvider);
		server.start();

		client = new DefaultHttpClient();
	}

	@After
	public void tearDown() throws Exception {
		proxy.stop();
		server.stop();
		client.getConnectionManager().shutdown();
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullForwardRequestBuilder() {
		new PartialMockingHttpServer(PROXY_PORT, null, mockLoggerFactory, proxyResponseProvider);
	}

	@Test(expected = IllegalArgumentException.class)
	public void noForwardRequestBuilder() {

		final Collection<ForwardHttpRequestBuilder> emptyCollection = Collections.emptyList();
		new PartialMockingHttpServer(PROXY_PORT, emptyCollection, mockLoggerFactory, proxyResponseProvider);
	}

	@Test(expected = NullPointerException.class)
	public void nullResponseProvider() {
		final ForwardHttpRequestBuilder mockRequestBuilder = mock(ForwardHttpRequestBuilder.class);
		new PartialMockingHttpServer(PROXY_PORT, Arrays.asList(mockRequestBuilder), mockLoggerFactory, null);
	}

	@Test
	public void successfulForwardRequestTest() throws ClientProtocolException, IOException {

		// Given a mock server configured to respond to a GET / with "OK"
		responseProvider.expect(Method.GET, "/").respondWith(200, "text/plain", "OK");

		final HttpGet req = new HttpGet(PROXY_URL + "/");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);

		final FullHttpRequestImpl expectedRequest = new FullHttpRequestImpl();
		expectedRequest.method(Method.GET);
		expectedRequest.path("/");
		expectedRequest.httpMessageHeader("Connection", "Keep-Alive");
		expectedRequest.httpMessageHeader("Host", "localhost:51234");
		expectedRequest.httpMessageHeader("User-Agent", "Apache-HttpClient/4.2.5 (java 1.5)");
		expectedRequest.port(-1);

		Set<HttpMessageHeader> httpMessageHeaders = new HashSet<HttpMessageHeader>();
		httpMessageHeaders.add(new HttpMessageHeader("Connection", "keep-alive"));
		httpMessageHeaders.add(new HttpMessageHeader("Content-Type", "text/plain"));
		httpMessageHeaders.add(new HttpMessageHeader("Transfer-Encoding", "chunked"));
		final HttpResponseImpl expectedResponse = new HttpResponseImpl(200, "text/plain", "OK".getBytes(),
				httpMessageHeaders);

		final InOrder inOrder = inOrder(mockLoggerFactory, mockLogger);
		inOrder.verify(mockLoggerFactory).getHttpRequestResponseLogger();
		inOrder.verify(mockLogger).log(expectedRequest);
		inOrder.verify(mockLogger).log(expectedResponse);
		verifyNoMoreInteractions(mockLogger, mockLoggerFactory, mockLogger);

	}

	@Test
	public void successfulParticialForwardRequestTest() throws ClientProtocolException, IOException {

		// Given a mock server configured to respond to a GET / with "OK"
		responseProvider.expect(Method.GET, "/").respondWith(200, "text/plain", "OK");
		proxyResponseProvider.expect(Method.GET, "/1024").respondWith(200, "text/plain", "1024-OK");

		HttpGet req = new HttpGet(PROXY_URL + "/");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);

		FullHttpRequestImpl expectedRequest = new FullHttpRequestImpl();
		expectedRequest.method(Method.GET);
		expectedRequest.path("/");
		expectedRequest.httpMessageHeader("Connection", "Keep-Alive");
		expectedRequest.httpMessageHeader("Host", "localhost:51234");
		expectedRequest.httpMessageHeader("User-Agent", "Apache-HttpClient/4.2.5 (java 1.5)");
		expectedRequest.port(-1);

		Set<HttpMessageHeader> httpMessageHeaders = new HashSet<HttpMessageHeader>();
		httpMessageHeaders.add(new HttpMessageHeader("Connection", "keep-alive"));
		httpMessageHeaders.add(new HttpMessageHeader("Content-Type", "text/plain"));
		httpMessageHeaders.add(new HttpMessageHeader("Transfer-Encoding", "chunked"));
		HttpResponseImpl expectedResponse = new HttpResponseImpl(200, "text/plain", "OK".getBytes(),
				httpMessageHeaders);

		InOrder inOrder = inOrder(mockLoggerFactory, mockLogger);
		inOrder.verify(mockLoggerFactory).getHttpRequestResponseLogger();
		inOrder.verify(mockLogger).log(expectedRequest);
		inOrder.verify(mockLogger).log(expectedResponse);
		verifyNoMoreInteractions(mockLogger, mockLoggerFactory, mockLogger);

		// next particial server

		req = new HttpGet(PROXY_URL + "/1024");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);
	}

	@Test
	public void successfulAllMockRequestTest() throws ClientProtocolException, IOException {

		// Given a mock server configured to respond to a GET / with "OK"
		proxyResponseProvider.expect(Method.GET, "/1024").respondWith(200, "text/plain", "1024-OK");
		proxyResponseProvider.expect(Method.GET, "/").respondWith(200, "text/plain", "OK");

		HttpGet req = new HttpGet(PROXY_URL + "/");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);

		req = new HttpGet(PROXY_URL + "/1024");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);
	}

}
