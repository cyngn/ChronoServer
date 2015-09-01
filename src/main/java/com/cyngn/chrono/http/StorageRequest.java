package com.cyngn.chrono.http;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 9/12/14
 */
public class StorageRequest {
    @JsonProperty
    public String unit;

    @JsonProperty
    public int size;

    @JsonProperty("test_batch")
    public String testBatch;

    @JsonProperty
    public String data;

    public StorageRequest() {}
}
