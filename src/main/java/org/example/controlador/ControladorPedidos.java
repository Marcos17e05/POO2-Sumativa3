package org.example.controlador;

import org.example.dao.EntregaDAO;
import org.example.dao.PedidoDAO;
import org.example.dao.RepartidorDAO;
import org.example.modelo.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ControladorPedidos {
    private final PedidoDAO pedidoDAO;
    private final RepartidorDAO repartidorDAO;
    private final EntregaDAO entregaDAO;

    public ControladorPedidos() {
        this.pedidoDAO = new PedidoDAO();
        this.repartidorDAO = new RepartidorDAO();
        this.entregaDAO = new EntregaDAO();
    }

    // Metodos Pedido

    public synchronized Pedido registrarPedido(String direccion, String tipo, EstadoPedido estado) {
        validarDireccion(direccion);
        Pedido pedido = crearPedido(0, direccion.trim(), tipo);
        pedido.setEstado(estado == null ? EstadoPedido.PENDIENTE : estado);

        try {
            pedidoDAO.guardar(pedido);
            return pedido;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void actualizarPedido(int id, String direccion, String tipo, EstadoPedido estado) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un pedido de la tabla.");
        }
        validarDireccion(direccion);
        Pedido pedido = crearPedido(id, direccion.trim(), tipo);
        pedido.setEstado(estado == null ? EstadoPedido.PENDIENTE : estado);

        try {
            pedidoDAO.actualizar(pedido);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void eliminarPedido(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un pedido de la tabla.");
        }
        try {
            pedidoDAO.eliminar(id);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized List<Pedido> getPedidos() {
        try {
            return pedidoDAO.listarTodos();
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized List<Pedido> getPedidos(String tipo, EstadoPedido estado) {
        try {
            return pedidoDAO.listar(tipo, estado);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized Pedido buscarPorId(int id) {
        try {
            return pedidoDAO.buscarPorId(id);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    // Metodos Repartidores

    public synchronized Repartidor registrarRepartidor(String nombre) {
        String nombreLimpio = validarNombre(nombre);
        try {
            if (repartidorDAO.existeNombre(nombreLimpio, null)) {
                throw new IllegalArgumentException("Ya existe un repartidor con ese nombre.");
            }
            Repartidor repartidor = new Repartidor(nombreLimpio);
            repartidorDAO.guardar(repartidor);
            return repartidor;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void actualizarRepartidor(int id, String nombre) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un repartidor de la tabla.");
        }
        String nombreLimpio = validarNombre(nombre);
        try {
            if (repartidorDAO.existeNombre(nombreLimpio, id)) {
                throw new IllegalArgumentException("Ya existe otro repartidor con ese nombre.");
            }
            repartidorDAO.actualizar(new Repartidor(id, nombreLimpio));
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void eliminarRepartidor(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un repartidor de la tabla.");
        }
        try {
            repartidorDAO.eliminar(id);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized List<Repartidor> getRepartidores() {
        try {
            return repartidorDAO.listarTodos();
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    // Metodos Entrega

    public synchronized Entrega registrarEntrega(int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        validarEntrega(idPedido, idRepartidor, fecha, hora);
        Entrega entrega = new Entrega(idPedido, idRepartidor, fecha, hora.withNano(0));
        try {
            entregaDAO.guardar(entrega);
            return entrega;
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void actualizarEntrega(int id, int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una entrega de la tabla.");
        }
        validarEntrega(idPedido, idRepartidor, fecha, hora);
        try {
            entregaDAO.actualizar(new Entrega(id, idPedido, idRepartidor, fecha, hora.withNano(0)));
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void eliminarEntrega(int id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Debe seleccionar una entrega de la tabla.");
        }
        try {
            entregaDAO.eliminar(id);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized List<Entrega> getEntregas() {
        return getEntregas(null, null);
    }

    public synchronized List<Entrega> getEntregas(Integer idPedido, Integer idRepartidor) {
        try {
            return entregaDAO.listar(idPedido, idRepartidor);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }
    }

    public synchronized void iniciarSimulacionEntrega(int idPedido, Repartidor repartidor) {
        if (repartidor == null) {
            throw new IllegalArgumentException("Debe seleccionar un repartidor.");
        }

        Pedido pedido = buscarPorId(idPedido);
        if (pedido == null) {
            throw new IllegalArgumentException("No se encontró el pedido seleccionado.");
        }
        if (pedido.getEstado() != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("El pedido debe estar PENDIENTE para iniciar la simulación.");
        }

        try {
            pedidoDAO.actualizarEstado(idPedido, EstadoPedido.EN_REPARTO);
        } catch (SQLException ex) {
            throw new IllegalStateException(mensajeBD(ex), ex);
        }

        pedido.setEstado(EstadoPedido.EN_REPARTO);
        pedido.asignarRepartidor(repartidor.getNombre());

        Repartidor repartidorSimulacion = new Repartidor(
                repartidor.getNombre(),
                pedido,
                () -> persistirEstadoFinal(pedido));

        Thread hilo = new Thread(repartidorSimulacion, "Entrega-Pedido-" + idPedido);
        hilo.start();
    }

    private void persistirEstadoFinal(Pedido pedido) {
        try {
            pedidoDAO.actualizarEstado(pedido.getIdPedido(), pedido.getEstado());
            System.out.println("Estado final persistido para el pedido #"
                    + pedido.getIdPedido() + ": " + pedido.getEstado());
        } catch (SQLException ex) {
            System.err.println("No se pudo actualizar el estado final del pedido #"
                    + pedido.getIdPedido() + ": " + ex.getMessage());
        }
    }

    private Pedido crearPedido(int id, String direccion, String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new IllegalArgumentException("Debe seleccionar un tipo de pedido.");
        }
        return switch (tipo.trim().toUpperCase()) {
            case "COMIDA" -> new PedidoComida(id, direccion);
            case "ENCOMIENDA" -> new PedidoEncomienda(id, direccion);
            case "EXPRESS" -> new PedidoExpress(id, direccion);
            default -> throw new IllegalArgumentException("Tipo de pedido no válido.");
        };
    }

    private void validarDireccion(String direccion) {
        if (direccion == null || direccion.trim().isEmpty()) {
            throw new IllegalArgumentException("La dirección es obilgatoria.");
        }
        if (direccion.trim().length() > 100) {
            throw new IllegalArgumentException("La dirección no puede superar los 100 caracteres.");
        }
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del repartidor es obligatorio.");
        }
        if (nombre.trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar los 100 caracteres.");
        }
        return nombre.trim();
    }

    private void validarEntrega(int idPedido, int idRepartidor, LocalDate fecha, LocalTime hora) {
        if (idPedido <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un pedido.");
        }
        if (idRepartidor <= 0) {
            throw new IllegalArgumentException("Debe seleccionar un repartidor.");
        }
        if (fecha == null || hora == null) {
            throw new IllegalArgumentException("La fecha y la hora son obligatorias.");
        }
    }

    private String mensajeBD(SQLException ex) {
        SQLException sqlEx = ex;
        while (sqlEx.getCause() instanceof SQLException) {
            sqlEx = (SQLException) sqlEx.getCause();
        }
        String detalle = sqlEx.getMessage() == null ? ex.getMessage() : sqlEx.getMessage();
        String extra = "";
        if (sqlEx.getSQLState() != null && sqlEx.getSQLState().startsWith("23")) {
            extra = "\nLa operación viola una relación entre tablas. Si intenta borrar un pedido o repartidor, elimine primero sus entregas asociadas.";
        }
        return "No fue posible completar la operación en la base de datos.\n" +
                "Verifique MySQL, la base speedfast_db y las credenciales de ConexionBD." + extra +
                "\nDetalle: " + detalle;
    }
}
