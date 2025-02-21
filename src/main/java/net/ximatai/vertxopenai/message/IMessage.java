package net.ximatai.vertxopenai.message;

import io.vertx.core.json.JsonObject;

/**
 * 消息
 */
public interface IMessage {

    /**
     * 内容
     *
     * @return 消息内容
     */
    String content();

    /**
     * 消息归属角色
     *
     * @return 角色
     */
    MessageRole role();

    /**
     * 转化消息为 Json
     *
     * @return 转化后消息
     */
    default JsonObject toJson() {
        return new JsonObject()
                .put("content", this.content())
                .put("role", this.role().toString().toLowerCase());
    }

    ;
}
