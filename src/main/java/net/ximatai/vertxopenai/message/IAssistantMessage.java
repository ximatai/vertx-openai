package net.ximatai.vertxopenai.message;

import io.vertx.core.json.JsonObject;

/**
 * AI消息
 */
public interface IAssistantMessage extends IMessage {

    /**
     * 转化成简单消息
     *
     * @return 简单消息
     */
    IMessage simple();

    /**
     * 原始 json
     *
     * @return 原始 json
     */
    JsonObject original();

    /**
     * 推理内容
     *
     * @return 推理内容
     */
    String reasoning();

    /**
     * 是否推理消息
     *
     * @return 是否推理消息
     */
    boolean isReasoning();
}
