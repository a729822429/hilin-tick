package icu.hilin.tick.core;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;

public class TickConstant {

    public static final String CMD_SERVER = "cmd_server_%s_%s";
    public static final String CMD_CLIENT = "cmd_client_%s_%s";

    public static final String CMD_SERVER_ALL = "cmd_server_%s_all";
    public static final String CMD_CLIENT_ALL = "cmd_client_%s_all";


    public static final String TUNNEL_SERVER = "tunnel_server_%s_%s";
    public static final String TUNNEL_CLIENT = "tunnel_client_%s_%s";

    public static final String TUNNEL_SERVER_ALL = "tunnel_server_%s_all";
    public static final String TUNNEL_CLIENT_ALL = "tunnel_client_%s_all";


    public static final String CHANNEL_SERVER = "channel_server_%s_%s";
    public static final String CHANNEL_CLIENT = "channel_client_%s_%s";

    public static final String CHANNEL_SERVER_ALL = "tunnel_server_%s_all";
    public static final String CHANNEL_CLIENT_ALL = "channel_client_%s_all";

    public static final Vertx VERTX = Vertx.vertx();
    public static final EventBus EVENT_BUS = VERTX.eventBus();

}
