package com.wojtek.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class Client {

    private final NioEventLoopGroup group;
    private final Bootstrap bootstrap;

    Client(NioEventLoopGroup group) {
        this.group = group;

        bootstrap = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class);

        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast("line-dec", new LineBasedFrameDecoder(256));
                ch.pipeline().addLast("s-d", new StringDecoder());
                ch.pipeline().addLast("s-e", new StringEncoder());
                ch.pipeline().addLast("my-thing", new ChannelInboundHandlerAdapter() {

                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        super.channelRead(ctx, msg);
//                        System.out.println(msg);
                    }
                });
            }
        });
    }

    ChannelFuture connect() {
        try {
            return bootstrap.connect("localhost", 8080).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        NioEventLoopGroup g = new NioEventLoopGroup();
        ChannelFuture connect = new Client(g).connect();

        connect.addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                future.channel().writeAndFlush("1!\n");
            } else {
                System.out.println("didn't connect");
            }
        });
    }
}
