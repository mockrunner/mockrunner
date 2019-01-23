package com.github.kristofa.test.http;

/**
 * 请求匹配接口
 * 
 * @author fartpig
 *
 */
public interface HttpRequestMatcher {

	public boolean match(HttpRequestMatchingContext matchingContext);

}
