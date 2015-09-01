package com.cyngn.chrono.http.streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple message class for requesting data.
 */
public class QueryMessage {

    public static class Request {
        @JsonProperty
        public String unit;

        @JsonProperty
        public int size;

        @Override
        public String toString() {
            return "Request{" +
                    "unit='" + unit + '\'' +
                    ", size=" + size +
                    '}';
        }
    }

    public static class Response {
        @JsonProperty
        public String unit;

        @JsonProperty
        public int size;

        @JsonProperty
        public String data;

        @JsonProperty
        public String type;

        public Response() {}

        public Response(String unit, int size, String data, String type) {
            this.unit = unit;
            this.size = size;
            this.data = data;
            this.type = type;
        }
    }
}
