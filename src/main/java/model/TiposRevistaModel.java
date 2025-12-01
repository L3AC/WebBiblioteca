package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class TiposRevistaModel extends Conexion {

    // === REGISTRAR TIPO REVISTA ===
    public boolean registrarTipoRevista(JSONObject data) {
        String nombre = (String) data.get("nombre_tipo_revista");
        if (existeNombreTipoRevista(nombre)) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO TiposRevista (nombre_tipo_revista)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.INFO, "Tipo de revista registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al registrar tipo de revista: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE TIPO REVISTA EXISTE ===
    private boolean existeNombreTipoRevista(String nombre) {
        String sql = "SELECT 1 FROM TiposRevista WHERE nombre_tipo_revista = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de tipo de revista: " + nombre, e);
            return false;
        }
    }

    // === OBTENER TIPO REVISTA POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idTipoRevista) {
        String sql = """
            SELECT id_tipo_revista, nombre_tipo_revista
            FROM TiposRevista
            WHERE id_tipo_revista = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTipoRevista);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoRevista = new JSONObject();
                    tipoRevista.put("id_tipo_revista", rs.getInt("id_tipo_revista"));
                    tipoRevista.put("nombre_tipo_revista", rs.getString("nombre_tipo_revista"));
                    return tipoRevista;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de revista por ID: " + idTipoRevista, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS TIPOS REVISTA (como lista de JSON) ===
    public List<JSONObject> listarTiposRevista() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_tipo_revista, nombre_tipo_revista
            FROM TiposRevista
            ORDER BY id_tipo_revista
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject tipoRevista = new JSONObject();
                tipoRevista.put("id_tipo_revista", rs.getInt("id_tipo_revista"));
                tipoRevista.put("nombre_tipo_revista", rs.getString("nombre_tipo_revista"));

                lista.add(tipoRevista);
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al listar tipos de revista", e);
        }
        return lista;
    }

    // === ACTUALIZAR TIPO REVISTA ===
    public boolean actualizarTipoRevista(JSONObject data) {
        int idTipoRevista = ((Long) data.get("id_tipo_revista")).intValue();
        String nombre = (String) data.get("nombre_tipo_revista");

        if (existeNombreTipoRevista(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_tipo_revista").equals((long) idTipoRevista)) {
                Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE TiposRevista
            SET nombre_tipo_revista = ?
            WHERE id_tipo_revista = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idTipoRevista);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al actualizar tipo de revista ID: " + idTipoRevista, e);
            return false;
        }
    }

    // === ELIMINAR TIPO REVISTA ===
    public boolean eliminarTipoRevista(int idTipoRevista) {
        String sql = "DELETE FROM TiposRevista WHERE id_tipo_revista = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTipoRevista);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al eliminar tipo de revista con ID: " + idTipoRevista, e);
            return false;
        }
    }

    // === OBTENER TIPO REVISTA POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_tipo_revista, nombre_tipo_revista
            FROM TiposRevista
            WHERE nombre_tipo_revista = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoRevista = new JSONObject();
                    tipoRevista.put("id_tipo_revista", rs.getInt("id_tipo_revista"));
                    tipoRevista.put("nombre_tipo_revista", rs.getString("nombre_tipo_revista"));
                    return tipoRevista;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposRevistaModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de revista por nombre: " + nombre, e);
        }
        return null;
    }
}