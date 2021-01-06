package com.kevin.minirpc.provider.handler;

import com.kevin.minirpc.RpcReponse;
import com.kevin.minirpc.RpcRequest;
import com.kevin.minirpc.provider.spring.SpringContextHolder;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

/**
 * 自定义的业务处理器
 */
public class ServiceHandler extends ChannelInboundHandlerAdapter {

    //当客户端读取数据时,该方法会被调用
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //注意:  客户端将来发送请求为RpcRequest对象
        //1.判断当前的请求是否符合规则
        if (msg instanceof RpcRequest) {
            //2.如果符合规则,调用实现类货到一个result
            RpcRequest rpcRequest = (RpcRequest) msg;

            // 3.根据RpcRequest寻找对应的接口和方法
            Class<?> aClass = Thread.currentThread().getContextClassLoader().loadClass(rpcRequest.getClassName());
            if (aClass == null) {
                writeAndFlushResponse(ctx, "not support class:" + rpcRequest.getClassName());
                return;
            }

            Object bean = SpringContextHolder.getBean(aClass);
            if (bean == null) {
                writeAndFlushResponse(ctx, "not support class:" + rpcRequest.getClassName());
                return;
            }
            Method declaredMethod = bean.getClass().getDeclaredMethod(rpcRequest.getMethodName(), rpcRequest.getParameterTypes());
            if (declaredMethod == null) {
                writeAndFlushResponse(ctx, "not support method:" + rpcRequest.getMethodName());
                return;
            }
            // 4.并执行它
            Object invoke = declaredMethod.invoke(bean, rpcRequest.getParameters());

            // 5.把调用实现类的方法获得的结果写到客户端
            writeAndFlushResponse(ctx, invoke);
        } else {
            System.out.println("请求了数据：" + msg.toString());
            writeAndFlushResponse(ctx, "fail");
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 忽略掉远方主机下线异常
        if ("远程主机强迫关闭了一个现有的连接。".equals(cause.getMessage())) {
            return;
        }
        super.exceptionCaught(ctx, cause);
    }

    private void writeAndFlushResponse(ChannelHandlerContext ctx, Object msg) {
        RpcReponse rpcReponse = new RpcReponse(null, msg);
        ctx.writeAndFlush(rpcReponse);
    }
}
