package net.ximatai.vertxopenai.message;

import io.vertx.core.json.JsonObject;

public record OpenMessage(String content, MessageRole role) implements IMessage {

    public OpenMessage(JsonObject jsonObject) {
        this(jsonObject.getString("content"), MessageRole.valueOf(jsonObject.getString("role").toUpperCase()));
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put("content", this.content)
                .put("role", this.role().toString().toLowerCase());
    }
}
