package com.cyngn.chrono.api;

import com.cyngn.vertx.web.HttpHelper;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simplistic shared api key checker
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/21/15
 */
public class SharedKeyHandler implements Handler<RoutingContext> {
    private static final Logger logger = LoggerFactory.getLogger(SharedKeyHandler.class);

    public static String API_KEY_HEADER = "X-API-Key";
    private final String apiSharedKey;

    public SharedKeyHandler(String apiSharedKey) {
        this.apiSharedKey = apiSharedKey;
    }

    @Override
    public void handle(RoutingContext context) {
        String uri = context.request().uri();
        // if it's the healthcheck or the config endpoint they get the shared key from pass them through
        if (uri.startsWith(HealthCheck.HEALTHCHECK_API) || uri.startsWith(TestEndpoints.ENDPOINTS_API_V1) ||
            uri.startsWith(TimingStreaming.ROOT_API)) {
            context.next();
        } else {
            String sharedKey = context.request().headers().get(API_KEY_HEADER);
            // if they have the correct key pass them through otherwise reject them
            if (apiSharedKey.equals(sharedKey)) {
                context.next();
            } else {
                logger.error("handle - invalid shared api key");
                HttpHelper.processResponse(context.response(), HttpResponseStatus.FORBIDDEN.code());
            }
        }
    }
}
