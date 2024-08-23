package icu.hilin.tick.mod.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EventMessage<T> {

    private T message;
    private Dispatcher<T> dispatcher;
    private MessageHandler<T> handler;

}
