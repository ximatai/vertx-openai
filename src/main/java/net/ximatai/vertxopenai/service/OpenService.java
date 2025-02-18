package net.ximatai.vertxopenai.service;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import net.ximatai.vertxopenai.session.ChatSession;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

public class OpenService implements IOpenService {

    private Vertx vertx;
    private String apiKey;
    private String baseUrl;

    public OpenService(String apiKey, String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        vertx = Vertx.vertx();
    }

    @Override
    public ChatSession connect(JsonObject config) {
        String chatPath;

        WebClientOptions options = new WebClientOptions();
        try {
            URI uri = new URI(this.baseUrl);
            boolean isSSL;
            isSSL = uri.toURL().getProtocol().equals("https");

            int port = uri.getPort();
            if (port == -1) {
                port = isSSL ? 443 : 80;
            }

            String host = uri.getHost();

            options.setSsl(isSSL);
            options.setDefaultHost(host);
            options.setDefaultPort(port);

            chatPath = uri.getPath();

        } catch (URISyntaxException | MalformedURLException e) {
            throw new RuntimeException(e);
        }

        WebClient webClient = WebClient.create(vertx, options);

        return new ChatSession(apiKey, chatPath, config, webClient);
    }

}
