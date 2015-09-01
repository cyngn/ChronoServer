package com.cyngn.chrono.http;

import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;

import javax.ws.rs.core.MediaType;
import java.nio.charset.StandardCharsets;

/**
 * Helper functions for setting cache related headers.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/21/15
 */
public class CacheUtil {
    public static String ALLOWED_METHODS = HttpMethod.POST + ", " + HttpMethod.GET + ", " + HttpMethod.OPTIONS;

    public static String CONTENT_TYPE = MediaType.APPLICATION_JSON + ";" +
            io.netty.handler.codec.http.HttpHeaders.Values.CHARSET + "=" + StandardCharsets.UTF_8;

    public static String NO_CACHE = io.netty.handler.codec.http.HttpHeaders.Values.PRIVATE + ","
            + io.netty.handler.codec.http.HttpHeaders.Values.MAX_AGE + "=0,"
            + io.netty.handler.codec.http.HttpHeaders.Values.NO_CACHE + ","
            + io.netty.handler.codec.http.HttpHeaders.Values.NO_STORE + ","
            + io.netty.handler.codec.http.HttpHeaders.Values.MUST_REVALIDATE;


    public static String USE_CACHE = io.netty.handler.codec.http.HttpHeaders.Values.PUBLIC + "," +
            // set to one day
            io.netty.handler.codec.http.HttpHeaders.Values.MAX_AGE + "=86400";


    /**
     * Append the standard headers we want on all responses
     *
     * @param headers the response headers
     * @return the updated headers
     */
     public static MultiMap setStandardHeaders(MultiMap headers) {
        headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*")
                .add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS)
                .add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, com.google.common.net.HttpHeaders.X_REQUESTED_WITH)
                .add(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE);
        return headers;
    }

    /**
     * Update a response object to tell upstream nodes to not cache this response
     *
     * @param headers the headers to update
     * @return the updated headers
     */
    public static MultiMap setNoCacheHeaders(MultiMap headers) {
        return setStandardHeaders(headers.add(HttpHeaders.CACHE_CONTROL, NO_CACHE)
                .add(com.google.common.net.HttpHeaders.PRAGMA, io.netty.handler.codec.http.HttpHeaders.Values.NO_CACHE));
    }

    /**
     * Update a response object to tell upstream nodes 'to' cache this response
     *
     * @param headers the headers to update
     * @return the updated headers
     */
    public static MultiMap setCacheHeaders(MultiMap headers) {
        return setStandardHeaders(headers.add(HttpHeaders.CACHE_CONTROL, USE_CACHE));
    }
}
