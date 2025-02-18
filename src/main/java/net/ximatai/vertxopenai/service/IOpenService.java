package net.ximatai.vertxopenai.service;

import io.vertx.core.json.JsonObject;
import net.ximatai.vertxopenai.session.ChatSession;

public interface IOpenService {

    static IOpenService create(String apiKey, String baseUrl) {
        return new OpenService(apiKey, baseUrl);
    }

    default ChatSession connect(String model) {
        return connect(new JsonObject().put("model", model));
    }

    ChatSession connect(JsonObject config);

}
