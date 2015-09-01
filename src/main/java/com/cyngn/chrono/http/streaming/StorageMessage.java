package com.cyngn.chrono.http.streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Simple storage message class
 */
public class StorageMessage {

    public static class Request {
        @JsonProperty("test_batch")
        public String testBatch;

        @JsonProperty
        public String unit;

        @JsonProperty
        public int size;

        @JsonProperty
        public String data;

        @Override
        public String toString() {
            return "Request{" +
                    "testBatch='" + testBatch + '\'' +
                    ", unit='" + unit + '\'' +
                    ", size=" + size +
                    ", data='" + data + '\'' +
                    '}';
        }
    }

    public static class Response {
        @JsonProperty("test_batch")
        public String testBatch;

        @JsonProperty
        public String unit;

        @JsonProperty
        public int size;

        @JsonProperty
        public String type;

        @JsonProperty
        public String state;

        public Response() { this.state = "stored"; }

        public Response(String testBatch, String unit, int size, String type) {
            this.testBatch = testBatch;
            this.unit = unit;
            this.size = size;
            this.state = "stored";
            this.type = type;
        }
    }
}
