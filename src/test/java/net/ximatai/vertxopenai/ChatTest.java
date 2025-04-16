package net.ximatai.vertxopenai;

import io.vertx.core.json.JsonObject;
import io.vertx.junit5.Timeout;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import net.ximatai.vertxopenai.message.AssistantMessage;
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

import java.util.concurrent.TimeUnit;

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
        chatSession.clear();
        chatSession.setSystemMessage("你是一个翻译器，我说中文你返回英文，不需要返回其他多余的内容");
        chatSession.open()
                .addMessage("你好，你是谁？")
                .send()
                .onSuccess(message -> {
                    String content = message.content();
                    logger.info(content);
                    Assertions.assertNotNull(content);
                    Assertions.assertEquals(2, chatSession.getMessages().size());
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);
    }

    @Test
    @DisplayName("对话测试（同步）")
    void testChatSync() {
        chatSession.clear();
        AssistantMessage message = chatSession
                .open()
                .addMessage("你好，你是谁？")
                .send()
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        logger.info(message.content());

        Assertions.assertEquals(2, chatSession.getMessages().size());
        Assertions.assertNotNull(message.content());

        message = chatSession
                .open()
                .addMessage("你好，你是谁？")
                .send()
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        logger.info(message.content());
        Assertions.assertEquals(4, chatSession.getMessages().size());
        Assertions.assertNotNull(message.content());
    }

    @Test
    @DisplayName("改变模型配置后，对话测试（同步）")
    void testConfigChanged() {
        chatSession.clear();
        chatSession.setConfig(new JsonObject().put("model", "meta-llama/Meta-Llama-3.1-8B-Instruct"));
        AssistantMessage message = chatSession
                .open()
                .addMessage("你好，你是谁？")
                .send()
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        logger.info(message.content());

        Assertions.assertEquals(2, chatSession.getMessages().size());
        Assertions.assertNotNull(message.content());
    }

    @Test
    @DisplayName("测试单次请求")
    void testSendOnce() {
        chatSession.clear();

        AssistantMessage message = chatSession
                .open()
                .temporary()
                .addMessage("你好，你是谁")
                .send()
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        logger.info(message.content());

        Assertions.assertEquals(0, chatSession.getMessages().size());
        Assertions.assertNotNull(message.content());
    }

    @Test
    @Timeout(value = 5, timeUnit = TimeUnit.MINUTES)
    @DisplayName("测试Stream版本会话")
    void testStream(VertxTestContext testContext) {
        chatSession
                .clear()
//                .setSystemMessage("你是一个翻译器，我说中文你返回英文，不需要返回其他多余的内容")
                .open()
                .addMessage("你好，你是谁？")
                .stream(msg -> {
                    if (msg.isReasoning()) {
                        logger.info(msg.reasoning()); // 持续输出推理过程
                    } else {
                        logger.info(msg.content()); // 持续输出结果
                    }
                })
                .send()
                .onSuccess(msg -> {
                    logger.info(msg.content()); // 最终输出结果
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

}
