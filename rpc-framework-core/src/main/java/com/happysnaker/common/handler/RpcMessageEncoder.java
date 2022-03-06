package com.happysnaker.common.handler;


import com.happysnaker.common.config.RpcConfig;
import com.happysnaker.common.utils.KryoUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public class RpcMessageEncoder extends MessageToByteEncoder {


    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        byte[] bytes = KryoUtil.writeToByteArray(msg);
        int len = bytes.length;
        // 幻数
        out.writeShort(RpcConfig.PHANTOM_NUMBER);
        // 消息体长度
        out.writeInt(len);
        // 序列化后的消息
        out.writeBytes(bytes);
    }
}
