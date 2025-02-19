package net.ximatai.vertxopenai;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import net.ximatai.vertxopenai.message.IMessage;
import net.ximatai.vertxopenai.service.IOpenService;
import net.ximatai.vertxopenai.session.ChatSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChatTest {

    private final Logger logger = LoggerFactory.getLogger(ChatTest.class);

    String url = "https://api.siliconflow.cn/v1/chat/completions";

    ChatSession chatSession;

    @BeforeAll
    void beforeAll() {
        IOpenService service = IOpenService.create(System.getenv("SF_API_KEY"), url);
        chatSession = service.connect("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
    }

    @Test
    @DisplayName("对话测试（异步）")
    void testChat(VertxTestContext testContext) {
        chatSession.send("你好，你是谁？")
                .onSuccess(message -> {
                    String content = message.content();
                    logger.info(content);
                    Assertions.assertNotNull(content);
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("对话测试（同步）")
    void testChatSync() {
        IMessage message = chatSession.send("你好，你是谁？")
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        logger.info(message.content());
        Assertions.assertNotNull(message.content());
    }

    @Test
    @DisplayName("改变模型配置后，对话测试（同步）")
    void testConfigChanged() {
        chatSession.setConfig(new JsonObject().put("model", "meta-llama/Meta-Llama-3.1-8B-Instruct"));
        IMessage message = chatSession.send("你好，你是谁？")
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        logger.info(message.content());
        Assertions.assertNotNull(message.content());
    }

}
