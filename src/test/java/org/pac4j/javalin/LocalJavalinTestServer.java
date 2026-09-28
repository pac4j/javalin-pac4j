package org.pac4j.javalin;

import io.javalin.Javalin;
import io.javalin.config.JavalinConfig;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.LocalConnector;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Runs real servlet requests without opening a network port. */
public class LocalJavalinTestServer implements AutoCloseable {
    private final Javalin app;
    private LocalConnector connector;

    public LocalJavalinTestServer(Consumer<JavalinConfig> configure) {
        app = Javalin.create(cfg -> {
            configure.accept(cfg);
            cfg.startup.showJavalinBanner = false;
            cfg.startup.startupWatcherEnabled = false;
            cfg.jetty.addConnector((server, http) -> {
                connector = new LocalConnector(server);
                return connector;
            });
        }).start();
    }

    public HttpTester.Response get(String path) throws Exception {
        return request("GET", path, "", Map.of());
    }

    public HttpTester.Response request(String method, String path, String body, Map<String, String> headers)
            throws Exception {
        HttpTester.Request request = HttpTester.newRequest();
        request.setMethod(method);
        request.setURI(path);
        request.setVersion("HTTP/1.1");
        request.put("Host", "localhost");
        request.put("Connection", "close");
        headers.forEach(request::put);
        request.setContent(body.getBytes(StandardCharsets.UTF_8));
        return HttpTester.parseResponse(connector.getResponse(request.generate(), 5, TimeUnit.SECONDS));
    }

    @Override
    public void close() {
        app.stop();
    }
}
