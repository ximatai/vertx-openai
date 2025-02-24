package net.ximatai.vertxopenai.session;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import net.ximatai.vertxopenai.message.AssistantMessage;
import net.ximatai.vertxopenai.message.IMessage;
import net.ximatai.vertxopenai.message.SSEParser;
import net.ximatai.vertxopenai.message.SystemMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 开启会话
 */
public class ChatSession {

    private final Logger logger = LoggerFactory.getLogger(ChatSession.class);

    private String apiKey;
    private String chatPath;
    private JsonObject config;
    private WebClient webClient;
    private List<IMessage> messages = new ArrayList<>();
    private SystemMessage systemMessage;

    /**
     * 开启会话
     *
     * @param apiKey    apiKey
     * @param chatPath  chat接口路径
     * @param config    模型可选配置，如：frequency_penalty、max_tokens、temperature 等
     * @param webClient webClient
     */
    public ChatSession(String apiKey, String chatPath, JsonObject config, WebClient webClient) {
        this.apiKey = apiKey;
        this.chatPath = chatPath;
        this.config = config;
        this.webClient = webClient;
    }

    /**
     * 设置系统消息
     *
     * @param message 系统消息内容
     * @return this
     */
    public ChatSession setSystemMessage(String message) {
        Objects.requireNonNull(message, "System message cannot be null");
        this.systemMessage = new SystemMessage(message);
        return this;
    }

    /**
     * 设置模型配置
     *
     * @param config 配置
     * @return this
     */
    public ChatSession setConfig(JsonObject config) {
        this.config = config;
        return this;
    }

    /**
     * 获取模型配置
     *
     * @return 模型配置
     */
    public JsonObject getConfig() {
        return config;
    }

    /**
     * 创建请求
     *
     * @return ChatRequest
     */
    public ChatRequest request() {
        return new ChatRequest(this);
    }

    /**
     * 发送消息对话
     *
     * @param messages    消息列表
     * @param isTemporary 是否临时消息
     * @return AI返回消息（异步）
     */
    public Future<AssistantMessage> sendBatch(List<IMessage> messages, boolean isTemporary) {
        Promise<AssistantMessage> promise = Promise.promise();

        if (!isTemporary) {
            this.messages.addAll(messages);
        }

        webClient
                .post(chatPath)
                .putHeader("Content-Type", "application/json")
                .putHeader("Authorization", "Bearer " + apiKey)
                .sendJsonObject(
                        buildRequestBody(messages)
                )
                .expecting(HttpResponseExpectation.SC_SUCCESS)
                .onSuccess(res -> {
                    logger.debug("http status: {}", res.statusCode());
                    logger.debug("http body: {}", res.bodyAsString());
                    AssistantMessage responseMessage = new AssistantMessage(res.bodyAsJsonObject());

                    if (!isTemporary) {
                        this.messages.add(responseMessage.simple());
                    }

                    promise.complete(responseMessage);
                })
                .onFailure(err -> {
                    logger.error(err.getMessage());
                    promise.fail(err);
                });

        return promise.future();
    }

    /**
     * 发送消息对话（stream模式）
     *
     * @param messages     消息列表
     * @param isTemporary  是否临时消息
     * @param eventHandler 流式回调
     * @return AI返回消息（异步）
     */
    public Future<AssistantMessage> sendBatchWithStream(List<IMessage> messages, boolean isTemporary, Handler<AssistantMessage> eventHandler) {

        Promise<AssistantMessage> promise = Promise.promise();
        StringBuilder messageBuilder = new StringBuilder();
        AtomicReference<String> lastData = new AtomicReference<>();

        if (!isTemporary) {
            this.messages.addAll(messages);
        }

        SSEParser sseParser = new SSEParser(data -> {
            AssistantMessage assistantMessage = new AssistantMessage(new JsonObject(data));
            lastData.set(data);
            eventHandler.handle(assistantMessage);
            if (!assistantMessage.isReasoning()) {
                messageBuilder.append(assistantMessage.content());
            }
        }, end -> {
            JsonObject json = new JsonObject(lastData.get());
            json.put("choices", new JsonArray(
                    List.of(
                            json.getJsonArray("choices")
                                    .getJsonObject(0)
                                    .put("message", new JsonObject().put("content", messageBuilder.toString().trim()))
                    )
            ));

            AssistantMessage assistantMessage = new AssistantMessage(json);

            if (!isTemporary) {
                this.messages.add(assistantMessage.simple());
            }

            promise.complete(assistantMessage);
        });

        webClient
                .post(chatPath)
                .as(BodyCodec.pipe(sseParser))
                .putHeader("Content-Type", "application/json")
                .putHeader("Authorization", "Bearer " + apiKey)
                .sendJsonObject(
                        buildRequestBody(messages)
                                .put("stream", true)
                )
                .expecting(HttpResponseExpectation.SC_SUCCESS)
                .onSuccess(res -> {
                    logger.debug("http status: {}", res.statusCode());
                })
                .onFailure(err -> {
                    logger.error(err.getMessage());
                    promise.fail(err);
                });

        return promise.future();
    }

    private JsonObject buildRequestBody(List<IMessage> messages) {

        List<JsonObject> messageList = new ArrayList<>(messages.stream().map(IMessage::toJson).toList());
        if (systemMessage != null) {
            messageList.add(0, systemMessage.toJson());
        }

        return config.copy()
                .put("messages", messageList);
    }

    /**
     * 清空会话历史包含 systemMessage
     *
     * @return this
     */
    public ChatSession clear() {
        clearMessages();
        systemMessage = null;
        return this;
    }

    /**
     * 清空消息历史
     *
     * @return this
     */
    public ChatSession clearMessages() {
        messages.clear();
        return this;
    }

    /**
     * 获取会话中所有消息列表
     *
     * @return 消息列表
     */
    public List<IMessage> getMessages() {
        return messages;
    }
}
