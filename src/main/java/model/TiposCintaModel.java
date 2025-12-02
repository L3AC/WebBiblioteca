package model;

import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import static utils.ConexionBD.getConnection;

public class TiposCintaModel extends Conexion {

    // === REGISTRAR TIPO CINTA ===
    public boolean registrarTipoCinta(JSONObject data) {
        String nombre = (String) data.get("nombre_tipo_cinta");
        if (existeNombreTipoCinta(nombre)) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.WARNING, "Intento de registro con nombre duplicado: {0}", nombre);
            return false;
        }

        String sql = """
            INSERT INTO TiposCinta (nombre_tipo_cinta)
            VALUES (?)
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, nombre);

            int filas = ps.executeUpdate();
            if (filas > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        Logger.getLogger(TiposCintaModel.class.getName()).log(Level.INFO, "Tipo de cinta registrado: ID={0}, Nombre={1}", new Object[]{rs.getInt(1), nombre});
                    }
                }
                return true;
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al registrar tipo de cinta: " + nombre, e);
        }
        return false;
    }

    // === VERIFICAR SI NOMBRE DE TIPO CINTA EXISTE ===
    private boolean existeNombreTipoCinta(String nombre) {
        String sql = "SELECT 1 FROM TiposCinta WHERE nombre_tipo_cinta = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al verificar existencia de tipo de cinta: " + nombre, e);
            return false;
        }
    }

    // === OBTENER TIPO CINTA POR ID (como JSON) ===
    public JSONObject obtenerPorId(int idTipoCinta) {
        String sql = """
            SELECT id_tipo_cinta, nombre_tipo_cinta
            FROM TiposCinta
            WHERE id_tipo_cinta = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, idTipoCinta);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoCinta = new JSONObject();
                    tipoCinta.put("id_tipo_cinta", rs.getInt("id_tipo_cinta"));
                    tipoCinta.put("nombre_tipo_cinta", rs.getString("nombre_tipo_cinta"));
                    return tipoCinta;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de cinta por ID: " + idTipoCinta, e);
        }
        return null;
    }

    // === LISTAR TODOS LOS TIPOS CINTA (como lista de JSON) ===
    public List<JSONObject> listarTiposCinta() {
        List<JSONObject> lista = new ArrayList<>();
        String sql = """
            SELECT id_tipo_cinta, nombre_tipo_cinta
            FROM TiposCinta
            ORDER BY id_tipo_cinta
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                JSONObject tipoCinta = new JSONObject();
                tipoCinta.put("id_tipo_cinta", rs.getInt("id_tipo_cinta"));
                tipoCinta.put("nombre_tipo_cinta", rs.getString("nombre_tipo_cinta"));

                lista.add(tipoCinta);
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al listar tipos de cinta", e);
        }
        return lista;
    }

    // === ACTUALIZAR TIPO CINTA ===
    public boolean actualizarTipoCinta(JSONObject data) {
        int idTipoCinta = ((Long) data.get("id_tipo_cinta")).intValue();
        String nombre = (String) data.get("nombre_tipo_cinta");

        if (existeNombreTipoCinta(nombre)) {
            JSONObject existente = obtenerPorNombre(nombre);
            if (existente != null && !existente.get("id_tipo_cinta").equals((long) idTipoCinta)) {
                Logger.getLogger(TiposCintaModel.class.getName()).log(Level.WARNING, "Actualización cancelada: nombre duplicado: {0}", nombre);
                return false;
            }
        }

        String sql = """
            UPDATE TiposCinta
            SET nombre_tipo_cinta = ?
            WHERE id_tipo_cinta = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, idTipoCinta);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al actualizar tipo de cinta ID: " + idTipoCinta, e);
            return false;
        }
    }

    // === ELIMINAR TIPO CINTA ===
    public boolean eliminarTipoCinta(int idTipoCinta) {
        String sql = "DELETE FROM TiposCinta WHERE id_tipo_cinta = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idTipoCinta);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al eliminar tipo de cinta con ID: " + idTipoCinta, e);
            return false;
        }
    }

    // === OBTENER TIPO CINTA POR NOMBRE (como JSON) ===
    public JSONObject obtenerPorNombre(String nombre) {
        String sql = """
            SELECT id_tipo_cinta, nombre_tipo_cinta
            FROM TiposCinta
            WHERE nombre_tipo_cinta = ?
            """;

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JSONObject tipoCinta = new JSONObject();
                    tipoCinta.put("id_tipo_cinta", rs.getInt("id_tipo_cinta"));
                    tipoCinta.put("nombre_tipo_cinta", rs.getString("nombre_tipo_cinta"));
                    return tipoCinta;
                }
            }

        } catch (SQLException e) {
            Logger.getLogger(TiposCintaModel.class.getName()).log(Level.SEVERE, "Error al obtener tipo de cinta por nombre: " + nombre, e);
        }
        return null;
    }
}