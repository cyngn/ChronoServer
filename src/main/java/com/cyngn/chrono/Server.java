package com.cyngn.chrono;

import com.cyngn.chrono.api.*;
import com.cyngn.chrono.config.ServerConfig;
import com.cyngn.chrono.data.SampleMapper;
import com.cyngn.chrono.storage.Bootstrap;
import com.cyngn.chrono.storage.StorageManager;
import com.cyngn.vertx.eventbus.EventBusTools;
import com.cyngn.vertx.web.RestApi;
import com.cyngn.vertx.web.RouterTools;
import com.datastax.driver.core.Cluster;
import com.englishtown.vertx.cassandra.impl.DefaultCassandraSession;
import com.englishtown.vertx.cassandra.impl.JsonCassandraConfigurator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.LocalMap;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.LoggerHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

/**
 * Server, starts everything up.
 *
 * to build:
 * ./gradlew clean shadowJar
 *
 * to run:
 * java -jar build/libs/chrono-server-0.5.0-fat.jar -conf conf.json -instances 2
 *
 * to test:
 * // the urls to hit
 * curl -v "http://localhost:7345/api/v1/endpoints"
 *
 * // sample url, you'll need to use the key in the response from the previous request or grab from the startup log
 * curl -v --header "X-API-Key": [shared_api_key]" "http://localhost:7345/api/v1/timing/static_cached?unit=mb%26size=1"
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/5/14
 */
public class Server extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    // local shared map, shared across all vert.x instances
    private LocalMap<String, Long> sharedData;

    private final String SHARED_DATA = "shared_data";
    public static final String INITIALIZER_THREAD_KEY = "InitializerThread";

    // needs to be static and initialized once to be shared across verticals
    private final static String apiSharedSecret;

    private ServerConfig svrConfig;
    static {
        // create a per startup shared key, that all config endpoints can return across verticle instances
        apiSharedSecret = DigestUtils.sha512Hex(DateTime.now().toString() + new Random().nextFloat());
    }

    private HttpServer server;
    private DefaultCassandraSession session;
    private static String INITIALIZED_MSG = "server.initialized";
    private StorageManager storageManager;

    @Override
    public void start(final Future<Void> startedResult) {
        try {
            JsonObject config = config();
            svrConfig = new ObjectMapper().readValue(config.toString(), ServerConfig.class);
            svrConfig.cassandra = config.getJsonObject("cassandra");
        } catch (IOException e) {
            logger.error("Failed to load the server config", e);
            stop();
        }

        // attempt to become the initializer thread
        sharedData = vertx.sharedData().getLocalMap(SHARED_DATA);
        sharedData.putIfAbsent(INITIALIZER_THREAD_KEY, Thread.currentThread().getId());

        // setup a cassandra connection and storage access
        JsonCassandraConfigurator configurator = new JsonCassandraConfigurator(vertx);
        session = new DefaultCassandraSession(Cluster.builder(), configurator, vertx);

        session.onReady(result -> {
            storageManager = new StorageManager(session, svrConfig);
            // listen for the initialized message, the sending thread receives this also
            EventBusTools.oneShotLocalConsumer(vertx.eventBus(), INITIALIZED_MSG, getStartupHandler());

            if (isInitializerThread()) {
                // initialize the test data
                SampleMapper.getInstance();
                try {
                    logger.info("Starting up server... on ip: {} port: {}",
                            InetAddress.getLocalHost().getHostAddress(), svrConfig.port);
                    logger.info("Using shared api key: {}", apiSharedSecret);

                    // make sure the basic test data is in place
                    new Bootstrap(storageManager, success -> {
                        // bootstrap is done let all threads know they can begin to listen on the server
                        if(success) { vertx.eventBus().publish(INITIALIZED_MSG, new JsonObject()); }
                        else { stop(); }
                    }, svrConfig.defaultTestBaseUrl);
                } catch (UnknownHostException e) {
                    logger.error("Failed to get host ip address", e);
                    stop();
                }
            }
        });
    }

    protected final boolean isInitializerThread() {
        return sharedData.get(INITIALIZER_THREAD_KEY) == Thread.currentThread().getId();
    }

    private Router buildApi(Router router) {
        // log all requests, and setup the shared api key checker in front of all REST calls
        RouterTools.registerRootHandlers(router, LoggerHandler.create(), new SharedKeyHandler(apiSharedSecret));

        List<RestApi> apis = Lists.newArrayList(
                new HealthCheck(),
                new Timing(storageManager),
                new TestEndpoints(storageManager.batchStorage, apiSharedSecret),
                new Report(storageManager.reportStorage)
        );

        // register rest endpoints
        for (RestApi api : apis) {
            api.init(router);
            if (isInitializerThread()) { api.outputApi(logger); }
        }

        return router;
    }

    /**
     * Handles starting the server listening on all interfaces
     *
     * @return a callback to hit when ready for the server to open up listening
     */
    private Handler<Message<Object>> getStartupHandler() {
        return message -> {
            server = vertx.createHttpServer();
            Router router = Router.router(vertx);

            buildApi(router);
            setupSockJSServer(router);
            server.requestHandler(router::accept);

            server.listen(svrConfig.port, "0.0.0.0", event -> {
                if (event.failed()) {
                    logger.error("Failed to start server, error:", event.cause());
                    stop();
                } else { logger.info("Thread: {} starting to handle request", Thread.currentThread().getId()); }
            });
        };
    }

    private void setupSockJSServer(Router router) {
        SockJSHandler handler = SockJSHandler.create(vertx).socketHandler(new TimingStreaming(storageManager, apiSharedSecret));

        router.route(TimingStreaming.ROOT_API + "/*").handler(handler);
    }

    @Override
    public void stop() {
        logger.info("Stopping the server");
        try {
            if(server != null) { server.close(); }
        } finally {
            // Only one thread can close the vertx as vertx is shared across all instances.
            Long shutdownThreadId = sharedData.putIfAbsent("shutdown", Thread.currentThread().getId());
            if (shutdownThreadId == null) {
                vertx.close(event -> {
                    logger.info("Vertx instance closed");
                    System.exit(-1);
                });
            }
        }
    }
}
