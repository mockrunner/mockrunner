package com.mockrunner.httpserver;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kristofa.test.http.HttpRequestResponseLogger;
import com.github.kristofa.test.http.HttpRequestResponseLoggerFactory;
import com.github.kristofa.test.http.LoggingHttpProxy;
import com.github.kristofa.test.http.file.HttpRequestFileWriter;
import com.github.kristofa.test.http.file.HttpRequestFileWriterImpl;
import com.github.kristofa.test.http.file.HttpResponseFileWriter;
import com.github.kristofa.test.http.file.HttpResponseFileWriterImpl;

/**
 * Factory that creates {@link HttpRequestResponseFileLogger} instances.
 * 
 * @see HttpRequestResponseFileLogger
 * @see LoggingHttpProxy
 * @author kristof
 */
public class CustFileNameHttpRequestResponseFileLoggerFactory implements HttpRequestResponseLoggerFactory {

	private final static Logger LOGGER = LoggerFactory
			.getLogger(CustFileNameHttpRequestResponseFileLoggerFactory.class);

	private final AtomicInteger atomicInteger = new AtomicInteger();
	private final String directory;
	private final FileNameExtractor fileNameExtractor;
	private final HttpRequestFileWriter requestWriter;
	private final HttpResponseFileWriter responseWriter;

	/**
	 * Creates a new instance.
	 * 
	 * @param directory
	 *            Target directory in which to store request/responses.
	 *            Directory should already exist. Should not be
	 *            <code>null</code> or blank.
	 * @param fileNameExtrator
	 *            Base file name extractor. Should not be null, it extracts the
	 *            file base name from contents.
	 * @param deleteExistingFiles
	 *            If value is <code>true</code> we will delete all existing
	 *            files prior to logging new requests. This is often helpful
	 *            because if we have less requests than before otherwise old
	 *            files keep on lingering which can cause failing tests.
	 */
	public CustFileNameHttpRequestResponseFileLoggerFactory(final String directory,
			final FileNameExtractor fileNameExtractor) {
		Validate.notBlank(directory);
		Validate.notNull(fileNameExtractor);
		this.directory = directory;
		this.fileNameExtractor = fileNameExtractor;
		requestWriter = new HttpRequestFileWriterImpl();
		responseWriter = new HttpResponseFileWriterImpl();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public HttpRequestResponseLogger getHttpRequestResponseLogger() {
		return new CustFileNameHttpRequestResponseFileLogger(directory, fileNameExtractor,
				atomicInteger.incrementAndGet(), requestWriter, responseWriter);
	}

}
