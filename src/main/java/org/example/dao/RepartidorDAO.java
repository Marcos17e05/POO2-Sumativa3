package org.example.dao;

import org.example.modelo.Repartidor;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class RepartidorDAO {

    public void guardar(Repartidor repartidor) throws SQLException {
        String sql = "INSERT INTO repartidores (nombre) VALUES (?)";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, repartidor.getNombre());

            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No se pudo guardar el repartidor.");
            }

            try (ResultSet claves = stmt.getGeneratedKeys()) {
                if (claves.next()) {
                    repartidor.setId(claves.getInt(1));
                }
            }
        }
    }

    public List<Repartidor> listarTodos() throws SQLException {
        String sql = "SELECT id, nombre FROM repartidores ORDER BY id";
        List<Repartidor> repartidores = new ArrayList<>();

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                repartidores.add(mapear(rs));
            }
        }

        return repartidores;
    }

    public boolean existeNombre(String nombre, Integer idExcluir) throws SQLException {
        String sql = idExcluir == null ? "SELECT 1 FROM repartidores WHERE LOWER(nombre) = LOWER(?) LIMIT 1" : "SELECT 1 FROM repartidores WHERE LOWER(nombre) = LOWER(?) AND id <> ? LIMIT 1";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nombre);
            if (idExcluir != null) {
                stmt.setInt(2, idExcluir);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    public void actualizar(Repartidor repartidor) throws SQLException {
        String sql = "UPDATE repartidores SET nombre = ? WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, repartidor.getNombre());
            stmt.setInt(2, repartidor.getId());
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe el repartidor #" + repartidor.getId() + ".");
            }
        }
    }

    public void eliminar(int id) throws SQLException {
        String sql = "DELETE FROM repartidores WHERE id = ?";

        try (Connection conexion = ConexionBD.obtenerConexion(); PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, id);
            if (stmt.executeUpdate() == 0) {
                throw new SQLException("No existe el repartidor #" + id + ".");
            }
        }
    }

    private Repartidor mapear(ResultSet rs) throws SQLException {
        return new Repartidor(rs.getInt("id"), rs.getString("nombre"));
    }
}
