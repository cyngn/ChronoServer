package com.cyngn.chrono.storage.accessor;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Upload data from user tests.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/19/15
 */
@Accessor
public interface UploadAccessor {
    @Query("INSERT INTO chrono.upload_data(test_batch, unit, size, data, created) VALUES (:test_batch, :unit, :size, :data, dateof(now())) USING TTL :ttl")
    ListenableFuture<ResultSet> uploadData(@Param("test_batch") String testBatch, @Param("unit") String unit,
                                           @Param("size") long size, @Param("data") String data, @Param("ttl") int ttl);

}
