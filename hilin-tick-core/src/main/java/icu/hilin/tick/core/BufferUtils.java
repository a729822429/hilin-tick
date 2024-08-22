package icu.hilin.tick.core;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.buffer.Buffer;

public class BufferUtils {

    public static void printBuffer(String msg, Buffer buffer) {
        System.out.println(msg);
        printBuffer(buffer);
    }

    public static void printBuffer(Buffer buffer) {
        System.out.println(ByteBufUtil.prettyHexDump(buffer.getByteBuf()));
    }

}
