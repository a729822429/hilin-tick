package icu.hilin.tick.mod.event;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件调度器
 */
public class Dispatcher<T> {

    private final Map<String, Set<MessageHandler<T>>> HANDLERS = new ConcurrentHashMap<>();

    public MessageConsumer<T> addHandler(String tag, MessageHandler<T> handler) {
        HANDLERS.put(tag, new HashSet<>());
        HANDLERS.get(tag).add(handler);
        return new MessageConsumer<>(this, tag, handler);
    }

    public void publish(String tag, T body) {
        Dispatcher<T> tThis = this;
        HANDLERS.get(tag).forEach(handler -> {
            try {
                handler.handle(new EventMessage<>(body, tThis, handler));
            } catch (Exception ignored) {
            }
        });
    }

    public void remove(MessageHandler<T> handler) {
        HANDLERS.forEach((tag, handlers) -> handlers.remove(handler));
    }

    public void remove(String tag, MessageHandler<T> handler) {
        try {
            HANDLERS.get(tag).remove(handler);
        } catch (Exception ignored) {
        }
    }

}
