package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class ReservasModel extends Conexion {

    // === RESERVAR UNA COPIA ===
    public boolean reservarCopia(int idCopia, String correoUsuario) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Obtener ID del usuario por correo
            String sqlUsuario = "SELECT id_usuario FROM Usuarios WHERE correo = ?";
            int idUsuario = -1;
            try (PreparedStatement psUsuario = conn.prepareStatement(sqlUsuario)) {
                psUsuario.setString(1, correoUsuario);
                try (ResultSet rs = psUsuario.executeQuery()) {
                    if (rs.next()) {
                        idUsuario = rs.getInt("id_usuario");
                    } else {
                        Logger.getLogger(ReservasModel.class.getName()).log(Level.WARNING, "Usuario no encontrado con correo: {0}", correoUsuario);
                        return false;
                    }
                }
            }

            // Verificar que la copia esté disponible
            String sqlCopia = "SELECT estado FROM Copias WHERE id_copia = ?";
            try (PreparedStatement psCopia = conn.prepareStatement(sqlCopia)) {
                psCopia.setInt(1, idCopia);
                try (ResultSet rs = psCopia.executeQuery()) {
                    if (!rs.next()) {
                        Logger.getLogger(ReservasModel.class.getName()).log(Level.WARNING, "Copia no encontrada con ID: {0}", idCopia);
                        return false;
                    }
                    String estado = rs.getString("estado");
                    if (!"Disponible".equals(estado)) {
                        Logger.getLogger(ReservasModel.class.getName()).log(Level.WARNING, "Copia {0} no está disponible para reserva. Estado actual: {1}", new Object[]{idCopia, estado});
                        return false;
                    }
                }
            }

            // Insertar en Reservas
            String sqlReserva = "INSERT INTO Reservas (id_usuario, id_copia, fecha_reserva) VALUES (?, ?, ?)";
            try (PreparedStatement psReserva = conn.prepareStatement(sqlReserva, Statement.RETURN_GENERATED_KEYS)) {
                psReserva.setInt(1, idUsuario);
                psReserva.setInt(2, idCopia);
                psReserva.setDate(3, new Date(System.currentTimeMillis()));
                psReserva.executeUpdate();
            }

            // Actualizar estado de la copia a "Reservado"
            String sqlActualizarCopia = "UPDATE Copias SET estado = 'Reservado' WHERE id_copia = ?";
            try (PreparedStatement psActualizar = conn.prepareStatement(sqlActualizarCopia)) {
                psActualizar.setInt(1, idCopia);
                psActualizar.executeUpdate();
            }

            conn.commit();
            Logger.getLogger(ReservasModel.class.getName()).log(Level.INFO, "Copia {0} reservada exitosamente para usuario {1}", new Object[]{idCopia, correoUsuario});
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, "Error al reservar copia", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(ReservasModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }

    // === LISTAR RESERVAS (opcional) ===
    public List<JSONObject> listarReservas() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT r.id_reserva, r.fecha_reserva, u.correo, c.codigo_unico, e.titulo
            FROM Reservas r
            JOIN Usuarios u ON r.id_usuario = u.id_usuario
            JOIN Copias c ON r.id_copia = c.id_copia
            JOIN Ejemplares e ON c.id_ejemplar = e.id_ejemplar
            ORDER BY r.id_reserva
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject reserva = new JSONObject();
                reserva.put("id_reserva", rs.getInt("id_reserva"));
                reserva.put("fecha_reserva", rs.getDate("fecha_reserva").toString());
                reserva.put("correo_usuario", rs.getString("correo"));
                reserva.put("codigo_copia", rs.getString("codigo_unico"));
                reserva.put("titulo_ejemplar", rs.getString("titulo"));

                lista.add(reserva);
            }

        } catch (SQLException e) {
            Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, "Error al listar reservas", e);
        }
        return lista;
    }

    // === CANCELAR RESERVA ===
    public boolean cancelarReserva(int idReserva) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Obtener ID de copia de la reserva
            int idCopia = -1;
            String sqlReserva = "SELECT id_copia FROM Reservas WHERE id_reserva = ?";
            try (PreparedStatement psReserva = conn.prepareStatement(sqlReserva)) {
                psReserva.setInt(1, idReserva);
                try (ResultSet rs = psReserva.executeQuery()) {
                    if (rs.next()) {
                        idCopia = rs.getInt("id_copia");
                    } else {
                        Logger.getLogger(ReservasModel.class.getName()).log(Level.WARNING, "Reserva no encontrada con ID: {0}", idReserva);
                        return false;
                    }
                }
            }

            // Eliminar la reserva
            String sqlEliminar = "DELETE FROM Reservas WHERE id_reserva = ?";
            try (PreparedStatement psEliminar = conn.prepareStatement(sqlEliminar)) {
                psEliminar.setInt(1, idReserva);
                psEliminar.executeUpdate();
            }

            // Actualizar estado de la copia a "Disponible"
            String sqlActualizarCopia = "UPDATE Copias SET estado = 'Disponible' WHERE id_copia = ?";
            try (PreparedStatement psActualizar = conn.prepareStatement(sqlActualizarCopia)) {
                psActualizar.setInt(1, idCopia);
                psActualizar.executeUpdate();
            }

            conn.commit();
            Logger.getLogger(ReservasModel.class.getName()).log(Level.INFO, "Reserva {0} cancelada exitosamente", idReserva);
            return true;

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, "Error en rollback", ex);
                }
            }
            Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, "Error al cancelar reserva", e);
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException e) {
                    Logger.getLogger(ReservasModel.class.getName()).log(Level.WARNING, "Error al cerrar conexión", e);
                }
            }
        }
    }
}