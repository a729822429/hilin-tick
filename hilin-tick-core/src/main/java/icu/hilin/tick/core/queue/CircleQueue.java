package icu.hilin.tick.core.queue;

import java.util.ArrayList;
import java.util.List;

import lombok.Setter;

/**
 * 环形队列
 */
public class CircleQueue<E> extends BaseQueue<E> {

    private final Object lock = new Object();

    /**
     * 当前实例
     * 初始化时为空
     */
    private QueueContainer<E> current = null;

    /**
     * 最后一个实例的下一个
     * 初始化时，为第0个数据
     * 写入数据后，向后移动一个 即：tail = tail.next;
     */
    public QueueContainer<E> tail;

    public CircleQueue(int length) {
        super(length);
        List<QueueContainer<E>> contains = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            contains.add(new QueueContainer<>());
        }

        for (int i = 0; i < contains.size(); i++) {
            contains.get(i).i = i;
            if (i == contains.size() - 1) {
                // 最后一个
                contains.get(i).next = contains.get(0);
            } else {
                contains.get(i).next = contains.get(i + 1);
            }
        }

        tail = contains.get(0);
    }

    @Override
    public void add(E t) throws OutOfQueueException {
        synchronized (lock) {
            if (tail.data == null) {
                tail.data = t;

                if (current == null) {
                    current = tail;
                }

                tail = tail.next;
                return;
            }
            throw new OutOfQueueException();
        }
    }

    @Override
    public boolean put(E t) {
        synchronized (lock) {
            if (tail.data == null) {
                tail.data = t;

                if (current == null) {
                    current = tail;
                }

                tail = tail.next;
                return true;
            }
            return false;
        }
    }

    @Override
    public E getBlock() throws Exception {
        synchronized (lock) {
            E data;
            if (current != null && current.data != null) {
                data = current.data;
                current.data = null;
                // 整体后移
                current = current.next;
                return data;
            }
            data = current.data;
            current.data = null;
            // 整体后移
            current = current.next;
            return data;
        }
    }

    @Override
    public E getNonBlock() {
        synchronized (lock) {
            if (current == null) {
                return null;
            }

            E data = current.data;

            if (data == null) {
                return null;
            }

            current.data = null;

            // 整体后移
            current = current.next;
            return data;
        }
    }

    /**
     * 队列数据容器
     */
    public static class QueueContainer<E> {
        // 队列id
        private int i;

        /**
         * 当前实例的数据
         */
        @Setter
        private E data;

        /**
         * 下一个实例
         */
        public QueueContainer<E> next;
    }

}
