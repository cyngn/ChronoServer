package com.cyngn.chrono.storage.entity;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A measurement from the client on the time taken to process a URL.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/15/14
 */
@UDT(keyspace = "chrono", name = "measurement")
public class Measurement {
    @Field
    @JsonProperty
    public String url;

    @Field(name = "time_in_milli")
    @JsonProperty("time_in_milli")
    public long timeInMilli;

    public Measurement() {}

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTimeInMilli() {
        return timeInMilli;
    }

    public void setTimeInMilli(long timeInMilli) { this.timeInMilli = timeInMilli; }

    @Override
    public String toString() {
        return "Measurement{" +
                "url='" + url + '\'' +
                ", timeInMilli=" + timeInMilli +
                '}';
    }
}
