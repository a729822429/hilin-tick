package icu.hilin.tick.core;

import io.vertx.core.buffer.Buffer;

import java.util.concurrent.ConcurrentSkipListSet;

public class ChannelDataCache extends ConcurrentSkipListSet<Buffer> {

    public ChannelDataCache() {
        super((o1, o2) -> (int) (o1.getLong(0) - o2.getLong(0)));
    }

}
