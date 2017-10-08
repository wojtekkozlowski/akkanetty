package com.wojtek.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * Hello world!
 */
public class Server {

    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap()
                .childHandler(new MyChannelInitializer())
                .localAddress(8080)
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class);
        try {
            serverBootstrap.bind().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ScheduledExecutorService s = Executors.newSingleThreadScheduledExecutor();
        s.scheduleAtFixedRate(() -> System.out.println(MyChannelInitializer.channels.size()), 0, 1, TimeUnit.SECONDS);
    }
}

class MyChannelInitializer extends ChannelInitializer<SocketChannel> {

    static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(LineBasedFrameDecoder.class.getName(), new LineBasedFrameDecoder(256));
        pipeline.addLast(StringDecoder.class.getName(), new StringDecoder(CharsetUtil.UTF_8));
        pipeline.addLast(StringEncoder.class.getName(), new StringEncoder(CharsetUtil.UTF_8));
        pipeline.addLast("stdout-handler", new SimpleChannelInboundHandler<String>() {

            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                super.channelActive(ctx);
                channels.add(ctx.channel());
            }

            protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception {
                channelHandlerContext.writeAndFlush(s + " to you too!\n");
            }
        });
    }
}
