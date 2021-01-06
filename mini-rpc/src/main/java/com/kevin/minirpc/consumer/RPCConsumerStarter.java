package com.kevin.minirpc.consumer;

import com.kevin.minirpc.RpcReponse;
import com.kevin.minirpc.RpcRequest;
import com.kevin.minirpc.config.MiniConfig;
import com.kevin.minirpc.consumer.handler.MiniRPCHandler;
import com.kevin.minirpc.serialized.JSONSerializer;
import com.kevin.minirpc.serialized.RpcDecoder;
import com.kevin.minirpc.serialized.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * 消费者netty启动器
 */
public class RPCConsumerStarter {

    //2.声明一个自定义事件处理器  UserClientHandler
    private static MiniRPCHandler miniRPCHandler;

    //3.编写方法,初始化客户端  ( 创建连接池  bootStrap  设置bootstrap  连接服务器)
    private static void initClient() throws InterruptedException {
        //1) 初始化UserClientHandler
        miniRPCHandler = new MiniRPCHandler();
        //2)创建连接池对象
        EventLoopGroup group = new NioEventLoopGroup();
        // 2.5 注册hook，优雅关闭
        Runtime.getRuntime().addShutdownHook(new Hook(group));
        //3)创建客户端的引导对象
        Bootstrap bootstrap = new Bootstrap();
        //4)配置启动引导对象
        bootstrap.group(group)
                //设置通道为NIO
                .channel(NioSocketChannel.class)
                //设置请求协议为TCP
                .option(ChannelOption.TCP_NODELAY, true)
                //监听channel 并初始化
                .handler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        //获取ChannelPipeline
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        //粘包问题处理
                        //这里使用自定义分隔符
                        // ByteBuf delimiter = Unpooled.copiedBuffer("\r\n".getBytes());
                        // pipeline.addFirst(new DelimiterBasedFrameDecoder(8192, delimiter));
                        //设置编码
                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(new RpcDecoder(RpcReponse.class, new JSONSerializer()));
                        //添加自定义事件处理器
                        pipeline.addLast(miniRPCHandler);
                    }
                });

        final String host = MiniConfig.host;
        final int port = MiniConfig.port;
        System.out.println("start rpc consumer...." + host + ":" + port);
        //5)连接服务端
        ChannelFuture future = bootstrap.connect(host, port).sync();

    }

    /**
     * 对外暴露服务，提供netty服务通信监听
     *
     * @return
     */
    public static synchronized MiniRPCHandler startListen() {
        //1)初始化客户端cliet
        if (miniRPCHandler == null) {
            try {
                initClient();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return miniRPCHandler;
    }

    // 钩子程序，优雅关闭netty
    private static class Hook extends Thread {
        private EventLoopGroup group;

        private Hook(EventLoopGroup group) {
            this.group = group;
        }

        @Override
        public void run() {
            group.shutdownGracefully();
            super.run();
        }
    }
}
