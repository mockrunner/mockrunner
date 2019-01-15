package com.mockrunner.test.httpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.kristofa.test.http.Method;
import com.github.kristofa.test.http.MockHttpServer;
import com.github.kristofa.test.http.SimpleHttpResponseProvider;
import com.github.kristofa.test.http.UnsatisfiedExpectationException;
import com.mockrunner.httpserver.FileNameExtractor;
import com.mockrunner.httpserver.HttpServerConfig;
import com.mockrunner.httpserver.HttpServerTestModule;

public class HttpServerTestModuleTest {

	private final static String TARGET_DOMAIN = "localhost";
	private final static int TARGET_PORT = 5000;
	private final static int CONSTS_PORT = 5225;
	private final static String TEST_FILE_DIRECTORY = "target/test-classes/";
	private final static String FILE_NAME = "FileHttpResponseProviderTest";

	private FileNameExtractor fileNameExtractor;
	private HttpServerConfig defaultConfig;
	private DefaultHttpClient client;
	private static SimpleHttpResponseProvider targetResponseProvider;
	private static MockHttpServer targetServer;
	private static int targetServerPort;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		targetResponseProvider = new SimpleHttpResponseProvider();
		targetServer = new MockHttpServer(TARGET_PORT, targetResponseProvider);
		targetServerPort = targetServer.start();
		assertTrue(targetServerPort != -1);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		try {
			if (targetServer != null) {
				targetServer.stop();
			}
		} catch (Exception e) {
		}
	}

	@Before
	public void setup() throws Exception {
		defaultConfig = new HttpServerConfig();
		defaultConfig.setTargetPort(targetServerPort);

		fileNameExtractor = new FileNameExtractor() {
			@Override
			public String extractFileName(byte[] content) {
				if (content != null)
					return new String(content);
				return FILE_NAME;
			}
		};
		client = new DefaultHttpClient();
		targetResponseProvider.reset();
	}

	@After
	public void tearDown() throws Exception {
		client.getConnectionManager().shutdown();
	}

	@Test(expected = NullPointerException.class)
	public void testHttpServerTestModule_MockingConfigNoDir() {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(null);
		defaultConfig.setResponseFileDir(null);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = IllegalStateException.class)
	public void testHttpServerTestModule_MockingConfigNoFileName() {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setUniqueFileNamePart(null);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpServerTestModule_MockingConfigPortInvalid() {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(-1);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpServerTestModule_LoggingConfigPortInvalid() {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(-1);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = NullPointerException.class)
	public void testHttpServerTestModule_LoggingConfigNoTargetUrl() {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setTargetDomain(null);
		defaultConfig.setTargetPort(0);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpServerTestModule_LoggingConfigTargetPortInvalid() {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);
		defaultConfig.setTargetPort(-1);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = NullPointerException.class)
	public void testHttpServerTestModule_LoggingConfigNoFileName() {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setTargetPort(1024);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);
		defaultConfig.setProxyPort(CONSTS_PORT);
		defaultConfig.setUniqueFileNamePart(null);

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testHttpServerTestModule_LoggingConfigFileNameBlank() {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setTargetPort(0);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);
		defaultConfig.setProxyPort(CONSTS_PORT);
		defaultConfig.setUniqueFileNamePart("");

		new HttpServerTestModule(defaultConfig);
	}

	@Test(expected = IllegalStateException.class)
	public void testHttpServerTestModule_ParticialMockingConfigFileNoExists() {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY + "fd");
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setTargetPort(1024);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);
		defaultConfig.setProxyPort(CONSTS_PORT);
		defaultConfig.setUniqueFileNamePart("");

		new HttpServerTestModule(defaultConfig);
	}

	@Test
	public void testHttpServerTestModule_MockingOK() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setIgnoreFileErrors(true);

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// Given a mock server configured to respond to a GET / with "OK"
		module.expect(Method.GET, "/").respondWith(200, "text/plain", "OK");

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		final HttpGet req = new HttpGet(baseUrl + "/");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void testHttpServerTestModule_MockingWithFile() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart(FILE_NAME);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		final HttpGet req = new HttpGet(baseUrl + "/a/b?a=1");
		req.setHeader("Content-Type", "application/json");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test(expected = IllegalStateException.class)
	public void testHttpServerTestModule_LoggingUnsupportFunction() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart("hello_server");
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/hello").respondWith(200, "application/json", "1024-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/hello").respondWith(200, "application/json", "1024-OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		final HttpGet req = new HttpGet(baseUrl + "/hello");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_LoggingOk() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.LOGGING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart("hello_server");
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/hello").respondWith(200, "application/json", "1024-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		final HttpGet req = new HttpGet(baseUrl + "/hello");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void testHttpServerTestModule_ParticialMockingFromFunctionOk() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart(FILE_NAME);
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/just_test").respondWith(200, "application/json", "1024-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/hello").respondWith(200, "application/json", "OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/hello");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		req = new HttpGet(baseUrl + "/just_test");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void testHttpServerTestModule_ParticialMockingFromFileOk() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart("ExpectedHttpResponseFileProviderTest");
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/hello").respondWith(200, "application/json", "1024-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/a/b?a=b");
		req.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		req = new HttpGet(baseUrl + "/hello");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_ParticialMockingMultiReqOk() throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart(FILE_NAME);
		defaultConfig.setPartialMockingNeedLoggingFile(false);

		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/hello").respondWith(200, "application/json", "1024-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/just_test");
		for (int i = 1; i <= 100; i++) {
			module.respondWith(200, "application/json", "OK-" + i);
		}

		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/hello");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1024-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		for (int i = 1; i <= 100; i++) {
			req = new HttpGet(baseUrl + "/just_test");
			response = client.execute(req);
			responseBody = IOUtils.toString(response.getEntity().getContent());
			statusCode = response.getStatusLine().getStatusCode();

			assertEquals("OK-" + i, responseBody);
			assertEquals(200, statusCode);
		}

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void testHttpServerTestModule_ParticialMockingFromFileOrFunctionOrProxyOk()
			throws ClientProtocolException, IOException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart("ExpectedHttpResponseFileProviderTest");
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/target-server").respondWith(200, "application/json",
				"target-server-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/mock-server").respondWith(200, "application/json", "mock-server-OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/a/b?a=b");
		req.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);

		req = new HttpGet(baseUrl + "/mock-server");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();
		assertEquals("mock-server-OK", responseBody);
		assertEquals(200, statusCode);

		req = new HttpGet(baseUrl + "/target-server");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();
		assertEquals("target-server-OK", responseBody);
		assertEquals(200, statusCode);

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_MockingVerifyOK()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart("");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// Given a mock server configured to respond to a GET / with "OK"
		module.expect(Method.GET, "/").respondWith(200, "text/plain", "OK");

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		final HttpGet req = new HttpGet(baseUrl + "/");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_MockingMultiReqVerifyOK()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setProxyPort(0);
		defaultConfig.setUniqueFileNamePart("");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// Given a mock server configured to respond to a GET / with "OK"
		module.expect(Method.GET, "/");
		for (int i = 1; i <= 100; i++) {
			module.respondWith(200, "text/plain", "OK" + i);
		}

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;

		for (int i = 1; i <= 100; i++) {
			HttpGet req = new HttpGet(baseUrl + "/");
			HttpResponse response = client.execute(req);
			String responseBody = IOUtils.toString(response.getEntity().getContent());
			int statusCode = response.getStatusLine().getStatusCode();

			// Then the response is "OK"
			assertEquals("OK" + i, responseBody);

			// And the status code is 200
			assertEquals(200, statusCode);
		}

		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_PartialMockingFromFileOrProxyVerifyOk()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/just_for_test").respondWith(200, "application/json", "1024-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/just_for_test").respondWith(200, "application/json", "OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		final HttpGet req = new HttpGet(baseUrl + "/just_for_test");
		final HttpResponse response = client.execute(req);
		final String responseBody = IOUtils.toString(response.getEntity().getContent());
		final int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_PartialMockingFromFileOrFunctionVerifyOk()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/just_for_test").respondWith(200, "application/json", "OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/just_for_test");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		// When a request for GET / arrives
		req = new HttpGet(baseUrl + "/a/b?a=b");
		req.addHeader("Content-Type", "application/json");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);

		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test
	public void testHttpServerTestModule_PartialMockingFromProxyVerifyOk()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		targetResponseProvider.expect(Method.GET, "/just_for_test_1").respondWith(200, "application/json", "1-OK");
		targetResponseProvider.expect(Method.GET, "/just_for_test_2").respondWith(200, "application/json", "2-OK");

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/just_for_test_1");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		// When a request for GET / arrives
		req = new HttpGet(baseUrl + "/just_for_test_2");
		response = client.execute(req);
		responseBody = IOUtils.toString(response.getEntity().getContent());
		statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("2-OK", responseBody);
		// And the status code is 200
		assertEquals(200, statusCode);
		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}

	}

	@Test
	public void testHttpServerTestModule_PartialMockingFromFileOrFunctionOverrideFileVerifyOk()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/a/b?a=b", "application/json", null).respondWith(200, "application/json", "1-OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/a/b?a=b");
		req.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("1-OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

	@Test(expected = UnsatisfiedExpectationException.class)
	public void testHttpServerTestModule_PartialMockingFromFileOrFunctionNoOverrideFileVerifyOk()
			throws ClientProtocolException, IOException, UnsatisfiedExpectationException {
		defaultConfig.setModel(HttpServerConfig.Mode.PARTICIALMOCKING);
		defaultConfig.setRequestFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setResponseFileDir(TEST_FILE_DIRECTORY);
		defaultConfig.setProxyPort(0);
		defaultConfig.setIgnoreFileErrors(true);
		defaultConfig.setIgnoreAdditionalHeaders(true);
		defaultConfig.setPartialMockingNeedLoggingFile(false);
		defaultConfig.setTargetDomain(TARGET_DOMAIN);
		defaultConfig.setOverrideFile(false);

		HttpServerTestModule module = new HttpServerTestModule(defaultConfig);
		module.expect(Method.GET, "/a/b?a=b", "application/json", null).respondWith(200, "application/json", "1-OK");
		int port = module.start();

		// When a request for GET / arrives
		String baseUrl = "http://localhost:" + port;
		HttpGet req = new HttpGet(baseUrl + "/a/b?a=b");
		req.addHeader("Content-Type", "application/json");
		HttpResponse response = client.execute(req);
		String responseBody = IOUtils.toString(response.getEntity().getContent());
		int statusCode = response.getStatusLine().getStatusCode();

		// Then the response is "OK"
		assertEquals("OK", responseBody);

		// And the status code is 200
		assertEquals(200, statusCode);

		module.verify();

		try {
			if (module != null) {
				module.reset();
				module.stop();
			}
		} catch (Exception e) {
		}
	}

}
