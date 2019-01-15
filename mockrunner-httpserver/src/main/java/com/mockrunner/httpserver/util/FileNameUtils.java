package com.mockrunner.httpserver.util;

import org.apache.commons.io.FilenameUtils;

public final class FileNameUtils {

	public static String[] extractFileInfo(String fileName, String fileType) {
		String fileBaseName = FilenameUtils.getBaseName(fileName);
		int fileIndex = fileBaseName.lastIndexOf("_" + fileType + "_");
		if (fileIndex == -1) {
			return null;
		}

		String[] strs = new String[2];
		strs[0] = fileBaseName.substring(0, fileIndex);
		strs[1] = fileBaseName.substring(fileIndex + 9);
		return strs;
	}

}
