# Vertx-OpenAI
### 采用 Vert.x Web Client 对 OpenAI 的 API进行一层薄封装，兼容 DeepSeek、SiliconFlow

### 示例
```java
package net.ximatai.vertxopenai;

import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import net.ximatai.vertxopenai.service.IOpenService;
import net.ximatai.vertxopenai.session.ChatSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(VertxExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChatTest {

    ChatSession chatSession;

    @BeforeAll
    void beforeAll() {
        String key = "sk-ztesneqsatkvopokaxwhjwchpzgubsstdzevzzkvrtrolaqs";
        String url = "https://api.siliconflow.cn/v1/chat/completions";
        IOpenService service = IOpenService.create(key, url);
        chatSession = service.connect("deepseek-ai/DeepSeek-R1-Distill-Qwen-7B");
    }

    @Test
    void testChat(VertxTestContext testContext) {
        chatSession.send("你好，你是谁？")
                .onSuccess(message -> {
                    String content = message.content();
                    System.out.println(content);
                    Assertions.assertNotNull(content);
                    testContext.completeNow();
                });
    }

}
```
