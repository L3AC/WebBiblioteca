package model;

import static java.lang.System.out;
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
        SELECT u.id_usuario, u.nombre, u.apellido, u.correo, u.contrasena,
               r.id_rol, r.nombre_rol, r.cant_max_prestamo, r.dias_prestamo, r.mora_diaria
        FROM Usuarios u
        JOIN Roles r ON u.id_rol = r.id_rol
        WHERE u.correo = ? 
        """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
                        return usuario;
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al intentar login con correo: " + correo, e);
            // ✅ No lanzar la excepción, solo loggearla
        }
        return null; // ✅ Si falla, devolver null
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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, (String) data.get("nombre"));
            ps.setString(2, (String) data.get("apellido"));
            ps.setString(3, correo);

            String contrasenaPlana = (String) data.get("contrasena");
            String hash = BCrypt.hashpw(contrasenaPlana, BCrypt.gensalt());
            ps.setString(4, hash);

            Object idRolObj = data.get("id_rol");
            int idRol = 0;
            if (idRolObj instanceof Long) {
                idRol = ((Long) idRolObj).intValue();
            } else if (idRolObj instanceof String) {
                idRol = Integer.parseInt((String) idRolObj);
            } else if (idRolObj instanceof Integer) {
                idRol = (Integer) idRolObj;
            }

            ps.setInt(5, idRol);

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
        } catch (NumberFormatException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al convertir id_rol a número: " + data.get("id_rol"), e);
        }
        return false;
    }

    // === VERIFICAR SI CORREO EXISTE ===
    private boolean existeCorreo(String correo) {
        String sql = "SELECT 1 FROM Usuarios WHERE correo = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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
            where u.id_rol>1
            ORDER BY u.id_usuario
            """;

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

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
        System.out.println(">>> INICIANDO ACTUALIZAR USUARIO <<<");
        Long idUsuarioLong = (Long) data.get("id_usuario");
        if (idUsuarioLong == null) {
            System.out.println(">>> id_usuario es null <<<");
            return false;
        }
        int idUsuario = idUsuarioLong.intValue();
        String correo = (String) data.get("correo");

        System.out.println(">>> Actualizando usuario ID: " + idUsuario + ", Correo: " + correo);

        // ✅ Validar que el nuevo correo no esté usado por otro usuario
        if (correoYaUsadoPorOtro(idUsuario, correo)) {
            System.out.println(">>> Actualización cancelada: correo duplicado: " + correo);
            return false;
        }

        StringBuilder sql = new StringBuilder("""
    UPDATE Usuarios
    SET nombre = ?, apellido = ?, correo = ?, id_rol=?
    """);
        String nuevaContrasena = (String) data.get("contrasena");
        boolean actualizarPassword = nuevaContrasena != null && !nuevaContrasena.isBlank();
        if (actualizarPassword) {
            sql.append(", contrasena = ?");
        }
        sql.append(" WHERE id_usuario = ?");

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int idx = 1;
            ps.setString(idx++, (String) data.get("nombre"));
            ps.setString(idx++, (String) data.get("apellido"));
            ps.setString(idx++, correo);
            ps.setInt(idx++, ((Long) data.get("id_rol")).intValue()); // ✅ Asegurar que sea int

            if (actualizarPassword) {
                String hash = BCrypt.hashpw(nuevaContrasena, BCrypt.gensalt());
                ps.setString(idx++, hash);
            }

            ps.setInt(idx, idUsuario);

            System.out.println(">>> Ejecutando SQL: " + sql.toString());
            System.out.println(">>> Parámetros: " + idUsuario + ", " + correo);

            int filas = ps.executeUpdate();
            System.out.println(">>> Filas afectadas: " + filas);

            return filas > 0;

        } catch (SQLException e) {
            System.out.println(">>> Error SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

// ✅ Nuevo método: Verifica si el correo ya está usado por otro usuario
    private boolean correoYaUsadoPorOtro(int idUsuario, String correo) {
        String sql = "SELECT id_usuario FROM Usuarios WHERE correo = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idExistente = rs.getInt("id_usuario");
                    // ✅ Si el correo ya existe, pero es del mismo usuario, está permitido
                    return idExistente != idUsuario;
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al verificar duplicado de correo", e);
        }
        return false;
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

        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

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

    // === ELIMINAR USUARIO ===
    public boolean eliminarUsuario(int idUsuario) {
        String sql = "DELETE FROM Usuarios WHERE id_usuario = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al eliminar usuario con ID: " + idUsuario, e);
            return false;
        }
    }
        public double calcularMoraActual(int idUsuario) {
        double moraTotal = 0.0;
        // Esta consulta calcula los días de retraso multiplicados por la mora diaria del rol
        // solo para préstamos activos que ya pasaron su fecha límite.
        String sql = """
            SELECT 
                SUM(DATEDIFF(CURRENT_DATE, DATE_ADD(p.fecha_prestamo, INTERVAL r.dias_prestamo DAY)) * r.mora_diaria) as total_mora
            FROM Prestamos p
            JOIN Usuarios u ON p.id_usuario = u.id_usuario
            JOIN Roles r ON u.id_rol = r.id_rol
            WHERE p.id_usuario = ? 
              AND p.estado = 'Activo'
              AND CURRENT_DATE > DATE_ADD(p.fecha_prestamo, INTERVAL r.dias_prestamo DAY)
        """;

        try (Connection conn = getConnection(); 
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    moraTotal = rs.getDouble("total_mora");
                }
            }
        } catch (SQLException e) {
            Logger.getLogger(UsuarioModel.class.getName()).log(Level.SEVERE, "Error al calcular mora", e);
        }
        return moraTotal;
    }

}
