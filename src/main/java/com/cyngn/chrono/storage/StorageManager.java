package com.cyngn.chrono.storage;

import com.cyngn.chrono.config.ServerConfig;
import com.englishtown.vertx.cassandra.CassandraSession;

/**
 * Manage access to storage objects.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/20/15
 */
public class StorageManager {
    public final TestBatchStorage batchStorage;
    public final PayloadStorage payloadStorage;
    public final ReportStorage reportStorage;
    public final CassandraSession session;
    public final UploadStorage uploadStorage;

    public StorageManager(CassandraSession session, ServerConfig cfg) {
        this.session = session;
        batchStorage = new TestBatchStorage(session);
        payloadStorage = new PayloadStorage(session);
        reportStorage = new ReportStorage(session);
        uploadStorage = new UploadStorage(session, cfg.dataRetentionSeconds);
    }
}
