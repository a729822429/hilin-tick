package icu.hilin.tick.mod.event;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class HilinEventBusHandler<T> {

    private String tag;

    private HilinEventBus<T> eventBus;


    public abstract void handler(HilinEventBusMessage<T> message);
}
