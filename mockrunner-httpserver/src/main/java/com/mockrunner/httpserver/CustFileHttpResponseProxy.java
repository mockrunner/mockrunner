package com.mockrunner.httpserver;

import java.io.File;

import com.github.kristofa.test.http.HttpResponse;
import com.github.kristofa.test.http.HttpResponseProxy;
import com.github.kristofa.test.http.file.HttpResponseFileReader;

public class CustFileHttpResponseProxy implements HttpResponseProxy {

	private final String responseFileName;
	private final String responseEntityFileName;
	private boolean isConsumed = false;
	private boolean isNeedVerify = true;

	private final HttpResponseFileReader httpResponseFileReader;

	public CustFileHttpResponseProxy(final String responseFileName, final String responseEntityFileName,
			final HttpResponseFileReader responseFileReader) {
		this.responseFileName = responseFileName;
		this.responseEntityFileName = responseEntityFileName;
		httpResponseFileReader = responseFileReader;
	}

	@Override
	public boolean consumed() {
		return isConsumed;
	}

	@Override
	public HttpResponse getResponse() {
		return readResponse();
	}

	@Override
	public HttpResponse consume() {
		final HttpResponse response = readResponse();
		isConsumed = true;
		return response;
	}

	private HttpResponse readResponse() {
		final File responseFile = new File(this.responseFileName);
		final File responseEntityFile = new File(this.responseEntityFileName);
		final HttpResponse response = httpResponseFileReader.read(responseFile, responseEntityFile);
		return response;
	}

	public boolean isNeedVerify() {
		return isNeedVerify;
	}

	public void setNeedVerify(boolean isNeedVerify) {
		this.isNeedVerify = isNeedVerify;
	}

}
