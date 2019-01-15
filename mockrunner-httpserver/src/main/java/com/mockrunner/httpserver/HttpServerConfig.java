package com.mockrunner.httpserver;

public class HttpServerConfig {

	/**
	 * Specifies the mode in which should be operating.
	 * 
	 */
	public static enum Mode {
		MOCKING, LOGGING, PARTICIALMOCKING
	};

	private Mode model;
	private int proxyPort = 0;

	private int targetPort = 0;
	private String targetDomain;

	private String requestFileDir = "target/test-classes/request/";
	private String responseFileDir = "target/test-classes/response/";
	private String uniqueFileNamePart;
	private FileNameExtractor fileNameExtractor = null;

	// ignore the other headers except the Content-Type
	private boolean ignoreAdditionalHeaders = true;
	// ignore the local file error, when load files to the response provider
	private boolean ignoreFileErrors = false;

	// close the log when partially mocking
	private boolean partialMockingNeedLoggingFile = false;

	private boolean isOverrideFile = true;

	public Mode getModel() {
		return model;
	}

	public void setModel(Mode model) {
		this.model = model;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public int getTargetPort() {
		return targetPort;
	}

	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}

	public String getTargetDomain() {
		return targetDomain;
	}

	public void setTargetDomain(String targetDomain) {
		this.targetDomain = targetDomain;
	}

	public String getRequestFileDir() {
		return requestFileDir;
	}

	public void setRequestFileDir(String requestFileDir) {
		this.requestFileDir = requestFileDir;
	}

	public String getResponseFileDir() {
		return responseFileDir;
	}

	public void setResponseFileDir(String responseFileDir) {
		this.responseFileDir = responseFileDir;
	}

	public String getUniqueFileNamePart() {
		return uniqueFileNamePart;
	}

	public void setUniqueFileNamePart(String uniqueFileNamePart) {
		this.uniqueFileNamePart = uniqueFileNamePart;
	}

	public FileNameExtractor getFileNameExtractor() {
		return fileNameExtractor;
	}

	public void setFileNameExtractor(FileNameExtractor fileNameExtractor) {
		this.fileNameExtractor = fileNameExtractor;
	}

	public boolean isIgnoreAdditionalHeaders() {
		return ignoreAdditionalHeaders;
	}

	public void setIgnoreAdditionalHeaders(boolean ignoreAdditionalHeaders) {
		this.ignoreAdditionalHeaders = ignoreAdditionalHeaders;
	}

	public boolean isIgnoreFileErrors() {
		return ignoreFileErrors;
	}

	public void setIgnoreFileErrors(boolean ignoreFileErrors) {
		this.ignoreFileErrors = ignoreFileErrors;
	}

	public boolean isPartialMockingNeedLoggingFile() {
		return partialMockingNeedLoggingFile;
	}

	public void setPartialMockingNeedLoggingFile(boolean partialMockingNeedLoggingFile) {
		this.partialMockingNeedLoggingFile = partialMockingNeedLoggingFile;
	}

	public boolean isOverrideFile() {
		return isOverrideFile;
	}

	public void setOverrideFile(boolean isOverrideFile) {
		this.isOverrideFile = isOverrideFile;
	}

}
