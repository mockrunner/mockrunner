package com.github.kristofa.test.http.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.kristofa.test.http.HttpMessageHeader;
import com.github.kristofa.test.http.HttpResponse;
import com.github.kristofa.test.http.HttpResponseImpl;

/**
 * Builds a {@link HttpResponse} for which the content is stored on disk. It can
 * reconstruct a {@link HttpResponse} which was previously stored with
 * {@link HttpResponseFileWriterImpl}.
 * 
 * @see HttpResponseFileWriterImpl
 * @author kristof
 */
public class HttpResponseFileReaderImpl implements HttpResponseFileReader {

	private final static String HTTPCODE = "[HttpCode]";
	private final static String CONTENTTYPE = "[ContentType]";
	private static final String HTTP_MESSAGE_HEADER = "[HttpMessageHeader]";

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpResponse read(final File httpResponseFile, final File httpResponseEntityFile) {

		try {
			return readResponse(httpResponseFile, httpResponseEntityFile);
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}

	}

	private HttpResponse readResponse(final File httpResponseFile, final File httpResponseEntityFile)
			throws IOException {
		final BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(httpResponseFile), "UTF-8"));
		try {
			readNextLine(reader, HTTPCODE);
			final int httpCode = Integer.valueOf(reader.readLine());
			// handle the possible HTTP_MESSAGE_HEADER segment
			Set<HttpMessageHeader> httpMessageHeaders = new HashSet<HttpMessageHeader>();
			String nextLine = readNextLine(reader);
			if (HTTP_MESSAGE_HEADER.equals(nextLine)) {
				final List<KeyValuePair> httpMessageHeaderValues = new ArrayList<KeyValuePair>();
				final String nextSection = readKeyValuePairs(reader, httpMessageHeaderValues);
				if (!CONTENTTYPE.equals(nextSection)) {
					throw new IllegalStateException(
							"Expected " + CONTENTTYPE + " after " + HTTP_MESSAGE_HEADER + " but got " + nextSection);
				}
				for (final KeyValuePair keyValuePair : httpMessageHeaderValues) {
					HttpMessageHeader aHeader = new HttpMessageHeader(keyValuePair.key, keyValuePair.value);
					httpMessageHeaders.add(aHeader);
				}
			} else if (!CONTENTTYPE.equals(nextLine)) {
				throw new IllegalStateException("Unexpected value. Expected " + CONTENTTYPE + " but was " + nextLine);
			}
			String contentType = reader.readLine();
			if (StringUtils.isBlank(contentType)) {
				contentType = null;
			}
			byte[] entity = null;
			if (httpResponseEntityFile.exists()) {
				entity = FileUtils.readFileToByteArray(httpResponseEntityFile);
			}
			return new HttpResponseImpl(httpCode, contentType, entity, httpMessageHeaders);
		} finally {
			reader.close();
		}
	}

	private String readNextLine(final BufferedReader reader, final String expectedValue) throws IOException {
		final String value = reader.readLine();
		if (!expectedValue.equals(value)) {
			throw new IllegalStateException("Unexpected value. Expected " + expectedValue + " but was " + value);
		}
		return value;
	}

	private String readNextLine(final BufferedReader reader) throws IOException {
		final String value = reader.readLine();
		return value;
	}

	private String readKeyValuePairs(final BufferedReader reader, final List<KeyValuePair> properties)
			throws IOException {
		String newLine = null;
		while ((newLine = reader.readLine()) != null) {
			final int equalSignIndex = newLine.indexOf("=");
			if (equalSignIndex != -1) {
				final KeyValuePair pair = new KeyValuePair();
				if (newLine.length() > equalSignIndex + 1) {
					pair.key = newLine.substring(0, equalSignIndex);
					pair.value = newLine.substring(equalSignIndex + 1);
				} else {
					pair.key = newLine.substring(0, equalSignIndex);
					pair.value = "";
				}
				properties.add(pair);
			} else {
				return newLine;
			}
		}
		return null;
	}

	private class KeyValuePair {

		public String key;
		public String value;

	}

}
