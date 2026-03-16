package model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class Avaliacao implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum TipoParte {
        CLIENTE,
        ENTREGADOR,
        RESTAURANTE
    }

    private final String id;
    private final String orderId;
    private final TipoParte avaliadorTipo;
    private final String avaliadorId;
    private final TipoParte alvoTipo;
    private final String alvoId;
    private final int nota;
    private final String comentario;
    private final LocalDateTime dataHora;

    public Avaliacao(String orderId,
            TipoParte avaliadorTipo,
            String avaliadorId,
            TipoParte alvoTipo,
            String alvoId,
            int nota,
            String comentario) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.avaliadorTipo = avaliadorTipo;
        this.avaliadorId = avaliadorId;
        this.alvoTipo = alvoTipo;
        this.alvoId = alvoId;
        this.nota = nota;
        this.comentario = comentario == null ? "" : comentario.trim();
        this.dataHora = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public TipoParte getAvaliadorTipo() {
        return avaliadorTipo;
    }

    public String getAvaliadorId() {
        return avaliadorId;
    }

    public TipoParte getAlvoTipo() {
        return alvoTipo;
    }

    public String getAlvoId() {
        return alvoId;
    }

    public int getNota() {
        return nota;
    }

    public String getComentario() {
        return comentario;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }
}
