package com.kevin.minirpc.serialized;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author kevin.lee
 * @date 2021/1/3 0003
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private int package_total_length = -1;

    // 用来临时保留没有处理过的请求报文
    private ByteBuf tempMsg = null;

    private Class<?> clazz;

    private Serializer serializer;

    public RpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
        // 0.可读字节数
        int totalReadableBytes = in.readableBytes();
        System.out.println(Thread.currentThread() + "收到了一次数据包，长度是：" + totalReadableBytes);

        // 1.第一次读取，初始化总包大小、待缓存buffer
        if (package_total_length == -1) {
            //创建有效字节长度数组
            package_total_length = in.readInt();
            tempMsg = Unpooled.buffer(package_total_length);
        }

        // 2.写入缓存中
        tempMsg.writeBytes(in);
        // 3.还未整包读取完整
        if (tempMsg.readableBytes() != package_total_length) {
            System.out.println("发生了半包问题");
            return;
        }

        // 4.读取buffer中数据保存在字节数组
        byte[] bytes = new byte[package_total_length];
        tempMsg.readBytes(bytes);
        if (bytes != null && bytes.length > 0) {
            Object deserialize = serializer.deserialize(clazz, bytes);
            list.add(deserialize);
        } else {
            System.out.println("解码错误！");
        }
        // 5.整包读取完整，资源释放
        tempMsg.release();
        package_total_length = -1;
    }
}
