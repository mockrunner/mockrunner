package com.github.kristofa.test.http;

import java.util.Set;

/**
 * HTTP response.
 * 
 * @author kristof
 */
public interface HttpResponse {

	/**
	 * Gets a HTTP response code.
	 * 
	 * @return Gets a HTTP response code.
	 */
	int getHttpCode();

	/**
	 * Get content type.
	 * 
	 * @return Get content type.
	 */
	String getContentType();

	/**
	 * Get response content.
	 * 
	 * @return Response content.
	 */
	byte[] getContent();

	Set<HttpMessageHeader> getHttpMessageHeaders();

	Set<HttpMessageHeader> getHttpMessageHeaders(final String name);

}
