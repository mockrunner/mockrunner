package com.mockrunner.httpserver.util;

public final class StringUtils {

	public static String getIpStr(byte[] src) {
		return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + (src[2] & 0xff) + "." + (src[3] & 0xff);
	}

}
