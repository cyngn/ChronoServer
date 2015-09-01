package com.cyngn.chrono.storage.entity;

import com.datastax.driver.mapping.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * The test URLs stored in our DB to have the client use for testing.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/15/14
 */
@Table(keyspace = "chrono", name = "test_batch")
public class TestBatch {

    @PartitionKey
    @Column
    @JsonProperty("name")
    public String name;

    @Column
    @JsonProperty
    public Date created;

    @FrozenValue
    @Column(name = "url_packages")
    @JsonProperty("url_packages")
    public List<UrlPackage> urlPackages;

    public TestBatch() {}

    public TestBatch(String name, List<UrlPackage> urlPackages) {
        this.name = name;
        this.urlPackages = urlPackages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UrlPackage> getUrlPackages() {
        return urlPackages;
    }

    public void setUrlPackages(List<UrlPackage> urlPackages) {
        this.urlPackages = urlPackages;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return "TestBatch{" +
                "name='" + name + '\'' +
                ", created=" + created +
                ", urlPackages=" + StringUtils.join(urlPackages, ",") +
                "]}";
    }
}
