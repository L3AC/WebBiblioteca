package model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class ReservasModel extends Conexion {

    // Crear una reserva
    public boolean reservarCopia(int idCopia, int idUsuario) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false); // Transacción

            // 1. Verificar disponibilidad
            String sqlCheck = "SELECT estado FROM Copias WHERE id_copia = ? AND estado = 'Disponible'";
            try (PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
                ps.setInt(1, idCopia);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) return false; // No está disponible
            }

            // 2. Insertar Reserva
            String sqlInsert = "INSERT INTO Reservas (id_usuario, id_copia, fecha_reserva) VALUES (?, ?, NOW())";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idCopia);
                ps.executeUpdate();
            }

            // 3. Actualizar estado de la copia
            String sqlUpdate = "UPDATE Copias SET estado = 'Reservado' WHERE id_copia = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setInt(1, idCopia);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, null, e);
            return false;
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true);conn.close();}catch(Exception ex){}
        }
    }

    // Aceptar reserva (Convertir en préstamo)
    public boolean aceptarReserva(int idReserva) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int idCopia = 0;
            int idUsuario = 0;

            // 1. Obtener datos de la reserva
            String sqlGet = "SELECT id_copia, id_usuario FROM Reservas WHERE id_reserva = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlGet)){
                ps.setInt(1, idReserva);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) {
                    idCopia = rs.getInt("id_copia");
                    idUsuario = rs.getInt("id_usuario");
                } else return false;
            }

            // 2. Insertar Préstamo
            String sqlInsert = "INSERT INTO Prestamos (id_usuario, id_copia, fecha_prestamo, estado) VALUES (?, ?, NOW(), 'Activo')";
            try(PreparedStatement ps = conn.prepareStatement(sqlInsert)){
                ps.setInt(1, idUsuario);
                ps.setInt(2, idCopia);
                ps.executeUpdate();
            }

            // 3. Eliminar Reserva
            String sqlDel = "DELETE FROM Reservas WHERE id_reserva = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlDel)){
                ps.setInt(1, idReserva);
                ps.executeUpdate();
            }

            // 4. Actualizar Copia a 'Prestado'
            String sqlUp = "UPDATE Copias SET estado = 'Prestado' WHERE id_copia = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlUp)){
                ps.setInt(1, idCopia);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            Logger.getLogger(ReservasModel.class.getName()).log(Level.SEVERE, null, e);
            return false;
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true);conn.close();}catch(Exception ex){}
        }
    }

    // Listar reservas (Filtra por usuario si no es admin)
    public List<JSONObject> listarReservas(int idUsuario, boolean esAdmin) {
        List<JSONObject> lista = new ArrayList<>();
        String sql = "SELECT r.id_reserva, r.fecha_reserva, c.codigo_unico, e.titulo, u.nombre, u.apellido, u.correo " +
                "FROM Reservas r " +
                "JOIN Copias c ON r.id_copia = c.id_copia " +
                "JOIN Ejemplares e ON c.id_ejemplar = e.id_ejemplar " +
                "JOIN Usuarios u ON r.id_usuario = u.id_usuario ";

        if (!esAdmin) {
            sql += "WHERE r.id_usuario = ? ";
        }
        sql += "ORDER BY r.fecha_reserva DESC";

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!esAdmin) ps.setInt(1, idUsuario);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JSONObject obj = new JSONObject();
                obj.put("id_reserva", rs.getInt("id_reserva"));
                obj.put("fecha", rs.getString("fecha_reserva"));
                obj.put("codigo", rs.getString("codigo_unico"));
                obj.put("titulo", rs.getString("titulo"));
                obj.put("usuario", rs.getString("nombre") + " " + rs.getString("apellido"));
                obj.put("correo", rs.getString("correo"));
                lista.add(obj);
            }
        } catch (Exception e) { e.printStackTrace(); }
        return lista;
    }

    // Cancelar reserva
    public boolean cancelarReserva(int idReserva) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            // Obtener id_copia
            int idCopia = 0;
            String sqlGet = "SELECT id_copia FROM Reservas WHERE id_reserva = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlGet)){
                ps.setInt(1, idReserva);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) idCopia = rs.getInt("id_copia");
                else return false;
            }

            // Borrar reserva
            String sqlDel = "DELETE FROM Reservas WHERE id_reserva = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlDel)){
                ps.setInt(1, idReserva);
                ps.executeUpdate();
            }

            // Liberar copia
            String sqlUp = "UPDATE Copias SET estado = 'Disponible' WHERE id_copia = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlUp)){
                ps.setInt(1, idCopia);
                ps.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if(conn!=null) try{conn.rollback();}catch(Exception ex){}
            return false;
        } finally {
            if(conn!=null) try{conn.setAutoCommit(true);conn.close();}catch(Exception ex){}
        }
    }
}