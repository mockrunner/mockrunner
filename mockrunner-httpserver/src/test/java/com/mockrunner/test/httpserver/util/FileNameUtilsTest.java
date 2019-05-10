package com.mockrunner.test.httpserver.util;

import org.junit.Test;

import com.mockrunner.httpserver.util.FileNameUtils;

import junit.framework.Assert;

public class FileNameUtilsTest {

	@Test
	public void testExtractFileInfoForRequestFileName() {
		String fileName = "xxx_request_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNotNull(fileInfos);
		Assert.assertEquals(fileInfos[0], "xxx");
		Assert.assertEquals(fileInfos[1], "00001");
	}

	@Test
	public void testExtractFileInfoForRequestFileNameWithSubChar() {
		String fileName = "xrequestx_x_request_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNotNull(fileInfos);
		Assert.assertEquals(fileInfos[0], "xrequestx_x");
		Assert.assertEquals(fileInfos[1], "00001");
	}

	@Test
	public void testExtractFileInfoForRequestFileNameWithSpecialChar() {
		String fileName = "xx_x_request_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNotNull(fileInfos);
		Assert.assertEquals(fileInfos[0], "xx_x");
		Assert.assertEquals(fileInfos[1], "00001");
	}

	@Test
	public void testExtractFileInfoForRequestEntityFileName() {
		String fileName = "xxx_request_entity_00001.txt";
		String fileType = "request_entity";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNotNull(fileInfos);
		Assert.assertEquals(fileInfos[0], "xxx");
		Assert.assertEquals(fileInfos[1], "00001");
	}

	@Test
	public void testExtractFileInfoForRequestEntityFileNameNoMatch() {
		String fileName = "xxx_request_entity_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

	@Test
	public void testExtractFileInfoForNoSeq() {
		String fileName = "xxx_request.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

	@Test
	public void testExtractFileInfoForNoFileName() {
		String fileName = "request_00001.txt";
		String fileType = "request";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

	@Test
	public void testExtractFileInfoForWrongFileType() {
		String fileName = "xx_request_00001.txt";
		String fileType = "response";
		String[] fileInfos = FileNameUtils.extractFileInfo(fileName, fileType);
		Assert.assertNull(fileInfos);
	}

}
