package com.cyngn.chrono.storage;

import com.cyngn.chrono.storage.accessor.TestBatchAccessor;
import com.cyngn.chrono.storage.entity.TestBatch;
import com.cyngn.chrono.storage.entity.UrlPackage;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.MappingManager;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.FutureUtils;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.ListenableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Handle access to the test batches of URLs.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/15/15
 */
public class TestBatchStorage {
    private static final Logger logger = LoggerFactory.getLogger(TestBatchStorage.class);

    private final TestBatchAccessor testBatchAccessor;
    private final CassandraSession session;

    public TestBatchStorage(CassandraSession session) {
        MappingManager manager = new MappingManager(session.getSession());

        this.session = session;
        testBatchAccessor = manager.createAccessor(TestBatchAccessor.class);
    }

    public void getTestBatch(String batchName, BiConsumer<Boolean,TestBatch> onComplete) {
        logger.info("getTestBatch - batchName: {}", batchName);
        ListenableFuture<TestBatch> future = testBatchAccessor.getTestBatchAsync(batchName);
        FutureUtils.addCallback(future, new FutureCallback<TestBatch>() {
            @Override
            public void onSuccess(TestBatch result) { onComplete.accept(true, result); }

            @Override
            public void onFailure(Throwable t) {
                logger.error("getTestBatch - batchName: {}", batchName, t);
                onComplete.accept(false, null);
            }
        }, session.getVertx());
    }

    public void createTestBatch(String name, List<UrlPackage> urlPackages, Consumer<Boolean> onComplete) {
        logger.info("createTestBatch - name: {} urlPackages: {}", name, urlPackages);
        ListenableFuture<ResultSet> future = testBatchAccessor.createTestBatch(name, urlPackages);

        FutureUtils.addCallback(future, new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet result) {
                onComplete.accept(result.wasApplied());
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("createTestBatch - name: {} urlPackages: {} ex: ", name, urlPackages, t);
                onComplete.accept(false);
            }
        }, session.getVertx());
    }
}
