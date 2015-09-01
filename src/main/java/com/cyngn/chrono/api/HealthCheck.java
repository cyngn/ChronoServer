package com.cyngn.chrono.api;

import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.RestApi;
import io.vertx.core.http.HttpMethod;

/**
 * Simplistic health check api.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/21/15
 */
public class HealthCheck implements RestApi {
    public static String HEALTHCHECK_API = "/healthcheck";

    private RestApiDescriptor [] apis = new RestApiDescriptor[] {
            new RestApiDescriptor(HttpMethod.GET, HEALTHCHECK_API,
                    context -> HttpHelper.processResponse(context.response()))
    };

    @Override
    public RestApiDescriptor[] supportedApi() { return apis; }
}
