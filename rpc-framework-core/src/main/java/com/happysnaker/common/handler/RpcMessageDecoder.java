package com.happysnaker.common.handler;

import com.happysnaker.common.config.RpcConfig;
import com.happysnaker.common.utils.KryoUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public class RpcMessageDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 幻数 2 字节，长度 4 字节，至少 6 字节
        if (in.readableBytes() > 6) {
            short p = in.readShort();
            // 如果幻数相等则进一步读取，如果不等说明这是垃圾信息，直接丢弃
            if (p == RpcConfig.PHANTOM_NUMBER) {
                int len = in.readInt();
                byte[] bytes = new byte[len];
                // 读取 len 个字节，避免粘包问题
                for (int i = 0; i < len; i++) {
                    bytes[i] = in.readByte();
                }
                Object o = KryoUtil.readFromByteArray(bytes);
                out.add(o);
            }
        }
    }
}
