package com.kevin.minirpc.provider;

import com.kevin.minirpc.RpcReponse;
import com.kevin.minirpc.RpcRequest;
import com.kevin.minirpc.config.MiniConfig;
import com.kevin.minirpc.provider.handler.ServiceHandler;
import com.kevin.minirpc.serialized.JSONSerializer;
import com.kevin.minirpc.serialized.RpcDecoder;
import com.kevin.minirpc.serialized.RpcEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

public class ProviderStarter implements ApplicationListener {

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        // 容器启动完成后开始初始化
        if (event instanceof ApplicationStartedEvent) {
            //启动服务器
            try {
                startServer(MiniConfig.host, MiniConfig.port);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    //创建一个方法启动服务器
    public static void startServer(String ip, int port) throws InterruptedException {
        System.out.println("start Rpc server...." + ip + ":" + port);
        //1.创建两个线程池对象
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workGroup = new NioEventLoopGroup();

        // 1.5 注册hook，优雅关闭
        Runtime.getRuntime().addShutdownHook(new Hook(bossGroup, workGroup));

        //2.创建服务端的启动引导对象
        ServerBootstrap serverBootstrap = new ServerBootstrap();

        //3.配置启动引导对象
        serverBootstrap.group(bossGroup, workGroup)
                //设置通道为NIO
                .channel(NioServerSocketChannel.class)
                //创建监听channel
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        //获取管道对象
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        //粘包问题处理
                        //这里使用自定义分隔符
                        // ByteBuf delimiter = Unpooled.copiedBuffer("\r\n".getBytes());
                        // pipeline.addFirst(new DelimiterBasedFrameDecoder(8192, delimiter));
                        //给管道对象pipeLine 设置编码
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerializer()));
                        pipeline.addLast(new RpcEncoder(RpcReponse.class, new JSONSerializer()));
                        //把我们自顶一个ChannelHander添加到通道中
                        pipeline.addLast(new ServiceHandler());
                    }
                });

        //4.绑定端口
        ChannelFuture future = serverBootstrap.bind(ip, port).sync();
    }

    private static class Hook extends Thread {

        NioEventLoopGroup bossGroup;
        NioEventLoopGroup workGroup;

        public Hook(NioEventLoopGroup bossGroup, NioEventLoopGroup workGroup) {
            this.bossGroup = bossGroup;
            this.workGroup = workGroup;
        }

        @Override
        public void run() {
            bossGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
            super.run();
        }
    }
}
