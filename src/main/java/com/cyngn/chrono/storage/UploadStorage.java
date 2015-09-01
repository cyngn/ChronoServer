package com.cyngn.chrono.storage;

import com.cyngn.chrono.storage.accessor.UploadAccessor;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.MappingManager;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.FutureUtils;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Upload data from client.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/15/15
 */
public class UploadStorage {
    private static final Logger logger = LoggerFactory.getLogger(UploadStorage.class);

    private final CassandraSession session;
    private final UploadAccessor uploadAccessor;
    private final int ttl;

    public UploadStorage(CassandraSession session, int ttl) {
        MappingManager manager = new MappingManager(session.getSession());
        this.session = session;
        uploadAccessor = manager.createAccessor(UploadAccessor.class);
        this.ttl = ttl;
    }

    public void uploadData(String testBatch, String unit, int size, String data, Consumer<Boolean> onComplete) {
        logger.info("uploadData - testBatch: {} unit: {} size: {} data size: {}", testBatch, unit, size, data.length());
        ListenableFuture<ResultSet> future = uploadAccessor.uploadData(testBatch, unit, size, data, ttl);
        FutureUtils.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) { onComplete.accept(result.wasApplied()); }

            @Override
            public void onFailure(Throwable t) {
                logger.error("uploadData - testBatch: {} unit: {} size: {} data size: {} ex: ", testBatch, unit, size,
                        data.length(), t);
                onComplete.accept(false);
            }
        }, session.getVertx());
    }
}

