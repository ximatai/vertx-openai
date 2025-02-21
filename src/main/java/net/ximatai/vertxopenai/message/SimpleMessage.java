package net.ximatai.vertxopenai.message;

/**
 * 只包含内容和角色的简单消息
 * @param content 内容
 * @param role 角色
 */
public record SimpleMessage(String content, MessageRole role) implements IMessage {
}
