package icu.hilin.tick.mod.event;

import lombok.Getter;

@Getter
public class HilinEventBusMessage<T> {
    private final T message;
    private boolean isAck = false;

    public HilinEventBusMessage(T message) {
        this.message = message;
    }


    /**
     * 消息确认，如未确认，则会不停重试当前消息
     */
    public void ack() {
        isAck = true;
    }

    public void unregisterHandler() {
    }

}
