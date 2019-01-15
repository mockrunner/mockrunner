package com.mockrunner.httpserver;

/**
 * 从文件内容中获取文件名称
 * 
 * @author fartpig
 *
 */
public interface FileNameExtractor {

	public String extractFileName(byte[] content);

}
