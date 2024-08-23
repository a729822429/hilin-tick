package icu.hilin.tick.mod.event;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MessageConsumer<T> {

    private final Dispatcher<T> dispatcher;
    private final String tag;
    private final MessageHandler<T> handler;

    public void unregister() {
        dispatcher.remove(handler);
    }

}
