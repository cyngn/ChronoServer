package com.cyngn.chrono.storage.entity;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.FrozenValue;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.List;

/**
 * The expected payload coming from the client after they've run all the tests.
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/12/14
 */
@Table(keyspace = "chrono", name = "report")
public class MetricReport {

    @JsonProperty("batch_name")
    @PartitionKey(0)
    @Column(name = "batch_name")
    public String batchName;

    /**
     * ie wifi or data
     */
    @JsonProperty
    @Column
    public String mode;

    @JsonProperty("device_id")
    @PartitionKey(1)
    @Column(name = "device_id")
    public String deviceId;

    @JsonProperty("mobile_carrier")
    @Column(name = "mobile_carrier")
    public String mobileCarrier;

    @JsonProperty("mobile_rssi")
    @Column(name = "mobile_rssi")
    public String mobileRSSI;

    @JsonProperty("wifi_state")
    @Column(name = "wifi_state")
    public String wifiState;

    @JsonProperty("wifi_rssi")
    @Column(name = "wifi_rssi")
    public String wifiRssi;

    @JsonProperty("gps_coordinates")
    @Column(name = "gps_coordinates")
    public String gpsCoordinates;

    @JsonProperty
    @Column
    public String tag;

    @JsonProperty("mobile_network_class")
    @Column(name = "mobile_network_class")
    public String  mobileNetworkClass;

    @JsonProperty("mobile_network_type")
    @Column(name = "mobile_network_type")
    public String  mobileNetworkType;

    @JsonIgnore
    @PartitionKey(2)
    @Column(name = "created")
    public Date created;

    @JsonIgnore
    @Column(name = "client_ip")
    public String client_ip;

    @JsonProperty
    @FrozenValue
    @Column
    public List<Measurement> measurements;

    public MetricReport() { }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getMobileCarrier() {
        return mobileCarrier;
    }

    public void setMobileCarrier(String mobileCarrier) {
        this.mobileCarrier = mobileCarrier;
    }

    public String getMobileRSSI() {
        return mobileRSSI;
    }

    public void setMobileRSSI(String mobileRSSI) {
        this.mobileRSSI = mobileRSSI;
    }

    public String getWifiState() {
        return wifiState;
    }

    public void setWifiState(String wifiState) {
        this.wifiState = wifiState;
    }

    public String getWifiRssi() {
        return wifiRssi;
    }

    public void setWifiRssi(String wifiRssi) {
        this.wifiRssi = wifiRssi;
    }

    public String getGpsCoordinates() {
        return gpsCoordinates;
    }

    public void setGpsCoordinates(String gpsCoordinates) {
        this.gpsCoordinates = gpsCoordinates;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getMobileNetworkClass() {
        return mobileNetworkClass;
    }

    public void setMobileNetworkClass(String mobileNetworkClass) {
        this.mobileNetworkClass = mobileNetworkClass;
    }

    public String getMobileNetworkType() {
        return mobileNetworkType;
    }

    public void setMobileNetworkType(String mobileNetworkType) {
        this.mobileNetworkType = mobileNetworkType;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getClient_ip() {
        return client_ip;
    }

    public void setClient_ip(String client_ip) {
        this.client_ip = client_ip;
    }

    public List<Measurement> getMeasurements() {
        return measurements;
    }

    public void setMeasurements(List<Measurement> measurements) {
        this.measurements = measurements;
    }

    @Override
    public String toString() {
        return "MetricReport{" +
                "batchName='" + batchName + '\'' +
                ", mode='" + mode + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", mobileCarrier='" + mobileCarrier + '\'' +
                ", mobileRSSI='" + mobileRSSI + '\'' +
                ", wifiState='" + wifiState + '\'' +
                ", wifiRssi='" + wifiRssi + '\'' +
                ", gpsCoordinates='" + gpsCoordinates + '\'' +
                ", tag='" + tag + '\'' +
                ", mobileNetworkClass='" + mobileNetworkClass + '\'' +
                ", mobileNetworkType='" + mobileNetworkType + '\'' +
                ", created=" + created +
                ", client_ip='" + client_ip + '\'' +
                ", measurements=[" + StringUtils.join(measurements, ",") +
                "]}";
    }
}