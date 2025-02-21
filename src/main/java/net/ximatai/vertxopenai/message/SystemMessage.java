package net.ximatai.vertxopenai.message;

/**
 * 系统消息，用来做 AI 助手初始化设置
 * @param content 内容
 */
public record SystemMessage(String content) implements IMessage {
    @Override
    public MessageRole role() {
        return MessageRole.SYSTEM;
    }
}
