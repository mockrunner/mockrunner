package com.github.kristofa.test.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.kristofa.test.http.file.FileHttpResponseProvider;
import com.github.kristofa.test.http.file.FileHttpResponseProxy;
import com.mockrunner.httpserver.CustFileHttpResponseProxy;

/**
 * Abstract {@link HttpResponseProvider} that contains the following
 * functionality:
 * <ul>
 * <li>Exactly matching HttpRequests</li>
 * <li>In case of non exact match use submitted
 * {@link HttpRequestMatchingFilter} to perform matching.</li>
 * <li>Support multiple times the same request with potentially different
 * responses that are returned in a fixed order.
 * </ul>
 *
 * If you create your own {@link HttpResponseProvider} it is probably a good
 * idea to extend this class.
 * 
 * @author kristof
 * @see DefaultHttpResponseProvider
 * @see FileHttpResponseProvider
 */
public abstract class AbstractHttpResponseProvider implements HttpResponseProvider {

	private final Map<HttpRequest, List<HttpResponseProxy>> requestMap = new HashMap<HttpRequest, List<HttpResponseProxy>>();
	private final List<HttpRequest> unexpectedRequests = new ArrayList<HttpRequest>();
	private HttpRequestMatchingFilter requestMatcherFilter;
	private boolean initialized = false;

	/**
	 * Adds an expected HttpRequest and response proxy combination.
	 * 
	 * @param request
	 *            Expected http request.
	 * @param responseProxy
	 *            Response proxy which gives us access to http response.
	 */
	public final void addExpected(final HttpRequest request, final HttpResponseProxy responseProxy) {
		List<HttpResponseProxy> list = requestMap.get(request);
		if (list == null) {
			list = new ArrayList<HttpResponseProxy>();
			requestMap.put(request, list);
		}
		list.add(responseProxy);
	}

	public final void addExpected(final HttpRequest request, final HttpResponseProxy responseProxy,
			final boolean isOverrideFile) {
		List<HttpResponseProxy> list = requestMap.get(request);
		boolean lastIsFile = false;
		if (list == null) {
			list = new ArrayList<HttpResponseProxy>();
			requestMap.put(request, list);
		} else {
			HttpResponseProxy proxy = list.get(list.size() - 1);
			if (proxy instanceof FileHttpResponseProxy || proxy instanceof CustFileHttpResponseProxy) {
				lastIsFile = true;
			}
		}
		if (isOverrideFile && lastIsFile) {
			list.clear();
			list.add(responseProxy);
		} else {
			list.add(responseProxy);
		}

	}

	/**
	 * Override this method if you want to lazily initialize requests/responses.
	 *
	 * This method will be called with the first call to
	 * {@link AbstractHttpResponseProvider#getResponse(HttpRequest)}.
	 *
	 * You can initialize expected requests and responses by calling
	 * {@link AbstractHttpResponseProvider#addExpected(HttpRequest, HttpResponseProxy)}.
	 */
	protected void lazyInitializeExpectedRequestsAndResponses() {

	}

	public void initialzeExpectedRequestsAndResponses() {
		if (!initialized) {
			lazyInitializeExpectedRequestsAndResponses();
			initialized = true;
		}
	}

	/**
	 * Clear expected request/responses as well as already received unexpected
	 * requests.
	 *
	 * Allows re-use for new test without having to recreate instance.
	 */
	public final void resetState() {
		requestMap.clear();
		unexpectedRequests.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final synchronized HttpResponse getResponse(final HttpRequest request) {

		if (!initialized) {
			lazyInitializeExpectedRequestsAndResponses();
			initialized = true;
		}

		final HttpResponseProxy responseProxyForExactMatchingRequest = getFirstNotYetConsumedResponseProxyFor(request);
		if (responseProxyForExactMatchingRequest != null) {
			return responseProxyForExactMatchingRequest.consume();
		}
		// Non exact matching...
		if (requestMatcherFilter != null) {
			for (final HttpRequest originalRequest : requestMap.keySet()) {
				final HttpResponseProxy originalResponseProxy = getFirstNotYetConsumedResponseProxyFor(originalRequest);
				if (originalResponseProxy == null) {
					continue;
				}
				HttpRequestMatchingContext context = new HttpRequestMatchingContextImpl(originalRequest, request,
						originalResponseProxy.getResponse());
				HttpRequestMatchingFilter next = requestMatcherFilter;
				while (next != null) {
					context = next.filter(context);
					if (context.originalRequest().equals(context.otherRequest())) {
						originalResponseProxy.consume();
						return context.response();
					}
					next = next.next();
				}
			}
		}

		unexpectedRequests.add(request);
		return null;
	}

	/**
	 * Adds a {@link HttpRequestMatchingFilter} to the chain of
	 * {@link HttpRequestMatchingFilter http request matching filters}.
	 * 
	 * @param filter
	 *            {@link HttpRequestMatchingFilter}.
	 */
	public final void addHttpRequestMatchingFilter(final HttpRequestMatchingFilter filter) {
		if (requestMatcherFilter == null) {
			requestMatcherFilter = filter;
		} else {
			HttpRequestMatchingFilter matchingFilter = requestMatcherFilter;
			while (matchingFilter.next() != null) {
				matchingFilter = matchingFilter.next();
			}
			matchingFilter.setNext(filter);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void verify() throws UnsatisfiedExpectationException {
		final Collection<HttpRequest> missingRequests = new ArrayList<HttpRequest>();
		for (final Entry<HttpRequest, List<HttpResponseProxy>> entry : requestMap.entrySet()) {
			for (final HttpResponseProxy responseProxy : entry.getValue()) {
				if (responseProxy.isNeedVerify() == true && responseProxy.consumed() == false) {
					missingRequests.add(entry.getKey());
				}
			}
		}

		if (!unexpectedRequests.isEmpty() || !missingRequests.isEmpty()) {
			throw new UnsatisfiedExpectationException(missingRequests, unexpectedRequests);
		}
	}

	public void removeUnexpectedRequest(HttpRequest request) {
		if (request != null) {
			this.unexpectedRequests.remove(request);
		}
	}

	private HttpResponseProxy getFirstNotYetConsumedResponseProxyFor(final HttpRequest request) {
		final List<HttpResponseProxy> list = requestMap.get(request);
		if (list != null) {
			for (final HttpResponseProxy proxy : list) {
				if (!proxy.consumed()) {
					return proxy;
				}
			}
		}
		return null;
	}

}
