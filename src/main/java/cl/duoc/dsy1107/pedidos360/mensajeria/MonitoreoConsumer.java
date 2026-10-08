package cl.duoc.dsy1107.pedidos360.mensajeria;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class MonitoreoConsumer {

    private static final Logger log = LoggerFactory.getLogger(MonitoreoConsumer.class);

    @RabbitListener(queues = RabbitConfig.MONITOREO_QUEUE)
    public void consumirMonitoreo(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        try {
            String routingKey = message.getMessageProperties().getReceivedRoutingKey();
            String exchange = message.getMessageProperties().getReceivedExchange();
            String body = new String(message.getBody(), StandardCharsets.UTF_8);

            log.info("[MONITOREO OBSERVABILIDAD] Exchange: '{}' | RoutingKey: '{}' | Payload: {}",
                    exchange, routingKey, body);

            // Se confirma el mensaje observado
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[MONITOREO OBSERVABILIDAD] Error procesando mensaje de monitoreo: {}", e.getMessage(), e);
            channel.basicAck(deliveryTag, false);
        }
    }
}
