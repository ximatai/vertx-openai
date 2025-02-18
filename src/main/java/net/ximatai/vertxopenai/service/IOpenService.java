package net.ximatai.vertxopenai.service;

import io.vertx.core.json.JsonObject;
import net.ximatai.vertxopenai.session.ChatSession;

/**
 * 消息服务接口
 */
public interface IOpenService {

    /**
     * 创建消息服务
     *
     * @param apiKey  apiKey
     * @param baseUrl baseUrl
     * @return 创建好的消息服务
     */
    static IOpenService create(String apiKey, String baseUrl) {
        return new OpenService(apiKey, baseUrl);
    }

    /**
     * 创建新会话
     *
     * @param model 模型名称
     * @return 创建好的 ChatSession
     */
    default ChatSession connect(String model) {
        return connect(new JsonObject().put("model", model));
    }

    /**
     * 创建新会话
     *
     * @param config 模型详细配置
     * @return 创建好的 ChatSession
     */
    ChatSession connect(JsonObject config);

}
