package com.github.kristofa.test.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;
import java.util.TreeSet;

import org.junit.Before;
import org.junit.Test;

public class HttpResponseImplTest {

	private final static int HTTP_RESPONSE_CODE = 200;
	private final static String CONTENT_TYPE = "application/json; charset=UTF-8";
	private final static byte[] CONTENT = new String("content").getBytes();

	private HttpResponseImpl response;
	private HttpResponseImpl responseNoContentAndType;
	private HttpResponseImpl responseWithMessageHeader;

	@Before
	public void setup() {
		Set<HttpMessageHeader> messageHeaders = new TreeSet<HttpMessageHeader>();
		messageHeaders.add(new HttpMessageHeader("CONTENT_TYPE", "type"));
		messageHeaders.add(new HttpMessageHeader("ACCEPT", "accept"));

		response = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT);
		responseNoContentAndType = new HttpResponseImpl(HTTP_RESPONSE_CODE, null, null);
		responseWithMessageHeader = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT, messageHeaders);
	}

	@Test
	public void testHashCode() {
		final HttpResponseImpl equalResponse = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT);
		assertEquals(response.hashCode(), equalResponse.hashCode());
	}

	@Test
	public void testHashCodeWithMessageHeader() {
		Set<HttpMessageHeader> messageHeaders = new TreeSet<HttpMessageHeader>();
		messageHeaders.add(new HttpMessageHeader("CONTENT_TYPE", "type"));
		messageHeaders.add(new HttpMessageHeader("ACCEPT", "accept"));
		final HttpResponseImpl equalResponse = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT,
				messageHeaders);
		assertEquals(responseWithMessageHeader.hashCode(), equalResponse.hashCode());
	}

	@Test
	public void testHttpMessageHeader() {
		Set<HttpMessageHeader> messageHeaders = new TreeSet<HttpMessageHeader>();
		messageHeaders.add(new HttpMessageHeader("CONTENT_TYPE", "type"));
		final HttpResponseImpl equalResponse = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT,
				messageHeaders);
		Set<HttpMessageHeader> equalMessageHeaders = equalResponse.getHttpMessageHeaders("CONTENT_TYPE");
		assertEquals(equalMessageHeaders.iterator().next().getValue(), "type");
	}

	@Test
	public void testHttpMessageHeaderAll() {
		Set<HttpMessageHeader> messageHeaders = new TreeSet<HttpMessageHeader>();
		messageHeaders.add(new HttpMessageHeader("CONTENT_TYPE", "type"));
		final HttpResponseImpl equalResponse = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT,
				messageHeaders);
		Set<HttpMessageHeader> equalMessageHeaders = equalResponse.getHttpMessageHeaders();
		assertEquals(equalMessageHeaders.iterator().next().getValue(), "type");
	}

	@Test
	public void testGetHttpCode() {
		assertEquals(HTTP_RESPONSE_CODE, response.getHttpCode());
		assertEquals(HTTP_RESPONSE_CODE, responseNoContentAndType.getHttpCode());
		assertEquals(HTTP_RESPONSE_CODE, responseWithMessageHeader.getHttpCode());
	}

	@Test
	public void testGetContentType() {
		assertEquals(CONTENT_TYPE, response.getContentType());
		assertNull(responseNoContentAndType.getContentType());
		assertEquals(CONTENT_TYPE, responseWithMessageHeader.getContentType());
	}

	@Test
	public void testGetContent() {
		assertEquals(CONTENT, response.getContent());
		assertNull(responseNoContentAndType.getContent());
		assertEquals(CONTENT, responseWithMessageHeader.getContent());
	}

	@Test
	public void testToString() {
		assertEquals("Http code: " + HTTP_RESPONSE_CODE + ", Content Type: " + CONTENT_TYPE + ", Content: "
				+ new String(CONTENT) + ", Http Message Headers: null", response.toString());
		assertEquals(
				"Http code: " + HTTP_RESPONSE_CODE + ", Content Type: null, Content: null, Http Message Headers: null",
				responseNoContentAndType.toString());
		assertEquals(
				"Http code: " + HTTP_RESPONSE_CODE + ", Content Type: " + CONTENT_TYPE + ", Content: "
						+ new String(CONTENT) + ", Http Message Headers: ACCEPT: accept , CONTENT_TYPE: type",
				responseWithMessageHeader.toString());
	}

	@Test
	public void testEqualsObject() {
		assertFalse(response.equals(null));
		assertFalse(response.equals(new String()));
		assertTrue(response.equals(response));
		assertFalse(response.equals(responseNoContentAndType));

		final HttpResponseImpl equalResponse = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE, CONTENT);
		assertTrue(response.equals(equalResponse));

		Set<HttpMessageHeader> messageHeaders = new TreeSet<HttpMessageHeader>();
		messageHeaders.add(new HttpMessageHeader("CONTENT_TYPE", "type"));
		messageHeaders.add(new HttpMessageHeader("ACCEPT", "accept"));
		final HttpResponseImpl equalResponseWithMessageHeaders = new HttpResponseImpl(HTTP_RESPONSE_CODE, CONTENT_TYPE,
				CONTENT, messageHeaders);
		assertTrue(responseWithMessageHeader.equals(equalResponseWithMessageHeaders));

	}

}
