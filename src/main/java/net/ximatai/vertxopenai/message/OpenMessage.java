package net.ximatai.vertxopenai.message;

import io.vertx.core.json.JsonObject;

/**
 * 标准 OpenAI 消息
 *
 * @param content 内容
 * @param role    角色
 */
public record OpenMessage(String content, MessageRole role) implements IMessage {

    /**
     * 基于 json 构建消息
     *
     * @param jsonObject json体
     */
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
