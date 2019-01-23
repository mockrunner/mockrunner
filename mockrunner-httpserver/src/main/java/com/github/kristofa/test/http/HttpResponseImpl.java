package com.github.kristofa.test.http;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * HttpResponse implementation.
 * 
 * @author kristof
 */
public class HttpResponseImpl implements HttpResponse {

	private final int httpCode;
	private final String contentType;
	private final byte[] content;
	private final Set<HttpMessageHeader> httpMessageHeaders = new TreeSet<HttpMessageHeader>();

	/**
	 * Creates a new instance.
	 * 
	 * @param httpCode
	 *            http response code.
	 * @param contentType
	 *            Content type, can be <code>null</code>.
	 * @param content
	 *            Content, can be <code>null</code>.
	 */
	public HttpResponseImpl(final int httpCode, final String contentType, final byte[] content) {
		this.httpCode = httpCode;
		this.contentType = contentType;
		this.content = content;
	}

	public HttpResponseImpl(final int httpCode, final String contentType, final byte[] content,
			final Set<HttpMessageHeader> httpMessageHeaders) {
		this(httpCode, contentType, content);
		if (httpMessageHeaders != null) {
			this.httpMessageHeaders.addAll(httpMessageHeaders);
		}
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
	public String getContentType() {
		return contentType;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getContent() {
		return content;
	}

	private String toString(Set<HttpMessageHeader> messageHeaders) {
		StringBuilder sb = new StringBuilder();
		boolean isFirst = true;
		for (HttpMessageHeader header : messageHeaders) {
			if (isFirst) {
				isFirst = false;
			} else {
				sb.append(" , ");
			}
			sb.append(header);
		}
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "Http code: " + getHttpCode() + ", Content Type: "
				+ (getContentType() == null ? "null" : getContentType()) + ", Content: "
				+ (getContent() == null ? "null" : new String(getContent())) + ", Http Message Headers: "
				+ ((getHttpMessageHeaders() == null || getHttpMessageHeaders().size() == 0) ? "null"
						: toString(getHttpMessageHeaders()));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this, false);
	}

	/**
	 * {@inheritDoc}
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
