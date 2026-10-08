package cl.duoc.dsy1107.pedidos360.mensajeria;

import java.io.Serializable;
import java.math.BigDecimal;

public class PedidoSolicitud implements Serializable {

    private Long pedidoId;
    private String clienteEmail;
    private BigDecimal total;
    private boolean falloNotificaciones;
    private boolean falloCocina;
    private boolean falloDocumentos;

    public PedidoSolicitud() {
    }

    public PedidoSolicitud(Long pedidoId, String clienteEmail, BigDecimal total,
                           boolean falloNotificaciones, boolean falloCocina, boolean falloDocumentos) {
        this.pedidoId = pedidoId;
        this.clienteEmail = clienteEmail;
        this.total = total;
        this.falloNotificaciones = falloNotificaciones;
        this.falloCocina = falloCocina;
        this.falloDocumentos = falloDocumentos;
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

    @Override
    public String toString() {
        return "PedidoSolicitud{" +
                "pedidoId=" + pedidoId +
                ", clienteEmail='" + clienteEmail + '\'' +
                ", total=" + total +
                ", falloNotificaciones=" + falloNotificaciones +
                ", falloCocina=" + falloCocina +
                ", falloDocumentos=" + falloDocumentos +
                '}';
    }
}
