package com.cyngn.chrono.http.streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Handles getting the API shared key from the server.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/26/15
 */
public class ConfigMessage {
    public static class Response {
        @JsonProperty("api_key")
        public String apiKey ;

        @JsonProperty
        public String type;

        public Response() {}

        public Response(String apiKey, String type) {
            this.apiKey = apiKey;
            this.type = type;
        }
    }
}
