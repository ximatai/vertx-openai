# Vertx-OpenAI

### 采用 Vert.x Web Client 对 OpenAI 的 API进行一层薄封装，兼容 DeepSeek、SiliconFlow

### 使用

#### gradle

```groovy
implementation("net.ximatai:vertx-openai:1.25.1")
```

#### maven

```xml

<dependency>
    <groupId>net.ximatai</groupId>
    <artifactId>vertx-openai</artifactId>
    <version>1.25.1</version>
</dependency>
```

#### 一个测试用例

```java
package net.ximatai.vertxopenai;

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

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChatTest {

    // 此处的 key 仅供示意，不能用的哦。
    String key = "sk-ztesneqsatkvopokaxwhjwchpzgubsstdzevzzkvrtrolaqs";
    String url = "https://api.siliconflow.cn/v1/chat/completions";

    ChatSession chatSession;

    @BeforeAll
    void beforeAll() {
        IOpenService service = IOpenService.create(key, url);
        chatSession = service.connect("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
    }

    @Test
    @DisplayName("对话测试（异步）")
    void testChat(VertxTestContext testContext) {
        chatSession.clear();
        chatSession.setSystemMessage("你是一个翻译器，我说中文你返回英文，不需要返回其他多余的内容");
        chatSession.send("你好，你是谁？")
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
        IMessage message = chatSession.send("你好，你是谁？")
                .toCompletionStage()
                .toCompletableFuture()
                .join();

        Assertions.assertNotNull(message.content());
    }

    @Test
    @DisplayName("测试Stream版本会话")
    void testStream(VertxTestContext testContext) {
        chatSession
                .clear()
                .setSystemMessage("你是一个翻译器，我说中文你返回英文，不需要返回其他多余的内容")
                .sendWithStream("你好", msg -> {
                    if (msg.isReasoning()) {
                        logger.info(msg.reasoning()); // 持续输出推理过程
                    } else {
                        logger.info(msg.content()); // 持续输出结果
                    }
                })
                .onSuccess(msg -> {
                    logger.info(msg.content()); // 最终输出结果
                    testContext.completeNow();
                })
                .onFailure(testContext::failNow);

    }

}
```
