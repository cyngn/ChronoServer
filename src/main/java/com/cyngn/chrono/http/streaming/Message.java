package com.cyngn.chrono.http.streaming;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The message container sent from the client.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/25/15
 */
public class Message {
    @JsonProperty
    public String address;

    @JsonProperty("api_key")
    public String apiKey;

    @JsonIgnore
    public String body;
}
