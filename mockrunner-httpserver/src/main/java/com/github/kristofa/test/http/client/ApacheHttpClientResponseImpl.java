package com.github.kristofa.test.http.client;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.github.kristofa.test.http.HttpMessageHeader;

/**
 * Response as being returned by {@link ApacheHttpClientImpl}. Provides the user
 * with the HTTP response code, a response entity and optional error message.
 * <p>
 * IMPORTANT: Call {@link ApacheHttpClientResponseImpl#close()} method when done
 * with process response. This will clean-up resources.
 * 
 * @author kristof
 * @param <T>
 *            Type for response entity.
 */
class ApacheHttpClientResponseImpl<T> implements HttpClientResponse<T> {

	private final int httpCode;
	private final org.apache.http.client.HttpClient httpClient;
	private String errorMessage;
	private T responseEntity;
	private String contentType;
	private Set<HttpMessageHeader> httpMessageHeaders = new TreeSet<HttpMessageHeader>();

	/**
	 * Create a new response instance.
	 * 
	 * @param httpCode
	 *            Http code.
	 * @param httpClient
	 *            The Http client that was used to generate this response.
	 */
	public ApacheHttpClientResponseImpl(final int httpCode, final org.apache.http.client.HttpClient httpClient) {
		Validate.notNull(httpClient);
		this.httpCode = httpCode;
		this.httpClient = httpClient;
	}

	public ApacheHttpClientResponseImpl(final int httpCode, final org.apache.http.client.HttpClient httpClient,
			Set<HttpMessageHeader> httpMessageHeaders) {
		this(httpCode, httpClient);
		this.httpMessageHeaders.addAll(httpMessageHeaders);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean success() {
		return StringUtils.isBlank(errorMessage);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getHttpCode() {
		return httpCode;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * Sets the error message.
	 * 
	 * @param errorMessage
	 *            Error message.
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getResponseEntity() {
		return responseEntity;
	}

	/**
	 * Sets the response object.
	 * 
	 * @param responseEntity
	 *            Response object.
	 */
	public void setResponseEntity(final T responseEntity) {
		this.responseEntity = responseEntity;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getContentType() {
		return contentType;
	}

	/**
	 * Sets the content type.
	 * 
	 * @param contentType
	 *            Content type.
	 */
	public void setContentType(final String contentType) {
		this.contentType = contentType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() {
		httpClient.getConnectionManager().shutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj, false);
	}

	@Override
	public Set<HttpMessageHeader> getHttpMessageHeaders() {
		return Collections.unmodifiableSet(httpMessageHeaders);
	}

	@Override
	public Set<HttpMessageHeader> getHttpMessageHeaders(final String name) {
		Validate.notBlank(name);
		final Set<HttpMessageHeader> mhSubset = new TreeSet<HttpMessageHeader>();
		for (final HttpMessageHeader header : httpMessageHeaders) {
			if (header.getName().equals(name)) {
				mhSubset.add(header);
			}
		}
		return mhSubset;
	}

}
