package com.oujiong.exchange.client.common;

import com.google.common.collect.Sets;
import com.oujiong.exchange.client.utils.MonitorTask;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description: 抽离出 父类
 * 以后主要新接入一家交易所，都需要继承该父类
 *
 * @author xub
 * @date 2019/7/30 下午5:30
 */
@Slf4j
public abstract class AbstractWebSocketClient {

    protected Channel channel;
    /**
     * 所有交易所的交易对
     */
    protected Set<String> subChannel = Sets.newHashSet();
    protected EventLoopGroup group;
    protected MonitorTask monitorTask;
    private ScheduledExecutorService scheduledExecutorService;

    /**
     * 启动订阅netty
     */
    public void start() {
        //建立连接
        this.connect();
        //这里的线程组用来查看本地webocket有没有与交易所的websocket断开
        monitorTask = new MonitorTask(this);
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(monitorTask, 0,5000, TimeUnit.MILLISECONDS);
    }

    public abstract void connect();

    /**
         * 连接WebSocket，
         *
         * @param uri url构造出URI
         * @param handler 处理消息
         */
        protected void connectWebSocket(final URI uri, SimpleChannelInboundHandler handler) {
            try {
                String scheme = uri.getScheme() == null ? "http" : uri.getScheme();
                final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
                final int port;

                if (uri.getPort() == -1) {
                    if ("http".equalsIgnoreCase(scheme) || "ws".equalsIgnoreCase(scheme)) {
                        port = 80;
                    } else if ("wss".equalsIgnoreCase(scheme)) {
                        port = 443;
                    } else {
                        port = -1;
                    }
                } else {
                    port = uri.getPort();
                }

                if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
                    System.out.println("Only WS(S) is supported");
                    throw new UnsupportedAddressTypeException();
                }
                final boolean ssl = "wss".equalsIgnoreCase(scheme);
                final SslContext sslCtx;
                if (ssl) {
                    sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
                } else {
                    sslCtx = null;
                }

                group = new NioEventLoopGroup(2);
                //构建客户端Bootstrap
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group).channel(NioSocketChannel.class).handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        if (sslCtx != null) {
                            pipeline.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                        }
                        //pipeline可以同时放入多个handler,最后一个为自定义hanler
                        pipeline.addLast(new HttpClientCodec(), new HttpObjectAggregator(8192), handler);
                    }
                });
                channel = bootstrap.connect(host, port).sync().channel();
            } catch (Exception e) {
                log.error(" webSocketClient start error.", e);
                if (group != null) {
                    group.shutdownGracefully();
                }
            }
    }

    public boolean isAlive() {
        return this.channel != null && this.channel.isActive();
    }

    public void sendMessage(String msg) {
        if (!isAlive()) {
            log.warn("webSocket is not alive addchannel error.");
            return;
        }
        log.info("send:" + msg);
        this.channel.writeAndFlush(new TextWebSocketFrame(msg));
    }

    /**
     * 断开与交易所的连接 也需要关闭线程组 不然线程组还会重新去连接
     */
    public void close() {
        monitorTask = null;
        scheduledExecutorService.shutdown();

    }

    public abstract void sendPing();

    public abstract void addChannel(String channel);

    public abstract void removeChannel(String channel);

    public abstract void reConnect();

    public abstract void onReceive(String msg);
}