package icu.hilin.tick.core;

import icu.hilin.tick.core.entity.BaseEntity;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseChannel<T> {

    private final Map<String, T> CHANNELS = new ConcurrentHashMap<>();

    public void addChannel(String channelId, T channel) {
        CHANNELS.put(channelId, channel);
    }

    public void removeChannel(String channelId) {
        CHANNELS.remove(channelId);
    }

    public void removeChannel(T channel) {
        StringBuilder removeChannelId = new StringBuilder();
        CHANNELS.forEach((channelId, mapChannel) -> {
            if (channel.equals(mapChannel)) {
                removeChannelId.append(channelId);
            }
        });
        if (!removeChannelId.isEmpty()) {
            CHANNELS.remove(removeChannelId.toString());
        }
    }

    public T get(String channelId) {
        return CHANNELS.get(channelId);
    }

    /**
     * 发送消息
     *
     * @param entity
     */
    abstract public void send(T channel, BaseEntity<?> entity);
}
