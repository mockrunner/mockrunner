package com.mockrunner.test.httpserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import com.github.kristofa.test.http.HttpRequestImpl;
import com.github.kristofa.test.http.HttpResponseImpl;
import com.github.kristofa.test.http.file.HttpRequestFileWriter;
import com.github.kristofa.test.http.file.HttpResponseFileWriter;
import com.mockrunner.httpserver.CustFileNameHttpRequestResponseFileLogger;
import com.mockrunner.httpserver.FileNameExtractor;

public class CustFileNameHttpRequestResponseFileLoggerTest {

	private static final String FILE_NAME = "testLog";

	private final static String TEMP_DIR = System.getProperty("java.io.tmpdir");

	private static final int HTTP_CODE = 200;
	private static final String CONTENTTYPE = "contentType";
	private static final byte[] RESPONSE_CONTENT = new String("responseContent").getBytes();

	private final static int SEQ_NR = 10;

	private CustFileNameHttpRequestResponseFileLogger logger;
	private HttpRequestFileWriter mockRequestWriter;
	private HttpResponseFileWriter mockResponseWriter;

	@Before
	public void setup() {
		mockRequestWriter = mock(HttpRequestFileWriter.class);
		mockResponseWriter = mock(HttpResponseFileWriter.class);
		FileNameExtractor fileNameExtractor = new FileNameExtractor() {

			@Override
			public String extractFileName(byte[] content) {
				return FILE_NAME;
			}

		};
		logger = new CustFileNameHttpRequestResponseFileLogger(TEMP_DIR, fileNameExtractor, SEQ_NR, mockRequestWriter,
				mockResponseWriter);
	}

	@Test(expected = NullPointerException.class)
	public void testFileHttpRequestResponseLogger() {
		FileNameExtractor fileNameExtractor = new FileNameExtractor() {

			@Override
			public String extractFileName(byte[] content) {
				return FILE_NAME;
			}

		};
		new CustFileNameHttpRequestResponseFileLogger(null, fileNameExtractor, SEQ_NR, mockRequestWriter,
				mockResponseWriter);
	}

	@Test
	public void testGetDirectory() {
		assertEquals(TEMP_DIR, logger.getDirectory());
	}

	@Test
	public void testGetFileNameExtractor() {
		assertNotNull(logger.getFileNameExtractor());
	}

	@Test
	public void testGetSeqNr() {
		assertEquals(SEQ_NR, logger.getSeqNr());
	}

	@Test
	public void testGetRequestFileWriter() {
		assertSame(mockRequestWriter, logger.getRequestFileWriter());
	}

	@Test
	public void testGetResponseFileWriter() {
		assertSame(mockResponseWriter, logger.getResponseFileWriter());
	}

	@Test
	public void testLogRequest() throws IOException {

		final HttpRequestImpl httpRequestImpl = new HttpRequestImpl();

		logger.log(httpRequestImpl);

		final File expectedRequestFile1 = new File(TEMP_DIR, FILE_NAME + "_request_00010.txt");
		final File expectedRequestEntityFile1 = new File(TEMP_DIR, FILE_NAME + "_request_entity_00010.txt");

		verify(mockRequestWriter).write(httpRequestImpl, expectedRequestFile1, expectedRequestEntityFile1);
		verifyNoMoreInteractions(mockRequestWriter, mockResponseWriter);

	}

	@Test
	public void testLogResponse() throws IOException {

		final HttpResponseImpl httpResponseImpl = new HttpResponseImpl(HTTP_CODE, CONTENTTYPE, RESPONSE_CONTENT);

		logger.log(httpResponseImpl);
		final File expectedResponseFile1 = new File(TEMP_DIR, FILE_NAME + "_response_00010.txt");
		final File expectedResponseEntityFile1 = new File(TEMP_DIR, FILE_NAME + "_response_entity_00010.txt");

		verify(mockResponseWriter).write(httpResponseImpl, expectedResponseFile1, expectedResponseEntityFile1);
		verifyNoMoreInteractions(mockRequestWriter, mockResponseWriter);
	}
}
