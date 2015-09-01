package com.cyngn.chrono.storage;

import com.cyngn.chrono.storage.entity.MetricReport;
import com.englishtown.vertx.cassandra.CassandraSession;
import com.englishtown.vertx.cassandra.mapping.VertxMapper;
import com.englishtown.vertx.cassandra.mapping.VertxMappingManager;
import com.englishtown.vertx.cassandra.mapping.impl.DefaultVertxMappingManager;
import com.google.common.util.concurrent.FutureCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Handles uploading client reports.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/15/15
 */
public class ReportStorage {
    private static final Logger logger = LoggerFactory.getLogger(ReportStorage.class);

    private final CassandraSession session;
    private final VertxMapper<MetricReport> mapper;

    public ReportStorage(CassandraSession session) {
        VertxMappingManager manager = new DefaultVertxMappingManager(session);
        mapper = manager.mapper(MetricReport.class);
        this.session = session;
    }

    public void createReport(MetricReport report, Consumer<Boolean> onComplete) {
        logger.info("createReport - report: {}", report);
        mapper.saveAsync(report, new FutureCallback<Void>() {
            @Override
            public void onSuccess(Void result)
            {
                onComplete.accept(true);
            }

            @Override
            public void onFailure(Throwable t) {
                logger.error("createReport - report: {} ex: ", report, t);
                onComplete.accept(false);
            }
        });
    }
}
