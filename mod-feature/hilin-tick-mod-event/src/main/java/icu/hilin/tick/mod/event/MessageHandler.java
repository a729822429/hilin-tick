package icu.hilin.tick.mod.event;

public interface MessageHandler<T> {

    void handle(EventMessage<T> message);

}
