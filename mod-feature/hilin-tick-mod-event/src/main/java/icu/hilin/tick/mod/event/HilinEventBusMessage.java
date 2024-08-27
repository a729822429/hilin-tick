package icu.hilin.tick.mod.event;

import lombok.Getter;

public class HilinEventBusMessage<T> {
    @Getter
    private final T message;

    public HilinEventBusMessage(T message) {
        this.message = message;
    }

    @Getter
    private boolean isAck = false;

    /**
     * 消息确认，如未确认，则会不停重试当前消息
     */
    public void ack() {
        isAck = true;
    }

    public void unregisterHandler() {
    }

}
