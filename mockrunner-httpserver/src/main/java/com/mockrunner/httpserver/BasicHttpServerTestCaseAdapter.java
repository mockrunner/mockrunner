package com.mockrunner.httpserver;

import junit.framework.TestCase;

public abstract class BasicHttpServerTestCaseAdapter extends TestCase {

	private HttpServerTestModule httpServerTestModule;
	private HttpServerConfig config;

	public BasicHttpServerTestCaseAdapter() {
		config = defaultConfig();
		setConfig(config);
		httpServerTestModule = new HttpServerTestModule(config);
	}

	public HttpServerConfig defaultConfig() {
		HttpServerConfig defaultConfig = new HttpServerConfig();
		defaultConfig.setModel(HttpServerConfig.Mode.MOCKING);
		defaultConfig.setProxyPort(0);

		defaultConfig.setRequestFileDir("target/test-classes/request/");
		defaultConfig.setResponseFileDir("target/test-classes/response/");

		defaultConfig.setUniqueFileNamePart("mockHttpServer");
		defaultConfig.setIgnoreAdditionalHeaders(false);
		return defaultConfig;
	}

	public void setConfig(HttpServerConfig config) {

	}

	public HttpServerTestModule getHttpServerTestModule() {
		return httpServerTestModule;
	}

}
