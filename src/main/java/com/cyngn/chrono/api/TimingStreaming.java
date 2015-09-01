package com.cyngn.chrono.api;

import com.cyngn.chrono.data.SampleMapper;
import com.cyngn.chrono.http.streaming.*;
import com.cyngn.chrono.storage.StorageManager;
import com.cyngn.vertx.web.JsonUtil;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.handler.sockjs.SockJSSocket;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Handles returning data sets to end users to determine network performance for streaming clients
 *
 * @author truelove@cyngn.com (Jeremy Truelove) 9/12/14
 */
public class TimingStreaming implements Handler<SockJSSocket> {
    private static final Logger logger = LoggerFactory.getLogger(TimingStreaming.class);
    public static final String ROOT_API = "/api/v1/timing_streaming";

    private final static String STREAMING_NAMESPACE = "streaming";
    public final static String STATIC_TOPIC = STREAMING_NAMESPACE + ".static";
    public final static String DYNAMIC_TOPIC = STREAMING_NAMESPACE + ".dynamic";
    public final static String STORE_TOPIC = STREAMING_NAMESPACE + ".store";
    public final static String CONFIG_TOPIC = STREAMING_NAMESPACE + ".config";

    private final StorageManager storageManager;
    private String apiSharedSecret;
    private Map<String, BiConsumer<SockJSSocket,Message>> actions;

    public TimingStreaming(StorageManager storageManager, String apiSharedSecret) {
        this.apiSharedSecret = apiSharedSecret;
        this.storageManager = storageManager;

        actions = new HashMap<>();
        actions.put(STATIC_TOPIC, this::handleStaticRequest);
        actions.put(DYNAMIC_TOPIC, this::handleDynamicRequest);
        actions.put(STORE_TOPIC, this::handleStoreRequest);
        actions.put(CONFIG_TOPIC, this::handleConfigRequest);
    }

    /**
     * Handles distributing the shared api key.
     *
     * @param socket the socket the event is coming from
     * @param message the message the socket sent
     */
    private void handleConfigRequest(SockJSSocket socket, Message message) {
        sendData(socket, new ConfigMessage.Response(apiSharedSecret, CONFIG_TOPIC));
    }

    /**
     * Handle returning static in memory content to a streaming socket.
     *
     * @param socket the socket the event is coming from
     * @param message the message the socket sent
     */
    protected void handleStaticRequest(SockJSSocket socket, Message message) {
        QueryMessage.Request msg = JsonUtil.parseJsonToObject(message.body,
                QueryMessage.Request.class);

        Object data;
        if (msg != null && isValidRequest(message.apiKey, msg.unit, msg.size)) {
            logger.info("handleStaticRequest - msg: {}", msg);
            data = new QueryMessage.Response(msg.unit, msg.size,
                    SampleMapper.getInstance().getPayload(msg.unit, msg.size), STATIC_TOPIC);
        } else { data = new ErrorMessage(String.format("Invalid message, msg: %s", message.body)); }

        sendData(socket, data);
    }

    private <T> void sendData(SockJSSocket socket, T data) {
        socket.write(Buffer.buffer(JsonUtil.getJsonForObject(data)));
    }

    /**
     * Handle streaming request for data in a DB and respond over the streaming socket.
     *
     * @param socket the socket the event is coming from
     * @param message the message the socket sent
     */
    protected void handleDynamicRequest(SockJSSocket socket, Message message) {
        QueryMessage.Request msg = JsonUtil.parseJsonToObject(message.body,
                QueryMessage.Request.class);

        if (msg != null && isValidRequest(message.apiKey, msg.unit, msg.size)) {
            logger.info("handleDynamicRequest - msg: {}", msg);
            storageManager.payloadStorage.getPayload(msg.unit, msg.size, (success, result) -> {
                if (success) {
                    sendData(socket, new QueryMessage.Response(msg.unit, msg.size, result.data, DYNAMIC_TOPIC));
                } else {
                    sendData(socket, new ErrorMessage("Failed to read from storage"));
                }
            });
        } else { sendData(socket, new ErrorMessage(String.format("Invalid message, msg: %s", message.body))); }
    }

    /**
     * Take a streaming request to store some data
     *
     * @param socket
     */
    protected void handleStoreRequest(SockJSSocket socket, Message event) {
        StorageMessage.Request msg = JsonUtil.parseJsonToObject(event.body,
                StorageMessage.Request.class);

        if (msg != null && StringUtils.equals(event.apiKey, apiSharedSecret)) {
            logger.info("handleStoreRequest - msg: {}", msg);
            storageManager.uploadStorage.uploadData(msg.testBatch, msg.unit, msg.size, msg.data, success -> {
                if (success) {
                    sendData(socket, new StorageMessage.Response(msg.testBatch, msg.unit, msg.size, STORE_TOPIC));
                } else {
                    sendData(socket, new ErrorMessage("Failed to write from storage"));
                }
            });
        } else { sendData(socket, new ErrorMessage(String.format("Invalid message, msg: %s", event.body))); }
    }

    private boolean isValidRequest(String apiKey, String unit, int size) {
        return StringUtils.equals(apiKey, apiSharedSecret) && SampleMapper.areDataParamsValid(unit, size);
    }

    @Override
    public void handle(SockJSSocket socket) {
        socket.handler(data -> {
            JsonObject json = new JsonObject(data.toString());

            // just look at messages we are explicitly sending
            if (!json.containsKey("address")) { return; }

            Message msg = JsonUtil.parseJsonToObject(data.toString(), Message.class);
            if ( msg != null) {
                // delay processing of the body content
                msg.body = json.getJsonObject("body").encode();

                BiConsumer<SockJSSocket, Message> action = actions.get(msg.address);
                // map the address to a known action
                if (action != null) {
                    action.accept(socket, msg);
                }
            }
        });
    }
}
