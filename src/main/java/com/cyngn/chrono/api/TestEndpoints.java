package com.cyngn.chrono.api;

import com.cyngn.chrono.http.ConfigResponse;
import com.cyngn.chrono.storage.Bootstrap;
import com.cyngn.chrono.storage.TestBatchStorage;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The API to hand out the config data to clients so they can run performance tests.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/14
 */
public class TestEndpoints implements RestApi {
    private static final Logger logger = LoggerFactory.getLogger(TestEndpoints.class);

    public final static String ENDPOINTS_API_V1 = "/api/v1/test_endpoints";
    private final TestBatchStorage storage;

    public final static String BATCH_NAME_FIELD = "batch_name";

    private final RestApiDescriptor [] supportApi = {
            new RestApiDescriptor(HttpMethod.GET, ENDPOINTS_API_V1, this::getEndpoints)
    };
    private final String sharedApiKey;

    public TestEndpoints(TestBatchStorage storage, String sharedApiKey) {
        this.storage = storage;
        this.sharedApiKey = sharedApiKey;
    }

    protected void getEndpoints(RoutingContext context) {
        String batchName = context.request().params().contains(BATCH_NAME_FIELD) ?
                context.request().params().get(BATCH_NAME_FIELD) : Bootstrap.DEFAULT_BATCH_NAME;
        logger.info("getEndpoints - batchName: {}", batchName);

        storage.getTestBatch(batchName, (success, batch) -> {
            if (batch == null) {
                HttpHelper.processErrorResponse(String.format("no test batch found by name %s", batchName),
                        context.response(), HttpResponseStatus.NOT_FOUND.code());
            } else {
                HttpHelper.processResponse(new ConfigResponse(sharedApiKey, batch.urlPackages), context.response());
            }
        });
    }

    @Override
    public RestApiDescriptor[] supportedApi() {
        return supportApi;
    }
}
