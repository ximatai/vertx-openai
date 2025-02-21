package net.ximatai.vertxopenai.message;

/**
 * 标准 OpenAI 消息
 *
 * @param content 内容
 */
public record UserMessage(String content) implements IMessage {

    @Override
    public MessageRole role() {
        return MessageRole.USER;
    }
}
