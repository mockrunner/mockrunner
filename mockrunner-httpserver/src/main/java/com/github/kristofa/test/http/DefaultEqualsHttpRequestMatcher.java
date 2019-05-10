package com.github.kristofa.test.http;

public class DefaultEqualsHttpRequestMatcher implements HttpRequestMatcher {

	@Override
	public boolean match(HttpRequestMatchingContext matchingContext) {
		HttpRequest originalRequest = matchingContext.originalRequest();
		HttpRequest otherRequest = matchingContext.otherRequest();
		return otherRequest.equals(originalRequest);
	}

}
