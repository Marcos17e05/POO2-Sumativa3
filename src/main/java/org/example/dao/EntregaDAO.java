package org.example.dao;

import org.example.modelo.Entrega;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EntregaDAO {

    public void guardar(Entrega entrega) throws SQLException {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            cargarParametros(stmt, entrega);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se pudo guardar la entrega.");
            }

            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    entrega.setId(claves.getInt(1));
                }
            }
        }
    }

    public List<Entrega> listar(Integer idPedido, Integer idRepartidor) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, " +
                "p.direccion AS direccion_pedido, r.nombre AS nombre_repartidor " +
                "FROM entregas e " +
                "JOIN pedidos p ON p.id = e.id_pedido " +
                "JOIN repartidores r ON r.id = e.id_repartidor WHERE 1=1");

        List<Integer> parametros = new ArrayList<>();
        if (idPedido != null) {
            sql.append(" AND e.id_pedido = ?");
            parametros.add(idPedido);
        }
        if (idRepartidor != null) {
            sql.append(" AND e.id_repartidor = ?");
            parametros.add(idRepartidor);
        }
        sql.append(" ORDER BY e.id");

        List<Entrega> entregas = new ArrayList<>();
        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                stmt.setInt(i + 1, parametros.get(i));
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    entregas.add(mapear(rs));
                }
            }
        }
        return entregas;
    }

    public void actualizar(Entrega entrega) throws SQLException {
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            cargarParametros(stmt, entrega);
            stmt.setInt(5, entrega.getId());
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe la entrega #" + entrega.getId() + ".");
            }
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM entregas WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion();
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe la entrega #" + id + ".");
            }
        }
    }

    private void cargarParametros(PreparedStatement stmt, Entrega entrega) throws SQLException {
        stmt.setInt(1, entrega.getIdPedido());
        stmt.setInt(2, entrega.getIdRepartidor());
        stmt.setDate(3, java.sql.Date.valueOf(entrega.getFecha()));
        stmt.setTime(4, java.sql.Time.valueOf(entrega.getHora()));
    }

    private Entrega mapear(ResultSet rs) throws SQLException {
        java.sql.Date fechaSql = rs.getDate("fecha");
        java.sql.Time horaSql = rs.getTime("hora");
        Entrega entrega = new Entrega(
                rs.getInt("id"),
                rs.getInt("id_pedido"),
                rs.getInt("id_repartidor"),
                fechaSql == null ? null : fechaSql.toLocalDate(),
                horaSql == null ? null : horaSql.toLocalTime()
        );
        entrega.setDireccionPedido(rs.getString("direccion_pedido"));
        entrega.setNombreRepartidor(rs.getString("nombre_repartidor"));
        return entrega;
    }
}
