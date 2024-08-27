package icu.hilin.tick.core.eventbus;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class HilinEventBus<T> {

    /**
     * 使用虚拟线程
     * 虚拟线程通常无需关注数量，因为一个虚拟线程占用内存只有1k左右
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    /**
     * 本事件总线的全局锁
     */
    private final Object allLock = new Object();

    /**
     * 所有消息处理器
     */
    private final Map<String, Set<HilinEventBusHandler<T>>> handlers = new ConcurrentHashMap<>();

    /**
     * 缓存未命中的消息
     */
    private final Map<String, Set<T>> valueCache = new ConcurrentHashMap<>();

    /**
     * 每个tag的锁
     */
    private final Map<String, Object> tagLocks = new ConcurrentHashMap<>();

    /**
     * 是否自动确认
     */
    public final boolean autoAck;

    public HilinEventBus() {
        this(false);
    }

    public HilinEventBus(boolean autoAck) {
        this.autoAck = autoAck;
    }

    public void consumer(String tag, HilinEventBusHandler<T> handler) {
        synchronized (allLock) {
            handler.setTag(tag);
            if (ObjectUtil.isEmpty(handlers.get(tag))) {
                handlers.put(tag, new HashSet<>());
            }
            handlers.get(tag).add(handler);
            if (ObjectUtil.isEmpty(valueCache.get(tag))) {
                valueCache.put(tag, new HashSet<>());
            }
            if (ObjectUtil.isEmpty(tagLocks.get(tag))) {
                tagLocks.put(tag, new Object());
            }
        }
    }

    /**
     * 不保证顺序，先到先发
     */
    public void publish(String tag, T message) {
        publishOrdered(tag, message, IdUtil.getSnowflakeNextId(), 0);
    }

    /**
     * 保证顺序，按照顺序发送
     *
     * @param seq    序号，从小到大发送
     * @param waitMi 缺少序号时等待的毫秒数，如果为0，那么不关心是否缺少序号
     */
    public void publishOrdered(String tag, T message, long seq, long waitMi) {
        if (message != null) {
            valueCache.get(tag).add(message);
        }

        if (ObjectUtil.isNotEmpty(handlers.get(tag))) {
            Iterator<HilinEventBusHandler<T>> iterator = handlers.get(tag).iterator();
            while (iterator.hasNext()) {
                HilinEventBusHandler<T> handler = iterator.next();
                HilinEventBusMessage<T> hilinEventBusMessage = new HilinEventBusMessage<>(message);
                do {
                    handler.handler(hilinEventBusMessage);
                } while (!autoAck && !hilinEventBusMessage.isAck());
                valueCache.get(tag).remove(message);
            }
        }
    }

    public void unregister(HilinEventBusHandler<T> handler) {
        String tag = handler.getTag();
        synchronized (allLock) {
            if (ObjectUtil.isNotEmpty(handlers.get(tag))) {
                handlers.get(tag).remove(handler);
            }
            if (ObjectUtil.isEmpty(handlers.get(tag))) {
                // 如果删除之后没有处理器，那么清空所有关联数据
                handlers.remove(tag);
                tagLocks.remove(tag);
                valueCache.remove(tag);
            }

        }
    }

    public static void main(String[] args) {
        HilinEventBus<String> eventBus = new HilinEventBus<>(true);

        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(() -> {
            eventBus.publish("123", RandomUtil.randomString(20));
        }, 1, 1, TimeUnit.SECONDS);

        new Thread(() -> {

            HilinEventBusHandler<String> handler = new HilinEventBusHandler<>() {
                @Override
                public void handler(HilinEventBusMessage<String> message) {
                    System.out.println(message.getMessage());
                }
            };

            eventBus.consumer("123", handler);

            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                eventBus.unregister(handler);
            }).start();
        }).start();


    }


}
