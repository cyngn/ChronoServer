package com.cyngn.chrono.storage.entity;

import com.datastax.driver.mapping.annotations.Field;
import com.datastax.driver.mapping.annotations.UDT;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * The URLs and the method to use to send them.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/25/14
 */
@UDT(keyspace = "chrono", name = "url_package")
public class UrlPackage {

    @Field
    @JsonProperty
    public String method;

    @Field
    @JsonProperty
    public Set<String> urls;

    public UrlPackage() {
    }

    public UrlPackage(String method, Set<String> urls) {
        this.method = method;
        this.urls = urls;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Set<String> getUrls() {
        return urls;
    }

    public void setUrls(Set<String> urls) {
        this.urls = urls;
    }

    @Override
    public String toString() {
        return "UrlPackage{" +
                "method='" + method + '\'' +
                ", urls=[" + StringUtils.join(urls, ",") +
                "]}";
    }
}
