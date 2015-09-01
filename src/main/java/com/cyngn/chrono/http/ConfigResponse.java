package com.cyngn.chrono.http;

import com.cyngn.chrono.storage.entity.UrlPackage;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author truelove@cyngn.com (Jeremy Truelove) 8/14/15
 */
public class ConfigResponse {
    @JsonProperty("api_key")
    public String apiKey;

    @JsonProperty("url_packages")
    public List<UrlPackage> urlPackages;

    public ConfigResponse(String apiKey, List<UrlPackage> urlPackages) {
        this.apiKey = apiKey;
        this.urlPackages = urlPackages;
    }
}
