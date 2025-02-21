package net.ximatai.vertxopenai.message;

import io.vertx.core.json.JsonObject;

/**
 * AI消息
 */
public class AssistantMessage implements IAssistantMessage {

    /**
     * 原始 json
     */
    private final JsonObject original;
    /**
     * 消息内容
     */
    private final String content;
    /**
     * 推理模型的推理内容
     */
    private final String reasoning;
    /**
     * 是否推理消息
     */
    private final boolean isReasoning;

    /**
     * 根据原始 json 构造AI消息
     *
     * @param original 原始 json
     */
    public AssistantMessage(JsonObject original) {
        this.original = original;
        JsonObject object = original.getJsonArray("choices").getJsonObject(0);
        JsonObject message;
        if (object.containsKey("message")) {
            message = object.getJsonObject("message");
        } else {
            message = object.getJsonObject("delta");
        }

        this.content = message.getString("content");
        this.reasoning = message.getString("reasoning_content");
        if (this.content == null) {
            this.isReasoning = true;
        } else {
            this.isReasoning = false;
        }
    }

    @Override
    public IMessage simple() {
        return new SimpleMessage(content(), role());
    }

    @Override
    public JsonObject original() {
        return original;
    }

    @Override
    public String content() {
        return content;
    }


    @Override
    public String reasoning() {
        return reasoning;
    }


    @Override
    public boolean isReasoning() {
        return isReasoning;
    }

    @Override
    public MessageRole role() {
        return MessageRole.ASSISTANT;
    }
}
