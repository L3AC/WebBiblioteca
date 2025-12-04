package model;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class PrestamosModel {
    public List<JSONObject> listarPrestamos(int idUsuario, boolean esAdmin) {
        List<JSONObject> lista = new ArrayList<>();
        String sql = "SELECT p.id_prestamo, p.fecha_prestamo, p.fecha_devolucion, p.estado, " +
                "u.nombre, u.apellido, c.codigo_unico, e.titulo, r.mora_diaria, r.dias_prestamo, " +
                "DATEDIFF(NOW(), p.fecha_prestamo) as dias_transcurridos " +
                "FROM Prestamos p " +
                "JOIN Usuarios u ON p.id_usuario = u.id_usuario " +
                "JOIN Roles r ON u.id_rol = r.id_rol " +
                "JOIN Copias c ON p.id_copia = c.id_copia " +
                "JOIN Ejemplares e ON c.id_ejemplar = e.id_ejemplar ";

        if(!esAdmin) sql += "WHERE p.id_usuario = ? ";
        sql += "ORDER BY p.fecha_prestamo DESC";

        try(Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)){
            if(!esAdmin) ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                JSONObject obj = new JSONObject();
                obj.put("id", rs.getInt("id_prestamo"));
                obj.put("fecha_inicio", rs.getString("fecha_prestamo"));
                obj.put("fecha_fin", rs.getString("fecha_devolucion"));
                obj.put("estado", rs.getString("estado"));
                obj.put("titulo", rs.getString("titulo"));
                obj.put("codigo", rs.getString("codigo_unico"));
                obj.put("usuario", rs.getString("nombre") + " " + rs.getString("apellido"));

                int diasTranscurridos = rs.getInt("dias_transcurridos");
                int diasPermitidos = rs.getInt("dias_prestamo");
                double tarifaDiaria = rs.getDouble("mora_diaria");

                double mora = 0.0;
                if(diasTranscurridos > diasPermitidos && "Activo".equals(rs.getString("estado"))) {
                    mora = (diasTranscurridos - diasPermitidos) * tarifaDiaria;
                }
                obj.put("mora", String.format("%.2f", mora));

                lista.add(obj);
            }
        } catch(Exception e){ e.printStackTrace(); }
        return lista;
    }

    public boolean devolverPrestamo(int idPrestamo) {
        Connection conn = null;
        try {
            conn = getConnection();
            conn.setAutoCommit(false);

            int idCopia = 0;
            
            // Obtener id_copia
            String sqlGet = "SELECT id_copia FROM Prestamos WHERE id_prestamo = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlGet)){
                ps.setInt(1, idPrestamo);
                ResultSet rs = ps.executeQuery();
                if(rs.next()) idCopia = rs.getInt("id_copia");
                else return false;
            }

            // Actualizar Préstamo
            String sqlUpP = "UPDATE Prestamos SET fecha_devolucion = NOW(), estado = 'Devuelto' WHERE id_prestamo = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlUpP)){
                ps.setInt(1, idPrestamo);
                ps.executeUpdate();
            }

            // Liberar Copia
            String sqlUpC = "UPDATE Copias SET estado = 'Disponible' WHERE id_copia = ?";
            try(PreparedStatement ps = conn.prepareStatement(sqlUpC)){
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