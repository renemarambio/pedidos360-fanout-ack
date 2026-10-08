package cl.duoc.dsy1107.pedidos360.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class ProcesadorMensajes {

    private static final Logger log = LoggerFactory.getLogger(ProcesadorMensajes.class);

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    public ProcesadorMensajes(RabbitTemplate rabbitTemplate, ObjectMapper objectMapper) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Procesa los eventos de cada dominio aplicando ACK manual, reintentos controlados con TTL y DLQ.
     *
     * - En exito: ejecuta channel.basicAck(deliveryTag, false).
     * - En fallo controlado (si intentos < 2 usando header x-lab-intentos):
     *     Incrementa el contador, republica el mensaje a <dominio>.retry y confirma (basicAck) el mensaje original.
     * - Al agotar reintentos (intentos >= 2):
     *     Ejecuta channel.basicNack(deliveryTag, false, false) para que el broker dead-letteree a la DLQ.
     */
    public void procesar(String dominio, PedidoEvento evento, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        // Deserializar fallback si no vino mapeado directamente
        if (evento == null && message.getBody() != null) {
            try {
                evento = objectMapper.readValue(message.getBody(), PedidoEvento.class);
            } catch (Exception e) {
                log.error("[{}] Error al deserializar PedidoEvento desde payload: {}", dominio.toUpperCase(), e.getMessage());
            }
        }

        int intentos = obtenerIntentos(message);
        boolean falloControlado = debeFallar(dominio, evento);

        try {
            if (falloControlado) {
                if (intentos < 2) {
                    int nuevosIntentos = intentos + 1;
                    log.warn("[{}] Fallo controlado detectado para pedidoId={} (intento actual={}, nuevo={}). Reenviando a {}.retry",
                            dominio.toUpperCase(), evento != null ? evento.getPedidoId() : "N/A", intentos, nuevosIntentos, dominio);

                    // Republica a la cola de reintento con header actualizado
                    republicarRetry(dominio, evento, nuevosIntentos);

                    // Confirma el mensaje original para retirarlo de la cola principal actual
                    channel.basicAck(deliveryTag, false);
                    log.info("[{}] basicAck confirmado para mensaje original en reintento (deliveryTag={})",
                            dominio.toUpperCase(), deliveryTag);
                } else {
                    log.error("[{}] Agotados reintentos ({}/2) para pedidoId={}. Enviando basicNack(requeue=false) -> pedidos360.dlx -> DLQ",
                            dominio.toUpperCase(), intentos, evento != null ? evento.getPedidoId() : "N/A");

                    // Nack sin requeue para que el broker active el DLX de la cola
                    channel.basicNack(deliveryTag, false, false);
                }
            } else {
                log.info("[{}] Mensaje procesado con exito para pedidoId={}. Enviando basicAck (deliveryTag={})",
                        dominio.toUpperCase(), evento != null ? evento.getPedidoId() : "N/A", deliveryTag);
                channel.basicAck(deliveryTag, false);
            }
        } catch (IOException e) {
            log.error("[{}] Error de comunicación AMQP con el broker: {}", dominio.toUpperCase(), e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("[{}] Excepcion inesperada en procesador: {}", dominio.toUpperCase(), e.getMessage(), e);
            channel.basicNack(deliveryTag, false, false);
        }
    }

    /**
     * Republica el evento hacia la cola de reintento del dominio con contador incrementado.
     */
    public void republicarRetry(String dominio, PedidoEvento evento, int nuevosIntentos) {
        String routingKeyRetry = dominio + ".retry";
        rabbitTemplate.convertAndSend(
                RabbitConfig.DIRECT_EXCHANGE,
                routingKeyRetry,
                evento,
                m -> {
                    m.getMessageProperties().setContentType(MessageProperties.CONTENT_TYPE_JSON);
                    m.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                    m.getMessageProperties().setHeader("x-lab-intentos", nuevosIntentos);
                    return m;
                }
        );
        log.info("[{}] Mensaje republicado en '{}' con routingKey '{}' (x-lab-intentos={})",
                dominio.toUpperCase(), RabbitConfig.DIRECT_EXCHANGE, routingKeyRetry, nuevosIntentos);
    }

    private int obtenerIntentos(Message message) {
        if (message == null || message.getMessageProperties() == null) {
            return 0;
        }
        Object header = message.getMessageProperties().getHeader("x-lab-intentos");
        if (header instanceof Number n) {
            return n.intValue();
        } else if (header != null) {
            try {
                return Integer.parseInt(header.toString());
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }

    private boolean debeFallar(String dominio, PedidoEvento evento) {
        if (evento == null) {
            return false;
        }
        return switch (dominio.toLowerCase()) {
            case "notificaciones" -> evento.isFalloNotificaciones();
            case "cocina" -> evento.isFalloCocina();
            case "documentos" -> evento.isFalloDocumentos();
            default -> false;
        };
    }
}
