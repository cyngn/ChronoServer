package com.cyngn.chrono.storage.accessor;

import com.cyngn.chrono.storage.entity.TestBatch;
import com.cyngn.chrono.storage.entity.UrlPackage;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

/**
 * Get and create test batches.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/14/15
 */
@Accessor
public interface TestBatchAccessor {

    @Query("SELECT * FROM chrono.test_batch where name = :name")
    ListenableFuture<TestBatch> getTestBatchAsync(@Param("name") String name);

    @Query("INSERT INTO chrono.test_batch(name, created, url_packages) VALUES(:name, dateof(now()), :url_packages) IF NOT EXISTS")
    ListenableFuture<ResultSet> createTestBatch(@Param("name") String name,
                                                @Param("url_packages")List<UrlPackage> urlPackages);
}
