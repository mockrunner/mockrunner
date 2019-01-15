package com.mockrunner.test.httpserver.util;

import org.junit.Test;

import com.mockrunner.httpserver.util.FileNameUtils;

import junit.framework.Assert;

public class FileNameUtilsTest {

	@Test
	public void tesTExtractFileInfoForRequestFileName() {
		String fileName = "xxx_request_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNotNull(fileInfos);
		Assert.assertEquals(fileInfos[0], "xxx");
		Assert.assertEquals(fileInfos[1], "00001");
	}

	@Test
	public void tesTExtractFileInfoForNoSeq() {
		String fileName = "xxx_request.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

	@Test
	public void tesTExtractFileInfoForNoFileName() {
		String fileName = "request_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

	@Test
	public void tesTExtractFileInfoForWrongFileType() {
		String fileName = "xx_request_00001.txt";
		String fileType = "response";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

}
