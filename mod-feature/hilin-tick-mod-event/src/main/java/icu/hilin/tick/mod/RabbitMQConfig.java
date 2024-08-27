package icu.hilin.tick.mod;

import com.rabbitmq.client.*;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RabbitMQConfig {

    public static void main(String[] args) throws Exception {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("admin");
        connectionFactory.setPassword("123456");
        connectionFactory.setVirtualHost("/");


        Connection connection = connectionFactory.newConnection();
        Channel channel = connection.createChannel();
        channel.queueDeclare("test", false, false, true, new HashMap<>());

        channel.exchangeDeclare("test", BuiltinExchangeType.DIRECT);

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                System.out.println(ByteBufUtil.prettyHexDump(Unpooled.copiedBuffer(body)));
            }
        };
        channel.basicConsume("test", false, consumer);


        new ScheduledThreadPoolExecutor(1)
                .scheduleAtFixedRate(() -> {
                    try {
                        channel.basicPublish("", "test", null, new byte[]{'\r', '\n'});
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }, 1, 1, TimeUnit.SECONDS);
    }

}
