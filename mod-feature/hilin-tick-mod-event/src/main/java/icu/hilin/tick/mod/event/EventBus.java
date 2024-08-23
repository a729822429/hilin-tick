package icu.hilin.tick.mod.event;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 事件驱动
 */
public class EventBus<T> {

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(10, 20, 1, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10000));

    private final Dispatcher<T> dispatcher;

    /**
     * 自定义调度器
     */
    public EventBus(Dispatcher<T> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public EventBus() {
        this.dispatcher = new Dispatcher<>();
    }

    public MessageConsumer<T> consumer(String tag, MessageHandler<T> handler) {
        return dispatcher.addHandler(tag, handler);
    }

    public void publish(String tag, T body) {
        dispatcher.publish(tag, body);
    }

    public void publishAsync(String tag, T body) {
        EXECUTOR.execute(() -> {
            dispatcher.publish(tag, body);
        });
    }

}
