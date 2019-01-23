package com.mockrunner.httpserver.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

public final class FileNameUtils {

	public static String[] extractFileInfo(String fileName, String fileType) {
		String fileBaseName = FilenameUtils.getBaseName(fileName);
		Pattern pattern = Pattern.compile("(\\S+)_" + fileType + "_(\\d+)");
		Matcher matcher = pattern.matcher(fileBaseName);
		if (!matcher.matches()) {
			return null;
		}

		if (matcher.groupCount() != 2) {
			return null;
		}

		String[] strs = new String[2];
		strs[0] = matcher.group(1);
		strs[1] = matcher.group(2);
		return strs;
	}

}
