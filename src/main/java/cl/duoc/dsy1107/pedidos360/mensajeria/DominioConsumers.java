package cl.duoc.dsy1107.pedidos360.mensajeria;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DominioConsumers {

    private static final Logger log = LoggerFactory.getLogger(DominioConsumers.class);

    private final ProcesadorMensajes procesadorMensajes;

    public DominioConsumers(ProcesadorMensajes procesadorMensajes) {
        this.procesadorMensajes = procesadorMensajes;
    }

    @RabbitListener(queues = RabbitConfig.NOTIFICACIONES_QUEUE)
    public void consumirNotificaciones(PedidoEvento evento, Message message, Channel channel) throws IOException {
        log.info("[DOMINIO CONSUMER] Mensaje recibido en cola {}", RabbitConfig.NOTIFICACIONES_QUEUE);
        procesadorMensajes.procesar("notificaciones", evento, message, channel);
    }

    @RabbitListener(queues = RabbitConfig.COCINA_QUEUE)
    public void consumirCocina(PedidoEvento evento, Message message, Channel channel) throws IOException {
        log.info("[DOMINIO CONSUMER] Mensaje recibido en cola {}", RabbitConfig.COCINA_QUEUE);
        procesadorMensajes.procesar("cocina", evento, message, channel);
    }

    @RabbitListener(queues = RabbitConfig.DOCUMENTOS_QUEUE)
    public void consumirDocumentos(PedidoEvento evento, Message message, Channel channel) throws IOException {
        log.info("[DOMINIO CONSUMER] Mensaje recibido en cola {}", RabbitConfig.DOCUMENTOS_QUEUE);
        procesadorMensajes.procesar("documentos", evento, message, channel);
    }
}
