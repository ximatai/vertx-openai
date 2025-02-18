package net.ximatai.vertxopenai.message;

import io.vertx.core.json.JsonObject;

public interface IMessage {

    String content();

    MessageRole role();

    JsonObject toJson();
}
