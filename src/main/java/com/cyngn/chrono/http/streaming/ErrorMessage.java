package com.cyngn.chrono.http.streaming;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Error related to a streaming message.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 8/25/15
 */
public class ErrorMessage {
    @JsonProperty
    public String error;

    public ErrorMessage() {
    }

    public ErrorMessage(String error) {
        this.error = error;
    }
}
