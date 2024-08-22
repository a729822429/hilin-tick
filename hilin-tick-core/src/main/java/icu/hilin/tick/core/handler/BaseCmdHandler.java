package icu.hilin.tick.core.handler;

import icu.hilin.tick.core.entity.BaseEntity;
import io.vertx.core.buffer.Buffer;

public abstract class BaseCmdHandler<T extends BaseEntity<?>> {

    /**
     * 是否需要处理
     */
    abstract public boolean needDeal(Long clientID, Buffer body);

    /**
     * 二进制消息转换实体
     */
    abstract public T buffer2Entity(Long clientID, Buffer body);

    /**
     * 消息处理
     */
    abstract public void doDeal(Long clientID, T entity);

}
