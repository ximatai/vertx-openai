package net.ximatai.vertxopenai.session;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import net.ximatai.vertxopenai.message.IMessage;
import net.ximatai.vertxopenai.message.MessageRole;
import net.ximatai.vertxopenai.message.OpenMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
     * 设置模型配置
     *
     * @param config 配置
     */
    public void setConfig(JsonObject config) {
        this.config = config;
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
     * 发送消息
     *
     * @param message 消息字符串
     * @return 返回消息（异步）
     */
    public Future<IMessage> send(String message) {
        return this.send(new OpenMessage(message, MessageRole.USER));
    }

    /**
     * 发送消息
     *
     * @param message 消息体
     * @return 返回消息（异步）
     */
    public Future<IMessage> send(IMessage message) {
        messages.add(message);

        return sendBatch(messages)
                .compose(result -> {
                    messages.add(result);
                    return Future.succeededFuture(result);
                });
    }

    /**
     * 发送消息
     *
     * @param message 消息字符串
     * @return 返回消息（异步）
     */
    public Future<IMessage> sendOnce(String message) {
        return this.sendOnce(new OpenMessage(message, MessageRole.USER));
    }

    /**
     * 发送一次性消息
     *
     * @param message 消息体
     * @return 返回消息（异步）
     */
    public Future<IMessage> sendOnce(IMessage message) {
        return sendBatch(List.of(message));
    }

    /**
     * 发送消息对话
     *
     * @param messages 消息列表
     * @return 返回消息（异步）
     */
    public Future<IMessage> sendBatch(List<IMessage> messages) {
        Promise<IMessage> promise = Promise.promise();

        webClient
                .post(chatPath)
                .putHeader("Content-Type", "application/json")
                .putHeader("Authorization", "Bearer " + apiKey)
                .sendJsonObject(
                        messagesToJson(messages)
                )
                .expecting(HttpResponseExpectation.SC_SUCCESS)
                .onSuccess(res -> {
                    logger.debug("http status: {}", res.statusCode());
                    logger.debug("http body: {}", res.bodyAsString());
                    JsonObject object = res.bodyAsJsonObject();
                    JsonObject jsonMessage = object.getJsonArray("choices").getJsonObject(0).getJsonObject("message");
                    OpenMessage responseMessage = new OpenMessage(jsonMessage);

                    promise.complete(responseMessage);
                })
                .onFailure(err -> {
                    logger.error(err.getMessage());
                    promise.fail(err);
                });

        return promise.future();
    }

    private JsonObject messagesToJson(List<IMessage> messages) {
        return config.copy()
                .put("messages", messages.stream().map(IMessage::toJson).toList());
    }

    /**
     * 清空消息历史
     */
    public void clearMessages() {
        messages.clear();
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
