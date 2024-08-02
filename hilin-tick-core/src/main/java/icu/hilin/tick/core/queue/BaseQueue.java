package icu.hilin.tick.core.queue;

import lombok.Getter;

public abstract class BaseQueue<T> {

    /**
     * 队列长度
     */
    @Getter
    private final int length;

    public BaseQueue() {
        this.length = Integer.MAX_VALUE;
    }

    public BaseQueue(int length) {
        this.length = length;
    }

    // 向队列尾部添加数据，如果队列满了，则抛出异常
    abstract public void add(T t) throws OutOfQueueException;

    // 向队列尾部添加数据，如果队列满了，则返回false
    abstract public boolean put(T t);

    // 获取当前第一个数据并删除，阻塞
    abstract public T getBlock() throws Exception;

    // 获取当前第一个数据并删除，非阻塞
    // 如果队列没有数据，则返回Null
    abstract public T getNonBlock();

}
