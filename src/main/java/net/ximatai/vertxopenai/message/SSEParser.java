package net.ximatai.vertxopenai.message;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.parsetools.RecordParser;
import io.vertx.core.streams.WriteStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SSE (Server-Sent Events) 解析器
 */
public class SSEParser implements WriteStream<Buffer> {
    private static final Logger logger = LoggerFactory.getLogger(SSEParser.class);
    private int writeQueueMaxSize = Integer.MAX_VALUE;
    private int currentQueueSize = 0;
    private Handler<Void> drainHandler;
    private final Handler<String> eventHandler;
    private Handler<Throwable> exceptionHandler;
    private Handler<Void> endHandler;
    private final RecordParser recordParser;
    private final StringBuilder dataBuffer = new StringBuilder();

    /**
     * 穿件 SSE 消息转换器
     * @param eventHandler 消息到达回调
     * @param endHandler SSE消息结束回调
     */
    public SSEParser(Handler<String> eventHandler, Handler<Void> endHandler) {
        this.eventHandler = eventHandler;
        this.endHandler = endHandler;
        this.recordParser = RecordParser.newDelimited("\n", this::handleLine);
    }

    @Override
    public WriteStream<Buffer> exceptionHandler(Handler<Throwable> handler) {
        this.exceptionHandler = handler;
        return this;
    }

    /**
     * 处理 SSE 数据并异步通知调用者
     */
    @Override
    public void write(Buffer data, Handler<AsyncResult<Void>> handler) {
        if (writeQueueFull()) {
            handler.handle(Future.failedFuture(new IllegalStateException("Write queue is full")));
            return;
        }

        try {
            currentQueueSize += data.length();
            recordParser.handle(data);
            handler.handle(Future.succeededFuture());

            // 如果写入后队列从满变成非满，触发 drainHandler
            if (currentQueueSize < writeQueueMaxSize && drainHandler != null) {
                drainHandler.handle(null);
            }
        } catch (Exception e) {
            logger.error("Error processing SSE data", e);
            handler.handle(Future.failedFuture(e));
        }
    }

    /**
     * 处理 SSE 数据并返回 Future
     */
    @Override
    public Future<Void> write(Buffer buffer) {
        if (writeQueueFull()) {
            return Future.failedFuture(new IllegalStateException("Write queue is full"));
        }

        currentQueueSize += buffer.length();
        recordParser.handle(buffer);

        // 如果写入后队列从满变成非满，触发 drainHandler
        if (currentQueueSize < writeQueueMaxSize && drainHandler != null) {
            drainHandler.handle(null);
        }

        return Future.succeededFuture();
    }

    /**
     * 结束 SSE 解析，返回 Future
     */
    @Override
    public Future<Void> end() {
        try {
            flushData();
        } catch (Exception e) {
            logger.error("Error flushing SSE data during end", e);
        }

        if (endHandler != null) {
            endHandler.handle(null);
        }

        logger.debug("SSE stream ended.");
        return Future.succeededFuture();
    }

    @Override
    public void end(Handler<AsyncResult<Void>> handler) {
        try {
            flushData(); // 处理剩余数据
            if (endHandler != null) {
                endHandler.handle(null);
            }
            logger.debug("SSE stream ended.");
            handler.handle(Future.succeededFuture()); // 通知调用方流已结束
        } catch (Exception e) {
            logger.error("Error ending SSE stream", e);
            handler.handle(Future.failedFuture(e)); // 发生异常，通知调用方
        }
    }

    @Override
    public WriteStream<Buffer> setWriteQueueMaxSize(int maxSize) {
        this.writeQueueMaxSize = maxSize;
        return this;
    }

    @Override
    public boolean writeQueueFull() {
        return currentQueueSize >= writeQueueMaxSize;
    }

    @Override
    public WriteStream<Buffer> drainHandler(Handler<Void> handler) {
        this.drainHandler = handler;
        return this;
    }

    /**
     * 解析 SSE 行
     */
    private void handleLine(Buffer buffer) {
        String line = buffer.toString().trim();

        if (line.isEmpty()) {
            flushData(); // 处理完整的 SSE 消息
        } else {
            dataBuffer.append(line).append("\n");
        }
    }

    /**
     * 处理完整的 SSE 消息
     */
    private void flushData() {
        if (dataBuffer.isEmpty()) {
            return;
        }

        String[] lines = dataBuffer.toString().split("\n");
        StringBuilder eventData = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("data: [DONE]")) { // 最后一行结束了
                logger.debug("end");
            } else if (line.startsWith("data:")) {
                eventData.append(line.substring(5).trim()).append("\n");
            } else {
                logger.debug("Skipping non-data SSE line: {}", line);
            }
        }

        if (eventData.isEmpty()) {
            dataBuffer.setLength(0);
            return;
        }

        String data = eventData.toString().trim();
        int dataSize = eventData.length();
        dataBuffer.setLength(0);

        try {
            if (eventHandler != null) {
                eventHandler.handle(data);
            }
        } catch (Exception e) {
            logger.error("Error processing SSE event: {}", data, e);
            if (exceptionHandler != null) {
                exceptionHandler.handle(e);
            }
        }

        // 释放队列占用的大小
        currentQueueSize -= dataSize;
        if (currentQueueSize < 0) {
            currentQueueSize = 0;
        }

        if (!writeQueueFull() && drainHandler != null) {
            drainHandler.handle(null);
        }
    }

}
