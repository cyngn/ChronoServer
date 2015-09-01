package com.cyngn.chrono.storage;

import com.cyngn.chrono.data.SampleMapper;
import com.cyngn.chrono.storage.entity.Payload;
import com.cyngn.chrono.storage.entity.UrlPackage;
import com.cyngn.vertx.async.promise.Promise;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.HttpMethod;
import java.util.List;
import java.util.function.Consumer;

/**
 * Handles making sure Cassandra has all the data needed for running tests.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/12/14
 */
public class Bootstrap {
    private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);

    public static String DEFAULT_BATCH_NAME = "test_batch";

    private final StorageManager storageManager;

    public Bootstrap(StorageManager storageManager, Consumer<Boolean> onComplete, String baseUrl) {
        this.storageManager = storageManager;
        bootstrap(onComplete, baseUrl);
    }

    private void bootstrap(Consumer<Boolean> onComplete, String baseUrl) {
        Promise.newInstance(storageManager.session.getVertx()).all(
            (context, onResult) -> setupPayloads(onResult),
            (context, onResult) -> handleDefaultUrls(baseUrl, onResult))
            .except(context -> onComplete.accept(false))
            .done(context -> onComplete.accept(true))
            .timeout(5000)
            .eval();
    }

    private void setupPayloads(Consumer<Boolean> whenDone) {
        storageManager.payloadStorage.getSupportedPayloads((success, payloads) -> {
            if (success) {
                verifyUpdateAndPayload(SampleMapper.KB_UNIT, SampleMapper.getInstance().supportedKbPayloads, payloads);
                verifyUpdateAndPayload(SampleMapper.MB_UNIT, SampleMapper.getInstance().supportedMbPayloads, payloads);
            }
            whenDone.accept(success);
        });
    }

    private void verifyUpdateAndPayload(String unit, int[] supportedPayloads, List<Payload> results) {
        for(int size : supportedPayloads) {
            if (!resultsContainPayload(results, unit, size)) {
                logger.info("cassandra missing payload unit: {}, size: {}", unit, size);
                savePayload(unit, size);
            } else { logger.info("Found payload unit: {}, size: {}", unit, size); }
        }
    }

    private boolean resultsContainPayload(List<Payload> results, String unit, int size) {
        boolean result = false;
        for (Payload payload : results) {
            if (StringUtils.equals(payload.unit, unit) && payload.size == size) {
                result = true;
                break;
            }
        }
        return result;
    }

    private void savePayload(String unit, int size) {
        storageManager.payloadStorage.createPayload(unit, size, SampleMapper.getInstance().getPayload(unit, size),
        success -> {
            if(success) { logger.info("Persisted payload unit: {} size: {}", unit, size); }
            else { logger.info("Failed to persist payload unit: {} size: {}", unit, size); }
        });
    }

    private void handleDefaultUrls(String baseUrl, Consumer<Boolean> whenDone) {
        String statusKey = "status";

        Promise.newInstance(storageManager.session.getVertx())
        .then((context, onComplete) -> storageManager.batchStorage.getTestBatch(DEFAULT_BATCH_NAME, (success, testBatch) -> {
            if(success) {
                context.put(statusKey, testBatch != null ? "found" : "missing");
                onComplete.accept(true);
            } else {
                logger.info("Encountered error locating test batch");
                context.put(statusKey, "error");
                onComplete.accept(false);
            }
        }))
        .except(context -> whenDone.accept(false))
        .done(context -> {
            String status = context.getString(statusKey);
            // populate some defaults if none are there
            if ("missing".equals(status)) {
                List<UrlPackage> packages = Lists.newArrayList(
                        new UrlPackage(HttpMethod.GET,
                                Lists.newArrayList(
                                        baseUrl + "/api/v1/timing/static?unit=kb&size=1",
                                        baseUrl + "/api/v1/timing/static_cached?unit=kb&size=1",
                                        baseUrl + "/api/v1/timing/dynamic?unit=kb&size=1",
                                        baseUrl + "/api/v1/timing/dynamic_cached?unit=kb&size=1"
                                )),
                        new UrlPackage(HttpMethod.POST,
                                Lists.newArrayList(
                                        baseUrl + "/api/v1/timing/store?unit=kb&size=1",
                                        baseUrl + "/api/v1/timing/store_mem?unit=kb&size=1"
                                )));
                storageManager.batchStorage.createTestBatch(DEFAULT_BATCH_NAME, packages, whenDone);
            } else { whenDone.accept(true); }
        })
        .eval();
    }
}

