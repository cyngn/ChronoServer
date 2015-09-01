
[![Build Status](https://travis-ci.org/cyngn/ChronoServer.svg?branch=master)](https://travis-ci.org/cyngn/ChronoServer)

# ChronoServer

A test server for sampling how long it takes mobile & web clients to make various types of requests to a server doing common request patterns. The idea is you can set this server up in various locations (think DCs) around the globe or country and test what the latency and experience for users will be as you vary the size and style of requests. You can also use the server to see how different CDNs might perform when sitting in front of your server and caching content.

The basic flow is as follows:

* Client gets a collection of URLs to call from server, a mix of GET & POST calls.
* Client executes the URLs timing each one.
* Client reports back to server all the timing data for each call and metadata about the client's connection.

## Getting Started

####Clone the repo & build

```bash
./gradlew clean shadowJar
```

####Setup the Cassandra DB

Options:
 * run standalone
 * run ccm
 * point at your internal cluster

Execute the scheme file in **db/scheme.cql** on your setup.

####Configuration

The server takes the following config

```json
{
    "port" : <port>,
    "default_test_base_url" : "<default http://localhost>",
    "data_retention_seconds" : <default 86400>,
    "cassandra": {
        "seeds": ["localhost"],
        "reconnect": {
            "name": "exponential",
            "base_delay": 1000,
            "max_delay": 10000
        }
    }
}
```

For example:
```json
{
    "port" : 7345,
    "default_test_base_url" : "http://localhost",
    "data_retention_seconds" : 86400,
    "cassandra": {
        "seeds": ["localhost"],
        "reconnect": {
            "name": "exponential",
            "base_delay": 1000,
            "max_delay": 10000
        }
    }
}
```

Field breakdown:

* `port` the port the server listens on
* `default_test_base_url` the base url used in creating the bootstrap sample data
* `data_retention_seconds` the seconds to allow the upload test data from users to live in the DB, this defaults to a day.
* `cassandra` The config to initialize the Cassandra driver with, see [vertx-cassandra](https://github.com/englishtown/vertx-cassandra) for more details.

####Running Server

```bash
#start the server
java -jar build/libs/ChronoServer-0.5.0-fat.jar -conf conf.json -instances 2
```

## REST APIs

#### /api/v1/test_endpoints - GET

Gets a set of URLs for the client to execute.

Parameters:
* `test_batch` - a named collection of URLs you want to pass back to a client to run. Defaults to the bootstrap collection if not specified.

Response:

See [ConfigResponse.java](src/main/java/com/cyngn/chrono/http/ConfigResponse.java)

####/api/v1/report - POST

Allows a user to upload detailed information on the timings for all the calls it made.

Parameters:

See [MetricReport.java](src/main/java/com/cyngn/chrono/storage/entity/MetricReport.java)

####/api/v1/timing - GET
Sub APIs

* /static - grabs `N` data from memory
* /static_cached - grabs `N` data from memory and puts the appropriate cache headers on
* /dynamic - grabs `N` data from the DB
* /dynamic_cached -  grabs `N` data from the DB and puts the appropriate cache headers on

Required Common Parameters:
* `unit` - the unit of the data to get, either `kb` or `mb`
* `size` - the size of data to get back

Response:
```json
{
  "data" : "<a bunch of random data matching your parameters>"
}
```

####/api/v1/timing - POST
Sub APIs

* /store - upload `N` amount data to the DB, server returns after it is successfully written to Cassandra
* /store_mem - upload `N` amount data to server memory, server returns after it gets the full body of the post

Parameters:

See [StorageRequest.java](src/main/java/com/cyngn/chrono/http/StorageRequest.java)

####/healthcheck - GET

Standard health check for things like a LB to hit.

####/api/v1/timing_streaming - SockJS interface

See: [streaming namespace](src/main/java/com/cyngn/chrono/http/streaming) and sample client below.

## Examples

Piping curl output to something like [jsonpp](https://github.com/jmhodges/jsonpp) makes it much more readable.

###curl requests
```bash
#start the server
java -jar build/libs/ChronoServer-0.5.0-fat.jar -conf conf.json -instances 2

#grab a sample config package, take note of the api_key
curl -v "http://localhost:7345/api/v1/test_endpoints" | jsonpp

#grab static data with no caching
curl -v --header "X-API-Key: [api_key here]" "http://localhost:7345/api/v1/timing/static?unit=kb&size=1" | jsonpp

#grab static data with caching
curl -v --header "X-API-Key: [api_key here]" "http://localhost:7345/api/v1/timing/static_cached?unit=kb&size=1" | jsonpp

#grab data from the DB with no caching
curl -v --header "X-API-Key: [api_key here]" "http://localhost:7345/api/v1/timing/dynamic?unit=kb&size=1" | jsonpp

#grab data from the DB with caching
curl -v --header "X-API-Key: [api_key here]" "http://localhost:7345/api/v1/timing/dynamic_cached?unit=kb&size=1" | jsonpp
```

For testing POST calls via the store api [Postman](https://www.getpostman.com/) is great for this.

###sample SockJS client

```html
<html>
    <script src="http:////cdn.jsdelivr.net/sockjs/0.3.4/sockjs.min.js"></script>
    <script src="vertxbus-3.0.0.js"></script>

    <!-- You're going to want to turn off security in your browser when running this probably -->
    <script>
        // this is acquired from the server
        var apiKey = ""
        var sock = new SockJS('http://localhost:7345/api/v1/timing_streaming');

        // download via socket
        var getData = function(address, unit, size) {
            sock.send(JSON.stringify({address: address, "api_key": apiKey,
                        body : {unit : unit, size: size}}));
        };

        // helper func for generating fake data
        var getKb = function(number) {
            var data = ['a', 'b', 'c', 'd', 'e', '1', '2', '3', '4', '5'];
            var str = "";

            var totalKb = 1024 * number;
            for(var i = 0; i < totalKb; i++) {
                str += data[Math.floor((Math.random() * 10))];
            }
            return str;
        };

        // upload via socket
        var putData = function(size) {
            sock.send(JSON.stringify({address: "streaming.store", "api_key": apiKey,
                        body : {test_batch: "some_batch", unit : "kb", size: size, data: getKb(size)}}));
        };

        // get the config, ie api key
        var getApiKey = function() {
            sock.send(JSON.stringify({address: "streaming.config", "api_key": "", body : {}}));
        }

        sock.onopen = function() {
            console.log('connected to server');
            getApiKey();
        };

        sock.onmessage = function(e) {
            console.log('message', e.data);

            var data = JSON.parse(e.data);

            // once we have the api key make some test calls
            if(data.type == "streaming.config") {
                apiKey = data.api_key;
                getData('streaming.static', "kb", 1);
                getData('streaming.dynamic', "kb", 5);
                putData(5);
            }
        };

        sock.onclose = function() {
            console.log('lost connection to server');
        };
    </script>
    <body>
    </body>
</html>
```
