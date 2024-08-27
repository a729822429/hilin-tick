package icu.hilin.tick.mod.event;

import lombok.Getter;
import lombok.Setter;

public abstract class HilinEventBusHandler<T> {

    @Getter
    @Setter
    private String tag;


    public abstract void handler(HilinEventBusMessage<T> message);
}
