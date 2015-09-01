package com.cyngn.chrono.api;

import com.cyngn.chrono.data.SampleMapper;
import com.cyngn.chrono.http.CacheUtil;
import com.cyngn.chrono.http.StorageRequest;
import com.cyngn.chrono.storage.StorageManager;
import com.cyngn.vertx.web.HttpHelper;
import com.cyngn.vertx.web.RestApi;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles returning data sets to end users to determine network performance
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/10/14
 */
public class Timing implements RestApi {
    private static final Logger logger = LoggerFactory.getLogger(Timing.class);

    public final static String TIMING_API_BASE_V1 = "/api/v1/timing/";
    public final static String STATIC_V1 = TIMING_API_BASE_V1 + "static";
    public final static String STATIC_CACHED_V1 =  TIMING_API_BASE_V1 + "static_cached";
    public final static String DYNAMIC_V1 = TIMING_API_BASE_V1 + "dynamic";
    public final static String DYNAMIC_CACHED_V1 =  TIMING_API_BASE_V1 + "dynamic_cached";
    public final static String STORE_V1 =  TIMING_API_BASE_V1 + "store";
    public final static String STORE_MEMORY_V1 =  TIMING_API_BASE_V1 + "store_mem";

    private final static String DATA_FIELD = "data";

    private final RestApiDescriptor [] supportApi = {
            new RestApiDescriptor(HttpMethod.GET, STATIC_V1, context -> getStatic(context, false)),
            new RestApiDescriptor(HttpMethod.GET, STATIC_CACHED_V1, context -> getStatic(context, true)),
            new RestApiDescriptor(HttpMethod.GET, DYNAMIC_V1, context -> getDynamic(context, false)),
            new RestApiDescriptor(HttpMethod.GET, DYNAMIC_CACHED_V1, context -> getDynamic(context, true)),
            new RestApiDescriptor(HttpMethod.POST, STORE_V1, context -> store(context, false)),
            new RestApiDescriptor(HttpMethod.POST, STORE_MEMORY_V1, context -> store(context, true))
    };

    private final StorageManager storage;

    public Timing(StorageManager storage) {
        this.storage = storage;
    }

    /**
     * Return static data that the server stores in memory
     *
     * @param context
     * @param allowCaching true if you want to allow upstream systems to cache like response false otherwise
     */
    protected void getStatic(RoutingContext context, boolean allowCaching) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        MultiMap params = request.params();
        if (!isGetRequestValid(params)) {
            HttpHelper.processResponse(response, HttpResponseStatus.BAD_REQUEST.code());
            return;
        }

        String unit = params.get(SampleMapper.UNIT_PARAM);
        int size = Integer.parseInt(params.get(SampleMapper.SIZE_PARAM));
        logger.info("getStatic - unit: {} size: {} allowCaching: {}", unit, size, allowCaching);

        if (allowCaching) { CacheUtil.setCacheHeaders(response.headers());
        } else { CacheUtil.setNoCacheHeaders(response.headers()); }

        sendResponse(response, unit, size);
    }

    private void sendResponse(HttpServerResponse response, String unit, int size) {
        HttpHelper.processResponse(new JsonObject().put(DATA_FIELD, getData(unit, size)), response);
    }

    private String getData(String unit, int size) {
        if (StringUtils.equalsIgnoreCase(unit, SampleMapper.KB_UNIT)) {
            return SampleMapper.getInstance().getKbPayload(size);
        } else if ( StringUtils.equalsIgnoreCase(unit, SampleMapper.MB_UNIT)) {
            return SampleMapper.getInstance().getMbPayload(size);
        }
        return "";
    }

    /**
     * Return dynamic data, aka stored in a DB that we have to query
     *
     * @param context
     * @param allowCaching true if you want to allow upstream systems to cache like response false otherwise
     */
    protected void getDynamic(RoutingContext context, boolean allowCaching) {
        HttpServerRequest request = context.request();
        MultiMap params = request.params();
        if (!isGetRequestValid(params)) {
            request.response().setStatusCode(HttpResponseStatus.BAD_REQUEST.code()).end();
            return;
        }
        String unit = params.get(SampleMapper.UNIT_PARAM);
        int size = Integer.parseInt(params.get(SampleMapper.SIZE_PARAM));
        logger.info("getDynamic - unit: {} size: {} allowCaching: {}", unit, size, allowCaching);

        sendDynamicResponse(request.response(), unit, size, allowCaching);
    }

    private void sendDynamicResponse(HttpServerResponse response, String unit, int size, boolean allowCaching) {
        storage.payloadStorage.getPayload(unit, size, (success, payload) -> {
            if (!success) {
                HttpHelper.processErrorResponse("failed to get payload", response, HttpResponseStatus.NOT_FOUND.code());
                return;
            }

            if (allowCaching) {
                CacheUtil.setCacheHeaders(response.headers());
            } else {
                CacheUtil.setNoCacheHeaders(response.headers());
            }

            HttpHelper.processResponse(new JsonObject().put(DATA_FIELD, payload.data), response);
        });
    }

    /**
     * Handles clients writing data to our server
     * @param context
     * @param writeToMemory true if you want to only measure uploading data to the server false if you want to wirte to
     *                      the DB as well.
     */
    protected void store(RoutingContext context, boolean writeToMemory) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();

        request.bodyHandler(body -> {
            //  if the request doesn't parse then this will send an error response
            StorageRequest req = HttpHelper.attemptToParse(body.toString(), StorageRequest.class, response);
            if(req == null) { return; }

            logger.info("store - unit: {} size: {} writeToMemory: {}", req.unit, req.size, writeToMemory);

            // this is just testing how long it takes clients to upload us data
            if (writeToMemory)
            {
                CacheUtil.setNoCacheHeaders(response.headers());
                HttpHelper.processResponse(response);
                return;
            }

            // this case tests the time it takes to upload data to our server and turn around and write it to disk.
            if (req != null) {
                storage.uploadStorage.uploadData(req.testBatch, req.unit, req.size, req.data,
                   success -> HttpHelper.processResponse(response, success ? HttpResponseStatus.OK.code() :
                           HttpResponseStatus.INTERNAL_SERVER_ERROR.code()));
            }
        });
    }

    private boolean isGetRequestValid(MultiMap params) {
        return params.contains(SampleMapper.SIZE_PARAM) &&
               params.contains(SampleMapper.UNIT_PARAM) &&
               SampleMapper.areDataParamsValid(params.get(SampleMapper.UNIT_PARAM),
                Integer.parseInt(params.get(SampleMapper.SIZE_PARAM)));
    }

    @Override
    public RestApiDescriptor [] supportedApi() {
        return supportApi;
    }
}
