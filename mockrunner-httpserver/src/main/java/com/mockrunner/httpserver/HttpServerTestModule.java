package com.mockrunner.httpserver;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.Validate;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import com.github.kristofa.test.http.AbstractHttpResponseProvider;
import com.github.kristofa.test.http.AllExceptOriginalHeadersFilter;
import com.github.kristofa.test.http.DefaultHttpResponseProvider;
import com.github.kristofa.test.http.DefaultHttpResponseProxy;
import com.github.kristofa.test.http.HttpRequestImpl;
import com.github.kristofa.test.http.HttpRequestResponseLoggerFactory;
import com.github.kristofa.test.http.HttpResponseImpl;
import com.github.kristofa.test.http.Method;
import com.github.kristofa.test.http.MockAndProxyFacade;
import com.github.kristofa.test.http.PassthroughForwardHttpRequestBuilder;
import com.github.kristofa.test.http.UnsatisfiedExpectationException;
import com.github.kristofa.test.http.file.FileHttpResponseProvider;
import com.github.kristofa.test.http.file.HttpRequestFileReader;
import com.github.kristofa.test.http.file.HttpRequestFileReaderImpl;
import com.github.kristofa.test.http.file.HttpRequestResponseFileLoggerFactory;
import com.github.kristofa.test.http.file.HttpResponseFileReader;
import com.github.kristofa.test.http.file.HttpResponseFileReaderImpl;
import com.mockrunner.httpserver.HttpServerConfig.Mode;
import com.mockrunner.httpserver.util.FileNameUtils;

public class HttpServerTestModule {

	/**
	 * File name in which to store http request (except entity).
	 */
	public static final String REQUEST_FILE_NAME = "request";
	/**
	 * File name in which to store http request entity.
	 */
	public static final String REQUEST_ENTITY_FILE_NAME = "request_entity";
	/**
	 * File name in which to store http response.
	 */
	public static final String RESPONSE_FILE_NAME = "response";
	/**
	 * File name in which to store http response entity.
	 */
	public static final String RESPONSE_ENTITY_FILE_NAME = "response_entity";

	private MockAndProxyFacade mockAndProxyFacade;
	private PartialMockingHttpServer particialMockingHttpServer;
	private HttpServerConfig config;
	private HttpRequestFileReader requestFileReader;
	private HttpResponseFileReader responseFileReader;

	private AbstractHttpResponseProvider responseProvider;

	public HttpServerTestModule(final HttpServerConfig config) {
		this.config = config;
		this.initObjs();
	}

	private void initObjs() {
		requestFileReader = new HttpRequestFileReaderImpl();
		responseFileReader = new HttpResponseFileReaderImpl();

		if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())
				|| HttpServerConfig.Mode.MOCKING.equals(this.config.getModel())) {
			MockAndProxyFacade.Builder builder = new MockAndProxyFacade.Builder();
			builder.port(config.getProxyPort());
			if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
				builder.mode(MockAndProxyFacade.Mode.LOGGING);
				builder.addForwardHttpRequestBuilder(new PassthroughForwardHttpRequestBuilder(
						this.config.getTargetDomain(), this.config.getTargetPort()));

			} else if (HttpServerConfig.Mode.MOCKING.equals(this.config.getModel())) {
				builder.mode(MockAndProxyFacade.Mode.MOCKING);
			}

			if (HttpServerConfig.Mode.MOCKING.equals(this.config.getModel())) {
				responseProvider = new CustFileNameFileHttpResponseProvider(this.config.getRequestFileDir(),
						this.config.isIgnoreFileErrors());
			} else if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
				if (this.config.getUniqueFileNamePart() == null
						|| this.config.getUniqueFileNamePart().trim().length() == 0) {
					responseProvider = new CustFileNameFileHttpResponseProvider(this.config.getRequestFileDir(),
							this.config.isIgnoreFileErrors());
				} else {
					responseProvider = new FileHttpResponseProvider(this.config.getRequestFileDir(),
							this.config.getUniqueFileNamePart());
				}

				if (this.config.getFileNameExtractor() != null) {
					HttpRequestResponseLoggerFactory loggerFactory = new CustFileNameHttpRequestResponseFileLoggerFactory(
							this.config.getRequestFileDir(), this.config.getFileNameExtractor());
					builder.httpRequestResponseLoggerFactory(loggerFactory);
				} else {
					HttpRequestResponseLoggerFactory loggerFactory = new HttpRequestResponseFileLoggerFactory(
							this.config.getRequestFileDir(), this.config.getUniqueFileNamePart());
					builder.httpRequestResponseLoggerFactory(loggerFactory);
				}

			} else {
				responseProvider = new DefaultHttpResponseProvider(this.config.isIgnoreAdditionalHeaders());
			}
			if (this.config.isIgnoreAdditionalHeaders()) {
				responseProvider.addHttpRequestMatchingFilter(new AllExceptOriginalHeadersFilter());
			}

			builder.httpResponseProvider(responseProvider);

			mockAndProxyFacade = builder.build();
		} else if (HttpServerConfig.Mode.PARTICIALMOCKING.equals(this.config.getModel())) {
			PartialMockingHttpServer.Builder builder = new PartialMockingHttpServer.Builder();
			builder.port(config.getProxyPort());
			builder.addForwardHttpRequestBuilder(new PassthroughForwardHttpRequestBuilder(this.config.getTargetDomain(),
					this.config.getTargetPort()));

			if (this.config.getUniqueFileNamePart() == null
					|| this.config.getUniqueFileNamePart().trim().length() == 0) {
				responseProvider = new CustFileNameFileHttpResponseProvider(this.config.getRequestFileDir(),
						this.config.isIgnoreFileErrors());
			} else {
				responseProvider = new FileHttpResponseProvider(this.config.getRequestFileDir(),
						this.config.getUniqueFileNamePart());
			}

			if (this.config.isPartialMockingNeedLoggingFile()) {
				if (this.config.getFileNameExtractor() != null) {
					HttpRequestResponseLoggerFactory loggerFactory = new CustFileNameHttpRequestResponseFileLoggerFactory(
							this.config.getRequestFileDir(), this.config.getFileNameExtractor());
					builder.httpRequestResponseLoggerFactory(loggerFactory);
				} else {
					HttpRequestResponseLoggerFactory loggerFactory = new HttpRequestResponseFileLoggerFactory(
							this.config.getRequestFileDir(), this.config.getUniqueFileNamePart());
					builder.httpRequestResponseLoggerFactory(loggerFactory);
				}
			}

			if (this.config.isIgnoreAdditionalHeaders()) {
				responseProvider.addHttpRequestMatchingFilter(new AllExceptOriginalHeadersFilter());
			}
			builder.httpResponseProvider(responseProvider);

			particialMockingHttpServer = builder.build();
		}

	}

	public int start() throws IOException {
		if (Mode.MOCKING.equals(config.getModel()) || Mode.LOGGING.equals(config.getModel())) {
			return mockAndProxyFacade.start();
		} else if (Mode.PARTICIALMOCKING.equals(config.getModel())) {
			return particialMockingHttpServer.start();
		}
		return 0;
	}

	public void stop() throws IOException {
		if (Mode.MOCKING.equals(config.getModel()) || Mode.LOGGING.equals(config.getModel())) {
			mockAndProxyFacade.stop();
		} else if (Mode.PARTICIALMOCKING.equals(config.getModel())) {
			particialMockingHttpServer.stop();
		}
	}

	/**
	 * In case we are in Mocking operation mode we will verify if all expected
	 * requests have been invoked. In case we are in Logging operation mode
	 * nothing will be checked.
	 * 
	 * @throws UnsatisfiedExpectationException
	 *             In case we got unexpected requests and/or not all expected
	 *             requests were invoked.
	 */
	public void verify() throws UnsatisfiedExpectationException, IOException {
		if (Mode.MOCKING.equals(config.getModel())) {
			mockAndProxyFacade.verify();
		} else if (Mode.PARTICIALMOCKING.equals(config.getModel())) {
			particialMockingHttpServer.verify();
		}
	}

	private static final String CONTENT_TYPE_HTTP_HEADER_NAME = "Content-Type";
	private HttpRequestImpl latestRequest;

	public HttpServerTestModule expect(final Method method, final String path, final String contentType,
			final String requestEntity) {
		if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
			throw new IllegalStateException("LOGGING mode unsupports this method");
		}

		latestRequest = new HttpRequestImpl();
		latestRequest.method(method).httpMessageHeader(CONTENT_TYPE_HTTP_HEADER_NAME, contentType);
		if (requestEntity != null) {
			latestRequest.content(requestEntity.getBytes());
		}

		extractAndSetQueryParams(latestRequest, path);
		return this;
	}

	public HttpServerTestModule expect(final Method method, final String path) {
		if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
			throw new IllegalStateException("LOGGING mode unsupports this method");
		}

		latestRequest = new HttpRequestImpl();
		latestRequest.method(method);
		extractAndSetQueryParams(latestRequest, path);
		return this;
	}

	public HttpServerTestModule respondWith(final int httpCode, final String contentType, final String requestEntity) {
		if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
			throw new IllegalStateException("LOGGING mode unsupports this method");
		}

		final HttpResponseImpl response = new HttpResponseImpl(httpCode, contentType,
				requestEntity == null ? null : requestEntity.getBytes());
		responseProvider.addExpected(latestRequest, new DefaultHttpResponseProxy(response),
				this.config.isOverrideFile());
		return this;
	}

	public void reset() {
		responseProvider.resetState();
	}

	private void extractAndSetQueryParams(final HttpRequestImpl request, final String path) {

		final int indexOfQuestionMark = path.indexOf("?");
		if (indexOfQuestionMark >= 0) {
			final String newPath = path.substring(0, indexOfQuestionMark);
			final String queryParams = path.substring(indexOfQuestionMark + 1);
			final List<NameValuePair> parameters = URLEncodedUtils.parse(queryParams, Charset.forName("UTF-8"));
			for (final NameValuePair parameter : parameters) {
				request.queryParameter(parameter.getName(), parameter.getValue());
			}
			request.path(newPath);
		} else {
			request.path(path);
		}
	}

	public HttpServerTestModule expectWithFile(String reqFileName) {
		String[] fileInfos = FileNameUtils.extractFileInfo(reqFileName, HttpServerTestModule.REQUEST_FILE_NAME);
		Validate.notNull(fileInfos,
				"expect file" + reqFileName + " not contain " + HttpServerTestModule.REQUEST_FILE_NAME);
		Validate.isTrue(fileInfos.length == 2, "expect file " + reqFileName + " is not right format **_"
				+ HttpServerTestModule.REQUEST_FILE_NAME + "_**.txt");
		String reqEntityFileName = FilenameUtils.getPath(reqFileName) + fileInfos[0] + "_"
				+ HttpServerTestModule.REQUEST_ENTITY_FILE_NAME + "_" + fileInfos[1];
		expectWithFile(reqFileName, reqEntityFileName);
		return this;
	}

	public HttpServerTestModule expectWithFile(String reqFileName, String reqEntityFileName) {
		File reqFile = new File(reqFileName);
		Validate.isTrue(reqFile.exists(), "expect file " + reqFileName + " is not exists");
		File reqEntityFile = new File(reqEntityFileName);
		Validate.isTrue(reqEntityFile.exists(), "expect file " + reqEntityFileName + " is not exists");
		latestRequest = (HttpRequestImpl) requestFileReader.read(reqFile, reqEntityFile);
		return this;
	}

	public HttpServerTestModule respondWithFile(String respFileName) {
		if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
			throw new IllegalStateException("LOGGING mode unsupports this method");
		}

		String[] fileInfos = FileNameUtils.extractFileInfo(respFileName, HttpServerTestModule.RESPONSE_FILE_NAME);
		Validate.notNull(fileInfos,
				"expect file" + respFileName + " not contain " + HttpServerTestModule.RESPONSE_FILE_NAME);
		Validate.isTrue(fileInfos.length == 2, "expect file " + respFileName + " is not right format **_"
				+ HttpServerTestModule.RESPONSE_FILE_NAME + "_**.txt");
		String respEntityFileName = FilenameUtils.getPath(respFileName) + fileInfos[0] + "_"
				+ HttpServerTestModule.RESPONSE_ENTITY_FILE_NAME + "_" + fileInfos[1];
		respondWithFile(respFileName, respEntityFileName);
		return this;
	}

	public HttpServerTestModule respondWithFile(String respFileName, String respEntityFileName) {
		if (HttpServerConfig.Mode.LOGGING.equals(this.config.getModel())) {
			throw new IllegalStateException("LOGGING mode unsupports this method");
		}

		File respFile = new File(respFileName);
		Validate.isTrue(respFile.exists(), "respond file " + respFileName + " is not exists");
		CustFileHttpResponseProxy responseProxy = new CustFileHttpResponseProxy(respFileName, respEntityFileName,
				this.responseFileReader);
		responseProvider.addExpected(latestRequest, responseProxy);
		return this;
	}
}
