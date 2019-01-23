package com.mockrunner.httpserver;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kristofa.test.http.AbstractHttpResponseProvider;
import com.github.kristofa.test.http.ForwardHttpRequestBuilder;
import com.github.kristofa.test.http.FullHttpRequest;
import com.github.kristofa.test.http.HttpMessageHeader;
import com.github.kristofa.test.http.HttpRequest;
import com.github.kristofa.test.http.HttpRequestImpl;
import com.github.kristofa.test.http.HttpRequestResponseLogger;
import com.github.kristofa.test.http.HttpRequestResponseLoggerFactory;
import com.github.kristofa.test.http.HttpResponse;
import com.github.kristofa.test.http.HttpResponseImpl;
import com.github.kristofa.test.http.HttpResponseProvider;
import com.github.kristofa.test.http.LoggingHttpProxy;
import com.github.kristofa.test.http.MockHttpServer;
import com.github.kristofa.test.http.RequestConvertor;
import com.github.kristofa.test.http.UnsatisfiedExpectationException;
import com.github.kristofa.test.http.client.ApacheHttpClientImpl;
import com.github.kristofa.test.http.client.HttpClient;
import com.github.kristofa.test.http.client.HttpClientResponse;
import com.github.kristofa.test.http.client.HttpRequestException;

public class PartialMockingHttpServer {

	private final static Logger LOGGER = LoggerFactory.getLogger(PartialMockingHttpServer.class);

	private final int port;
	private final Collection<ForwardHttpRequestBuilder> requestBuilders = new HashSet<ForwardHttpRequestBuilder>();
	private final HttpRequestResponseLoggerFactory loggerFactory;
	private final HttpResponseProvider responseProvider;
	private Connection connection;
	private PartialMockingProxyImplementation proxy;

	private class PartialMockingProxyImplementation implements Container {

		private static final int UNKNOWN_EXCEPTION_HTTP_CODE = 573;
		private static final int FORWARD_REQUEST_FAILED_HTTP_CODE = 571;
		private static final int COPY_RESPONSE_FAILED_ERROR_HTTP_CODE = 572;
		private static final int NO_FORWARD_REQUEST_ERROR_HTTP_CODE = 570;
		private static final String CONTENT_TYPE = "Content-Type";

		public PartialMockingProxyImplementation() {
			super();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void handle(final Request request, final Response response) {

			try {
				final FullHttpRequest httpRequest = RequestConvertor.convert(request);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Received request: " + httpRequest);
				}
				// first handle the mock mode, then log mode

				// We need to copy it because HttpResponseProvider works with
				// HttpRequest, not with FullHttpRequest.
				// If we did not copy matching would fail.
				final HttpRequest receivedRequest = new HttpRequestImpl(httpRequest);
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Got request: " + receivedRequest);
				}
				final HttpResponse expectedResponse = responseProvider.getResponse(receivedRequest);
				if (expectedResponse != null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Got response for request: " + expectedResponse);
					}
					response.setCode(expectedResponse.getHttpCode());
					if (!StringUtils.isEmpty(expectedResponse.getContentType())) {
						response.set("Content-Type", expectedResponse.getContentType());
					}
					// copy the response headers
					for (HttpMessageHeader aHeader : expectedResponse.getHttpMessageHeaders()) {
						response.set(aHeader.getName(), aHeader.getValue());
					}
					OutputStream body = null;
					try {
						body = response.getOutputStream();
						if (expectedResponse.getContent() != null) {
							body.write(expectedResponse.getContent());
						}
						body.close();
					} catch (final IOException e) {
						LOGGER.error("IOException when getting response content.", e);
					}
					return;
				}

				FullHttpRequest forwardHttpRequest = null;
				for (final ForwardHttpRequestBuilder forwardRequestBuilder : requestBuilders) {
					forwardHttpRequest = forwardRequestBuilder.getForwardRequest(httpRequest);
					if (forwardHttpRequest != null) {
						break;
					}
				}

				if (forwardHttpRequest == null) {
					LOGGER.error("Got unexpected request: " + httpRequest);
					errorResponse(response, NO_FORWARD_REQUEST_ERROR_HTTP_CODE,
							"Received unexpected request:\n" + httpRequest.toString());
				} else {

					HttpRequestResponseLogger logger = null;
					if (loggerFactory != null) {
						LOGGER.debug("Logging request.");
						logger = loggerFactory.getHttpRequestResponseLogger();
						logger.log(httpRequest);
					}

					try {
						LOGGER.debug("Forward request.");
						final HttpClientResponse<InputStream> forwardResponse = forward(forwardHttpRequest);
						LOGGER.debug("Got response for forward request.");
						try {
							final InputStream inputStream = forwardResponse.getResponseEntity();
							byte[] responseEntity;
							try {
								// This is tricky as we keep the full response
								// in memory... reason is that we need to copy
								// it
								// twice.
								// Once to return to response, another time to
								// log.
								responseEntity = IOUtils.toByteArray(inputStream);
							} finally {
								inputStream.close();
							}

							// remove the mocking server unexpected request
							// before the server response
							if (responseProvider instanceof AbstractHttpResponseProvider) {
								((AbstractHttpResponseProvider) responseProvider)
										.removeUnexpectedRequest(receivedRequest);
							}

							final HttpResponse httpResponse = new HttpResponseImpl(forwardResponse.getHttpCode(),
									forwardResponse.getContentType(), responseEntity,
									forwardResponse.getHttpMessageHeaders());
							if (logger != null) {
								LOGGER.debug("Logging response");
								logger.log(httpResponse);
							}
							response.setCode(forwardResponse.getHttpCode());
							response.set(CONTENT_TYPE, forwardResponse.getContentType());
							final OutputStream outputStream = response.getOutputStream();
							try {
								final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
										responseEntity);
								IOUtils.copy(byteArrayInputStream, outputStream);
								byteArrayInputStream.close();
							} finally {
								outputStream.close();
							}

						} catch (final IOException e) {
							LOGGER.error("IOException when trying to copy response of forward request.", e);
							errorResponse(response, COPY_RESPONSE_FAILED_ERROR_HTTP_CODE,
									"Exception when copying streams." + e.getMessage());
						} finally {
							forwardResponse.close();
						}

					} catch (final HttpRequestException e) {
						LOGGER.error("HttpRequestException when forwarding request.", e);
						errorResponse(response, FORWARD_REQUEST_FAILED_HTTP_CODE,
								"Exception when forwarding request." + e.getMessage());
					}
				}
			} catch (final Exception e) {
				LOGGER.error("Exception.", e);
				errorResponse(response, UNKNOWN_EXCEPTION_HTTP_CODE, "Exception: " + e.getMessage());
			}

		}

		private HttpClientResponse<InputStream> forward(final FullHttpRequest request) throws HttpRequestException {
			final HttpClient client = new ApacheHttpClientImpl();
			return client.execute(request);
		}

		private void errorResponse(final Response response, final int httpCode, final String message) {
			response.setCode(httpCode);
			response.set(CONTENT_TYPE, "text/plain;charset=utf-8");
			PrintStream body;
			try {
				body = response.getPrintStream();
				body.print(message);
				body.close();
			} catch (final IOException e) {
				throw new IllegalStateException("Exception when building response.", e);
			}
		}

		public void verify() throws UnsatisfiedExpectationException {
			responseProvider.verify();
		}

	}

	/**
	 * Create a new instance.
	 * 
	 * @param port
	 *            Port at which proxy will be running.
	 * @param requestBuilders
	 *            Forward request builders. Should not be <code>null</code> and
	 *            at least 1 should be specified.
	 * @param loggerFactory
	 *            Request/Response logger factory.. Should not be
	 *            <code>null</code>.
	 */
	public PartialMockingHttpServer(final int port, final Collection<ForwardHttpRequestBuilder> requestBuilders,
			final HttpRequestResponseLoggerFactory loggerFactory, final HttpResponseProvider responseProvider) {
		Validate.isTrue(requestBuilders != null && !requestBuilders.isEmpty(),
				"At least 1 ForwardHttpRequestBuilder should be provided.");
		Validate.notNull(responseProvider, "HttpResponseProvider should not be null.");
		this.port = port;
		this.requestBuilders.addAll(requestBuilders);
		this.loggerFactory = loggerFactory;
		this.responseProvider = responseProvider;
		// initialize the request map for the override the file definitions
		if (responseProvider instanceof AbstractHttpResponseProvider) {
			((AbstractHttpResponseProvider) responseProvider).initialzeExpectedRequestsAndResponses();
		}
	}

	/**
	 * Starts proxy.
	 * 
	 * @throws IOException
	 *             In case starting fails.
	 */
	public int start() throws IOException {
		// Close existing connection if it exists.
		if (connection != null) {
			connection.close();
		}

		proxy = new PartialMockingProxyImplementation();
		connection = new SocketConnection(proxy);
		final SocketAddress address = new InetSocketAddress(port);
		final InetSocketAddress connectedAddress = (InetSocketAddress) connection.connect(address);
		int connectedPort = connectedAddress.getPort();
		LOGGER.debug("Started on port: " + port);
		return connectedPort;
	}

	/**
	 * Stops proxy.
	 * 
	 * @throws IOException
	 *             In case closing connection fails.
	 */
	public void stop() throws IOException {
		LOGGER.debug("Stopping and closing connection.");
		connection.close();
	}

	public static class Builder {

		private static final int MINPORT = 0;
		private static final int MAXPORT = 65535;

		private int port;
		private HttpResponseProvider responseProvider;
		private final Collection<ForwardHttpRequestBuilder> requestBuilders = new ArrayList<ForwardHttpRequestBuilder>();
		private HttpRequestResponseLoggerFactory loggerFactory;

		/**
		 * Sets the port that will be used for either {@link MockHttpServer} or
		 * {@link LoggingHttpProxy}.
		 * 
		 * @param port
		 *            Port.
		 * @return Builder.
		 */
		public Builder port(final int port) {
			Validate.inclusiveBetween(MINPORT, MAXPORT, port);
			this.port = port;
			return this;
		}

		/**
		 * Sets the {@link HttpResponseProvider} that will be used for
		 * {@link MockHttpServer}.
		 * 
		 * @param responseProvider
		 *            {@link HttpResponseProvider}. Should not be
		 *            <code>null</code>.
		 * @return Builder.
		 */
		public Builder httpResponseProvider(final HttpResponseProvider responseProvider) {
			Validate.notNull(responseProvider);
			this.responseProvider = responseProvider;
			return this;
		}

		/**
		 * Adds a {@link ForwardHttpRequestBuilder} that will be used with
		 * {@link LoggingHttpProxy}.
		 * 
		 * @param requestBuilder
		 *            {@link ForwardHttpRequestBuilder}. Should not be
		 *            <code>null</code>.
		 * @return Builder.
		 */
		public Builder addForwardHttpRequestBuilder(final ForwardHttpRequestBuilder requestBuilder) {
			Validate.notNull(requestBuilder);
			requestBuilders.add(requestBuilder);
			return this;
		}

		/**
		 * Sets the {@link HttpRequestResponseLoggerFactory} that will be used
		 * with {@link LoggingHttpProxy}.
		 * 
		 * @param loggerFactory
		 *            {@link HttpRequestResponseLoggerFactory}. Should not be
		 *            <code>null</code>.
		 * @return Builder.
		 */
		public Builder httpRequestResponseLoggerFactory(final HttpRequestResponseLoggerFactory loggerFactory) {
			Validate.notNull(loggerFactory);
			this.loggerFactory = loggerFactory;
			return this;
		}

		public PartialMockingHttpServer build() {
			return new PartialMockingHttpServer(this);
		}
	}

	private PartialMockingHttpServer(final Builder builder) {
		this(builder.port, builder.requestBuilders, builder.loggerFactory, builder.responseProvider);
	}

	public void verify() throws UnsatisfiedExpectationException {
		proxy.verify();
	}
}
