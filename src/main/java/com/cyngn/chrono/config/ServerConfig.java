package com.cyngn.chrono.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;

/**
 * The config data to initialize the service with.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/12/14
 */
public class ServerConfig {

    @JsonProperty
    public int port;

    @JsonProperty("default_test_base_url")
    public String defaultTestBaseUrl;

    @JsonProperty("data_retention_seconds")
    public int dataRetentionSeconds;

    @JsonProperty
    @JsonIgnore
    public JsonObject cassandra;
}
