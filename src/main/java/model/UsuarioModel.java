package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.mindrot.jbcrypt.BCrypt;
import static utils.ConexionBD.getConnection;

public class UsuarioModel extends Conexion {

    // === LOGIN: Validar credenciales y retornar información del usuario como JSON ===
    public JSONObject login(String correo, String contrasenaPlana) {
        String sql = """
            SELECT u.id_usuario, u.nombre, u.apellido, u.correo,
                   r.id_rol, r.nombre_rol, r.cant_max_prestamo, r.dias_prestamo, r.mora_diaria
            FROM Usuarios u
            JOIN Roles r ON u.id_rol = r.id_rol
            WHERE u.correo = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String hashAlmacenado = rs.getString("contrasena");

                    if (BCrypt.checkpw(contrasenaPlana, hashAlmacenado)) {
                        JSONObject usuario = new JSONObject();
                        usuario.put("id_usuario", rs.getInt("id_usuario"));
                        usuario.put("nombre", rs.getString("nombre"));
                        usuario.put("apellido", rs.getString("apellido"));
                        usuario.put("correo", rs.getString("correo"));

                        JSONObject rol = new JSONObject();
                        rol.put("id_rol", rs.getInt("id_rol"));
                        rol.put("nombre_rol", rs.getString("nombre_rol"));
                        rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                        rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                        rol.put("mora_diaria", rs.getDouble("mora_diaria"));

                        usuario.put("rol", rol);

                        Logger.getLogger(UsuarioModel.class.getName()).log(Level.INFO, "Login exitoso: {0}", correo);
                        return usuario;
                    } else {
                        Logger.getLogger(UsuarioModel.class.getName()).log(Level.WARNING, "Contraseña incorrecta para correo: {0}", correo);
                    }
                } else {
                    Logger.getLogger(UsuarioModel.class.getName()).log(Level.WARNING, "Correo no encontrado: {0}", correo);
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al intentar login con correo: " + correo, e);
        }
        return null;
    }

    // === REGISTRAR USUARIO (con contraseña encriptada) ===
    public boolean registrarUsuario(JSONObject data) {
        String correo = (String) data.get("correo");
        if (existeCorreo(correo)) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.WARNING, "Intento de registro con correo duplicado: {0}", correo);
            return false;
        }

        String sql = """
            INSERT INTO Usuarios (nombre, apellido, correo, contrasena, id_rol)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, (String) data.get("nombre"));
            ps.setString(2, (String) data.get("apellido"));
            ps.setString(3, correo);

            String contrasenaPlana = (String) data.get("contrasena");
            String hash = BCrypt.hashpw(contrasenaPlana, BCrypt.gensalt());
            ps.setString(4, hash);

            Long idRolLong = (Long) data.get("id_rol");
            ps.setInt(5, idRolLong != null ? idRolLong.intValue() : 0);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(UsuarioModel.class.getName()).log(Level.INFO, "Usuario registrado: ID={0}, Correo={1}", new Object[]{rs.getInt(1), correo});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al registrar usuario: " + correo, e);
        }
        return false;
    }

    // === VERIFICAR SI CORREO EXISTE ===
    private boolean existeCorreo(String correo) {
        String sql = "SELECT 1 FROM Usuarios WHERE correo = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de correo: " + correo, e);
            return false;
        }
    }

    // === OBTENER USUARIO POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idUsuario) {
        String sql = """
            SELECT u.id_usuario, u.nombre, u.apellido, u.correo,
                   r.id_rol, r.nombre_rol, r.cant_max_prestamo, r.dias_prestamo, r.mora_diaria
            FROM Usuarios u
            JOIN Roles r ON u.id_rol = r.id_rol
            WHERE u.id_usuario = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject rol = new JSONObject();
                    rol.put("id_rol", rs.getInt("id_rol"));
                    rol.put("nombre_rol", rs.getString("nombre_rol"));
                    rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                    rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                    rol.put("mora_diaria", rs.getDouble("mora_diaria"));

                    JSONObject usuario = new JSONObject();
                    usuario.put("id_usuario", rs.getInt("id_usuario"));
                    usuario.put("nombre", rs.getString("nombre"));
                    usuario.put("apellido", rs.getString("apellido"));
                    usuario.put("correo", rs.getString("correo"));
                    usuario.put("rol", rol);
                    return usuario;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al obtener usuario por ID: " + idUsuario, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS USUARIOS (como lista de JSON) ===
    public List<JSONObject> listarUsuarios() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT u.id_usuario, u.nombre, u.apellido, u.correo,
                   r.id_rol, r.nombre_rol, r.cant_max_prestamo, r.dias_prestamo, r.mora_diaria
            FROM Usuarios u
            JOIN Roles r ON u.id_rol = r.id_rol
            ORDER BY u.id_usuario
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject rol = new JSONObject();
                rol.put("id_rol", rs.getInt("id_rol"));
                rol.put("nombre_rol", rs.getString("nombre_rol"));
                rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                rol.put("mora_diaria", rs.getDouble("mora_diaria"));

                JSONObject usuario = new JSONObject();
                usuario.put("id_usuario", rs.getInt("id_usuario"));
                usuario.put("nombre", rs.getString("nombre"));
                usuario.put("apellido", rs.getString("apellido"));
                usuario.put("correo", rs.getString("correo"));
                usuario.put("rol", rol);

                lista.add(usuario);
            }

        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al listar usuarios", e);
        }
        return lista;
    }

    // === ACTUALIZAR USUARIO (con contraseña opcional) ===
    public boolean actualizarUsuario(JSONObject data) {
        int idUsuario = ((Long) data.get("id_usuario")).intValue();
        String correo = (String) data.get("correo");

        StringBuilder sql = new StringBuilder("""
        UPDATE Usuarios
        SET nombre = ?, apellido = ?, correo = ?
        """);
        String nuevaContrasena = (String) data.get("contrasena");
        boolean actualizarPassword = nuevaContrasena != null && !nuevaContrasena.isBlank();
        if (actualizarPassword) {
            sql.append(", contrasena = ?");
        }
        sql.append(" WHERE id_usuario = ?");

        if (existeCorreo(correo)) {
            JSONObject existente = obtenerPorCorreo(correo);
            if (existente != null && !existente.get("id_usuario").equals((long) idUsuario)) {
                Logger.getLogger(UsuarioModel.class.getName()).log(Level.WARNING, "Actualización cancelada: correo duplicado: {0}", correo);
                return false;
            }
        }

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setString(idx++, (String) data.get("nombre"));
            ps.setString(idx++, (String) data.get("apellido"));
            ps.setString(idx++, correo);

            if (actualizarPassword) {
                String hash = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());
                ps.setString(idx++, hash);
            }

            ps.setInt(idx, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al actualizar usuario ID: " + idUsuario, e);
            return false;
        }
    }

    // === ELIMINAR USUARIO ===
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM Usuarios WHERE id_usuario = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al eliminar usuario con ID: " + idUsuario, e);
            return false;
        }
    }

    // === OBTENER USUARIO POR CORREO (como JSON) ===
    public JSONObject obtenerPorCorreo(String correo) {
        String sql = """
            SELECT u.id_usuario, u.nombre, u.apellido, u.correo,
                   r.id_rol, r.nombre_rol, r.cant_max_prestamo, r.dias_prestamo, r.mora_diaria
            FROM Usuarios u
            JOIN Roles r ON u.id_rol = r.id_rol
            WHERE u.correo = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject rol = new JSONObject();
                    rol.put("id_rol", rs.getInt("id_rol"));
                    rol.put("nombre_rol", rs.getString("nombre_rol"));
                    rol.put("cant_max_prestamo", rs.getInt("cant_max_prestamo"));
                    rol.put("dias_prestamo", rs.getInt("dias_prestamo"));
                    rol.put("mora_diaria", rs.getDouble("mora_diaria"));

                    JSONObject usuario = new JSONObject();
                    usuario.put("id_usuario", rs.getInt("id_usuario"));
                    usuario.put("nombre", rs.getString("nombre"));
                    usuario.put("apellido", rs.getString("apellido"));
                    usuario.put("correo", rs.getString("correo"));
                    usuario.put("rol", rol);
                    return usuario;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al obtener usuario por correo: " + correo, e);
        }
        return null;
    }
}