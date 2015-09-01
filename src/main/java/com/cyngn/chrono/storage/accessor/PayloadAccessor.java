package com.cyngn.chrono.storage.accessor;

import com.cyngn.chrono.storage.entity.Payload;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.mapping.annotations.Accessor;
import com.datastax.driver.mapping.annotations.Param;
import com.datastax.driver.mapping.annotations.Query;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * Get and create payloads.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/19/15
 */
@Accessor
public interface PayloadAccessor {
    @Query("SELECT * FROM chrono.payload WHERE unit=:unit AND size=:size")
    ListenableFuture<Payload> getPayloadAsync(@Param("unit") String unit, @Param("size") long size);

    @Query("SELECT unit,size FROM chrono.payload")
    ListenableFuture<ResultSet> getSupportedPayloads();

    @Query("INSERT INTO chrono.payload(unit, size, data) VALUES (:unit, :size, :data) IF NOT EXISTS")
    ListenableFuture<ResultSet> createPayload(@Param("unit") String unit, @Param("size") long size,
                                              @Param("data") String data);

}
