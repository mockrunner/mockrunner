package com.mockrunner.httpserver;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.kristofa.test.http.AbstractHttpResponseProvider;
import com.github.kristofa.test.http.HttpRequest;
import com.github.kristofa.test.http.file.FileHttpResponseProxy;
import com.github.kristofa.test.http.file.FileNameBuilder;
import com.github.kristofa.test.http.file.HttpRequestFileReader;
import com.github.kristofa.test.http.file.HttpRequestFileReaderImpl;
import com.github.kristofa.test.http.file.HttpResponseFileReader;
import com.github.kristofa.test.http.file.HttpResponseFileReaderImpl;
import com.mockrunner.httpserver.util.FileNameUtils;

public class CustFileNameFileHttpResponseProvider extends AbstractHttpResponseProvider {

	private final String directory;
	private List<String> fileNames;
	private final HttpRequestFileReader httpRequestFileReader;
	private final HttpResponseFileReader httpResponseFileReader;
	private final boolean ignoreFileErrors;

	public CustFileNameFileHttpResponseProvider(final String directory, String... fileNames) {
		this(directory, new HttpRequestFileReaderImpl(), new HttpResponseFileReaderImpl(), false, fileNames);
	}

	public CustFileNameFileHttpResponseProvider(final String directory, final boolean ignoreFileErrors,
			String... fileNames) {
		this(directory, new HttpRequestFileReaderImpl(), new HttpResponseFileReaderImpl(), ignoreFileErrors, fileNames);
	}

	public CustFileNameFileHttpResponseProvider(final String directory, final HttpRequestFileReader requestFileReader,
			final HttpResponseFileReader responseFileReader, final boolean ignoreFileErrors, String... fileNames) {
		this.directory = directory;
		httpRequestFileReader = requestFileReader;
		httpResponseFileReader = responseFileReader;
		if (fileNames != null) {
			this.fileNames = new ArrayList<String>(Arrays.asList(fileNames));
		}
		this.ignoreFileErrors = ignoreFileErrors;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void lazyInitializeExpectedRequestsAndResponses() {

		File requestFile = new File(directory);
		if (!requestFile.exists()) {
			throw new IllegalStateException(
					"No saved http request/responses found. File " + requestFile.getPath() + " not found.");
		}
		for (File aFile : FileUtils.listFiles(requestFile, new String[] { "txt" }, false)) {
			if (aFile.getName().contains("_request_")) {
				String[] fileInfos = FileNameUtils.extractFileInfo(aFile.getName(), "request");
				String fileName = fileInfos[0];
				if (this.fileNames != null && this.fileNames.size() > 0) {
					boolean isContain = false;
					for (String aTempFileName : this.fileNames) {
						if (aTempFileName.equals(fileName)) {
							isContain = true;
							break;
						}
					}

					if (!isContain) {
						continue;
					}
				}

				int seqNr = Integer.parseInt(fileInfos[1]);
				final File responseFile = new File(directory,
						FileNameBuilder.RESPONSE_FILE_NAME.getFileName(fileName, seqNr));
				if (!responseFile.exists()) {
					if (!this.ignoreFileErrors) {
						throw new IllegalStateException("Found request file (" + aFile.getPath()
								+ ") but no matching response file: " + responseFile.getPath());
					} else {
						continue;
					}
				}

				// load all files mode, no verify
				if (this.fileNames != null && this.fileNames.size() > 0) {
					submitRequest(fileName, seqNr, true);
				} else {
					submitRequest(fileName, seqNr, false);
				}

			}
		}
	}

	private void submitRequest(final String fileName, final int seqNr, boolean isNeedVerify) {
		final File requestFile = new File(directory, FileNameBuilder.REQUEST_FILE_NAME.getFileName(fileName, seqNr));
		final File requestEntityFile = new File(directory,
				FileNameBuilder.REQUEST_ENTITY_FILE_NAME.getFileName(fileName, seqNr));
		final HttpRequest request = httpRequestFileReader.read(requestFile, requestEntityFile);
		final FileHttpResponseProxy responseProxy = new FileHttpResponseProxy(directory, fileName, seqNr,
				httpResponseFileReader);
		responseProxy.setNeedVerify(isNeedVerify);

		addExpected(request, responseProxy);
	}

	public List<String> getFileNames() {
		return fileNames;
	}

	public void setFileName(List<String> fileNames) {
		this.fileNames = fileNames;
	}

}
