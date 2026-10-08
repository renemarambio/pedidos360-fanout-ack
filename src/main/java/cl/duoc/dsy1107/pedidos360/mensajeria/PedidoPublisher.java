package cl.duoc.dsy1107.pedidos360.mensajeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PedidoPublisher {

    private static final Logger log = LoggerFactory.getLogger(PedidoPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public PedidoPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento hacia el Direct Exchange para los 3 dominios: notificaciones, cocina, documentos.
     * Garantiza mensajes persistentes y formato JSON.
     */
    public void publicar(PedidoEvento evento) {
        List<String> dominios = List.of(
                RabbitConfig.ROUTING_KEY_NOTIFICACIONES,
                RabbitConfig.ROUTING_KEY_COCINA,
                RabbitConfig.ROUTING_KEY_DOCUMENTOS
        );

        for (String dominio : dominios) {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.DIRECT_EXCHANGE,
                    dominio,
                    evento,
                    message -> {
                        message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                        message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                        message.getMessageProperties().setHeader("x-lab-intentos", 0);
                        return message;
                    }
            );
            log.info("[PUBLISHER] Evento pedidoId={} publicado en Exchange '{}' con routingKey '{}' (persistent=true)",
                    evento.getPedidoId(), RabbitConfig.DIRECT_EXCHANGE, dominio);
        }
    }

    public void publicarPedido(PedidoEvento evento) {
        publicar(evento);
    }

    /**
     * Metodo de prueba para publicar eventos en el Topic Exchange (pedidos360.topic.exchange).
     */
    public void publicarEnTopic(String routingKey, Object payload) {
        rabbitTemplate.convertAndSend(
                RabbitConfig.TOPIC_EXCHANGE,
                routingKey,
                payload,
                message -> {
                    message.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    return message;
                }
        );
        log.info("[PUBLISHER-TOPIC] Mensaje publicado en Topic Exchange '{}' con routingKey '{}': {}",
                RabbitConfig.TOPIC_EXCHANGE, routingKey, payload);
    }

    public void publicarEnTopic(Object payload) {
        publicarEnTopic("pedidos.evento", payload);
    }
}
