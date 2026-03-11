package com.theodo.springblueprint.testhelpers.utils;

import com.theodo.springblueprint.common.utils.collections.Mutable;
import java.io.IOException;
import java.net.HttpCookie;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;
import org.eclipse.collections.api.map.MutableMap;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseCookie;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;

public class CookieStore {

    private final MutableMap<String, ResponseCookie> cookiesStore = Mutable.map.empty();

    public ClientHttpRequestInterceptor requestInterceptor() {
        return this::interceptAndStoreCookies;
    }

    private ClientHttpResponse interceptAndStoreCookies(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution) throws IOException {
        HttpRequest wrappedRequest = getRequestWithCookies(request);
        ClientHttpResponse response = execution.execute(wrappedRequest, body);
        storeNewCookies(response.getHeaders());
        return response;
    }

    private HttpRequest getRequestWithCookies(HttpRequest request) {
        return new HttpRequestWrapper(request) {
            @Override
            public HttpHeaders getHeaders() {
                Optional<String> requestCookies = computeCookiesHeaderFor(getURI());
                if (requestCookies.isEmpty()) {
                    return super.getHeaders();
                }
                HttpHeaders headers = new HttpHeaders();
                headers.putAll(super.getHeaders());
                headers.add(HttpHeaders.COOKIE, requestCookies.orElseThrow());
                return headers;
            }
        };
    }

    private Optional<String> computeCookiesHeaderFor(URI url) {
        String requestCookies = cookiesStore
            .values()
            .stream()
            .filter(cookie -> mustInject(url, cookie))
            .map(cookie -> "%s=%s".formatted(cookie.getName(), cookie.getValue()))
            .collect(Collectors.joining("; "));
        return requestCookies.isEmpty() ? Optional.empty() : Optional.of(requestCookies);
    }

    private static boolean mustInject(URI url, ResponseCookie cookie) {
        return cookie.getPath() != null && url.getPath().startsWith(cookie.getPath());
    }

    private void storeNewCookies(HttpHeaders responseHeaders) {
        responseHeaders.getOrEmpty(HttpHeaders.SET_COOKIE)
            .stream()
            .flatMap(setCookieHeader -> HttpCookie.parse(setCookieHeader).stream())
            .forEach(this::storeCookie);
    }

    private void storeCookie(HttpCookie cookie) {
        ResponseCookie responseCookie = ResponseCookie.from(cookie).build();
        String cookieKey = responseCookie.getName() + ':' + responseCookie.getPath();
        if (responseCookie.getMaxAge().isZero()) {
            cookiesStore.remove(cookieKey);
            return;
        }
        cookiesStore.put(cookieKey, responseCookie);
    }
}
