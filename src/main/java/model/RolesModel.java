package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class RolesModel extends Conexion {

    // === REGISTRAR ROL ===
    public boolean registrarRol(JSONObject data) {
        String nombre = (String) data.get("nombre_rol");
        if (existeNombreRol(nombre)) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO Roles (nombre_rol, cant_max_prestamo, dias_prestamo, mora_diaria)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);
            Long cantMaxPrestamoLong = (Long) data.get("cant_max_prestamo");
            ps.setInt(2, cantMaxPrestamoLong != null ? cantMaxPrestamoLong.intValue() : 0);
            Long diasPrestamoLong = (Long) data.get("dias_prestamo");
            ps.setInt(3, diasPrestamoLong != null ? diasPrestamoLong.intValue() : 0);
            Double moraDiaria = (Double) data.get("mora_diaria");
            ps.setDouble(4, moraDiaria != null ? moraDiaria : 0.0);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(RolesModel.class.getName()).log(Level.INFO, "Rol registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al registrar rol: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE ROL EXISTE ===
    private boolean existeNombreRol(String nombre) {
        String sql = "SELECT 1 FROM Roles WHERE nombre_rol = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de rol: " + nombre, e);
            return false;
        }
    }

    // === OBTENER ROL POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idRol) {
        String sql = """
            SELECT id_rol, nombre_rol, cant_max_prestamo, dias_prestamo, mora_diaria
            FROM Roles
            WHERE id_rol = ?
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject rol = new JSONObject();
                    rol.put("id_rol", rs.getInt("id_rol"));
                    rol.put("nombre_rol", rs.getString("nombre_rol"));
                    rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                    rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                    rol.put("mora_diaria", rs.getDouble("mora_diaria"));
                    return rol;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al obtener rol por ID: " + idRol, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS ROLES (como lista de JSON) ===
    public List<JSONObject> listarRoles() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_rol, nombre_rol, cant_max_prestamo, dias_prestamo, mora_diaria
            FROM Roles
            WHEN id_rol>1
            ORDER BY id_rol
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject rol = new JSONObject();
                rol.put("id_rol", rs.getInt("id_rol"));
                rol.put("nombre_rol", rs.getString("nombre_rol"));
                rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                rol.put("mora_diaria", rs.getDouble("mora_diaria"));

                lista.add(rol);
            }

        } catch (SQLException e) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al listar roles", e);
        }
        return lista;
    }

    // === ACTUALIZAR ROL ===
    public boolean actualizarRol(JSONObject data) {
        try {
            int idRol = ((Long) data.get("id_rol")).intValue();
            String nombre = (String) data.get("nombre_rol");

            System.out.println("Actualizando rol - ID: " + idRol + ", Nombre: " + nombre);

            if (existeNombreRol(nombre)) {
                JSONObject existente = obtenerPorNombre(nombre);
                if (existente != null && !existente.get("id_rol").equals((long) idRol)) {
                    System.out.println("Nombre duplicado detectado para rol ID: " + idRol);
                    Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Actualización cancelada: nombre duplicado: {0}", nombre);
                    return false;
                }
            }

            String sql = """
            UPDATE Roles
            SET nombre_rol = ?, cant_max_prestamo = ?, dias_prestamo = ?, mora_diaria = ?
            WHERE id_rol = ?
            """;

            try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, nombre);
                Long cantMaxPrestamoLong = (Long) data.get("cant_max_prestamo");
                ps.setInt(2, cantMaxPrestamoLong != null ? cantMaxPrestamoLong.intValue() : 0);
                Long diasPrestamoLong = (Long) data.get("dias_prestamo");
                ps.setInt(3, diasPrestamoLong != null ? diasPrestamoLong.intValue() : 0);
                Double moraDiaria = (Double) data.get("mora_diaria");
                ps.setDouble(4, moraDiaria != null ? moraDiaria : 0.0);
                ps.setInt(5, idRol);

                System.out.println("Ejecutando SQL: " + sql);
                System.out.println("Parámetros: " + nombre + ", " + cantMaxPrestamoLong + ", " + diasPrestamoLong + ", " + moraDiaria + ", " + idRol);

                int rowsAffected = ps.executeUpdate();
                System.out.println("Filas afectadas: " + rowsAffected);

                return rowsAffected > 0;

            } catch (SQLException e) {
                System.out.println("Error SQL: " + e.getMessage());
                e.printStackTrace();
                Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al actualizar rol ID: " + idRol, e);
                return false;
            }
        } catch (Exception e) {
            System.out.println("Error general en actualizarRol: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // === ELIMINAR ROL ===
    public boolean eliminarRol(int idRol) {
        String sql = "DELETE FROM Roles WHERE id_rol = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al eliminar rol con ID: " + idRol, e);
            return false;
        }
    }

    // === OBTENER ROL POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_rol, nombre_rol, cant_max_prestamo, dias_prestamo, mora_diaria
            FROM Roles
            WHERE nombre_rol = ?
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject rol = new JSONObject();
                    rol.put("id_rol", rs.getInt("id_rol"));
                    rol.put("nombre_rol", rs.getString("nombre_rol"));
                    rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                    rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                    rol.put("mora_diaria", rs.getDouble("mora_diaria"));
                    return rol;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(RolesModel.class.getName()).log(Level.SEVERE, "Error al obtener rol por nombre: " + nombre, e);
        }
        return null;
    }
}
