package net.ximatai.vertxopenai.session;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import net.ximatai.vertxopenai.message.AssistantMessage;
import net.ximatai.vertxopenai.message.IMessage;
import net.ximatai.vertxopenai.message.UserMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * 一次请求对应一个ChatRequest
 */
public class ChatRequest {

    private final ChatSession chatSession;
    private boolean isTemporary = false;
    private boolean isStream;
    private Handler<AssistantMessage> eventHandler;

    List<IMessage> messages = new ArrayList<>();

    /**
     * 一次请求
     *
     * @param chatSession 会话
     */
    public ChatRequest(ChatSession chatSession) {
        this.chatSession = chatSession;
    }

    /**
     * 设置为临时请求（不记录消息到会话上下文）
     *
     * @return this
     */
    public ChatRequest temporary() {
        this.isTemporary = true;
        return this;
    }

    /**
     * 添加消息（字符串）
     *
     * @param message 消息字符串
     * @return this
     */
    public ChatRequest addMessage(String message) {
        return this.addMessage(new UserMessage(message));
    }

    /**
     * 添加消息
     *
     * @param message 消息
     * @return this
     */
    public ChatRequest addMessage(IMessage message) {
        this.messages.add(message);
        return this;
    }

    /**
     * 设置为流式请求
     * @param eventHandler 流过程回调
     * @return this
     */
    public ChatRequest stream(Handler<AssistantMessage> eventHandler) {
        this.eventHandler = eventHandler;
        this.isStream = true;
        return this;
    }

    /**
     * 发送请求
     * @return AI返回消息（异步）
     */
    public Future<AssistantMessage> send() {
        if (this.messages.isEmpty()) {
            throw new IllegalStateException("No messages to send");
        }

        if (isStream) {
            return chatSession.sendBatchWithStream(messages, isTemporary, eventHandler);
        } else {
            return chatSession.sendBatch(messages, isTemporary);
        }
    }
}
