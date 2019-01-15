package com.mockrunner.httpserver;

import java.io.File;

import org.apache.commons.lang3.Validate;

import com.github.kristofa.test.http.HttpRequest;
import com.github.kristofa.test.http.HttpRequestResponseLogger;
import com.github.kristofa.test.http.HttpResponse;
import com.github.kristofa.test.http.file.FileNameBuilder;
import com.github.kristofa.test.http.file.HttpRequestFileWriter;
import com.github.kristofa.test.http.file.HttpResponseFileWriter;

/**
 * {@link HttpRequestResponseLogger} that logs requests/responses to file. For
 * each request/response that is logged it will generate following files:
 * <ul>
 * <li>&lt;fileName&gt;_request_0000x.txt : Contains http method, headers, path
 * and query parameters. Is readable (text file), UTF-8 encoded. 0000x =
 * sequence number that increments with each request.</li>
 * <li>&lt;fileName&gt;_request_entity_0000x.txt : Contains request entity if
 * entity is available. If no entity is specified file is not written. This is a
 * binary file.</li>
 * <li>&lt;fileName&gt;_response_0000x.txt : Contains http code and Content
 * Type. Is readable (text file), UTF-8 encoded. 0000x = sequence number that
 * increments with each request.</li>
 * <li>&lt;fileName&gt;_response_entity_0000x.txt : Contains response entity if
 * entity is available. If no entity is specified file is not written. This is a
 * binary file.</li>
 * </ul>
 * It use {@link FileNameBuilder} to build these file names.
 * 
 * @see FileNameBuilder
 * @author kristof
 */
public class CustFileNameHttpRequestResponseFileLogger implements HttpRequestResponseLogger {

	private final String directory;
	private final FileNameExtractor fileNameExtractor;
	private final int seqNr;
	private final HttpRequestFileWriter requestWriter;
	private final HttpResponseFileWriter responseWriter;

	public CustFileNameHttpRequestResponseFileLogger(final String directory, final FileNameExtractor fileNameExtractor,
			final int seqNr, final HttpRequestFileWriter requestWriter, final HttpResponseFileWriter responseWriter) {
		Validate.notNull(directory);
		Validate.notNull(fileNameExtractor);
		Validate.notNull(requestWriter);
		Validate.notNull(responseWriter);

		this.directory = directory;
		this.fileNameExtractor = fileNameExtractor;
		this.seqNr = seqNr;
		this.requestWriter = requestWriter;
		this.responseWriter = responseWriter;
	}

	/**
	 * Gets the target directory in which to store request/responses.
	 * 
	 * @return the target directory in which to store request/responses.
	 */
	public String getDirectory() {
		return directory;
	}

	/**
	 * Gets the base file name.
	 * 
	 * @return Base file name.
	 */
	public FileNameExtractor getFileNameExtractor() {
		return fileNameExtractor;
	}

	/**
	 * Gets the seqnr for request/response.
	 * 
	 * @return Seqnr for request/response.
	 */
	public int getSeqNr() {
		return seqNr;
	}

	/**
	 * Gets the {@link HttpRequestFileWriter} instance.
	 * 
	 * @return the {@link HttpRequestFileWriter} instance.
	 */
	public HttpRequestFileWriter getRequestFileWriter() {
		return requestWriter;
	}

	/**
	 * Gets the {@link HttpResponseFileWriter} instance.
	 * 
	 * @return the {@link HttpResponseFileWriter} instance.
	 */
	public HttpResponseFileWriter getResponseFileWriter() {
		return responseWriter;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void log(final HttpRequest request) {
		String fileName = fileNameExtractor.extractFileName(request.getContent());
		final String requestFileName = FileNameBuilder.REQUEST_FILE_NAME.getFileName(fileName, seqNr);
		final String requestEntityFileName = FileNameBuilder.REQUEST_ENTITY_FILE_NAME.getFileName(fileName, seqNr);

		requestWriter.write(request, new File(directory, requestFileName), new File(directory, requestEntityFileName));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void log(final HttpResponse response) {
		String fileName = fileNameExtractor.extractFileName(response.getContent());
		final String responseFileName = FileNameBuilder.RESPONSE_FILE_NAME.getFileName(fileName, seqNr);
		final String responseEntityFileName = FileNameBuilder.RESPONSE_ENTITY_FILE_NAME.getFileName(fileName, seqNr);

		responseWriter.write(response, new File(directory, responseFileName),
				new File(directory, responseEntityFileName));
	}

}
