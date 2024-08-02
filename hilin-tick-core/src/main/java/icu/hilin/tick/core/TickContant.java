package icu.hilin.tick.core;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class TickContant {

    public static final Vertx VERTX = Vertx.vertx();
    public static final EventBus EVENT_BUS = VERTX.eventBus();

}
