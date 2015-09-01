package com.cyngn.chrono.storage.entity;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A collection of data returned to the client.
 *
* @author truelove@cyngn.com (Jeremy Truelove) 9/12/14
*/
@Table(keyspace = "chrono", name = "payload")
public class Payload {
    @PartitionKey(0)
    @Column
    @JsonProperty
    public String unit;

    @PartitionKey(1)
    @Column
    @JsonProperty
    public long size;

    @Column
    @JsonProperty
    public String data;

    public Payload() { }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
