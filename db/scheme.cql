CREATE KEYSPACE IF NOT EXISTS "chrono"
  WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 1 };

CREATE TYPE IF NOT EXISTS chrono.url_package (
   method varchar,
   urls set<varchar>
);

CREATE TABLE IF NOT EXISTS chrono.test_batch (
    name varchar,
    url_packages list<frozen<url_package>>,
    created timestamp,
    PRIMARY KEY (name)
);

CREATE TYPE IF NOT EXISTS chrono.measurement (
   url varchar,
   time_in_milli bigint
);

CREATE TABLE IF NOT EXISTS chrono.payload (
    unit varchar,
    size bigint,
    data varchar,
    PRIMARY KEY (unit, size)
);

CREATE TABLE IF NOT EXISTS chrono.upload_data (
	test_batch varchar,
    unit varchar,
    size bigint,
    data varchar,
    created timestamp,
    PRIMARY KEY (test_batch, unit, size, created)
);

CREATE TABLE IF NOT EXISTS chrono.report (
	batch_name varchar,
    mode varchar,
    device_id varchar,
	mobile_carrier varchar,
    mobile_rssi varchar,
    wifi_state varchar,
    wifi_rssi varchar,
	gps_coordinates varchar,
  	tag varchar,
	mobile_network_class varchar,
	mobile_network_type varchar,
	client_ip varchar,
    created timestamp,
    measurements list<frozen<measurement>>,
    PRIMARY KEY (batch_name, device_id, created)
);