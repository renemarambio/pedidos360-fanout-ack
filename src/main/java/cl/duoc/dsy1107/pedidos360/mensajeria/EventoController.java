package cl.duoc.dsy1107.pedidos360.mensajeria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class EventoController {

    private static final Logger log = LoggerFactory.getLogger(EventoController.class);

    private final PedidoPublisher pedidoPublisher;

    public EventoController(PedidoPublisher pedidoPublisher) {
        this.pedidoPublisher = pedidoPublisher;
    }

    /**
     * Endpoint para aceptar pedidos y distribuirlos hacia los 3 dominios (notificaciones, cocina, documentos).
     * Permite activar fallos controlados para probar retries con TTL y dead-lettering hacia DLQ.
     */
    @PostMapping("/lab/pedidos/aceptar")
    public ResponseEntity<Map<String, Object>> aceptarPedido(@RequestBody PedidoSolicitud solicitud) {
        log.info("[CONTROLLER] Solicitud de pedido recibida: {}", solicitud);

        Long pedidoId = solicitud.getPedidoId() != null
                ? solicitud.getPedidoId()
                : (long) (Math.random() * 90000 + 10000);
        String clienteEmail = solicitud.getClienteEmail() != null && !solicitud.getClienteEmail().isBlank()
                ? solicitud.getClienteEmail()
                : "cliente" + pedidoId + "@pedidos360.cl";
        BigDecimal total = solicitud.getTotal() != null
                ? solicitud.getTotal()
                : BigDecimal.valueOf(15990);

        Map<String, Object> metadatos = new HashMap<>();
        metadatos.put("timestamp", LocalDateTime.now().toString());
        metadatos.put("origen", "API_REST");
        metadatos.put("version", "1.0");

        PedidoEvento evento = new PedidoEvento(
                UUID.randomUUID().toString(),
                pedidoId,
                clienteEmail,
                total,
                solicitud.isFalloNotificaciones(),
                solicitud.isFalloCocina(),
                solicitud.isFalloDocumentos(),
                metadatos
        );

        // Publica a los 3 dominios con modo persistente
        pedidoPublisher.publicar(evento);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("publicado", true);
        respuesta.put("mensaje", "Pedido aceptado y distribuido a colas de dominios");
        respuesta.put("pedidoId", evento.getPedidoId());
        respuesta.put("eventoId", evento.getEventoId());
        respuesta.put("clienteEmail", evento.getClienteEmail());
        respuesta.put("total", evento.getTotal());
        respuesta.put("falloNotificaciones", evento.isFalloNotificaciones());
        respuesta.put("falloCocina", evento.isFalloCocina());
        respuesta.put("falloDocumentos", evento.isFalloDocumentos());
        respuesta.put("metadatos", evento.getMetadatos());

        return ResponseEntity.accepted().body(respuesta);
    }

    /**
     * Endpoint de prueba para publicar en Topic Exchange (pedidos360.topic.exchange)
     * y observar el comportamiento en el consumidor de monitoreo.
     */
    @PostMapping("/lab/eventos/topic")
    public ResponseEntity<Map<String, Object>> publicarEventoTopic(
            @RequestParam(required = false) String routingKey,
            @RequestBody(required = false) Map<String, Object> payload) {

        Map<String, Object> datos = (payload != null) ? new HashMap<>(payload) : new HashMap<>();

        String rk = routingKey;
        if (rk == null || rk.isBlank()) {
            if (datos.containsKey("routingKey") && datos.get("routingKey") != null) {
                rk = datos.get("routingKey").toString();
            } else {
                rk = "pedidos.evento.nuevo";
            }
        }

        if (!datos.containsKey("timestamp")) {
            datos.put("timestamp", LocalDateTime.now().toString());
        }

        pedidoPublisher.publicarEnTopic(rk, datos);

        Map<String, Object> respuesta = new HashMap<>();
        respuesta.put("publicado", true);
        respuesta.put("mensaje", "Evento enviado al Topic Exchange de monitoreo");
        respuesta.put("exchange", RabbitConfig.TOPIC_EXCHANGE);
        respuesta.put("routingKey", rk);
        respuesta.put("payload", datos);

        return ResponseEntity.ok(respuesta);
    }

    /**
     * Endpoint de compatibilidad retroactiva con pruebas de la Semana 08.
     */
    @PostMapping("/eventos/pedido-aceptado")
    public ResponseEntity<Map<String, Object>> pedidoAceptadoLegacy(@RequestBody PedidoSolicitud solicitud) {
        return aceptarPedido(solicitud);
    }
}
