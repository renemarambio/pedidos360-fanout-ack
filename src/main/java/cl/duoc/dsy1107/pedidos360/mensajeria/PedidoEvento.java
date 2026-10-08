package cl.duoc.dsy1107.pedidos360.mensajeria;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PedidoEvento implements Serializable {

    private String eventoId;
    private Long pedidoId;
    private String clienteEmail;
    private BigDecimal total;
    private boolean falloNotificaciones;
    private boolean falloCocina;
    private boolean falloDocumentos;
    private Map<String, Object> metadatos;

    public PedidoEvento() {
        this.eventoId = UUID.randomUUID().toString();
        this.metadatos = new HashMap<>();
    }

    public PedidoEvento(Long pedidoId, String clienteEmail, BigDecimal total,
                        boolean falloNotificaciones, boolean falloCocina, boolean falloDocumentos) {
        this.eventoId = UUID.randomUUID().toString();
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.total = total;
        this.falloNotificaciones = falloNotificaciones;
        this.falloCocina = falloCocina;
        this.falloDocumentos = falloDocumentos;
        this.metadatos = new HashMap<>();
        this.metadatos.put("timestamp", LocalDateTime.now().toString());
        this.metadatos.put("version", "1.0");
    }

    public PedidoEvento(String eventoId, Long pedidoId, String clienteEmail, BigDecimal total,
                        boolean falloNotificaciones, boolean falloCocina, boolean falloDocumentos,
                        Map<String, Object> metadatos) {
        this.eventoId = eventoId != null ? eventoId : UUID.randomUUID().toString();
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.total = total;
        this.falloNotificaciones = falloNotificaciones;
        this.falloCocina = falloCocina;
        this.falloDocumentos = falloDocumentos;
        this.metadatos = metadatos != null ? metadatos : new HashMap<>();
    }

    public String getEventoId() {
        return eventoId;
    }

    public void setEventoId(String eventoId) {
        this.eventoId = eventoId;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public boolean isFalloNotificaciones() {
        return falloNotificaciones;
    }

    public void setFalloNotificaciones(boolean falloNotificaciones) {
        this.falloNotificaciones = falloNotificaciones;
    }

    public boolean isFalloCocina() {
        return falloCocina;
    }

    public void setFalloCocina(boolean falloCocina) {
        this.falloCocina = falloCocina;
    }

    public boolean isFalloDocumentos() {
        return falloDocumentos;
    }

    public void setFalloDocumentos(boolean falloDocumentos) {
        this.falloDocumentos = falloDocumentos;
    }

    public Map<String, Object> getMetadatos() {
        return metadatos;
    }

    public void setMetadatos(Map<String, Object> metadatos) {
        this.metadatos = metadatos;
    }

    @Override
    public String toString() {
        return "PedidoEvento{" +
                "eventoId='" + eventoId + '\'' +
                ", pedidoId=" + pedidoId +
                ", clienteEmail='" + clienteEmail + '\'' +
                ", total=" + total +
                ", falloNotificaciones=" + falloNotificaciones +
                ", falloCocina=" + falloCocina +
                ", falloDocumentos=" + falloDocumentos +
                ", metadatos=" + metadatos +
                '}';
    }
}
