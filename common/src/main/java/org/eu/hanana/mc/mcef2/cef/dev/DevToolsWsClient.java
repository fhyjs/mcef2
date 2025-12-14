package org.eu.hanana.mc.mcef2.cef.dev;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.concurrent.*;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DevToolsWsClient implements Closeable {

    private static final Logger log = LogManager.getLogger(DevToolsWsClient.class);
    private final Gson gson = new Gson();
    private int msgId = 1;
    private final String wsUrl;
    private final HttpClient hc = HttpClient.newHttpClient();
    private CompletableFuture<String> future;
    private WebSocket ws;
    private static final int MAX_RETRY = 10;
    private static final long RETRY_DELAY_MS = 50;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    private volatile int retryCount = 0;
    public DevToolsWsClient(String wsUrl) {
        this.wsUrl = wsUrl;
    }

    public CompletableFuture<String> getBrowserId() {
        if (future!=null) return future;
        future = new CompletableFuture<>();

        hc.newWebSocketBuilder()
        .buildAsync(URI.create(wsUrl), new Listener() {

            @Override
            public void onOpen(WebSocket webSocket) {
                log.info("WebSocket 已连接");
                DevToolsWsClient.this.ws=webSocket;
                sendEvaluate(webSocket);
                Listener.super.onOpen(webSocket);
            }

            @Override
            public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
                JsonObject json = gson.fromJson(data.toString(), JsonObject.class);
                if (!json.has("id")) return Listener.super.onText(ws, data, last);

                JsonObject result = json
                        .getAsJsonObject("result")
                        .getAsJsonObject("result");

                if (result != null && result.has("value") && !result.get("value").isJsonNull()) {
                    String browserId = result.get("value").getAsString();
                    completeSuccess(browserId);
                } else {
                    retry(ws);
                }
                return Listener.super.onText(ws, data, last);
            }

            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                future.completeExceptionally(error);
                Listener.super.onError(webSocket, error);
            }
        });
        return future;
    }
    private void completeSuccess(String id) {
        if (future.complete(id)) {
            cleanup();
        }
    }

    private void completeError(Throwable t) {
        if (future.completeExceptionally(t)) {
            cleanup();
        }
    }

    private void cleanup() {
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "done");
        }
        scheduler.shutdown();
    }
    private void retry(WebSocket ws) {
        if (future.isDone()) return;

        if (++retryCount > MAX_RETRY) {
            completeError(new TimeoutException("等待 __JCEF_BROWSER_ID__ 超时"));
            return;
        }

        scheduler.schedule(() -> {
            if (!future.isDone()) {
                sendEvaluate(ws);
            }
        }, RETRY_DELAY_MS, TimeUnit.MILLISECONDS);
    }
    private void sendEvaluate(WebSocket ws) {
        JsonObject cmd = new JsonObject();
        cmd.addProperty("id", msgId++);
        cmd.addProperty("method", "Runtime.evaluate");

        JsonObject params = new JsonObject();
        params.addProperty("expression", "window.__JCEF_BROWSER_ID__");
        params.addProperty("returnByValue", true);

        cmd.add("params", params);
        ws.sendText(gson.toJson(cmd), true);
    }

    // 测试
    public static void main(String[] args) throws Exception {
        String wsUrl = "ws://127.0.0.1:9222/devtools/page/<targetId>";
        String browserId;
        try (DevToolsWsClient client = new DevToolsWsClient(wsUrl)) {

            browserId = client.getBrowserId().join();
        }
        System.out.println("浏览器唯一标识符: " + browserId);
    }

    @Override
    public void close()  {
        if (ws!=null){
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "bye");
        }
    }
}
