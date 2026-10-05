package org.example.modelo;

import java.time.LocalDate;
import java.time.LocalTime;

public class Entrega {
    private int id;
    private int idPedido;
    private int idRepartidor;
    private LocalDate fecha;
    private LocalTime hora;
    private String direccionPedido;
    private String nombreRepartidor;

    public Entrega(int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        this(0, idPedido, idRepartidor, fecha, hora);
    }

    public Entrega(int id, int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        this.id = id;
        this.idPedido = idPedido;
        this.idRepartidor = idRepartidor;
        this.fecha = fecha;
        this.hora = hora;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public void setIdPedido(int idPedido) {
        this.idPedido = idPedido;
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public void setIdRepartidor(int idRepartidor) {
        this.idRepartidor = idRepartidor;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public String getDireccionPedido() {
        return direccionPedido;
    }

    public void setDireccionPedido(String direccionPedido) {
        this.direccionPedido = direccionPedido;
    }

    public String getNombreRepartidor() {
        return nombreRepartidor;
    }

    public void setNombreRepartidor(String nombreRepartidor) {
        this.nombreRepartidor = nombreRepartidor;
    }
}
