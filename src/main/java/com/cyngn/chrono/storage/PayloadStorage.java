package com.cyngn.chrono.storage;

import com.cyngn.chrono.storage.accessor.PayloadAccessor;
import com.cyngn.chrono.storage.entity.Payload;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.mapping.MappingManager;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.FutureUtils;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Interface to the payload storage.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/15/15
 */
public class PayloadStorage {
    private static final Logger logger = LoggerFactory.getLogger(PayloadStorage.class);
    private final CassandraSession session;
    private final PayloadAccessor payloadAccessor;

    public PayloadStorage(CassandraSession session) {
        MappingManager manager = new MappingManager(session.getSession());
        this.session = session;
        payloadAccessor = manager.createAccessor(PayloadAccessor.class);
    }

    public void getSupportedPayloads(BiConsumer<Boolean,List<Payload>> onComplete) {
        logger.info("getSupportedPayloads - ");
        ListenableFuture<ResultSet> future = payloadAccessor.getSupportedPayloads();
        FutureUtils.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                // we don't care about the data we just want to know which values are currently mapped.
                List<Payload> payloads = new ArrayList<>();
                for (Row row : result.all()) {
                    Payload p = new Payload();
                    p.unit = row.getString(0);
                    p.size = row.getLong(1);
                    payloads.add(p);
                }
                onComplete.accept(true, payloads);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("getSupportedPayloads - ex: ", t);
                onComplete.accept(false, null);
            }
        }, session.getVertx());
    }

    public void getPayload(String unit, int size, BiConsumer<Boolean,Payload> onComplete) {
        logger.info("getPayload - unit: {} size: {}", unit, size);
        ListenableFuture<Payload> future = payloadAccessor.getPayloadAsync(unit, size);
        FutureUtils.addCallback(future, new FutureCallback<Payload>() {
            @Override
            public void onSuccess(Payload result) { onComplete.accept(result != null, result); }

            @Override
            public void onFailure(Throwable t) {
                logger.error("getPayload - unit: {} size: {} ex: ", unit, size, t);
                onComplete.accept(false, null);
            }
        }, session.getVertx());
    }

    public void createPayload(String unit, int size, String data, Consumer<Boolean> onComplete) {
        logger.info("createPayload - unit: {} size: {} data size: {}", unit, size, data.length());
        ListenableFuture<ResultSet> future = payloadAccessor.createPayload(unit, size, data);

        FutureUtils.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                onComplete.accept(result != null && result.wasApplied());
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("createPayload - unit: {} size: {} data size: {} ex: ", unit, size, data.length(), t);
                onComplete.accept(false);
            }
        }, session.getVertx());
    }
}
