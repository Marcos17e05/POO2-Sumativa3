package org.example.dao;

import org.example.modelo.EstadoPedido;
import org.example.modelo.Pedido;
import org.example.modelo.PedidoComida;
import org.example.modelo.PedidoEncomienda;
import org.example.modelo.PedidoExpress;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    public void guardar(Pedido pedido) throws SQLException {
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, pedido.getDireccionEntrega());
            stmt.setString(2, tipoParaBD(pedido));
            stmt.setString(3, pedido.getEstado().name());

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se pudo guardar el pedido.");
            }

            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    pedido.setIdPedido(claves.getInt(1));
                }
            }
        }
    }

    public List<Pedido> listarTodos() throws SQLException {
        return listar(null, null);
    }

    public List<Pedido> listar(String tipo, EstadoPedido estado) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.direccion, p.tipo, p.estado, " +
                "(SELECT r.nombre FROM entregas e " +
                " JOIN repartidores r ON r.id = e.id_repartidor " +
                " WHERE e.id_pedido = p.id ORDER BY e.id DESC LIMIT 1) AS repartidor " +
                "FROM pedidos p WHERE 1=1");

        List<Object> parametros = new ArrayList<>();
        if (tipo != null && !tipo.isBlank()) {
            sql.append(" AND p.tipo = ?");
            parametros.add(tipo.trim().toUpperCase());
        }
        if (estado != null) {
            sql.append(" AND p.estado = ?");
            parametros.add(estado.name());
        }
        sql.append(" ORDER BY p.id");

        List<Pedido> pedidos = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setObject(i + 1, parametros.get(i));
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pedidos.add(mapearPedido(rs));
                }
            }
        }
        return pedidos;
    }

    public Pedido buscarPorId(int id) throws SQLException {
        String sql = "SELECT p.id, p.direccion, p.tipo, p.estado, " +
                "(SELECT r.nombre FROM entregas e " +
                " JOIN repartidores r ON r.id = e.id_repartidor " +
                " WHERE e.id_pedido = p.id ORDER BY e.id DESC LIMIT 1) AS repartidor " +
                "FROM pedidos p WHERE p.id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapearPedido(rs) : null;
            }
        }
    }

    public void actualizar(Pedido pedido) throws SQLException {
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, pedido.getDireccionEntrega());
            stmt.setString(2, tipoParaBD(pedido));
            stmt.setString(3, pedido.getEstado().name());
            stmt.setInt(4, pedido.getIdPedido());
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe el pedido #" + pedido.getIdPedido() + ".");
            }
        }
    }

    public void actualizarEstado(int idPedido, EstadoPedido estado) throws SQLException {
        String sql = "UPDATE pedidos SET estado = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, estado.name());
            stmt.setInt(2, idPedido);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe el pedido #" + idPedido + ".");
            }
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe el pedido #" + id + ".");
            }
        }
    }

    private Pedido mapearPedido(ResultSet rs) throws SQLException {
        Pedido pedido = crearPedido(rs.getInt("id"), rs.getString("direccion"), rs.getString("tipo"));
        String estadoTexto = rs.getString("estado");
        try {
            pedido.setEstado(EstadoPedido.valueOf(estadoTexto.toUpperCase()));
        } catch (IllegalArgumentException | NullPointerException ex) {
            pedido.setEstado(EstadoPedido.PENDIENTE);
        }

        String repartidor = rs.getString("repartidor");
        if (repartidor != null && !repartidor.isBlank()) {
            pedido.asignarRepartidor(repartidor);
        }
        return pedido;
    }

    private Pedido crearPedido(int id, String direccion, String tipo) throws SQLException {
        if (tipo == null) {
            throw new SQLException("El pedido #" + id + " no tiene un tipo válido.");
        }
        return switch (tipo.trim().toUpperCase()) {
            case "COMIDA" -> new PedidoComida(id, direccion);
            case "ENCOMIENDA" -> new PedidoEncomienda(id, direccion);
            case "EXPRESS" -> new PedidoExpress(id, direccion);
            default -> throw new SQLException("Tipo de pedido desconocido en la BD: " + tipo);
        };
    }

    private String tipoParaBD(Pedido pedido) {
        return pedido.getTipoPedido().trim().toUpperCase();
    }
}
