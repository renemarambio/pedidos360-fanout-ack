package cl.duoc.dsy1107.pedidos360;

import cl.duoc.dsy1107.pedidos360.mensajeria.PedidoSolicitud;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Pedidos360IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testAceptarPedidoExitoso() throws Exception {
        PedidoSolicitud solicitud = new PedidoSolicitud(
                1001L,
                "cliente1@duoc.cl",
                BigDecimal.valueOf(25000),
                false,
                false,
                false
        );

        mockMvc.perform(post("/api/lab/pedidos/aceptar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.publicado").value(true))
                .andExpect(jsonPath("$.pedidoId").value(1001))
                .andExpect(jsonPath("$.falloNotificaciones").value(false))
                .andExpect(jsonPath("$.falloCocina").value(false))
                .andExpect(jsonPath("$.falloDocumentos").value(false));
    }

    @Test
    void testAceptarPedidoConFalloControlado() throws Exception {
        PedidoSolicitud solicitud = new PedidoSolicitud(
                1002L,
                "cliente2@duoc.cl",
                BigDecimal.valueOf(18500),
                false,
                true, // Fallo controlado en Cocina
                false
        );

        mockMvc.perform(post("/api/lab/pedidos/aceptar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(solicitud)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.publicado").value(true))
                .andExpect(jsonPath("$.pedidoId").value(1002))
                .andExpect(jsonPath("$.falloCocina").value(true));
    }

    @Test
    void testPublicarEventoTopic() throws Exception {
        Map<String, Object> payload = Map.of(
                "evento", "PEDIDO_PAGADO",
                "pedidoId", 1003,
                "detalle", "Pago verificado via Webpay"
        );

        mockMvc.perform(post("/api/lab/eventos/topic")
                        .param("routingKey", "pedidos.pagos.confirmado")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.publicado").value(true))
                .andExpect(jsonPath("$.routingKey").value("pedidos.pagos.confirmado"));
    }
}
