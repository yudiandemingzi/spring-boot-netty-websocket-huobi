package com.oujiong.exchange.client.huobi;


import com.oujiong.exchange.client.utils.GZipUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @Description: 火币网WebSocket 消息处理类
 * 自定义入站的handler 这个也是核心类
 *
 * @author xub
 * @date 2019/7/30 下午7:07
 */
@Slf4j
public class HuoBiProWebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private HuoBiProWebSocketClient client;

    /**
     * 该handel获取消息的方法
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel channel = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(channel, (FullHttpResponse) msg);
            handshakeFuture.setSuccess();
            return;
        }

        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof BinaryWebSocketFrame) {
            //火币网的数据是压缩过的，所以需要我们进行解压
            BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
            client.onReceive(decodeByteBuf(binaryFrame.content()));
        } else if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textWebSocketFrame = (TextWebSocketFrame) frame;
            client.onReceive(textWebSocketFrame.text());
        } else if (frame instanceof PongWebSocketFrame) {
            log.info("websocket client recived pong!");
        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("WebSocket Client Received Closing.");
            channel.close();
        }
    }

    public HuoBiProWebSocketClientHandler(WebSocketClientHandshaker handshaker, HuoBiProWebSocketClient client) {
        this.handshaker = handshaker;
        this.client = client;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("websocket client disconnected.");
        client.start();
    }
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    /**
     * 解压数据
     */
    private String decodeByteBuf(ByteBuf buf) throws Exception {
        byte[] temp = new byte[buf.readableBytes()];
        buf.readBytes(temp);
        // gzip 解压
        temp = GZipUtils.decompress(temp);
        return new String(temp, StandardCharsets.UTF_8);
    }
}
